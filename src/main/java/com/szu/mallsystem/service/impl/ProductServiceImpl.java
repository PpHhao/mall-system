package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.dto.product.CreateProductRequest;
import com.szu.mallsystem.dto.product.ProductSearchRequest;
import com.szu.mallsystem.dto.product.UpdateProductRequest;
import com.szu.mallsystem.entity.Category;
import com.szu.mallsystem.entity.Product;
import com.szu.mallsystem.entity.ProductImage;
import com.szu.mallsystem.mapper.CategoryMapper;
import com.szu.mallsystem.mapper.ProductImageMapper;
import com.szu.mallsystem.mapper.ProductMapper;
import com.szu.mallsystem.service.ProductService;
import com.szu.mallsystem.vo.PageResult;
import com.szu.mallsystem.vo.ProductDetailVO;
import com.szu.mallsystem.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    private final CategoryMapper categoryMapper;
    private final ProductImageMapper productImageMapper;

    @Override
    @Transactional
    public ProductVO createProduct(CreateProductRequest request) {
        // 验证分类是否存在
        Category category = categoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }

        // 创建商品
        Product product = new Product();
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setSubtitle(request.getSubtitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setStatus(1); // 默认上架
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        save(product);

        // 保存商品图片
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            saveProductImages(product.getId(), request.getImageUrls());
        }

        return buildProductVO(product);
    }

    @Override
    @Transactional
    public ProductVO updateProduct(Long productId, UpdateProductRequest request) {
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }

        // 如果更新分类，验证分类是否存在
        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategoryId())) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
            }
            product.setCategoryId(request.getCategoryId());
        }

        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName());
        }
        if (request.getSubtitle() != null) {
            product.setSubtitle(request.getSubtitle());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        product.setUpdatedAt(LocalDateTime.now());
        updateById(product);

        // 更新商品图片
        if (request.getImageUrls() != null) {
            // 删除旧图片
            productImageMapper.delete(new LambdaQueryWrapper<ProductImage>()
                    .eq(ProductImage::getProductId, productId));
            // 保存新图片
            if (!request.getImageUrls().isEmpty()) {
                saveProductImages(productId, request.getImageUrls());
            }
        }

        return buildProductVO(product);
    }

    @Override
    public ProductDetailVO getProductDetail(Long productId) {
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }

        Category category = categoryMapper.selectById(product.getCategoryId());
        List<String> images = getProductImages(productId);

        return ProductDetailVO.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .categoryName(category != null ? category.getName() : null)
                .name(product.getName())
                .subtitle(product.getSubtitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .status(product.getStatus())
                .images(images)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    @Override
    public PageResult<ProductVO> searchProducts(ProductSearchRequest request) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（商品名称、副标题、描述）
        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Product::getName, request.getKeyword())
                    .or()
                    .like(Product::getSubtitle, request.getKeyword())
                    .or()
                    .like(Product::getDescription, request.getKeyword())
            );
        }

        // 分类筛选
        if (request.getCategoryId() != null) {
            queryWrapper.eq(Product::getCategoryId, request.getCategoryId());
        }

        // 状态筛选
        if (request.getStatus() != null) {
            queryWrapper.eq(Product::getStatus, request.getStatus());
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(Product::getCreatedAt);

        // 分页查询
        Page<Product> page = new Page<>(request.getPage(), request.getPageSize());
        IPage<Product> productPage = page(page, queryWrapper);

        // 转换为VO
        List<ProductVO> productVOs = productPage.getRecords().stream()
                .map(this::buildProductVO)
                .collect(Collectors.toList());

        long totalPages = (productPage.getTotal() + request.getPageSize() - 1) / request.getPageSize();

        return PageResult.<ProductVO>builder()
                .records(productVOs)
                .total(productPage.getTotal())
                .page(request.getPage())
                .pageSize(request.getPageSize())
                .totalPages((int) totalPages)
                .build();
    }

    @Override
    public void updateProductStatus(Long productId, Integer status) {
        if (status != 1 && status != 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态值无效，只能为1（上架）或2（下架）");
        }

        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }

        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());
        updateById(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }

        // 删除商品图片
        productImageMapper.delete(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, productId));

        // 删除商品（直接使用mapper的deleteById方法）
        baseMapper.deleteById(productId);
    }

    /**
     * 构建ProductVO
     */
    private ProductVO buildProductVO(Product product) {
        Category category = categoryMapper.selectById(product.getCategoryId());
        List<String> images = getProductImages(product.getId());

        return ProductVO.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .categoryName(category != null ? category.getName() : null)
                .name(product.getName())
                .subtitle(product.getSubtitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .status(product.getStatus())
                .images(images)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    /**
     * 获取商品图片列表
     */
    private List<String> getProductImages(Long productId) {
        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .orderByAsc(ProductImage::getSort)
        );
        return images.stream()
                .map(ProductImage::getUrl)
                .collect(Collectors.toList());
    }

    /**
     * 保存商品图片
     */
    private void saveProductImages(Long productId, List<String> imageUrls) {
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setUrl(imageUrls.get(i));
            image.setSort(i);
            productImageMapper.insert(image);
        }
    }
}

