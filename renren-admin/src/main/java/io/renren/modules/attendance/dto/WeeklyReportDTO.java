package io.renren.modules.attendance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

public class WeeklyReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public WeeklyReportDTO() {
        // 显式提供无参构造函数
    }

    /** 关键：Long 用字符串序列化，避免前端精度丢失 */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    private String username;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date weekStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date weekEndDate;

    private String weeklySummary;
    private String nextWeekPlan;
    private String problems;
    private String suggestions;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public Date getWeekStartDate() {
        return weekStartDate;
    }
    public void setWeekStartDate(Date weekStartDate) {
        this.weekStartDate = weekStartDate;
    }
    public Date getWeekEndDate() {
        return weekEndDate;
    }
    public void setWeekEndDate(Date weekEndDate) {
        this.weekEndDate = weekEndDate;
    }
    public String getWeeklySummary() {
        return weeklySummary;
    }
    public void setWeeklySummary(String weeklySummary) {
        this.weeklySummary = weeklySummary;
    }
    public String getNextWeekPlan() {
        return nextWeekPlan;
    }
    public void setNextWeekPlan(String nextWeekPlan) {
        this.nextWeekPlan = nextWeekPlan;
    }
    public String getProblems() {
        return problems;
    }
    public void setProblems(String problems) {
        this.problems = problems;
    }
    public String getSuggestions() {
        return suggestions;
    }
    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    public Date getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "WeeklyReportDTO{" +
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
