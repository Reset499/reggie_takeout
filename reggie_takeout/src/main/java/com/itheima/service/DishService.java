package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.common.Result;
import com.itheima.domain.Dish;
import com.itheima.dto.DishDto;

public interface DishService extends IService<Dish> {
    public void saveWithFlavors(DishDto dishDto);

    public DishDto getByIdWithFlavor(Long id);

    public void updateWithFlavors(DishDto dishDto);

    public Result<String> remove(Long[] ids);

    public Result<Object> changeStatus(int status,Long[] ids);
}
