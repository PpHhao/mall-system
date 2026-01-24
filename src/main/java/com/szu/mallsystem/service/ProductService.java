package com.szu.mallsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.szu.mallsystem.dto.product.CreateProductRequest;
import com.szu.mallsystem.dto.product.ProductSearchRequest;
import com.szu.mallsystem.dto.product.UpdateProductRequest;
import com.szu.mallsystem.entity.Product;
import com.szu.mallsystem.vo.PageResult;
import com.szu.mallsystem.vo.ProductDetailVO;
import com.szu.mallsystem.vo.ProductVO;

public interface ProductService extends IService<Product> {
    /**
     * 创建商品
     */
    ProductVO createProduct(CreateProductRequest request);

    /**
     * 更新商品
     */
    ProductVO updateProduct(Long productId, UpdateProductRequest request);

    /**
     * 获取商品详情
     */
    ProductDetailVO getProductDetail(Long productId);

    /**
     * 搜索商品
     */
    PageResult<ProductVO> searchProducts(ProductSearchRequest request);

    /**
     * 更新商品状态（上下架）
     */
    void updateProductStatus(Long productId, Integer status);

    /**
     * 删除商品
     */
    void deleteProduct(Long productId);
}

