package io.renren.modules.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.attendance.dto.AttendanceGeoDTO;
import io.renren.modules.attendance.entity.AttendanceGeoEntity;

import java.util.List;
import java.util.Map;

/**
 * 定位签到服务接口（对齐日报 Service 结构）
 */
public interface AttendanceGeoService extends IService<AttendanceGeoEntity> {

    /**
     * 分页列表（返回 {list,total}，与日报 getList 一致）
     */
    Map<String, Object> getList(Map<String, Object> params);

    /**
     * 导出用列表（DTO 列表，供 ExcelUtils 使用）
     */
    List<AttendanceGeoDTO> list(Map<String, Object> params);
}
