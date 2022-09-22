package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.domain.DishFlavor;
import com.itheima.domain.Setmeal;
import com.itheima.domain.SetmealDish;
import com.itheima.dto.DishDto;
import com.itheima.dto.SetmealDto;
import com.itheima.mapper.SetmealMapper;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    //将前端传回来的json数据封装为dto格式,然后通过此方法将dto中的数据传入两个数据库
    public void saveWithSetmealDish(SetmealDto setmealDto) {
        this.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes().stream().map((item)->{
            String setmealId = setmealDto.getId().toString();
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    //根据ids删除ids中每个id对应的setmeal表和setmealdish表
    public void deleteWithsetmealDish(Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            this.removeById(ids[i]);
            LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<SetmealDish>();
            lambdaQueryWrapper.eq(SetmealDish::getSetmealId,ids[i]);
            setmealDishService.remove(lambdaQueryWrapper);
        }
    }

    @Override
    //根据id查询setmeal和setmealdish表,并合并封装为dto对象
    public SetmealDto showWithsetmealDish(Long id) {
        //创建dto对象,根据id查询setmeal并赋值给dto
        SetmealDto setmealDto = new SetmealDto();
        Setmeal setmeal = this.getById(id);
        BeanUtils.copyProperties(setmeal,setmealDto);
        //根据id条件查询setmealdish中符合条件的数据,并封装为list
        Long setmealId = setmeal.getId();
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<SetmealDish>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        List<SetmealDish> setmealDishes = setmealDishService.list(lambdaQueryWrapper);
        //将list赋值给dto
        setmealDto.setSetmealDishes(setmealDishes);
        return setmealDto;
    }

    @Override
    @Transactional
    //得到前端传来的json数据后,封装为dto然后将dto数据更新到数据库中,注意口味必须先删除后再重新添加
    public void updateWithSetmealDish(SetmealDto setmealDto) {
        //修改基本套餐信息到Setmeal表中
        this.updateById(setmealDto);
        //将原先的套餐菜品信息先删除掉
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<SetmealDish>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(lambdaQueryWrapper);

        //添加套餐中具体菜品到SetmealDish表中
        String setmealDishId = setmealDto.getId().toString();
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDishId);
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }


}
