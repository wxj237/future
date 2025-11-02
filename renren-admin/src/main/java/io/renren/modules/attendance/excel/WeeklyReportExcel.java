package io.renren.modules.attendance.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;
import java.util.Date;

@Data
public class WeeklyReportExcel {
    public WeeklyReportExcel() {
        // 显式提供无参构造函数
    }
    
    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("用户ID")
    private Long userId;

    @ExcelProperty("用户名")
    private String username;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty("周起始日期")
    private Date weekStartDate;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty("周结束日期")
    private Date weekEndDate;

    @ExcelProperty("本周总结")
    private String weeklySummary;

    @ExcelProperty("下周计划")
    private String nextWeekPlan;

    @ExcelProperty("存在问题")
    private String problems;

    @ExcelProperty("建议")
    private String suggestions;

    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("创建时间")
    private Date createTime;

    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("更新时间")
    private Date updateTime;
    
    @Override
    public String toString() {
        return "WeeklyReportExcel{" +
                "id=" + id +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", weekStartDate=" + weekStartDate +
                ", weekEndDate=" + weekEndDate +
                ", weeklySummary='" + weeklySummary + '\'' +
                ", nextWeekPlan='" + nextWeekPlan + '\'' +
                ", problems='" + problems + '\'' +
                ", suggestions='" + suggestions + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}