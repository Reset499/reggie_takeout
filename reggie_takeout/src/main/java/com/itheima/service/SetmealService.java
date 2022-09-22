package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.domain.Setmeal;
import com.itheima.dto.DishDto;
import com.itheima.dto.SetmealDto;

public interface SetmealService extends IService<Setmeal> {

    public void saveWithSetmealDish(SetmealDto setmealDto);

    public void deleteWithsetmealDish(Long[] ids);

    public SetmealDto showWithsetmealDish(Long id);

    public void updateWithSetmealDish(SetmealDto setmealDto);
}
