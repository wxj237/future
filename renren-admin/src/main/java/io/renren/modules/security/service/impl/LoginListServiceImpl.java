package io.renren.modules.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.modules.security.dao.LoginListDao;
import io.renren.modules.security.entity.LoginListEntity;
import io.renren.modules.security.service.LoginListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoginListServiceImpl implements LoginListService {
    @Autowired
    private LoginListDao loginListDao;

    @Override
    public boolean isStudentInLoginList(String studentId) {
        return loginListDao.getByStudentId(studentId) != null;
    }

    @Override
    public Map<String, Object> getStudentInfo(String studentId) {
        LoginListEntity entity = loginListDao.getByStudentId(studentId);
        if (entity == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("studentId", entity.getStudentId());
        result.put("name", entity.getName());

        return result;
    }

    @Override
    @Transactional
    public void batchImportLoginList(List<Map<String, String>> loginList) {
        for (Map<String, String> map : loginList) {
            LoginListEntity entity = new LoginListEntity();
            entity.setStudentId(map.get("studentId"));
            entity.setName(map.get("name"));

            loginListDao.insert(entity);
        }
    }

    @Override
    public List<Map<String, String>> getAllLoginList() {
        List<LoginListEntity> entities = loginListDao.selectList(new QueryWrapper<>());
        List<Map<String, String>> result = new ArrayList<>();

        for (LoginListEntity entity : entities) {
            Map<String, String> map = new HashMap<>();
            map.put("studentId", entity.getStudentId());
            map.put("name", entity.getName());
            result.add(map);
        }

        return result;
    }
}