package io.renren.modules.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.attendance.dao.AttendanceGeoDao;
import io.renren.modules.attendance.dto.AttendanceGeoDTO;
import io.renren.modules.attendance.entity.AttendanceGeoEntity;
import io.renren.modules.attendance.service.AttendanceGeoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 地理签到 - 实现分页、条件筛选、导出
 * 已对齐 sign_in_record：使用 sign_time 做时间筛选与排序
 */
@Service("attendanceGeoService")
public class AttendanceGeoServiceImpl extends ServiceImpl<AttendanceGeoDao, AttendanceGeoEntity>
        implements AttendanceGeoService {

    @Autowired
    private DataSource dataSource;

    /** 允许排序的白名单字段，映射到真实列名（防注入） */
    private String mapSortField(String field) {
        if (StringUtils.isBlank(field)) return null;
        switch (field) {
            case "id":        return "id";
            case "userId":    return "user_id";
            case "pointId":   return "point_id";
            case "longitude": return "longitude";
            case "latitude":  return "latitude";
            case "distance":  return "distance";
            case "result":    return "result";
            case "reason":    return "reason";
            case "address":   return "address";  // ✅ 新增：address字段排序
            case "signTime":  return "sign_time";   // ✅ 关键：用 sign_time 排序
            default: return null;
        }
    }

    /** 日期筛选：单日/区间，全部落到 sign_time 列 */
    private void applyDateFilters(QueryWrapper<AttendanceGeoEntity> qw, String signDate, String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // 区间优先
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            try {
                Date s = sdf.parse(startDate);
                Date e = sdf.parse(endDate);
                // 结束天包含：转为次日 00:00 用 lt
                Calendar cal = Calendar.getInstance();
                cal.setTime(e);
                cal.add(Calendar.DATE, 1);
                Date endNext = cal.getTime();

                qw.ge("sign_time", s).lt("sign_time", endNext);
            } catch (Exception ex) {
                // 字符串兜底（可能不走索引）
                qw.apply("DATE_FORMAT(sign_time, '%Y-%m-%d') >= {0}", startDate)
                        .apply("DATE_FORMAT(sign_time, '%Y-%m-%d') <= {0}", endDate);
            }
            return;
        }

        // 单日
        if (StringUtils.isNotBlank(signDate)) {
            try {
                Date d = sdf.parse(signDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                Date start = cal.getTime();
                cal.add(Calendar.DATE, 1);
                Date endNext = cal.getTime();
                qw.ge("sign_time", start).lt("sign_time", endNext);
            } catch (Exception ex) {
                qw.apply("DATE_FORMAT(sign_time, '%Y-%m-%d') = {0}", signDate);
            }
        }
    }

    /** 组装排序（默认 sign_time DESC） */
    private void applySort(QueryWrapper<AttendanceGeoEntity> qw, String sortField, String sortOrder) {
        String col = mapSortField(sortField);
        boolean desc = !"asc".equalsIgnoreCase(sortOrder);

        if (col != null) {
            if (desc) qw.orderByDesc(col); else qw.orderByAsc(col);
        } else {
            qw.orderByDesc("sign_time");
        }
    }

    @Override
    public Map<String, Object> getList(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
            Integer limit = params.get("limit") != null ? Integer.parseInt(params.get("limit").toString()) : 10;
            Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
            String username = (String) params.get("username");
            String signDate = (String) params.get("signDate");
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");
            String sortField = (String) params.get("sortField");
            String sortOrder = (String) params.get("sortOrder");

            IPage<AttendanceGeoEntity> pageObj = new Page<>(page, limit);
            QueryWrapper<AttendanceGeoEntity> qw = new QueryWrapper<>();

            // 用户筛选
            if (userId != null) {
                qw.eq("user_id", userId);
            }
            if (StringUtils.isNotBlank(username)) {
                // ✅ 安全绑定
                qw.apply("user_id IN (SELECT id FROM sys_user WHERE username LIKE {0})", "%" + username + "%");
            }

            // 日期筛选（sign_time）
            applyDateFilters(qw, signDate, startDate, endDate);

            // 排序
            applySort(qw, sortField, sortOrder);

            IPage<AttendanceGeoEntity> resultPage = this.page(pageObj, qw);

            // ✅ 批量拉取用户名和部门信息，避免 N+1
            List<AttendanceGeoEntity> recs = resultPage.getRecords();
            Set<Long> userIds = recs.stream().map(AttendanceGeoEntity::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Long, String> unameMap = getUserInfoMapByIds(userIds, "username");
            Map<Long, String> deptMap = getUserDeptMapByIds(userIds); // ✅ 修改：使用新的部门查询方法

            // 封装结果
            List<Map<String, Object>> list = new ArrayList<>();
            for (AttendanceGeoEntity e : recs) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", e.getId());
                item.put("userId", e.getUserId());
                item.put("username", unameMap.getOrDefault(e.getUserId(), "用户" + e.getUserId()));
                item.put("deptName", deptMap.getOrDefault(e.getUserId(), "-")); // ✅ 新增：部门信息
                item.put("pointId", e.getPointId());
                item.put("longitude", e.getLongitude());
                item.put("latitude", e.getLatitude());
                item.put("distance", e.getDistance());
                item.put("result", e.getResult());
                item.put("reason", e.getReason());
                item.put("address", e.getAddress()); // ✅ 新增：确保address字段返回
                item.put("signTime", e.getSignTime());

                // ✅ 新增：计算是否在范围内（兼容前端）
                Double distance = e.getDistance();
                if (distance != null) {
                    // 假设签到范围是500米，你可以根据实际情况调整
                    item.put("inRange", distance <= 500 ? 1 : 0);
                } else {
                    item.put("inRange", 0); // 默认不在范围内
                }

                list.add(item);
            }

            result.put("list", list);
            result.put("total", resultPage.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            result.put("list", new ArrayList<>());
            result.put("total", 0L);
        }
        return result;
    }

    @Override
    public List<AttendanceGeoDTO> list(Map<String, Object> params) {
        try {
            Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
            String username = (String) params.get("username");
            String signDate = (String) params.get("signDate");
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");
            String sortField = (String) params.get("sortField");
            String sortOrder = (String) params.get("sortOrder");

            QueryWrapper<AttendanceGeoEntity> qw = new QueryWrapper<>();

            if (userId != null) {
                qw.eq("user_id", userId);
            }
            if (StringUtils.isNotBlank(username)) {
                qw.apply("user_id IN (SELECT id FROM sys_user WHERE username LIKE {0})", "%" + username + "%");
            }

            applyDateFilters(qw, signDate, startDate, endDate);
            applySort(qw, sortField, sortOrder);

            List<AttendanceGeoEntity> entities = this.list(qw);

            // ✅ 批量用户名和部门映射
            Set<Long> userIds = entities.stream().map(AttendanceGeoEntity::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Long, String> unameMap = getUserInfoMapByIds(userIds, "username");
            Map<Long, String> deptMap = getUserDeptMapByIds(userIds); // ✅ 修改：使用新的部门查询方法

            return entities.stream().map(e -> {
                AttendanceGeoDTO dto = new AttendanceGeoDTO();
                // 复制所有属性
                BeanUtils.copyProperties(e, dto);

                // ✅ 关键：确保所有字段都被正确设置
                dto.setUsername(unameMap.getOrDefault(e.getUserId(), "用户" + e.getUserId()));
                dto.setDeptName(deptMap.getOrDefault(e.getUserId(), "-")); // ✅ 新增：设置部门名称
                dto.setCheckTime(e.getSignTime()); // 签到时间
                dto.setReason(e.getReason());      // 地址信息（reason字段）
                dto.setAddress(e.getAddress());    // 地址信息（address字段）

                // ✅ 设置是否在范围内
                Double distance = e.getDistance();
                if (distance != null) {
                    try {
                        dto.getClass().getMethod("setInRange", Integer.class)
                                .invoke(dto, distance <= 500 ? 1 : 0);
                    } catch (Exception ignore) { }
                }

                return dto;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * ✅ 修改：通用方法，一次性取回用户信息映射（用户名或部门名），避免 N+1 次查询
     * @param field 要查询的字段：username 或 deptName
     */
    private Map<Long, String> getUserInfoMapByIds(Set<Long> ids, String field) {
        Map<Long, String> map = new HashMap<>();
        if (ids == null || ids.isEmpty()) return map;

        // 根据字段类型构建不同的SQL查询
        String sql;
        if ("deptName".equals(field)) {
            // ✅ 修改：根据您的数据库结构，直接通过 sys_user.dept_id 关联 sys_dept
            String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
            sql = "SELECT u.id, d.name as dept_name " +
                    "FROM sys_user u " +
                    "LEFT JOIN sys_dept d ON u.dept_id = d.id " +
                    "WHERE u.id IN (" + placeholders + ")";
        } else {
            // 默认查询用户名
            String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
            sql = "SELECT id, username FROM sys_user WHERE id IN (" + placeholders + ")";
        }

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (Long id : ids) ps.setLong(i++, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long userId = rs.getLong("id");
                    String value;
                    if ("deptName".equals(field)) {
                        value = rs.getString("dept_name");
                        // 如果用户有多个部门，取第一个非空的
                        if (value != null && !map.containsKey(userId)) {
                            map.put(userId, value);
                        } else if (value == null && !map.containsKey(userId)) {
                            map.put(userId, "-");
                        }
                    } else {
                        value = rs.getString("username");
                        map.put(userId, value);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * ✅ 新增：专门用于查询用户部门信息的方法
     * 根据您的数据库结构，用户直接通过 dept_id 关联部门
     */
    private Map<Long, String> getUserDeptMapByIds(Set<Long> userIds) {
        Map<Long, String> deptMap = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) return deptMap;

        String placeholders = userIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT u.id, d.name as dept_name " +
                "FROM sys_user u " +
                "LEFT JOIN sys_dept d ON u.dept_id = d.id " +
                "WHERE u.id IN (" + placeholders + ")";

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (Long userId : userIds) ps.setLong(i++, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long userId = rs.getLong("id");
                    String deptName = rs.getString("dept_name");
                    deptMap.put(userId, deptName != null ? deptName : "-");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deptMap;
    }

    /** 旧方法保留（如有调用处） */
    @SuppressWarnings("unused")
    private String getUsernameByUserId(Long userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT username FROM sys_user WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getString("username");
            return "用户" + userId;
        } catch (Exception e) {
            e.printStackTrace();
            return "未知用户";
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
}