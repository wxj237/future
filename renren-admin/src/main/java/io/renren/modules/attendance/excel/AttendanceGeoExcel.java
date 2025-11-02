package io.renren.modules.attendance.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import java.io.Serializable;
import java.util.Date;

/**
 * 导出 Excel 映射类（与日报的 DailyPlanExcel 同用途）
 * 添加EasyExcel注解来定义中文表头
 */
public class AttendanceGeoExcel implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelProperty("ID")
    @ColumnWidth(8)
    private Long id;

    @ExcelProperty("用户ID")
    @ColumnWidth(12)
    private Long userId;

    @ExcelProperty("用户名")
    @ColumnWidth(15)
    private String username;

    @ExcelProperty("部门") // ✅ 新增：部门列
    @ColumnWidth(20)
    private String deptName;

    @ExcelProperty("签到时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date checkTime;

    @ExcelProperty("经度")
    @ColumnWidth(15)
    private Double longitude;

    @ExcelProperty("纬度")
    @ColumnWidth(15)
    private Double latitude;

    @ExcelProperty("地址")
    @ColumnWidth(30)
    private String address;

    @ExcelProperty("备注")
    @ColumnWidth(20)
    private String remark;

    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ExcelProperty("更新时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // ===== Getter/Setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDeptName() { return deptName; } // ✅ 新增：部门名称getter
    public void setDeptName(String deptName) { this.deptName = deptName; } // ✅ 新增：部门名称setter
    public Date getCheckTime() { return checkTime; }
    public void setCheckTime(Date checkTime) { this.checkTime = checkTime; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}