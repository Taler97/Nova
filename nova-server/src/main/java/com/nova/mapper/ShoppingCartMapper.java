package com.nova.mapper;

import com.nova.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    @Select("SELECT * FROM shopping_cart WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ShoppingCart> list(Long userId);

    @Insert("INSERT INTO shopping_cart (user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            "VALUES (#{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})")
    void insert(ShoppingCart cart);

    @Delete("DELETE FROM shopping_cart WHERE user_id = #{userId}")
    void cleanByUserId(Long userId);

    @Delete("DELETE FROM shopping_cart WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT * FROM shopping_cart WHERE user_id = #{userId} AND dish_id = #{dishId}")
    ShoppingCart getByUserIdAndDishId(Long userId, Long dishId);

    @Select("SELECT * FROM shopping_cart WHERE user_id = #{userId} AND setmeal_id = #{setmealId}")
    ShoppingCart getByUserIdAndSetmealId(Long userId, Long setmealId);

    @Update("UPDATE shopping_cart SET number = #{number} WHERE id = #{id}")
    void update(ShoppingCart cart);
}
