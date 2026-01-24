package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.szu.mallsystem.vo.PageResult;
import com.szu.mallsystem.vo.ProductDetailVO;
import com.szu.mallsystem.vo.ProductVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService单元测试")
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private ProductImageMapper productImageMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category testCategory;
    private Product testProduct;
    private ProductImage testProductImage;

    @BeforeEach
    void setUp() throws Exception {
        // 使用反射设置baseMapper
        Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(productService, productMapper);

        // 设置测试数据
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("电子产品");
        testCategory.setParentId(0L);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setCategoryId(1L);
        testProduct.setName("iPhone 15");
        testProduct.setSubtitle("最新款苹果手机");
        testProduct.setDescription("iPhone 15详细描述");
        testProduct.setPrice(new BigDecimal("5999.00"));
        testProduct.setStock(100);
        testProduct.setStatus(1);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());

        testProductImage = new ProductImage();
        testProductImage.setId(1L);
        testProductImage.setProductId(1L);
        testProductImage.setUrl("https://example.com/image1.jpg");
        testProductImage.setSort(0);
    }

    @Test
    @DisplayName("创建商品-成功")
    void testCreateProduct_Success() {
        // Given
        CreateProductRequest request = new CreateProductRequest();
        request.setCategoryId(1L);
        request.setName("iPhone 15");
        request.setSubtitle("最新款苹果手机");
        request.setDescription("iPhone 15详细描述");
        request.setPrice(new BigDecimal("5999.00"));
        request.setStock(100);
        request.setImageUrls(Arrays.asList("https://example.com/image1.jpg", "https://example.com/image2.jpg"));

        when(categoryMapper.selectById(1L)).thenReturn(testCategory);
        when(productMapper.insert(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(1L);
            return 1;
        });
        when(productImageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        // When
        ProductVO result = productService.createProduct(request);

        // Then
        assertNotNull(result);
        assertEquals("iPhone 15", result.getName());
        assertEquals(1L, result.getCategoryId());
        assertEquals("电子产品", result.getCategoryName());
        assertEquals(1, result.getStatus());
        verify(categoryMapper, times(2)).selectById(1L); // 一次在createProduct中，一次在buildProductVO中
        verify(productMapper, times(1)).insert(any(Product.class));
        verify(productImageMapper, times(2)).insert(any(ProductImage.class));
    }

    @Test
    @DisplayName("创建商品-分类不存在")
    void testCreateProduct_CategoryNotFound() {
        // Given
        CreateProductRequest request = new CreateProductRequest();
        request.setCategoryId(999L);
        request.setName("iPhone 15");
        request.setPrice(new BigDecimal("5999.00"));
        request.setStock(100);

        when(categoryMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.createProduct(request);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("分类不存在", exception.getMessage());
        verify(categoryMapper, times(1)).selectById(999L);
        verify(productMapper, never()).insert(any(Product.class));
    }

    @Test
    @DisplayName("更新商品-成功")
    void testUpdateProduct_Success() {
        // Given
        Long productId = 1L;
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("iPhone 15 Pro");
        request.setPrice(new BigDecimal("6999.00"));
        request.setStock(50);

        when(productMapper.selectById(productId)).thenReturn(testProduct);
        when(categoryMapper.selectById(1L)).thenReturn(testCategory);
        when(productImageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // When
        ProductVO result = productService.updateProduct(productId, request);

        // Then
        assertNotNull(result);
        assertEquals("iPhone 15 Pro", result.getName());
        verify(productMapper, times(1)).selectById(productId);
        verify(productMapper, times(1)).updateById(any(Product.class));
    }

    @Test
    @DisplayName("更新商品-商品不存在")
    void testUpdateProduct_ProductNotFound() {
        // Given
        Long productId = 999L;
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("iPhone 15 Pro");

        when(productMapper.selectById(productId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.updateProduct(productId, request);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("商品不存在", exception.getMessage());
        verify(productMapper, times(1)).selectById(productId);
        verify(productMapper, never()).updateById(any(Product.class));
    }

    @Test
    @DisplayName("更新商品-更新分类时分类不存在")
    void testUpdateProduct_CategoryNotFound() {
        // Given
        Long productId = 1L;
        UpdateProductRequest request = new UpdateProductRequest();
        request.setCategoryId(999L);

        when(productMapper.selectById(productId)).thenReturn(testProduct);
        when(categoryMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.updateProduct(productId, request);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("分类不存在", exception.getMessage());
    }

    @Test
    @DisplayName("获取商品详情-成功")
    void testGetProductDetail_Success() {
        // Given
        Long productId = 1L;
        List<ProductImage> images = Arrays.asList(testProductImage);

        when(productMapper.selectById(productId)).thenReturn(testProduct);
        when(categoryMapper.selectById(1L)).thenReturn(testCategory);
        when(productImageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(images);

        // When
        ProductDetailVO result = productService.getProductDetail(productId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("iPhone 15", result.getName());
        assertEquals("电子产品", result.getCategoryName());
        assertNotNull(result.getImages());
        assertEquals(1, result.getImages().size());
        assertEquals("https://example.com/image1.jpg", result.getImages().get(0));
    }

    @Test
    @DisplayName("获取商品详情-商品不存在")
    void testGetProductDetail_ProductNotFound() {
        // Given
        Long productId = 999L;
        when(productMapper.selectById(productId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.getProductDetail(productId);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("商品不存在", exception.getMessage());
    }

    @Test
    @DisplayName("搜索商品-按关键词搜索")
    void testSearchProducts_ByKeyword() {
        // Given
        ProductSearchRequest request = new ProductSearchRequest();
        request.setKeyword("iPhone");
        request.setPage(1);
        request.setPageSize(10);

        List<Product> products = Arrays.asList(testProduct);
        Page<Product> page = new Page<>(1, 10);
        page.setRecords(products);
        page.setTotal(1);

        when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(categoryMapper.selectById(1L)).thenReturn(testCategory);
        when(productImageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        // When
        PageResult<ProductVO> result = productService.searchProducts(request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getPage());
        assertEquals(10, result.getPageSize());
    }

    @Test
    @DisplayName("搜索商品-按分类筛选")
    void testSearchProducts_ByCategory() {
        // Given
        ProductSearchRequest request = new ProductSearchRequest();
        request.setCategoryId(1L);
        request.setPage(1);
        request.setPageSize(10);

        List<Product> products = Arrays.asList(testProduct);
        Page<Product> page = new Page<>(1, 10);
        page.setRecords(products);
        page.setTotal(1);

        when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(categoryMapper.selectById(1L)).thenReturn(testCategory);
        when(productImageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        // When
        PageResult<ProductVO> result = productService.searchProducts(request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("搜索商品-按状态筛选")
    void testSearchProducts_ByStatus() {
        // Given
        ProductSearchRequest request = new ProductSearchRequest();
        request.setStatus(1);
        request.setPage(1);
        request.setPageSize(10);

        List<Product> products = Arrays.asList(testProduct);
        Page<Product> page = new Page<>(1, 10);
        page.setRecords(products);
        page.setTotal(1);

        when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(categoryMapper.selectById(1L)).thenReturn(testCategory);
        when(productImageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        // When
        PageResult<ProductVO> result = productService.searchProducts(request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("更新商品状态-上架")
    void testUpdateProductStatus_OnShelf() {
        // Given
        Long productId = 1L;
        Integer status = 1;

        when(productMapper.selectById(productId)).thenReturn(testProduct);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // When
        productService.updateProductStatus(productId, status);

        // Then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper, times(1)).updateById(productCaptor.capture());
        assertEquals(1, productCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("更新商品状态-下架")
    void testUpdateProductStatus_OffShelf() {
        // Given
        Long productId = 1L;
        Integer status = 2;

        when(productMapper.selectById(productId)).thenReturn(testProduct);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // When
        productService.updateProductStatus(productId, status);

        // Then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper, times(1)).updateById(productCaptor.capture());
        assertEquals(2, productCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("更新商品状态-状态值无效")
    void testUpdateProductStatus_InvalidStatus() {
        // Given
        Long productId = 1L;
        Integer status = 3;

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.updateProductStatus(productId, status);
        });

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("状态值无效"));
        verify(productMapper, never()).updateById(any(Product.class));
    }

    @Test
    @DisplayName("更新商品状态-商品不存在")
    void testUpdateProductStatus_ProductNotFound() {
        // Given
        Long productId = 999L;
        Integer status = 1;

        when(productMapper.selectById(productId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.updateProductStatus(productId, status);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("商品不存在", exception.getMessage());
    }

    @Test
    @DisplayName("删除商品-成功")
    void testDeleteProduct_Success() {
        // Given
        Long productId = 1L;
        when(productMapper.selectById(productId)).thenReturn(testProduct);
        when(productImageMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(productMapper.deleteById(productId)).thenReturn(1);

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productMapper, times(1)).selectById(productId);
        verify(productImageMapper, times(1)).delete(any(LambdaQueryWrapper.class));
        verify(productMapper, times(1)).deleteById(productId);
    }

    @Test
    @DisplayName("删除商品-商品不存在")
    void testDeleteProduct_ProductNotFound() {
        // Given
        Long productId = 999L;
        when(productMapper.selectById(productId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.deleteProduct(productId);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("商品不存在", exception.getMessage());
        verify(productMapper, never()).deleteById(anyLong());
    }
}

