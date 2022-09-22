package com.itheima.common;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Result<Type> {
    private Integer code;//1表示成功,0表示失败
    private String message;//错误信息
    private Type data;//数据 实体类
    private Map map = new HashMap();//动态数据

    //成功时返回一个成功Result对象,对象中包含了成功后要返回的数据,以及返回成功后成功代码1
    public static <Type> Result<Type> success(Type object){
        Result<Type> result = new Result<Type>();
        result.data = object;
        result.code = 1;
        return result;
    }

    //失败时返回一个失败Result对象,对象中包含了失败后要返回的失败信息,以及返回失败后的代码0
    public static <Type> Result<Type> error(String message){
        Result<Type> result = new Result<Type>();
        result.message = message;
        result.code = 0;
        return result;
    }

    //添加动态数据的方法
    public Result<Type> add(String key, Object value){
        this.map.put(key,value);
        return this;
    }
}
