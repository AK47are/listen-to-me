package com.github.listen_to_me;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.github.listen_to_me.mapper")
public class ListenToMeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ListenToMeApplication.class, args);
	}

}
