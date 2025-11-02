package io.renren.modules.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.attendance.dao.DailyPlanDao;
import io.renren.modules.attendance.entity.DailyPlanEntity;
import io.renren.modules.attendance.dto.DailyPlanDTO;
import io.renren.modules.attendance.service.DailyPlanService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service("dailyPlanService")
public class DailyPlanServiceImpl extends ServiceImpl<DailyPlanDao, DailyPlanEntity> implements DailyPlanService {

    @Autowired
    private DataSource dataSource;

    @Override
    public DailyPlanEntity getByUserIdAndDate(Long userId, String planDate) {
        try {
            QueryWrapper<DailyPlanEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);

            if (StringUtils.isNotBlank(planDate)) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(planDate);
                    queryWrapper.eq("plan_date", date);
                } catch (ParseException e) {
                    // 回退到字符串比较（避免时区/类型误差）
                    queryWrapper.apply("DATE_FORMAT(plan_date, '%Y-%m-%d') = {0}", planDate);
                }
            }
            return this.getOne(queryWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** 将安全字段映射到数据库列，白名单控制，避免 SQL 注入 */
    private String mapSortField(String field) {
        if (StringUtils.isBlank(field)) return null;
        switch (field) {
            case "id": return "id";
            case "userId": return "user_id";
            case "planDate": return "plan_date";
            case "createTime": return "create_time";
            case "updateTime": return "update_time";
            // username 为关联字段，当前实现未 join，不在白名单
            default: return null;
        }
    }

    /** 组装：单日 / 区间 日期条件 */
    private void applyDateFilters(QueryWrapper<DailyPlanEntity> qw, String planDate, String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // 区间优先（start & end 同时存在）
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            try {
                Date s = sdf.parse(startDate);
                Date e = sdf.parse(endDate);
                // 右闭口：<= end ；如果表里含时分秒且为 00:00:00，可改为 < nextDay
                qw.ge("plan_date", s).le("plan_date", e);
            } catch (Exception ex) {
                // 兜底：字符串比较（DATE_FORMAT）
                qw.apply("DATE_FORMAT(plan_date, '%Y-%m-%d') >= {0}", startDate)
                        .apply("DATE_FORMAT(plan_date, '%Y-%m-%d') <= {0}", endDate);
            }
            return;
        }

        // 单日
        if (StringUtils.isNotBlank(planDate)) {
            try {
                Date d = sdf.parse(planDate);
                qw.eq("plan_date", d);
            } catch (Exception ex) {
                qw.apply("DATE_FORMAT(plan_date, '%Y-%m-%d') = {0}", planDate);
            }
        }
    }

    /** 组装：排序（默认 create_time DESC） */
    private void applySort(QueryWrapper<DailyPlanEntity> qw, String sortField, String sortOrder) {
        String col = mapSortField(sortField);
        boolean desc = !"asc".equalsIgnoreCase(sortOrder);

        if (col != null) {
            if (desc) {
                qw.orderByDesc(col);
            } else {
                qw.orderByAsc(col);
            }
        } else {
            // 默认
            qw.orderByDesc("create_time");
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
            String planDate = (String) params.get("planDate");
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");
            String sortField = (String) params.get("sortField");
            String sortOrder = (String) params.get("sortOrder");

            IPage<DailyPlanEntity> pageObj = new Page<>(page, limit);
            QueryWrapper<DailyPlanEntity> queryWrapper = new QueryWrapper<>();

            // 用户筛选
            if (userId != null) {
                queryWrapper.eq("user_id", userId);
            }
            if (StringUtils.isNotBlank(username)) {
                queryWrapper.inSql("user_id",
                        "SELECT id FROM sys_user WHERE username LIKE '%" + username + "%'");
            }

            // 日期筛选：区间优先，其次单日
            applyDateFilters(queryWrapper, planDate, startDate, endDate);

            // 排序
            applySort(queryWrapper, sortField, sortOrder);

            IPage<DailyPlanEntity> resultPage = this.page(pageObj, queryWrapper);

            // 映射结果（带 username）
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (DailyPlanEntity entity : resultPage.getRecords()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", entity.getId());
                item.put("userId", entity.getUserId());
                item.put("username", getUsernameByUserId(entity.getUserId()));
                item.put("planDate", entity.getPlanDate());
                item.put("content", entity.getContent());
                item.put("completion", entity.getCompletion());
                item.put("createTime", entity.getCreateTime());
                item.put("updateTime", entity.getUpdateTime());
                resultList.add(item);
            }

            result.put("list", resultList);
            result.put("total", resultPage.getTotal());

        } catch (Exception e) {
            e.printStackTrace();
            result.put("list", new ArrayList<>());
            result.put("total", 0L);
        }

        return result;
    }

    @Override
    public List<DailyPlanDTO> list(Map<String, Object> params) {
        try {
            Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
            String username = (String) params.get("username");
            String planDate = (String) params.get("planDate");
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");
            String sortField = (String) params.get("sortField");
            String sortOrder = (String) params.get("sortOrder");

            QueryWrapper<DailyPlanEntity> queryWrapper = new QueryWrapper<>();

            if (userId != null) {
                queryWrapper.eq("user_id", userId);
            }
            if (StringUtils.isNotBlank(username)) {
                queryWrapper.inSql("user_id",
                        "SELECT id FROM sys_user WHERE username LIKE '%" + username + "%'");
            }

            // 日期条件
            applyDateFilters(queryWrapper, planDate, startDate, endDate);

            // 排序
            applySort(queryWrapper, sortField, sortOrder);

            List<DailyPlanEntity> entityList = this.list(queryWrapper);

            return entityList.stream().map(entity -> {
                DailyPlanDTO dto = new DailyPlanDTO();
                BeanUtils.copyProperties(entity, dto);
                dto.setUsername(getUsernameByUserId(entity.getUserId()));
                return dto;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

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

            if (rs.next()) {
                return rs.getString("username");
            } else {
                return "用户" + userId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "未知用户";
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
