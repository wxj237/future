package io.renren.modules.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.attendance.dao.WeeklyReportDao;
import io.renren.modules.attendance.dto.WeeklyReportDTO;
import io.renren.modules.attendance.entity.WeeklyReportEntity;
import io.renren.modules.attendance.service.WeeklyReportService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("weeklyReportService")
public class WeeklyReportServiceImpl extends ServiceImpl<WeeklyReportDao, WeeklyReportEntity> implements WeeklyReportService {

    private static final Logger log = LoggerFactory.getLogger(WeeklyReportServiceImpl.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private WeeklyReportDao weeklyReportDao;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 将 ISO 周（2025-W41）解析为该周周一/周日 */
    private LocalDate[] parseWeekToRange(String weekIso) {
        try {
            if (StringUtils.isBlank(weekIso) || !weekIso.matches("\\d{4}-W\\d{2}")) return null;
            int year = Integer.parseInt(weekIso.substring(0, 4));
            int week = Integer.parseInt(weekIso.substring(6, 8));
            LocalDate any = LocalDate.ofYearDay(year, 4).with(WeekFields.ISO.weekOfYear(), week);
            LocalDate start = any.with(WeekFields.ISO.dayOfWeek(), 1);
            LocalDate end = any.with(WeekFields.ISO.dayOfWeek(), 7);
            return new LocalDate[]{start, end};
        } catch (Exception e) {
            return null;
        }
    }

    /** 排序白名单（Entity 字段 -> DB 列） */
    private String mapSortField(String f) {
        if (StringUtils.isBlank(f)) return null;
        switch (f) {
            case "id": return "id";
            case "userId": return "user_id";
            case "weekStartDate": return "week_start_date";
            case "weekEndDate": return "week_end_date";
            case "createTime": return "create_time";
            case "updateTime": return "update_time";
            default: return null;
        }
    }

    /** 应用周/区间筛选（week 优先） */
    private void applyDateFilter(QueryWrapper<WeeklyReportEntity> qw, String week, String startDate, String endDate) {
        if (StringUtils.isNotBlank(week)) {
            LocalDate[] r = parseWeekToRange(week);
            if (r != null) {
                qw.ge("week_start_date", java.sql.Date.valueOf(r[0]));
                qw.le("week_end_date", java.sql.Date.valueOf(r[1]));
                return;
            }
        }
        if (StringUtils.isNotBlank(startDate)) {
            try { qw.ge("week_start_date", java.sql.Date.valueOf(LocalDate.parse(startDate, DF))); }
            catch (Exception ignore) { qw.apply("DATE(week_start_date) >= {0}", startDate); }
        }
        if (StringUtils.isNotBlank(endDate)) {
            try { qw.le("week_end_date", java.sql.Date.valueOf(LocalDate.parse(endDate, DF))); }
            catch (Exception ignore) { qw.apply("DATE(week_end_date) <= {0}", endDate); }
        }
    }

    /** 应用排序（默认 create_time desc） */
    private void applySort(QueryWrapper<WeeklyReportEntity> qw, String sortField, String sortOrder) {
        String col = mapSortField(sortField);
        boolean desc = !"asc".equalsIgnoreCase(sortOrder);
        if (col != null) {
            if (desc) qw.orderByDesc(col); else qw.orderByAsc(col);
        } else {
            qw.orderByDesc("create_time");
        }
    }

    private String getUsernameByUserId(Long userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT username FROM sys_user WHERE id = ?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception ignore) {}
        return "用户" + userId;
    }

    @Override
    public WeeklyReportEntity getByUserIdAndWeek(Long userId, String weekIso) {
        QueryWrapper<WeeklyReportEntity> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        LocalDate[] r = parseWeekToRange(weekIso);
        if (r != null) {
            qw.ge("week_start_date", java.sql.Date.valueOf(r[0]));
            qw.le("week_end_date", java.sql.Date.valueOf(r[1]));
        }
        return this.getOne(qw);
    }

