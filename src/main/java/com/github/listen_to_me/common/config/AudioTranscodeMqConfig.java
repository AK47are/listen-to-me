package com.github.listen_to_me.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 音频转码消息队列配置
 */
@Configuration
public class AudioTranscodeMqConfig {

    // 1. 定义交换机名称
    public static final String EXCHANGE_NAME = "audio.transcode.exchange";
    // 2. 定义队列名称
    public static final String QUEUE_NAME = "audio.transcode.queue";
    // 3. 定义路由键
    public static final String ROUTING_KEY = "audio.transcode.rk";

    /**
     * 声明交换机
     */
    @Bean
    public DirectExchange audioTranscodeExchange() {
        // durable(true) 持久化，服务器重启不丢失
        return ExchangeBuilder.directExchange(EXCHANGE_NAME).durable(true).build();
    }

    /**
     * 声明队列
     */
    @Bean
    public Queue audioTranscodeQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding bindingAudioTranscode() {
        return BindingBuilder.bind(audioTranscodeQueue())
                .to(audioTranscodeExchange())
                .with(ROUTING_KEY);
    }
}