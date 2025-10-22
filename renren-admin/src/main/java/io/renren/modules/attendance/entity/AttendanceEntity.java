package io.renren.modules.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * 考勤记录实体
 */
@TableName("attendance_record")
public class AttendanceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 签到时间
     */
    private Date signInTime;
    
    /**
     * 签退时间
     */
    private Date signOutTime;
    
    /**
     * 签到类型 (manual-手动签到, location-定位签到)
     */
    private String signInType;
    
    /**
     * 签到位置信息
     */
    private String signInLocation;
    
    /**
     * 签退位置信息
     */
    private String signOutLocation;
    
    /**
     * 状态 (0-未签退, 1-已签退)
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;

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

    public Date getSignInTime() {
        return signInTime;
    }

    public void setSignInTime(Date signInTime) {
        this.signInTime = signInTime;
    }

    public Date getSignOutTime() {
        return signOutTime;
    }

    public void setSignOutTime(Date signOutTime) {
        this.signOutTime = signOutTime;
    }

    public String getSignInType() {
        return signInType;
    }

    public void setSignInType(String signInType) {
        this.signInType = signInType;
    }

    public String getSignInLocation() {
        return signInLocation;
    }

    public void setSignInLocation(String signInLocation) {
        this.signInLocation = signInLocation;
    }

    public String getSignOutLocation() {
        return signOutLocation;
    }

    public void setSignOutLocation(String signOutLocation) {
        this.signOutLocation = signOutLocation;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}