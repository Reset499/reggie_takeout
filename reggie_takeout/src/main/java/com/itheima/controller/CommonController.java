package com.itheima.controller;

import com.itheima.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        //multipartFile 后的文件名必须与网页中payload中的file中的name属性必须保持一致
        //file是一个临时文件,需要转存到制定位置,不然本次请求完成后临时文件会消失
        log.info(file.toString());

        //获取原始文件名,并用uuid来生成随机的名称,来避免用户所上传的文件名重复导致文件被覆盖
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String Filename = UUID.randomUUID().toString() + suffix;

        //判断basePath是否存在,若不存在则创建一个basePath文件夹
        //创建一个文件对象
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(basePath + Filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Result.success(Filename);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse httpServletResponse) {
        //下载指定文件
        try {
            //输入流,通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //输出流,通过输出流将文件写回浏览器,在浏览器中展示该图片
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();//使服务端可以得到输出流数据
            httpServletResponse.setContentType("image/jpeg");
            int len = 0;
            byte[] bytes = new byte[1024];
            //当一直读入输入流,当输入流不为空,即不为-1时,统计了输入流的bytes长度len
            while ((len = fileInputStream.read(bytes)) != -1) {
                //读取bytes,从0开始,读到len长度
                servletOutputStream.write(bytes, 0, len);
                servletOutputStream.flush();
            }

            //关闭资源
            servletOutputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
