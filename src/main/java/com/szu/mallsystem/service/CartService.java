package com.szu.mallsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.szu.mallsystem.dto.cart.AddCartItemRequest;
import com.szu.mallsystem.entity.CartItem;
import com.szu.mallsystem.vo.CartItemVO;

import java.util.List;

public interface CartService extends IService<CartItem> {
    CartItemVO addItem(Long userId, AddCartItemRequest request);

    CartItemVO updateQuantity(Long userId, Long itemId, Integer quantity);

    void removeItem(Long userId, Long itemId);

    List<CartItemVO> listItems(Long userId);
}
