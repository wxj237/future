// src/main/java/io/renren/modules/sys/entity/GroupEntity.java
package io.renren.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_group")
public class GroupEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String groupName;

    private Long parentId;

    private Integer sort;

    private Integer status;

    private String description;

    private Long createUserId;

    private Date createTime;
}