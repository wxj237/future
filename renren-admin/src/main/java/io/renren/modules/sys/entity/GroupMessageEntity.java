// src/main/java/io/renren/modules/sys/entity/GroupMessageEntity.java
package io.renren.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_group_message")
public class GroupMessageEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long groupId;

    private Long sendUserId;

    private String messageType; // text, image, file, link

    private String content;

    private String filePath;

    private String mentionedUsers; // @的用户ID列表，逗号分隔

    private Date createTime;
}