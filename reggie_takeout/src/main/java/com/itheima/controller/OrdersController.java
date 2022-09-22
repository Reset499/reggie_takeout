package com.itheima.controller;

import com.itheima.common.Result;
import com.itheima.domain.Orders;
import com.itheima.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    //提交信息,汇总为订单信息,最终存入数据库中,用户信息和订单行信息不需要提交,因为随时可以通过用户id来查到
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders){
        ordersService.submit(orders);
        return Result.success("下单成功");
    }
}
