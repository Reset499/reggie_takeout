package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.domain.Category;
import com.itheima.domain.Dish;
import com.itheima.domain.Setmeal;
import com.itheima.mapper.CategoryMapper;
import com.itheima.service.CategoryService;
import com.itheima.service.DishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setMealService;

    @Override
    public void remove(Long id) {
        //查询分类是否关联了菜品和套餐,若关联其中一项,则result.error报错,若两个都没有关联的话,则可以直接删除
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<Dish>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int dishCount = dishService.count(dishLambdaQueryWrapper);
        if (dishCount > 0) {
            throw new CustomException("当前分类下关联了其他菜品,不可删除");
        }
        LambdaQueryWrapper<Setmeal> setMealLambdaQueryWrapper = new LambdaQueryWrapper<Setmeal>();
        setMealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int setMealCount = setMealService.count(setMealLambdaQueryWrapper);
        if (setMealCount > 0) {
            throw new CustomException("当前分类下关联了其他套餐,不可删除");
        }
        super.removeById(id);//调用者调用此方法并removeById,即在controller中调用
    }
}
