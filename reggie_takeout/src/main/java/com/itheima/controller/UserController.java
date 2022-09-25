package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.Result;
import com.itheima.domain.User;
import com.itheima.service.UserService;
import com.itheima.utils.SMSUtils;
import com.itheima.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    //1.发送验证码短信
    @PostMapping("/sendMsg")
    public Result<String> sendMessage(@RequestBody User user, HttpSession session) {
        //获取手机号
        String phoneNumber = user.getPhone();
        //生成验证码,并发送短信
        if (!StringUtils.isEmpty(phoneNumber)) {
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            //为了减少次数消耗,这里将signName故意填写错误,正确的signName为 阿里云短信测试
            SMSUtils.sendMessage("阿里云短信测试1", "SMS_154950909", phoneNumber, code);
            log.info("code={}", code);
//            将验证码保存到session,key为手机号
//            session.setAttribute(phoneNumber, code);
            //将生成的code验证码存入Redis中,并设置有效时间是2分钟
            ValueOperations valueOperations = redisTemplate.opsForValue();
            valueOperations.set(phoneNumber,code,2, TimeUnit.MINUTES);
            return Result.success("短信验证码发送成功");
        }
        return Result.error("短信发送失败");
    }

    //2.校验账号和验证码登录
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map map, HttpSession session) {
        //获取前端传来的map中的phone和code
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
//        获取session中的验证码
//        Object sessionCode = session.getAttribute(phone);
        //从Redis中获取验证码
        Object redisCode = redisTemplate.opsForValue().get(phone);
        //判断验证码是否一致,若一致,则登陆成功,查询该用户是否在用户表中,若不在,则添加用户到用户表中,若存在,则直接返回该用户信息
        //存不存在都直接返回user对象
        if (redisCode != null && redisCode.equals(code)) {
            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<User>();
            lambdaQueryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(lambdaQueryWrapper);
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            //如果用户登录成功,则删除Redis中的验证码数据
            redisTemplate.delete(phone);
            return Result.success(user);
        }
        return Result.error("登陆失败");
    }

    //3.账号登出
    @PostMapping("/logout")
    public Result<String> logout(HttpSession session){
        session.removeAttribute("user");
        return Result.success("退出登陆成功");
    }
}
