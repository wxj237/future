package io.renren.modules.security.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("login_list")
public class LoginListEntity {
    @TableId
    private Long id;

    private String studentId; // 学号
    private String name; // 姓名
    
    // 显式添加getter和setter方法，解决Lombok未被正确识别的问题
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}