    @Override
    public Map<String, Object> getList(Map<String, Object> params) {
        Map<String, Object> out = new HashMap<>();
        try {
            int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
            int limit = params.get("limit") != null ? Integer.parseInt(params.get("limit").toString()) : 10;
            Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
            String username = (String) params.get("username");
            String week = (String) params.get("week");
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");
            String sortField = (String) params.get("sortField");
            String sortOrder = (String) params.get("sortOrder");

            IPage<WeeklyReportEntity> pageObj = new Page<>(page, limit);
            QueryWrapper<WeeklyReportEntity> qw = new QueryWrapper<>();

            if (userId != null) qw.eq("user_id", userId);
            if (StringUtils.isNotBlank(username)) {
                qw.inSql("user_id", "SELECT id FROM sys_user WHERE username LIKE '%" + username + "%'");
            }

            applyDateFilter(qw, week, startDate, endDate);
            applySort(qw, sortField, sortOrder);

            IPage<WeeklyReportEntity> res = this.page(pageObj, qw);

            List<Map<String, Object>> list = new ArrayList<>();
            for (WeeklyReportEntity e : res.getRecords()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", e.getId());
                m.put("userId", e.getUserId());
                m.put("username", getUsernameByUserId(e.getUserId()));
                m.put("weekStartDate", e.getWeekStartDate());
                m.put("weekEndDate", e.getWeekEndDate());
                m.put("weeklySummary", e.getWeeklySummary());
                m.put("nextWeekPlan", e.getNextWeekPlan());
                m.put("problems", e.getProblems());
                m.put("suggestions", e.getSuggestions());
                m.put("createTime", e.getCreateTime());
                m.put("updateTime", e.getUpdateTime());
                list.add(m);
            }
            out.put("list", list);
            out.put("total", res.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            out.put("list", Collections.emptyList());
            out.put("total", 0);
        }
        return out;
    }

    @Override
    public List<WeeklyReportDTO> list(Map<String, Object> params) {
        try {
            Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
            String username = (String) params.get("username");
            String week = (String) params.get("week");
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");
            String sortField = (String) params.get("sortField");
            String sortOrder = (String) params.get("sortOrder");

            QueryWrapper<WeeklyReportEntity> qw = new QueryWrapper<>();
            if (userId != null) qw.eq("user_id", userId);
            if (StringUtils.isNotBlank(username)) {
                qw.inSql("user_id", "SELECT id FROM sys_user WHERE username LIKE '%" + username + "%'");
            }

            applyDateFilter(qw, week, startDate, endDate);
            applySort(qw, sortField, sortOrder);

            List<WeeklyReportEntity> list = this.list(qw);

            // 添加日志记录
            log.info("查询到周报数据 {} 条", list != null ? list.size() : 0);
            log.debug("查询参数: userId={}, username={}, week={}, startDate={}, endDate={}", userId, username, week, startDate, endDate);

            if (list == null) {
                return new ArrayList<>();
            }

            return list.stream().map(e -> {
                if (e == null) {
                    return null;
                }
                WeeklyReportDTO d = new WeeklyReportDTO();
                BeanUtils.copyProperties(e, d);
                d.setUsername(getUsernameByUserId(e.getUserId()));
                return d;
            }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取周报列表失败", e);
            log.error("查询参数: {}", params);
            return new ArrayList<>();
        }
    }

    @Override
    public WeeklyReportEntity saveOrUpdateReturning(WeeklyReportEntity req) {
        java.util.Date now = new java.util.Date();
        if (req.getId() == null) req.setCreateTime(now);
        req.setUpdateTime(now);
        this.saveOrUpdate(req);
        return this.getById(req.getId());
    }

    @Override
    public WeeklyReportEntity saveOrUpsertForUserAndWeek(WeeklyReportEntity req) {
        QueryWrapper<WeeklyReportEntity> qw = new QueryWrapper<>();
        qw.eq("user_id", req.getUserId());
        if (req.getWeekStartDate() != null) qw.eq("week_start_date", req.getWeekStartDate());
        if (req.getWeekEndDate() != null) qw.eq("week_end_date", req.getWeekEndDate());

        WeeklyReportEntity exist = this.getOne(qw);
        java.util.Date now = new java.util.Date();

        if (exist != null) {
            exist.setWeeklySummary(req.getWeeklySummary());
            exist.setNextWeekPlan(req.getNextWeekPlan());
            exist.setProblems(req.getProblems());
            exist.setSuggestions(req.getSuggestions());
            exist.setUpdateTime(now);
            this.updateById(exist);
            return exist;
        } else {
            req.setCreateTime(now);
            req.setUpdateTime(now);
            this.save(req);
            return req;
        }
    }

    // === 覆盖删除（不吞异常） ===
    @Override
    public boolean removeById(Serializable id) {
        try {
            WeeklyReportEntity entity = this.getById(id);
            if (entity == null) {
                return false;
            }

            boolean result = super.removeById(id);

            if (result) {
                WeeklyReportEntity after = this.getById(id);
                if (after != null) {
                    result = false;
                }
            }
            return result;
        } catch (Exception e) {
            // 不要吞异常；抛出去让 Controller 返回清晰 msg（如外键约束失败）
            throw new RuntimeException(e);
        }
    }

    /** 直接SQL删除（物理删除；异常不吞） */
    @Override
    public boolean directDelete(Long id) {
        try {
            log.info("=== 使用直接SQL删除，ID: {} ===", id);

            WeeklyReportEntity entity = this.getById(id);
            if (entity == null) {
                log.warn("要删除的记录不存在，ID: {}", id);
                return false;
            }

            int affectedRows = weeklyReportDao.directDeleteById(id);
            boolean result = affectedRows > 0;

            if (result) {
                WeeklyReportEntity afterDelete = this.getById(id);
                if (afterDelete != null) {
                    log.error("=== 直接SQL删除验证失败，记录仍然存在 ===");
                    result = false;
                }
            }
            return result;
        } catch (Exception e) {
            log.error("直接SQL删除异常", e);
            // 抛出给 Controller，让前端能看到具体 msg
            throw new RuntimeException(e);
        }
    }
}
