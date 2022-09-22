package com.itheima.service.impl;

import com.alibaba.druid.sql.visitor.functions.Length;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.Result;
import com.itheima.domain.Dish;
import com.itheima.domain.DishFlavor;
import com.itheima.domain.SetmealDish;
import com.itheima.dto.DishDto;
import com.itheima.mapper.DishMapper;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import com.itheima.service.SetmealDishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setMealDishService;

    @Override
    @Transactional
    //存入前端页面中发来的json数据,包装为dto形式,同时存入两个表,dish表和dishflavor表
    public void saveWithFlavors(DishDto dishDto) {
        //保存基本信息到dish表中
        this.save(dishDto);

        //dishFlavor表中必须有dishId属性,即菜品id,获取菜品id然后将菜品id插入到每个dishFlavor对象中(遍历dishFlavors)
        Long dishId = dishDto.getId();
        List<DishFlavor> dishFlavors = dishDto.getFlavors();
        dishFlavors = dishFlavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(dishFlavors);
    }

    //得到id后,根据id查询对应的dish表以及根据id对应dishflavor中的dishid来查询出flavors最终封装成一个dishDto对象
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();
        //获取基础的dish信息
        Dish dish = this.getById(id);
        //根据dishid查出flavors
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<DishFlavor>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper);
        //将基本信息和flavors分别附给dishdto
        BeanUtils.copyProperties(dish, dishDto);
        dishDto.setFlavors(dishFlavors);
        return dishDto;
    }

    @Override
    @Transactional
    //得到前端传来的json数据后,封装为dto然后将dto数据更新到数据库中,注意口味必须先删除后再重新添加
    public void updateWithFlavors(DishDto dishDto) {
        //修改基本信息到dish表中
        this.updateById(dishDto);
        //将原先的口味信息先删除掉
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());

        dishFlavorService.remove(lambdaQueryWrapper);

        //添加口味到DishFlavor表中
        Long dishId = dishDto.getId();
        List<DishFlavor> dishFlavors = dishDto.getFlavors();
        dishFlavors = dishFlavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(dishFlavors);
    }

    @Override
    //根据前端传来的id删除对应的dish表和dishflavor表,若有套餐绑定了该菜品,则不可删除
    public Result<String> remove(Long[] ids) {
        //循环查询该ids数组中是否有菜品的id关联了套餐
        int count = 0;
        for (int i = 0; i < ids.length; i++) {
            LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<SetmealDish>();
            lambdaQueryWrapper.eq(SetmealDish::getDishId, ids[i]);
            count += setMealDishService.count(lambdaQueryWrapper);
        }
        //若关联了则无法删除,返回删除失败信息
        if (count > 0) {
            return Result.error("有套餐中关联了该菜品无法删除,请先修改套餐再删除该菜品");
        }
        //若无关联则可放心删除,根据id删除dish表以及dishFlavor表;
        for (int i = 0; i < ids.length; i++) {
            this.removeById(ids[i]);
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper1 = new LambdaQueryWrapper<DishFlavor>();
            lambdaQueryWrapper1.eq(DishFlavor::getDishId, ids[i]);
            dishFlavorService.remove(lambdaQueryWrapper1);
        }
        return Result.success("删除成功");
    }

    @Override
    //根据前端传来的状态以及id对应的修改数据库,若有套餐绑定了该菜品,则不可改变其停售起售状态
    public Result<Object> changeStatus(int status, Long[] ids) {
        //判断是否有套餐绑定了该菜品
        int count = 0;
        for (int i = 0; i < ids.length; i++) {
            LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<SetmealDish>();
            lambdaQueryWrapper.eq(SetmealDish::getDishId, ids[i]);
            count += setMealDishService.count(lambdaQueryWrapper);
        }
        if (count > 0) {
            return Result.error("该菜品已被其他套餐绑定,不可改变其状态");
        }
        for (int i = 0; i < ids.length; i++) {
            Dish dish = new Dish();
            dish.setId(ids[i]);
            dish.setStatus(status);
            dish.setPrice(this.getById(ids[i]).getPrice());
            this.updateById(dish);
        }
        return Result.success("修改状态成功");
    }

}
