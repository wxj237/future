package io.renren.modules.attendance.dto;

import java.io.Serializable;
import java.util.Date;

/** 导出/列表用 DTO（与日报 DTO 角色一致） */
public class AttendanceGeoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String username;   // 由 Service 组装
    private String deptName;   // ✅ 新增：部门名称
    private Date checkTime;    // ✅ 新增：签到时间
    private Double longitude;
    private Double latitude;
    private String address;
    private String remark;
    private Date createTime;
    private Date updateTime;

    /** ✅ 新增：以下字段确保从实体类完整映射 */
    private Long pointId;
    private Double distance;
    private Integer result;
    private String reason;     // ✅ 关键：添加这个字段
    private Date signTime;
    private Integer inRange;   // 用于前端显示是否在范围内

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

    /** ✅ 新增字段的 getter/setter */
    public Long getPointId() { return pointId; }
    public void setPointId(Long pointId) { this.pointId = pointId; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Integer getResult() { return result; }
    public void setResult(Integer result) { this.result = result; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Date getSignTime() { return signTime; }
    public void setSignTime(Date signTime) { this.signTime = signTime; }

    public Integer getInRange() { return inRange; }
    public void setInRange(Integer inRange) { this.inRange = inRange; }
}