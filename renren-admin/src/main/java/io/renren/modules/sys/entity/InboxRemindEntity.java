package io.renren.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_inbox_remind")
public class InboxRemindEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private Long userId;
    private String remindContent;
    private Integer remindType;
    private Integer readStatus;
    private Date remindTime;
    private Date createTime;
}