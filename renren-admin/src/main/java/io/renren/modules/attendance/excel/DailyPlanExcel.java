// DailyPlanExcel.java
package io.renren.modules.attendance.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;
import java.util.Date;

@Data
public class DailyPlanExcel {
    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("用户ID")
    private Long userId;

    @ExcelProperty("用户名")
    private String username;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty("计划日期")
    private Date planDate;

    @ExcelProperty("工作内容")
    private String content;

    @ExcelProperty("完成情况")
    private String completion;

    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("创建时间")
    private Date createTime;

    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("更新时间")
    private Date updateTime;
}