package com.nova.mapper;

import com.nova.entity.AddressBook;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AddressBookMapper {

    @Select("SELECT * FROM address_book WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<AddressBook> list(Long userId);

    @Select("SELECT * FROM address_book WHERE id = #{id}")
    AddressBook getById(Long id);

    @Insert("INSERT INTO address_book (user_id, consignee, phone, detail, is_default, create_time) " +
            "VALUES (#{userId}, #{consignee}, #{phone}, #{detail}, #{isDefault}, #{createTime})")
    void insert(AddressBook addressBook);

    @Update("UPDATE address_book SET consignee = #{consignee}, phone = #{phone}, detail = #{detail}, is_default = #{isDefault} WHERE id = #{id} AND user_id = #{userId}")
    void update(AddressBook addressBook);

    @Delete("DELETE FROM address_book WHERE id = #{id} AND user_id = #{userId}")
    void deleteById(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE address_book SET is_default = 0 WHERE user_id = #{userId}")
    void clearDefault(Long userId);

    @Select("SELECT * FROM address_book WHERE user_id = #{userId} AND is_default = 1 LIMIT 1")
    AddressBook getDefaultByUserId(Long userId);
}
