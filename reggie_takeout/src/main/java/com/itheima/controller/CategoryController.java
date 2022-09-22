package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.Result;
import com.itheima.domain.Category;
import com.itheima.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //1.分页查询功能
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize) {
        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<Category>();
        //构造条件
        lambdaQueryWrapper.orderByAsc(Category::getSort);
        //实现分页
        categoryService.page(pageInfo, lambdaQueryWrapper);
        return Result.success(pageInfo);
    }

    //2.添加分类功能
    @PostMapping
    public Result<String> save(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success("添加分类成功");
    }

    //3.删除分类功能(分类中有套餐或菜品的可以删)
    @DeleteMapping
    public Result<String> delete(Long ids) {
        categoryService.remove(ids);
        return Result.success("删除成功");
    }

    //4.修改分类
    @PutMapping
    public Result<String> alter(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success("修改分类成功");
    }

    //5.在添加菜品页面中回显菜品分类
    @GetMapping("/list")
    public Result<List<Category>> getCategoryList(Integer type) {
        List<Category> categories = new ArrayList<Category>();
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<Category>();
        lambdaQueryWrapper.eq(Category::getType, type);
        lambdaQueryWrapper.orderByDesc(Category::getSort);
        categories = categoryService.list(lambdaQueryWrapper);
        return Result.success(categories);
    }
    @GetMapping("/list1")
    public Result<List<Category>> getCategoryList() {
        List<Category> categories = new ArrayList<Category>();
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<Category>();
        lambdaQueryWrapper.orderByDesc(Category::getSort);
        categories = categoryService.list(lambdaQueryWrapper);
        return Result.success(categories);
    }
}
