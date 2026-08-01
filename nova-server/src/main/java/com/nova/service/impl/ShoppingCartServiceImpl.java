package com.nova.service.impl;

import com.nova.context.BaseContext;
import com.nova.dto.ShoppingCartDTO;
import com.nova.entity.Dish;
import com.nova.entity.ShoppingCart;
import com.nova.mapper.DishMapper;
import com.nova.mapper.ShoppingCartMapper;
import com.nova.service.ShoppingCartService;
import com.nova.utils.AliOssUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartMapper shoppingCartMapper;
    private final DishMapper dishMapper;
    private final AliOssUtil aliOssUtil;

    @Override
    public void sub(ShoppingCartDTO dto) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart cart = null;
        if (dto.getDishId() != null) {
            cart = shoppingCartMapper.getByUserIdAndDishId(userId, dto.getDishId());
        } else if (dto.getSetmealId() != null) {
            cart = shoppingCartMapper.getByUserIdAndSetmealId(userId, dto.getSetmealId());
        }
        if (cart != null) {
            if (cart.getNumber() > 1) {
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.update(cart);
            } else {
                shoppingCartMapper.deleteById(cart.getId());
            }
        }
    }

    @Override
    public void add(ShoppingCartDTO dto) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart cart = null;
        if (dto.getDishId() != null) {
            cart = shoppingCartMapper.getByUserIdAndDishId(userId, dto.getDishId());
        }
        if (cart != null) {
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.update(cart);
            return;
        }
        cart = new ShoppingCart();
        BeanUtils.copyProperties(dto, cart);
        cart.setUserId(userId);
        cart.setNumber(1);
        cart.setCreateTime(LocalDateTime.now());
        if (dto.getDishId() != null) {
            Dish dish = dishMapper.getById(dto.getDishId());
            if (dish != null) {
                cart.setAmount(dish.getPrice());
                cart.setImage(dish.getImage());
            }
        }
        shoppingCartMapper.insert(cart);
    }

    @Override
    public List<ShoppingCart> list() {
        List<ShoppingCart> list = shoppingCartMapper.list(BaseContext.getCurrentId());
        list.forEach(cart -> cart.setImage(aliOssUtil.convertToSignedUrl(cart.getImage())));
        return list;
    }

    @Override
    public void clean() {
        shoppingCartMapper.cleanByUserId(BaseContext.getCurrentId());
    }

    @Override
    public void deleteById(Long id) {
        shoppingCartMapper.deleteById(id);
    }
}
