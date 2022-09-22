package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.common.Result;
import com.itheima.domain.ShoppingCart;
import com.itheima.service.ShoppingCartService;
import javafx.scene.shape.ShapeBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    //1.显该用户目前购物车中所有的已添加的菜品(初始化)
    @GetMapping("/list")
    public Result<List<ShoppingCart>> showShoppingCart(HttpSession session) {
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<ShoppingCart>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, session.getAttribute("user"));
        lambdaQueryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(lambdaQueryWrapper);
        return Result.success(shoppingCarts);
    }

    //2.添加菜品到购物车里
    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart, HttpSession session) {
        //获取当前session中的用户id,从而可以根据此id来查询盖该id对应所有该用户的shoppingCart对象
        Long userId = (Long) session.getAttribute("user");
        //查询当前要添加的菜品是否已经在购物车中
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<ShoppingCart>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);
        Long dishId = shoppingCart.getDishId();
        if (dishId != null) {
            //如果添加的是菜品,则查菜品id
            lambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //如果添加的是套餐id,则查套餐id
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(lambdaQueryWrapper);
        //若已经存在,则数量直接加一即可
        if (shoppingCart1 != null) {
            Integer number = shoppingCart1.getNumber();
            shoppingCart1.setNumber(number + 1);
            shoppingCartService.updateById(shoppingCart1);
            return Result.success(shoppingCart1);
        } else {
            //若不存在,则创建一个写有各种信息的shoppingCart对象,并存入数据库
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            return Result.success(shoppingCart);
        }

    }

    //3.清空购物车
    @DeleteMapping("/clean")
    public Result<String> clean(HttpSession session) {
        Long userId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<ShoppingCart>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);
        shoppingCartService.remove(lambdaQueryWrapper);
        return Result.success("清空成功");
    }

    //4.减少购物车数量
    @PostMapping("/sub")
    public Result<String> reduce(@RequestBody ShoppingCart shoppingCart, HttpSession session) {
        //根据用户和dishid or setmealid 来查询出一个shoppingcart对象
        Long userId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<ShoppingCart>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);
        //若传来的是菜品
        if (shoppingCart.getDishId() != null) {
            lambdaQueryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            //若传来的是套餐
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        shoppingCart = shoppingCartService.getOne(lambdaQueryWrapper);
        int number = shoppingCart.getNumber();
        if (number <= 1) {
            shoppingCartService.removeById(shoppingCart.getId());
        }else {
            shoppingCart.setNumber(number-1);
            shoppingCartService.updateById(shoppingCart);
        }
        return Result.success("减少成功");
    }
}
