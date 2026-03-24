package com.github.listen_to_me.common.util;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/listen-to-me?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "root";
        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> {
                    builder.author("kun")
                            .fileOverride()
                            .outputDir(System.getProperty("user.dir") + "/src/main/java");
                })
                .packageConfig(builder -> {
                    builder.parent("com.github.listen_to_me")
                            .entity("domain.entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller");

                    builder.pathInfo(Collections.singletonMap(
                            OutputFile.xml,
                            System.getProperty("user.dir") + "/src/main/resources/mapper"
                    ));
                })

                .strategyConfig(builder -> {
                    builder.addInclude(
                            "audio_info",
                            "audio_tag_relation",
                            "audio_transcript",
                            "consult_slot",
                            "order_info",
                            "play_history",
                            "sys_permission",
                            "sys_role",
                            "sys_role_permission",
                            "sys_tag",
                            "sys_user",
                            "sys_user_role"
                    );
                    builder.entityBuilder().enableLombok();
                    builder.controllerBuilder().enableRestStyle();
                })

                .templateEngine(new VelocityTemplateEngine())
                .execute();
    }
}