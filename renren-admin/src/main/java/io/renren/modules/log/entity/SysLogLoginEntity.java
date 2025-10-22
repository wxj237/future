/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.log.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.renren.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录日志
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("sys_log_login")
public class SysLogLoginEntity extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/**
	 * 用户操作   0：用户登录   1：用户退出
	 */
	private Integer operation;
	/**
	 * 状态  0：失败    1：成功    2：账号已锁定
	 */
	private Integer status;
	/**
	 * 用户代理
	 */
	private String userAgent;
	/**
	 * 操作IP
	 */
	private String ip;
	/**
	 * 用户名
	 */
	private String creatorName;
	
	// 显式添加getter和setter方法，解决Lombok未被正确识别的问题
	
	public Integer getOperation() {
		return operation;
	}
	
	public void setOperation(Integer operation) {
		this.operation = operation;
	}
	
	public Integer getStatus() {
		return status;
	}
	
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public String getUserAgent() {
		return userAgent;
	}
	
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getCreatorName() {
		return creatorName;
	}
	
	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
	
	// 继承自BaseEntity的字段也需要显式添加getter/setter方法
	public Long getCreator() {
		return super.getCreator();
	}
	
	public void setCreator(Long creator) {
		super.setCreator(creator);
	}
	
	public Long getId() {
		return super.getId();
	}
	
	public void setId(Long id) {
		super.setId(id);
	}
	
	public java.util.Date getCreateDate() {
		return super.getCreateDate();
	}
	
	public void setCreateDate(java.util.Date createDate) {
		super.setCreateDate(createDate);
	}
}