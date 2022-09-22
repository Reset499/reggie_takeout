package com.itheima.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.common.BaseContext;
import com.itheima.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.text.StyledEditorKit;
import java.io.IOException;

//前后端界面应该分别写一个过滤器
@Slf4j
@WebFilter(filterName = "LoginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //路径匹配器,可以识别通配符**
    public static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //强制转换,将servlet转换为httpServlet形式
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        //1.获取本次请求的url
        String requestUrl = httpServletRequest.getRequestURI();
        log.info("拦截到请求:{}", requestUrl);

        //2.定义一个不需要被处理的请求集合
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };

        //3.判断本次请求是否需要处理
        boolean check = check(urls, requestUrl);

        //4.如果不需要处理,则放行
        if (check) {
            log.info("本次请求不需要处理:{}", requestUrl);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        //5.需要处理,要判断用户是否已经登录,若已登录则放行,前后端要分开
        //电脑后台端
        if (httpServletRequest.getSession().getAttribute("employee") != null) {
            Long empId = (Long) httpServletRequest.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            log.info("用户已登录,用户id为:{}", empId);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        //移动前台端
        if (httpServletRequest.getSession().getAttribute("user") != null) {
            Long userId = (Long) httpServletRequest.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            log.info("前端用户已经登录,用户id为:{}",userId);
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }

        //6.若用户未登录,则通过result对象输出流的方式来向客户端响应(request.js中也设置有拦截器)
//        if (res.data.code === 0 && res.data.msg === 'NOTLOGIN') {// 返回登录页面
//            console.log('---/backend/page/login/login.html---')
//            localStorage.removeItem('userInfo')
//            window.top.location.href = '/page/login/login.html'
//        } else {
//            return res.data
//        }
        log.info("用户未登录");
        httpServletResponse.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
        return;
    }

    public boolean check(String[] urls, String requestUrl) {
        for (String url : urls) {
            boolean match = pathMatcher.match(url, requestUrl);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
