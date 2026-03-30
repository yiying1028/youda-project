package com.youda;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.youda.mapper")
public class YoudaApplication {

    public static void main(String[] args) {
        SpringApplication.run(YoudaApplication.class, args);
        System.out.println("==========================================");
        System.out.println("  优答·中小学答疑网 启动成功!");
        System.out.println("  访问地址: http://localhost:8080");
        System.out.println("==========================================");
    }
}
