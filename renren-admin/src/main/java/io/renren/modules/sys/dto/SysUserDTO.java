/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户管理
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Data
@Schema(title = "用户管理")
public class SysUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@Schema(title = "id")
	@Null(message="{id.null}", groups = AddGroup.class)
	@NotNull(message="{id.require}", groups = UpdateGroup.class)
	private Long id;

	@Schema(title = "用户名", required = true)
	@NotBlank(message="{sysuser.username.require}", groups = DefaultGroup.class)
	private String username;

	@Schema(title = "密码")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@NotBlank(message="{sysuser.password.require}", groups = AddGroup.class)
	private String password;

	@Schema(title = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message="{sysuser.realName.require}", groups = DefaultGroup.class)
	private String realName;

	@Schema(title = "头像")
	private String headUrl;

	@Schema(title = "性别   0：男   1：女    2：保密", required = true)
	@Range(min=0, max=2, message = "{sysuser.gender.range}", groups = DefaultGroup.class)
	private Integer gender;

	@Schema(title = "邮箱")
	@Email(message="{sysuser.email.error}", groups = DefaultGroup.class)
	private String email;

	@Schema(title = "手机号")
	private String mobile;

	@Schema(title = "部门ID", required = true)
	@NotNull(message="{sysuser.deptId.require}", groups = DefaultGroup.class)
	private Long deptId;

	@Schema(title = "状态  0：停用    1：正常", required = true)
	@Range(min=0, max=1, message = "{sysuser.status.range}", groups = DefaultGroup.class)
	private Integer status;

	@Schema(title = "创建时间")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Date createDate;

	@Schema(title = "超级管理员   0：否   1：是")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Integer superAdmin;

	@Schema(title = "角色ID列表")
	private List<Long> roleIdList;

	@Schema(title = "部门名称")
	private String deptName;
	
	// 显式添加getter和setter方法，解决Lombok未被正确识别的问题
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getRealName() {
		return realName;
	}
	
	public void setRealName(String realName) {
		this.realName = realName;
	}
	
	public Integer getStatus() {
		return status;
	}
	
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public Date getCreateDate() {
		return createDate;
	}
	
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public Long getDeptId() {
		return deptId;
	}
	
	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}
	
	public String getMobile() {
		return mobile;
	}
	
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public Integer getGender() {
		return gender;
	}
	
	public void setGender(Integer gender) {
		this.gender = gender;
	}
	
	public String getHeadUrl() {
		return headUrl;
	}
	
	public void setHeadUrl(String headUrl) {
		this.headUrl = headUrl;
	}
	
	public Integer getSuperAdmin() {
		return superAdmin;
	}
	
	public void setSuperAdmin(Integer superAdmin) {
		this.superAdmin = superAdmin;
	}
	
	public List<Long> getRoleIdList() {
		return roleIdList;
	}
	
	public void setRoleIdList(List<Long> roleIdList) {
		this.roleIdList = roleIdList;
	}
	
	public String getDeptName() {
		return deptName;
	}
	
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
}