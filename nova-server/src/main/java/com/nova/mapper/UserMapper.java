package com.nova.mapper;

import com.nova.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM `user` WHERE openid = #{openid}")
    User getByOpenid(String openid);

    @Select("SELECT * FROM `user` WHERE phone = #{phone}")
    User getByPhone(String phone);

    @Select("SELECT * FROM `user` WHERE id = #{id}")
    User getById(Long id);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO `user` (openid, name, phone, password, sex, avatar, create_time) VALUES (#{openid}, #{name}, #{phone}, #{password}, #{sex}, #{avatar}, #{createTime})")
    void insert(User user);

    @Update("UPDATE `user` SET name = #{name}, avatar = #{avatar} WHERE id = #{id}")
    void update(User user);
}
