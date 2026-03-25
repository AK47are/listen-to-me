package com.github.listen_to_me.common.util;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;

import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {

        Dict dict = YamlUtil.loadByPath("application.yaml");
        String url = dict.getByPath("spring.datasource.url");
        String username = dict.getByPath("spring.datasource.username");
        String password = dict.getByPath("spring.datasource.password");

        FastAutoGenerator.create(url, username, password)
                .globalConfig(
                        builder -> {
                            builder.enableSpringdoc().outputDir(System.getProperty("user.dir") + "/src/main/java");
                        })
                .packageConfig(builder -> {
                    builder.parent("com.github.listen_to_me")
                            .entity("domain.entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl");

                    builder.pathInfo(Collections.singletonMap(
                            OutputFile.xml,
                            System.getProperty("user.dir") + "/src/main/resources/mapper"));
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
                            "sys_user_role");
                    builder.entityBuilder().enableLombok();
                    builder.controllerBuilder().enableRestStyle();
                    builder.entityBuilder().enableFileOverride();
                })

                .templateEngine(new VelocityTemplateEngine())
                .execute();
    }
}
