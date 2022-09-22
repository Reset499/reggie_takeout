package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.itheima.common.Result;
import com.itheima.domain.AddressBook;
import com.itheima.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    //1.展示地址簿
    @GetMapping("/list")
    public Result<List<AddressBook>> showAddressBook(HttpSession session) {
        String UserId = session.getAttribute("user").toString();
        LambdaQueryWrapper<AddressBook> lambdaQueryWrapper = new LambdaQueryWrapper<AddressBook>();
        lambdaQueryWrapper.eq(AddressBook::getUserId, UserId);
        List<AddressBook> addressBooks = addressBookService.list(lambdaQueryWrapper);
        return Result.success(addressBooks);
    }

    //2.添加地址簿
    @PostMapping
    public Result<String> addAddressBook(@RequestBody AddressBook addressBook, HttpSession session) {
        Long userId = (Long) session.getAttribute("user");
        addressBook.setUserId(userId);
        addressBookService.save(addressBook);
        return Result.success("添加成功");
    }

    //3.根据id进行修改地址簿的回显
    @GetMapping("/{id}")
    public Result<AddressBook> showAddressBook(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        return Result.success(addressBook);
    }

    //4.保存修改后的内容
    @PutMapping
    public Result<String> update(@RequestBody AddressBook addressBook) {
        addressBookService.updateById(addressBook);
        return Result.success("更新成功");
    }

    //5.删除地址
    @DeleteMapping
    public Result<String> delete(Long ids) {
        addressBookService.removeById(ids);
        return Result.success("删除成功");
    }

    //6.设置默认地址值
    @PutMapping("/default")
    public Result<String> setDefault(@RequestBody AddressBook addressBook) {
        //先将该userid下的所有地址值的default设置为0,再将传入的设为1
        Long userId = addressBook.getUserId();
        LambdaUpdateWrapper<AddressBook> lambdaUpdateWrapper = new LambdaUpdateWrapper<AddressBook>();
        lambdaUpdateWrapper.eq(AddressBook::getUserId, userId);
        lambdaUpdateWrapper.set(AddressBook::getIsDefault, 0);
        addressBookService.update(lambdaUpdateWrapper);

        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return Result.success("设置成功");
    }

    //7.订单结算页面返回默认地址值
    @GetMapping("default")
    public Result<AddressBook> returnAddress(HttpSession session){
        Long userId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<AddressBook> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AddressBook::getUserId,userId);
        lambdaQueryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook addressBook = addressBookService.getOne(lambdaQueryWrapper);
        if(addressBook != null){
            return Result.success(addressBook);
        }
        return Result.error("无默认地址,请添加地址");
    }
}