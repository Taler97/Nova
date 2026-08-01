package com.nova.mapper;

import com.github.pagehelper.Page;
import com.nova.annotation.AutoFill;
import com.nova.dto.EmployeePageQueryDTO;
import com.nova.entity.Employee;
import com.nova.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    @Select("SELECT * FROM employee WHERE username = #{username}")
    Employee getByUsername(String username);

    @Select("SELECT * FROM employee WHERE id = #{id}")
    Employee getById(Long id);

    @AutoFill(OperationType.INSERT)
    @Insert("INSERT INTO employee (name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "VALUES (#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Employee employee);

    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);

    Page<Employee> pageQuery(EmployeePageQueryDTO dto);
}
