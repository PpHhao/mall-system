package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.dto.cart.AddCartItemRequest;
import com.szu.mallsystem.entity.CartItem;
import com.szu.mallsystem.entity.Product;
import com.szu.mallsystem.entity.ProductImage;
import com.szu.mallsystem.mapper.CartItemMapper;
import com.szu.mallsystem.mapper.ProductImageMapper;
import com.szu.mallsystem.mapper.ProductMapper;
import com.szu.mallsystem.service.CartService;
import com.szu.mallsystem.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartService {
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;

    @Override
    @Transactional
    public CartItemVO addItem(Long userId, AddCartItemRequest request) {
        Product product = getProductOrThrow(request.getProductId());
        validateQuantity(product, request.getQuantity());

        CartItem existing = lambdaQuery()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, request.getProductId())
                .one();

        if (existing != null) {
            int newQuantity = existing.getQuantity() + request.getQuantity();
            validateQuantity(product, newQuantity);
            existing.setQuantity(newQuantity);
            existing.setUpdatedAt(LocalDateTime.now());
            updateById(existing);
            return buildCartItemVO(existing, product, getProductImage(product.getId()));
        }

        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setProductId(product.getId());
        cartItem.setQuantity(request.getQuantity());
        cartItem.setChecked(1);
        cartItem.setPriceAtAdd(product.getPrice());
        cartItem.setCreatedAt(LocalDateTime.now());
        cartItem.setUpdatedAt(LocalDateTime.now());
        save(cartItem);
        return buildCartItemVO(cartItem, product, getProductImage(product.getId()));
    }

    @Override
    @Transactional
    public CartItemVO updateQuantity(Long userId, Long itemId, Integer quantity) {
        CartItem cartItem = lambdaQuery()
                .eq(CartItem::getId, itemId)
                .eq(CartItem::getUserId, userId)
                .one();
        if (cartItem == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Cart item not found");
        }
        Product product = getProductOrThrow(cartItem.getProductId());
        validateQuantity(product, quantity);
        cartItem.setQuantity(quantity);
        cartItem.setUpdatedAt(LocalDateTime.now());
        updateById(cartItem);
        return buildCartItemVO(cartItem, product, getProductImage(product.getId()));
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long itemId) {
        CartItem cartItem = lambdaQuery()
                .eq(CartItem::getId, itemId)
                .eq(CartItem::getUserId, userId)
                .one();
        if (cartItem == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Cart item not found");
        }
        removeById(cartItem.getId());
    }

    @Override
    public List<CartItemVO> listItems(Long userId) {
        List<CartItem> items = lambdaQuery()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getUpdatedAt)
                .list();
        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> productIds = items.stream()
                .map(CartItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        Map<Long, String> productImages = loadProductImages(productIds);

        List<CartItemVO> result = new ArrayList<>();
        for (CartItem item : items) {
            Product product = productMap.get(item.getProductId());
            String image = productImages.get(item.getProductId());
            result.add(buildCartItemVO(item, product, image));
        }
        return result;
    }

    private Product getProductOrThrow(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Product not found");
        }
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Product is not available");
        }
        return product;
    }

    private void validateQuantity(Product product, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Quantity must be >= 1");
        }
        if (product.getStock() != null && product.getStock() < quantity) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Insufficient stock");
        }
    }

    private CartItemVO buildCartItemVO(CartItem cartItem, Product product, String productImage) {
        BigDecimal price = cartItem.getPriceAtAdd();
        BigDecimal total = price != null ? price.multiply(BigDecimal.valueOf(cartItem.getQuantity())) : null;
        return CartItemVO.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProductId())
                .productName(product != null ? product.getName() : null)
                .productImage(productImage)
                .price(price)
                .quantity(cartItem.getQuantity())
                .checked(cartItem.getChecked())
                .totalPrice(total)
                .build();
    }

    private String getProductImage(Long productId) {
        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .orderByAsc(ProductImage::getSort)
                        .last("limit 1")
        );
        if (images.isEmpty()) {
            return null;
        }
        return images.get(0).getUrl();
    }

    private Map<Long, String> loadProductImages(List<Long> productIds) {
        Map<Long, String> result = new HashMap<>();
        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .in(ProductImage::getProductId, productIds)
                        .orderByAsc(ProductImage::getSort)
        );
        for (ProductImage image : images) {
            result.putIfAbsent(image.getProductId(), image.getUrl());
        }
        return result;
    }
}
