package com.szu.mallsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.szu.mallsystem.dto.category.CreateCategoryRequest;
import com.szu.mallsystem.dto.category.UpdateCategoryRequest;
import com.szu.mallsystem.entity.Category;
import com.szu.mallsystem.vo.CategoryVO;

import java.util.List;

public interface CategoryService extends IService<Category> {
    /**
     * 创建分类
     */
    CategoryVO createCategory(CreateCategoryRequest request);

    /**
     * 更新分类
     */
    CategoryVO updateCategory(Long categoryId, UpdateCategoryRequest request);

    /**
     * 获取分类树（多级分类）
     */
    List<CategoryVO> getCategoryTree();

    /**
     * 获取分类详情
     */
    CategoryVO getCategory(Long categoryId);

    /**
     * 删除分类
     */
    void deleteCategory(Long categoryId);
}

