package com.github.listen_to_me;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ListenToMeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ListenToMeApplication.class, args);
    }

}
