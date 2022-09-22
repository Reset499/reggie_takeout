package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.Result;
import com.itheima.domain.Employee;
import com.itheima.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.rmi.CORBA.Util;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    //1.登录功能
    @PostMapping("/login")
    //@RequestBody将网页中发送来的数据接收封装为Employee对象
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1.将password进行加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据用户名查询数据库
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<Employee>();
        lambdaQueryWrapper.eq(Employee::getUsername, employee.getUsername());//username为唯一
        Employee employee1 = employeeService.getOne(lambdaQueryWrapper);
        //3.判断其查询的用户名是否存在
        if (employee1 == null) {
            return Result.error("该用户不存在");
        }
        //4.判断查询出的employee1对象密码是否与网页上用户封装为employee输入的密码匹配
        if (!employee1.getPassword().equals(password)) {
            return Result.error("密码错误,请重试");
        }
        //5.查看员工状态,看其是否被禁用
        if (employee1.getStatus() == 0) {
            return Result.error("该用户已被禁用");
        }
        //6.查询成功,将员工id存入session并返回登录成功
        HttpSession session = request.getSession();
        session.setAttribute("employee", employee1.getId());
        return Result.success(employee1);
    }

    //2.登出功能
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        //清理当前在session中的员工id
        HttpSession session = request.getSession();
        session.removeAttribute("employee");
        return Result.success("退出成功");
    }

    //3.新增员工功能
    @PostMapping
    public Result<String> save(HttpServletRequest httpServletRequest, @RequestBody Employee employee) {
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        Long empId = (Long) httpServletRequest.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employeeService.save(employee);
        return Result.success("新增员工成功");
    }

    //4.分页查询功能
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {//这里的page,pageSize,name都是请求路径上的属性,mp自动装配
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);
            //构造分页构造器
        Page pageInfo = new Page(page, pageSize);//page表示你要查询第几页,而pageSize是设置每页展示的数据数量
            //构造条件构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<Employee>();
                //添加过滤条件
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name), Employee::getName, name);
                //添加排序条件
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,lambdaQueryWrapper);
        return Result.success(pageInfo);
    }

    //5.修改员工状态和信息功能
    @PutMapping
    //这里传入的两个信息只有id和status 因此修改也只用修改为这两个,但因为id是一致的,所以只需要修改status
    public Result<String> updateStatus(HttpServletRequest httpServletRequest, @RequestBody Employee employee){
//        Long empId = (Long) httpServletRequest.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return Result.success("修改状态成功");
    }

    //6.员工信息在页面回显功能
    @GetMapping("/{id}")
    public Result<Employee> upadateInformation(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        if (employee!=null) {
            return Result.success(employee);
        }else{
            return Result.error("数据初始化失败");
        }
    }
}
