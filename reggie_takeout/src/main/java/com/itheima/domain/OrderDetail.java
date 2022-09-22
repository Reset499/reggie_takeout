package com.itheima.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetail {

    private Long id;

    private String name;

    private String image;

    private Long orderId;

    private Long dishId;

    private Long setmealId;

    private String dishFlavor;

    private Long number;

    private BigDecimal amount;
}
