package io.renren.modules.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.attendance.dao.WeeklyReportDao;
import io.renren.modules.attendance.entity.WeeklyReportEntity;
import io.renren.modules.attendance.dto.WeeklyReportDTO;
import io.renren.modules.attendance.service.WeeklyReportService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import java.net.URLEncoder;
import jakarta.servlet.http.HttpServletResponse;

@Service("weeklyReportService")
public class WeeklyReportServiceImpl extends ServiceImpl<WeeklyReportDao, WeeklyReportEntity> implements WeeklyReportService {

    @Autowired
    private DataSource dataSource;

    @Override
    public WeeklyReportEntity getByUserIdAndWeek(Long userId, String weekStartDate) {
        try {
            QueryWrapper<WeeklyReportEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);

            if (StringUtils.isNotBlank(weekStartDate)) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(weekStartDate);
                    queryWrapper.eq("week_start_date", date);
                } catch (ParseException e) {
                    queryWrapper.apply("DATE_FORMAT(week_start_date, '%Y-%m-%d') = '" + weekStartDate + "'");
                }
            }

            return this.getOne(queryWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    @Override
    public Map<String, Object> getList(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            Integer page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
            Integer limit = params.get("limit") != null ? Integer.parseInt(params.get("limit").toString()) : 10;
            Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
            String username = (String) params.get("username");
            String weekStartDate = (String) params.get("weekStartDate");

            IPage<WeeklyReportEntity> pageObj = new Page<>(page, limit);

            QueryWrapper<WeeklyReportEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("create_time");

            if (userId != null) {
                queryWrapper.eq("user_id", userId);
            }

            if (StringUtils.isNotBlank(username)) {
                queryWrapper.inSql("user_id",
                        "SELECT id FROM sys_user WHERE username LIKE '%" + username + "%'");
            }

            if (StringUtils.isNotBlank(weekStartDate)) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(weekStartDate);
                    queryWrapper.eq("week_start_date", date);
                } catch (ParseException e) {
                    queryWrapper.apply("DATE_FORMAT(week_start_date, '%Y-%m-%d') = '" + weekStartDate + "'");
                }
            }

            IPage<WeeklyReportEntity> resultPage = this.page(pageObj, queryWrapper);

            List<WeeklyReportDTO> dtoList = new ArrayList<>();
            for (WeeklyReportEntity entity : resultPage.getRecords()) {
                WeeklyReportDTO dto = new WeeklyReportDTO();
                dto.setId(entity.getId());
                dto.setUserId(entity.getUserId());
                dto.setWeekStartDate(entity.getWeekStartDate());
                dto.setWeekEndDate(entity.getWeekEndDate());
                dto.setWeeklySummary(entity.getWeeklySummary());
                dto.setNextWeekPlan(entity.getNextWeekPlan());
                dto.setProblems(entity.getProblems());
                dto.setSuggestions(entity.getSuggestions());
                dto.setCreateTime(entity.getCreateTime());
                dto.setUpdateTime(entity.getUpdateTime());
                dto.setUsername(getUsernameByUserId(entity.getUserId()));

                dtoList.add(dto);
            }

            result.put("list", dtoList);
            result.put("total", resultPage.getTotal());

        } catch (Exception e) {
            e.printStackTrace();
            result.put("list", null);
            result.put("total", 0L);
        }

        return result;
    }

    @Override
    public void exportWeeklyReport(HttpServletResponse response, Map<String, Object> params) throws Exception {
        try {
            // 获取要导出的数据
            Map<String, Object> exportParams = new HashMap<>(params);

            // 根据导出范围设置分页参数
            String exportScope = (String) exportParams.get("exportScope");
            if ("current".equals(exportScope)) {
                // 当前页数据，保留分页参数
                if (exportParams.get("page") == null) {
                    exportParams.put("page", 1);
                }
                if (exportParams.get("limit") == null) {
                    exportParams.put("limit", 10);
                }
            } else {
                // 全部数据，移除分页参数
                exportParams.remove("page");
                exportParams.remove("limit");
            }

            // 移除columns参数，避免影响查询
            String columns = (String) exportParams.remove("columns");
            exportParams.remove("exportScope");

            Map<String, Object> result = getList(exportParams);
            List<WeeklyReportDTO> dataList = (List<WeeklyReportDTO>) result.get("list");

            if (dataList == null || dataList.isEmpty()) {
                throw new Exception("没有数据可导出");
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("周报数据_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()), "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            // 准备导出数据
            List<ExportWeeklyReportVO> exportData = new ArrayList<>();
            for (WeeklyReportDTO item : dataList) {
                ExportWeeklyReportVO vo = new ExportWeeklyReportVO();
                vo.setId(item.getId());
                vo.setUserId(item.getUserId());
                vo.setUsername(item.getUsername());
                vo.setWeekStartDate(item.getWeekStartDate());
                vo.setWeekEndDate(item.getWeekEndDate());
                vo.setWeeklySummary(item.getWeeklySummary());
                vo.setNextWeekPlan(item.getNextWeekPlan());
                vo.setProblems(item.getProblems());
                vo.setSuggestions(item.getSuggestions());
                vo.setCreateTime(item.getCreateTime());
                vo.setUpdateTime(item.getUpdateTime());
                exportData.add(vo);
            }

            // 使用EasyExcel导出
            EasyExcel.write(response.getOutputStream(), ExportWeeklyReportVO.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 自动列宽
                    .sheet("周报数据")
                    .doWrite(exportData);

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("导出失败: " + e.getMessage());
        }
    }

    // 内部导出VO类
    public static class ExportWeeklyReportVO {
        @com.alibaba.excel.annotation.ExcelProperty("ID")
        private Long id;

        @com.alibaba.excel.annotation.ExcelProperty("用户ID")
        private Long userId;

        @com.alibaba.excel.annotation.ExcelProperty("用户名")
        private String username;

        @com.alibaba.excel.annotation.ExcelProperty("周开始日期")
        private Date weekStartDate;

        @com.alibaba.excel.annotation.ExcelProperty("周结束日期")
        private Date weekEndDate;

        @com.alibaba.excel.annotation.ExcelProperty("本周总结")
        private String weeklySummary;

        @com.alibaba.excel.annotation.ExcelProperty("下周计划")
        private String nextWeekPlan;

        @com.alibaba.excel.annotation.ExcelProperty("遇到的问题")
        private String problems;

        @com.alibaba.excel.annotation.ExcelProperty("建议")
        private String suggestions;

        @com.alibaba.excel.annotation.ExcelProperty("创建时间")
        private Date createTime;

        @com.alibaba.excel.annotation.ExcelProperty("更新时间")
        private Date updateTime;

        // getter setter 方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public Date getWeekStartDate() { return weekStartDate; }
        public void setWeekStartDate(Date weekStartDate) { this.weekStartDate = weekStartDate; }
        public Date getWeekEndDate() { return weekEndDate; }
        public void setWeekEndDate(Date weekEndDate) { this.weekEndDate = weekEndDate; }
        public String getWeeklySummary() { return weeklySummary; }
        public void setWeeklySummary(String weeklySummary) { this.weeklySummary = weeklySummary; }
        public String getNextWeekPlan() { return nextWeekPlan; }
        public void setNextWeekPlan(String nextWeekPlan) { this.nextWeekPlan = nextWeekPlan; }
        public String getProblems() { return problems; }
        public void setProblems(String problems) { this.problems = problems; }
        public String getSuggestions() { return suggestions; }
        public void setSuggestions(String suggestions) { this.suggestions = suggestions; }
        public Date getCreateTime() { return createTime; }
        public void setCreateTime(Date createTime) { this.createTime = createTime; }
        public Date getUpdateTime() { return updateTime; }
        public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    }
}