package com.nova.service;

import com.nova.dto.ShoppingCartDTO;
import com.nova.entity.ShoppingCart;
import java.util.List;

public interface ShoppingCartService {
    void add(ShoppingCartDTO dto);
    void sub(ShoppingCartDTO dto);
    List<ShoppingCart> list();
    void clean();
    void deleteById(Long id);
}
