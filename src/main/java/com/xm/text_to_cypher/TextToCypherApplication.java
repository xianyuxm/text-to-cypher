package com.xm.text_to_cypher;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = {"com.xm.text_to_cypher.mapper"})
public class TextToCypherApplication {

    public static void main(String[] args) {
        SpringApplication.run(TextToCypherApplication.class, args);
    }

}
