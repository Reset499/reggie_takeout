package com.itheima.dto;

import com.itheima.domain.Setmeal;
import com.itheima.domain.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private String categoryName;

    private List<SetmealDish> setmealDishes;

}
