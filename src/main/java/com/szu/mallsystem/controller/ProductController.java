package com.szu.mallsystem.controller;

import com.szu.mallsystem.common.Result;
import com.szu.mallsystem.dto.product.CreateProductRequest;
import com.szu.mallsystem.dto.product.ProductSearchRequest;
import com.szu.mallsystem.dto.product.UpdateProductRequest;
import com.szu.mallsystem.dto.product.UpdateProductStatusRequest;
import com.szu.mallsystem.service.ProductService;
import com.szu.mallsystem.vo.PageResult;
import com.szu.mallsystem.vo.ProductDetailVO;
import com.szu.mallsystem.vo.ProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    /**
     * 创建商品（需要管理员权限）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ProductVO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return Result.success(productService.createProduct(request));
    }

    /**
     * 更新商品（需要管理员权限）
     */
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ProductVO> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request) {
        return Result.success(productService.updateProduct(productId, request));
    }

    /**
     * 获取商品详情（公开接口）
     */
    @GetMapping("/{productId}")
    public Result<ProductDetailVO> getProductDetail(@PathVariable Long productId) {
        return Result.success(productService.getProductDetail(productId));
    }

    /**
     * 搜索商品（公开接口）
     */
    @GetMapping("/search")
    public Result<PageResult<ProductVO>> searchProducts(ProductSearchRequest request) {
        return Result.success(productService.searchProducts(request));
    }

    /**
     * 更新商品状态（上下架，需要管理员权限）
     */
    @PutMapping("/{productId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateProductStatus(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductStatusRequest request) {
        productService.updateProductStatus(productId, request.getStatus());
        return Result.success();
    }

    /**
     * 删除商品（需要管理员权限）
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return Result.success();
    }
}

