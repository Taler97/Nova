package com.nova.service;

import com.nova.dto.EmployeeDTO;
import com.nova.dto.EmployeeLoginDTO;
import com.nova.dto.EmployeePageQueryDTO;
import com.nova.dto.PasswordEditDTO;
import com.nova.entity.Employee;
import com.nova.result.PageResult;

public interface EmployeeService {
    Employee login(EmployeeLoginDTO dto, String clientIp);
    PageResult pageQuery(EmployeePageQueryDTO dto);
    Employee getById(Long id);
    void save(EmployeeDTO dto);
    void update(EmployeeDTO dto);
    void startOrStop(Integer status, Long id);
    void editPassword(PasswordEditDTO dto);
}
