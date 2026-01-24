package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.dto.category.CreateCategoryRequest;
import com.szu.mallsystem.dto.category.UpdateCategoryRequest;
import com.szu.mallsystem.entity.Category;
import com.szu.mallsystem.entity.Product;
import com.szu.mallsystem.mapper.CategoryMapper;
import com.szu.mallsystem.mapper.ProductMapper;
import com.szu.mallsystem.vo.CategoryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService单元测试")
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category rootCategory;
    private Category childCategory;
    private Category grandchildCategory;

    @BeforeEach
    void setUp() throws Exception {
        // 使用反射设置baseMapper
        Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(categoryService, categoryMapper);

        // 设置测试数据
        rootCategory = new Category();
        rootCategory.setId(1L);
        rootCategory.setName("电子产品");
        rootCategory.setParentId(0L);

        childCategory = new Category();
        childCategory.setId(2L);
        childCategory.setName("手机");
        childCategory.setParentId(1L);

        grandchildCategory = new Category();
        grandchildCategory.setId(3L);
        grandchildCategory.setName("智能手机");
        grandchildCategory.setParentId(2L);
    }

    @Test
    @DisplayName("创建分类-顶级分类")
    void testCreateCategory_RootCategory() {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setParentId(0L);
        request.setName("电子产品");

        when(categoryMapper.insert(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(1L);
            return 1;
        });

        // When
        CategoryVO result = categoryService.createCategory(request);

        // Then
        assertNotNull(result);
        assertEquals("电子产品", result.getName());
        assertEquals(0L, result.getParentId());
        verify(categoryMapper, times(1)).insert(any(Category.class));
    }

    @Test
    @DisplayName("创建分类-子分类")
    void testCreateCategory_ChildCategory() {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setParentId(1L);
        request.setName("手机");

        when(categoryMapper.selectById(1L)).thenReturn(rootCategory);
        when(categoryMapper.insert(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(2L);
            return 1;
        });

        // When
        CategoryVO result = categoryService.createCategory(request);

        // Then
        assertNotNull(result);
        assertEquals("手机", result.getName());
        assertEquals(1L, result.getParentId());
        verify(categoryMapper, times(1)).selectById(1L);
        verify(categoryMapper, times(1)).insert(any(Category.class));
    }

    @Test
    @DisplayName("创建分类-父分类不存在")
    void testCreateCategory_ParentNotFound() {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setParentId(999L);
        request.setName("手机");

        when(categoryMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            categoryService.createCategory(request);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("父分类不存在", exception.getMessage());
        verify(categoryMapper, times(1)).selectById(999L);
        verify(categoryMapper, never()).insert(any(Category.class));
    }

    @Test
    @DisplayName("更新分类-成功")
    void testUpdateCategory_Success() {
        // Given
        Long categoryId = 2L;
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("智能手机");

        when(categoryMapper.selectById(categoryId)).thenReturn(childCategory);
        when(categoryMapper.updateById(any(Category.class))).thenReturn(1);

        // When
        CategoryVO result = categoryService.updateCategory(categoryId, request);

        // Then
        assertNotNull(result);
        assertEquals("智能手机", result.getName());
        verify(categoryMapper, times(1)).selectById(categoryId);
        verify(categoryMapper, times(1)).updateById(any(Category.class));
    }

    @Test
    @DisplayName("更新分类-分类不存在")
    void testUpdateCategory_CategoryNotFound() {
        // Given
        Long categoryId = 999L;
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("新名称");

        when(categoryMapper.selectById(categoryId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            categoryService.updateCategory(categoryId, request);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("分类不存在", exception.getMessage());
        verify(categoryMapper, never()).updateById(any(Category.class));
    }

    @Test
    @DisplayName("更新分类-不能将自己设为父分类")
    void testUpdateCategory_CannotSetSelfAsParent() {
        // Given
        Long categoryId = 2L;
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setParentId(2L); // 将自己设为父分类

        when(categoryMapper.selectById(categoryId)).thenReturn(childCategory);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            categoryService.updateCategory(categoryId, request);
        });

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertEquals("不能将自己设为父分类", exception.getMessage());
        verify(categoryMapper, never()).updateById(any(Category.class));
    }

    @Test
    @DisplayName("更新分类-父分类不存在")
    void testUpdateCategory_ParentNotFound() {
        // Given
        Long categoryId = 2L;
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setParentId(999L);

        when(categoryMapper.selectById(categoryId)).thenReturn(childCategory);
        when(categoryMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            categoryService.updateCategory(categoryId, request);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("父分类不存在", exception.getMessage());
        verify(categoryMapper, never()).updateById(any(Category.class));
    }

    @Test
    @DisplayName("更新分类-不能将子分类设为父分类")
    void testUpdateCategory_CannotSetDescendantAsParent() {
        // Given
        // 测试场景：3L(grandchildCategory)的父分类是2L，2L的父分类是1L
        // 尝试将1L设为3L的父分类，但由于3L是1L的后代，所以不能将1L设为3L的父分类
        Long testCategoryId = 3L; // grandchildCategory
        UpdateCategoryRequest testRequest = new UpdateCategoryRequest();
        testRequest.setParentId(1L); // 尝试将1L设为3L的父分类

        when(categoryMapper.selectById(testCategoryId)).thenReturn(grandchildCategory);
        when(categoryMapper.selectById(1L)).thenReturn(rootCategory);
        // isDescendant(3L, 1L)会递归检查：3L的parentId是2L，2L的parentId是1L，所以3L是1L的后代
        when(categoryMapper.selectById(2L)).thenReturn(childCategory);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            categoryService.updateCategory(testCategoryId, testRequest);
        });

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertEquals("不能将子分类设为父分类", exception.getMessage());
    }

    @Test
    @DisplayName("获取分类树-成功")
    void testGetCategoryTree_Success() {
        // Given
        List<Category> allCategories = Arrays.asList(rootCategory, childCategory, grandchildCategory);
        when(categoryMapper.selectList(any())).thenReturn(allCategories);

        // When
        List<CategoryVO> result = categoryService.getCategoryTree();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // 应该只有一个根分类
        assertEquals("电子产品", result.get(0).getName());
        assertNotNull(result.get(0).getChildren());
        assertEquals(1, result.get(0).getChildren().size()); // 应该有一个子分类
        assertEquals("手机", result.get(0).getChildren().get(0).getName());
    }

    @Test
    @DisplayName("获取分类详情-成功")
    void testGetCategory_Success() {
        // Given
        Long categoryId = 1L;
        List<Category> children = Arrays.asList(childCategory);

        when(categoryMapper.selectById(categoryId)).thenReturn(rootCategory);
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(children);

        // When
        CategoryVO result = categoryService.getCategory(categoryId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("电子产品", result.getName());
        assertNotNull(result.getChildren());
        assertEquals(1, result.getChildren().size());
        assertEquals("手机", result.getChildren().get(0).getName());
    }

    @Test
    @DisplayName("获取分类详情-分类不存在")
    void testGetCategory_CategoryNotFound() {
        // Given
        Long categoryId = 999L;
        when(categoryMapper.selectById(categoryId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            categoryService.getCategory(categoryId);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("分类不存在", exception.getMessage());
    }

    @Test
    @DisplayName("删除分类-成功")
    void testDeleteCategory_Success() {
        // Given
        Long categoryId = 3L;
        when(categoryMapper.selectById(categoryId)).thenReturn(grandchildCategory);
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.deleteById(categoryId)).thenReturn(1);
        when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // When
        categoryService.deleteCategory(categoryId);

        // Then
        verify(categoryMapper, times(1)).selectById(categoryId);
        verify(categoryMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(productMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(categoryMapper, times(1)).deleteById(categoryId);
    }

    @Test
    @DisplayName("删除分类-分类不存在")
    void testDeleteCategory_CategoryNotFound() {
        // Given
        Long categoryId = 999L;
        when(categoryMapper.selectById(categoryId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            categoryService.deleteCategory(categoryId);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("分类不存在", exception.getMessage());
        verify(categoryMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("删除分类-存在子分类")
    void testDeleteCategory_HasChildren() {
        // Given
        Long categoryId = 1L;
        when(categoryMapper.selectById(categoryId)).thenReturn(rootCategory);
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L); // 有子分类

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            categoryService.deleteCategory(categoryId);
        });

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertEquals("该分类下存在子分类，无法删除", exception.getMessage());
        verify(categoryMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("删除分类-存在商品")
    void testDeleteCategory_HasProducts() {
        // Given
        Long categoryId = 3L;
        when(categoryMapper.selectById(categoryId)).thenReturn(grandchildCategory);
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L); // 无子分类
        when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L); // 有5个商品

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            categoryService.deleteCategory(categoryId);
        });

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertEquals("该分类下存在商品，无法删除", exception.getMessage());
        verify(categoryMapper, never()).deleteById(anyLong());
    }
}

