package com.szu.mallsystem.controller;

import com.szu.mallsystem.common.Result;
import com.szu.mallsystem.dto.category.CreateCategoryRequest;
import com.szu.mallsystem.dto.category.UpdateCategoryRequest;
import com.szu.mallsystem.service.CategoryService;
import com.szu.mallsystem.vo.CategoryVO;
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

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    /**
     * 创建分类（需要管理员权限）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CategoryVO> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return Result.success(categoryService.createCategory(request));
    }

    /**
     * 更新分类（需要管理员权限）
     */
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CategoryVO> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return Result.success(categoryService.updateCategory(categoryId, request));
    }

    /**
     * 获取分类树（多级分类，公开接口）
     */
    @GetMapping("/tree")
    public Result<List<CategoryVO>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    /**
     * 获取分类详情（公开接口）
     */
    @GetMapping("/{categoryId}")
    public Result<CategoryVO> getCategory(@PathVariable Long categoryId) {
        return Result.success(categoryService.getCategory(categoryId));
    }

    /**
     * 删除分类（需要管理员权限）
     */
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return Result.success();
    }
}

