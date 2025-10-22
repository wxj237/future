package io.renren.modules.sys.dto;

import lombok.Data;
import java.util.Date;

@Data
public class InboxDTO {
    private Long id;
    private String title;
    private String content;
    private String senderName;
    private String receiverType;
    private String groupName;
    private String menuName;
    private Integer readStatus;
    private Date createTime;
}