/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户导入Excel数据格式
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
public class SysUserImportExcel implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("姓名")
    private String realName;

    @ExcelProperty(value = "性别", converter = GenderConverter.class)
    private Integer gender;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("部门ID")
    private Long deptId;
}