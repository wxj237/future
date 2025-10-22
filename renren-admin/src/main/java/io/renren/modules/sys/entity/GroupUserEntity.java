// src/main/java/io/renren/modules/sys/entity/GroupUserEntity.java
package io.renren.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_group_user")
public class GroupUserEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long groupId;

    private Long userId;

    private Integer roleType; // 0:普通成员 1:管理员
}