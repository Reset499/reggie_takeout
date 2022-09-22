package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.Result;
import com.itheima.domain.Category;
import com.itheima.domain.Dish;
import com.itheima.domain.DishFlavor;
import com.itheima.dto.DishDto;
import com.itheima.service.CategoryService;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    //1.菜品分页功能
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        Page pageInfo = new Page(page, pageSize);
        //因为dish中只有categoryId,并没有categoryName,因此前端页面在显示分类名称时找不到categoryName无法显示,因此要用dto对象page
        Page dishDtoInfo = new Page(page, pageSize);
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<Dish>();
        lambdaQueryWrapper.orderByDesc(Dish::getPrice);
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name), Dish::getName, name);
        dishService.page(pageInfo, lambdaQueryWrapper);
        //将查询出结果的dish形式的pageInfo复制给dishDto,除了records,因为records中是dish对象的list,而要dishDto对象的list
        BeanUtils.copyProperties(pageInfo, dishDtoInfo, "records");
        List<Dish> dishes = pageInfo.getRecords();
        List<DishDto> dishDtos = dishes.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            Long categoryId = item.getCategoryId();
            BeanUtils.copyProperties(item, dishDto);
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoInfo.setRecords(dishDtos);
        return Result.success(dishDtoInfo);
    }

    //2.新增菜品功能
    @PostMapping
    public Result<String> addDish(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavors(dishDto);
        return Result.success("新增菜品成功");
    }

    //3.修改菜品功能
    //3.1修改菜品功能(回显)
    @GetMapping("/{id}")
    public Result<DishDto> reshow(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return Result.success(dishDto);
    }

    //3.2修改菜品功能(保存)
    @PutMapping()
    public Result<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavors(dishDto);
        return Result.success("菜品修改成功");
    }

    //4.删除,批量删除菜品
    @DeleteMapping()
    public Result<String> delete(Long[] ids) {
        return dishService.remove(ids);
    }

    //5.修改,批量修改停售起售状态
    @PostMapping("/status/{status}")
    public Result<Object> status(@PathVariable Integer status,Long[] ids){
        return dishService.changeStatus(status,ids);
    }

    //6.查询菜品的分类回显
    @GetMapping("/list")
    public Result<List<Dish>> showDishes(Long categoryId,String name){
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<Dish>();
        lambdaQueryWrapper.eq(!StringUtils.isEmpty(categoryId),Dish::getCategoryId,categoryId);
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name),Dish::getName,name);
        lambdaQueryWrapper.eq(Dish::getStatus,1);
        List<Dish> dishes = dishService.list(lambdaQueryWrapper);
        return Result.success(dishes);
    }
    //查询菜品的同时,显示菜品的口味
    @GetMapping("/list1")
    public Result<List<DishDto>> showDishesWithFlavors(Long categoryId,Integer status){
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<Dish>();
        lambdaQueryWrapper.eq(!StringUtils.isEmpty(categoryId),Dish::getCategoryId,categoryId);
        lambdaQueryWrapper.eq(Dish::getStatus,status);
        List<Dish> dishes = dishService.list(lambdaQueryWrapper);
        List<DishDto> dishDtos = dishes.stream().map((item)->{
            DishDto dishDto = dishService.getByIdWithFlavor(item.getId());
            return dishDto;
        }).collect(Collectors.toList());
        return Result.success(dishDtos);
    }
}
