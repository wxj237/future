package io.renren.modules.attendance.dto;

import lombok.Data;

@Data
public class GeoSignRequest {
    private Double latitude;   // 前端获取
    private Double longitude;  // 前端获取
    private String address;    // 可选（前端逆地理或高德返回）
    private String remark;     // 可选
}
