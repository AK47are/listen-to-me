package com.github.listen_to_me.controller.common;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "AI任务")
@RequiredArgsConstructor
@RequestMapping("/common/ai")
@RestController("commonAiController")
public class AiController {
}
