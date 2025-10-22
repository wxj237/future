// DailyPlanServiceImpl.java
package io.renren.modules.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.attendance.dao.DailyPlanDao;
import io.renren.modules.attendance.entity.DailyPlanEntity;
import io.renren.modules.attendance.dto.DailyPlanDTO;
import io.renren.modules.attendance.service.DailyPlanService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service("dailyPlanService")
public class DailyPlanServiceImpl extends ServiceImpl<DailyPlanDao, DailyPlanEntity> implements DailyPlanService {

    @Autowired
    private DataSource dataSource;

    @Override
    public DailyPlanEntity getByUserIdAndDate(Long userId, String planDate) {
        try {
            QueryWrapper<DailyPlanEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);

            if (StringUtils.isNotBlank(planDate)) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(planDate);
                    queryWrapper.eq("plan_date", date);
                } catch (ParseException e) {
                    queryWrapper.apply("DATE_FORMAT(plan_date, '%Y-%m-%d') = '" + planDate + "'");
                }
            }

            return this.getOne(queryWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map<String, Object> getList(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            Integer page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
            Integer limit = params.get("limit") != null ? Integer.parseInt(params.get("limit").toString()) : 10;
            Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
            String username = (String) params.get("username");
            String planDate = (String) params.get("planDate");

            IPage<DailyPlanEntity> pageObj = new Page<>(page, limit);
            QueryWrapper<DailyPlanEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("create_time");

            if (userId != null) {
                queryWrapper.eq("user_id", userId);
            }

            if (StringUtils.isNotBlank(username)) {
                queryWrapper.inSql("user_id",
                        "SELECT id FROM sys_user WHERE username LIKE '%" + username + "%'");
            }

            if (StringUtils.isNotBlank(planDate)) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(planDate);
                    queryWrapper.eq("plan_date", date);
                } catch (ParseException e) {
                    queryWrapper.apply("DATE_FORMAT(plan_date, '%Y-%m-%d') = '" + planDate + "'");
                }
            }

            IPage<DailyPlanEntity> resultPage = this.page(pageObj, queryWrapper);

            // 转换为包含用户名的DTO列表
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (DailyPlanEntity entity : resultPage.getRecords()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", entity.getId());
                item.put("userId", entity.getUserId());
                item.put("username", getUsernameByUserId(entity.getUserId()));
                item.put("planDate", entity.getPlanDate());
                item.put("content", entity.getContent());
                item.put("completion", entity.getCompletion());
                item.put("createTime", entity.getCreateTime());
                item.put("updateTime", entity.getUpdateTime());
                resultList.add(item);
            }

            result.put("list", resultList);
            result.put("total", resultPage.getTotal());

        } catch (Exception e) {
            e.printStackTrace();
            result.put("list", new ArrayList<>());
            result.put("total", 0L);
        }

        return result;
    }

    @Override
    public List<DailyPlanDTO> list(Map<String, Object> params) {
        try {
            Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
            String username = (String) params.get("username");
            String planDate = (String) params.get("planDate");

            QueryWrapper<DailyPlanEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("create_time");

            if (userId != null) {
                queryWrapper.eq("user_id", userId);
            }

            if (StringUtils.isNotBlank(username)) {
                queryWrapper.inSql("user_id",
                        "SELECT id FROM sys_user WHERE username LIKE '%" + username + "%'");
            }

            if (StringUtils.isNotBlank(planDate)) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(planDate);
                    queryWrapper.eq("plan_date", date);
                } catch (ParseException e) {
                    queryWrapper.apply("DATE_FORMAT(plan_date, '%Y-%m-%d') = '" + planDate + "'");
                }
            }

            List<DailyPlanEntity> entityList = this.list(queryWrapper);

            // 转换为DTO列表
            return entityList.stream().map(entity -> {
                DailyPlanDTO dto = new DailyPlanDTO();
                BeanUtils.copyProperties(entity, dto);
                dto.setUsername(getUsernameByUserId(entity.getUserId()));
                return dto;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String getUsernameByUserId(Long userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT username FROM sys_user WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            } else {
                return "用户" + userId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "未知用户";
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}