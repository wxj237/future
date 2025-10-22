package io.renren.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_inbox")
public class InboxEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private String title;
    private String content;
    private Long senderId;
    private String receiverType;
    private String receiverIds;
    private Long groupId;
    private Long menuId;
    private Integer readStatus;
    private Date createTime;
}