package com.szu.mallsystem.controller;

import com.szu.mallsystem.common.Result;
import com.szu.mallsystem.dto.cart.AddCartItemRequest;
import com.szu.mallsystem.dto.cart.UpdateCartItemRequest;
import com.szu.mallsystem.entity.User;
import com.szu.mallsystem.security.CurrentUserProvider;
import com.szu.mallsystem.service.CartService;
import com.szu.mallsystem.vo.CartItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public Result<List<CartItemVO>> listCartItems() {
        User user = currentUserProvider.getCurrentUser();
        return Result.success(cartService.listItems(user.getId()));
    }

    @PostMapping("/items")
    public Result<CartItemVO> addItem(@Valid @RequestBody AddCartItemRequest request) {
        User user = currentUserProvider.getCurrentUser();
        return Result.success(cartService.addItem(user.getId(), request));
    }

    @PutMapping("/items/{itemId}")
    public Result<CartItemVO> updateQuantity(@PathVariable Long itemId,
                                             @Valid @RequestBody UpdateCartItemRequest request) {
        User user = currentUserProvider.getCurrentUser();
        return Result.success(cartService.updateQuantity(user.getId(), itemId, request.getQuantity()));
    }

    @DeleteMapping("/items/{itemId}")
    public Result<Void> removeItem(@PathVariable Long itemId) {
        User user = currentUserProvider.getCurrentUser();
        cartService.removeItem(user.getId(), itemId);
        return Result.success();
    }
}
