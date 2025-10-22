package io.renren.modules.sys.dto;

import lombok.Data;
import java.util.Date;

@Data
public class InboxRemindDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String remindContent;
    private Integer remindType;
    private Integer readStatus;
    private Date remindTime;
    private Date createTime;
}