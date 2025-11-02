package io.renren.modules.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 地理签到记录（对齐 sign_in_record 表）
 * 表字段：
 * id, user_id, point_id, longitude, latitude, distance, result, reason, sign_time, address
 */
@TableName("sign_in_record")
public class AttendanceGeoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 签到点ID（可为空） */
    private Long pointId;

    /** 经度 */
    private Double longitude;

    /** 纬度 */
    private Double latitude;

    /** 距离（米，或你的单位） */
    private Double distance;

    /** 结果（如 0/1，或业务约定） */
    private Integer result;

    /** 原因/备注（对应 reason 列） */
    private String reason;

    /** 签到时间（对应 sign_time 列） */
    private Date signTime;

    /** ✅ 新增：地址字段 */
    private String address;

    // ===== Getter / Setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPointId() { return pointId; }
    public void setPointId(Long pointId) { this.pointId = pointId; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Integer getResult() { return result; }
    public void setResult(Integer result) { this.result = result; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Date getSignTime() { return signTime; }
    public void setSignTime(Date signTime) { this.signTime = signTime; }

    /** ✅ 新增：address 的 getter/setter */
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}