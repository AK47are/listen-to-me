package com.github.listen_to_me.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiTaskMqConfig {

    public static final String EXCHANGE_NAME = "ai.task.exchange";
    public static final String TRANSCRIPTION_QUEUE = "ai.transcription.queue";
    public static final String TRANSCRIPTION_ROUTING_KEY = "ai.transcription";
    public static final String SUMMARIZATION_QUEUE = "ai.summarization.queue";
    public static final String SUMMARIZATION_ROUTING_KEY = "ai.summarization";

    @Bean
    public DirectExchange aiTaskExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue transcriptionQueue() {
        return new Queue(TRANSCRIPTION_QUEUE, true);
    }

    @Bean
    public Binding transcriptionBinding() {
        return BindingBuilder.bind(transcriptionQueue())
                .to(aiTaskExchange())
                .with(TRANSCRIPTION_ROUTING_KEY);
    }

    @Bean
    public Queue summarizationQueue() {
        return new Queue(SUMMARIZATION_QUEUE, true);
    }

    @Bean
    public Binding summarizationBinding() {
        return BindingBuilder.bind(summarizationQueue())
                .to(aiTaskExchange())
                .with(SUMMARIZATION_ROUTING_KEY);
    }
}
