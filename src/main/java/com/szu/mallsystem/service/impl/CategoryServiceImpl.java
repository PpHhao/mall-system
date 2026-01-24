package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.dto.category.CreateCategoryRequest;
import com.szu.mallsystem.dto.category.UpdateCategoryRequest;
import com.szu.mallsystem.entity.Category;
import com.szu.mallsystem.entity.Product;
import com.szu.mallsystem.mapper.CategoryMapper;
import com.szu.mallsystem.mapper.ProductMapper;
import com.szu.mallsystem.service.CategoryService;
import com.szu.mallsystem.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public CategoryVO createCategory(CreateCategoryRequest request) {
        // 如果指定了父分类，验证父分类是否存在
        if (request.getParentId() != null && request.getParentId() != 0) {
            Category parent = getById(request.getParentId());
            if (parent == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "父分类不存在");
            }
        }

        Category category = new Category();
        category.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        category.setName(request.getName());
        save(category);

        return buildCategoryVO(category);
    }

    @Override
    @Transactional
    public CategoryVO updateCategory(Long categoryId, UpdateCategoryRequest request) {
        Category category = getById(categoryId);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }

        // 如果更新父分类，验证父分类是否存在且不能是自己
        if (request.getParentId() != null) {
            if (request.getParentId().equals(categoryId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "不能将自己设为父分类");
            }
            if (request.getParentId() != 0) {
                Category parent = getById(request.getParentId());
                if (parent == null) {
                    throw new BusinessException(ErrorCode.NOT_FOUND, "父分类不存在");
                }
                // 检查是否会形成循环（简单检查：不能将父分类设为子分类）
                if (isDescendant(categoryId, request.getParentId())) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "不能将子分类设为父分类");
                }
            }
            category.setParentId(request.getParentId());
        }

        if (StringUtils.hasText(request.getName())) {
            category.setName(request.getName());
        }

        updateById(category);
        return buildCategoryVO(category);
    }

    @Override
    public List<CategoryVO> getCategoryTree() {
        // 查询所有分类
        List<Category> allCategories = list();

        // 转换为VO
        List<CategoryVO> categoryVOs = allCategories.stream()
                .map(this::buildCategoryVO)
                .collect(Collectors.toList());

        // 按parentId分组
        Map<Long, List<CategoryVO>> categoryMap = categoryVOs.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() != null ? c.getParentId() : 0L));

        // 构建树形结构
        List<CategoryVO> rootCategories = categoryMap.getOrDefault(0L, new ArrayList<>());
        for (CategoryVO category : categoryVOs) {
            List<CategoryVO> children = categoryMap.getOrDefault(category.getId(), new ArrayList<>());
            category.setChildren(children);
        }

        return rootCategories;
    }

    @Override
    public CategoryVO getCategory(Long categoryId) {
        Category category = getById(categoryId);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }

        CategoryVO categoryVO = buildCategoryVO(category);

        // 加载子分类
        List<Category> children = list(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, categoryId));
        List<CategoryVO> childrenVOs = children.stream()
                .map(this::buildCategoryVO)
                .collect(Collectors.toList());
        categoryVO.setChildren(childrenVOs);

        return categoryVO;
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = getById(categoryId);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }

        // 检查是否有子分类
        long childCount = count(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, categoryId));
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该分类下存在子分类，无法删除");
        }

        // 检查是否有商品使用该分类
        long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getCategoryId, categoryId));
        if (productCount > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该分类下存在商品，无法删除");
        }

        // 删除分类（直接使用mapper的deleteById方法）
        baseMapper.deleteById(categoryId);
    }

    /**
     * 检查categoryId是否是parentId的后代
     */
    private boolean isDescendant(Long categoryId, Long parentId) {
        Category current = getById(categoryId);
        if (current == null) {
            return false;
        }

        Long currentParentId = current.getParentId();
        if (currentParentId == null || currentParentId == 0) {
            return false;
        }

        if (currentParentId.equals(parentId)) {
            return true;
        }

        return isDescendant(currentParentId, parentId);
    }

    /**
     * 构建CategoryVO
     */
    private CategoryVO buildCategoryVO(Category category) {
        return CategoryVO.builder()
                .id(category.getId())
                .parentId(category.getParentId())
                .name(category.getName())
                .children(new ArrayList<>())
                .build();
    }
}

