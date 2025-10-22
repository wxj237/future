package io.renren.modules.security.service;

import java.util.List;
import java.util.Map;

public interface LoginListService {
    boolean isStudentInLoginList(String studentId);
    Map<String, Object> getStudentInfo(String studentId);
    void batchImportLoginList(List<Map<String, String>> loginList);
    List<Map<String, String>> getAllLoginList();
}