package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.Result;
import com.itheima.domain.Category;
import com.itheima.domain.Setmeal;
import com.itheima.dto.SetmealDto;
import com.itheima.service.CategoryService;
import com.itheima.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    //1.分页功能,同时利用dto来根据id查到name最后将categoryname显示在页面上
    public Result<Page> page(int page, int pageSize, String name) {
        Page pageInfo = new Page(page, pageSize);
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<Setmeal>();
        lambdaQueryWrapper.orderByAsc(Setmeal::getPrice);
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name), Setmeal::getName, name);
        setmealService.page(pageInfo, lambdaQueryWrapper);
        //根据categoryid来查找categoryname并在基础上加入name封装为dto
        Page dtoInfo = new Page(page, pageSize);
        BeanUtils.copyProperties(pageInfo, dtoInfo, "records");
        List<Setmeal> setmeals = pageInfo.getRecords();
        List<SetmealDto> setmealDtos = setmeals.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();
            String categoryName = categoryService.getById(categoryId).getName();
            setmealDto.setCategoryName(categoryName);
            return setmealDto;
        }).collect(Collectors.toList());
        dtoInfo.setRecords(setmealDtos);
        return Result.success(dtoInfo);
    }

    //2.保存数据功能
    @PostMapping()
    public Result<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithSetmealDish(setmealDto);
        return Result.success("保存成功");
    }

    //3.删除功能
    @DeleteMapping
    public Result<String> delete(Long[] ids) {
        setmealService.deleteWithsetmealDish(ids);
        return Result.success("删除成功");
    }

    //4.修改回显功能
    @GetMapping("/{id}")
    public Result<SetmealDto> showBack(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.showWithsetmealDish(id);
        return Result.success(setmealDto);
    }

    //5.修改保存功能
    @PutMapping()
    public Result<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateWithSetmealDish(setmealDto);
        return Result.success("修改成功");
    }

    //6.停售起售,批量停售起售功能
    @PostMapping("/status/{status}")
    public Result<String> changeStatus(@PathVariable Integer status, Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            Setmeal setmeal = setmealService.getById(ids[i]);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return Result.success("修改成功");
    }

    //7.前端页面中显示套餐功能
    @GetMapping("/list")
    public Result<List<Setmeal>> showList(Long categoryId, Integer status) {
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<Setmeal>();
        lambdaQueryWrapper.eq(Setmeal::getCategoryId,categoryId);
        lambdaQueryWrapper.eq(Setmeal::getStatus,status);
        lambdaQueryWrapper.orderByAsc(Setmeal::getCreateTime);
        List<Setmeal> setmeals = setmealService.list(lambdaQueryWrapper);
        return Result.success(setmeals);
    }
}
