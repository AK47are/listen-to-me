package com.github.listen_to_me.controller.creator;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "AI增强")
@RequiredArgsConstructor
@RequestMapping("/creator/ai")
@RestController("creatorAiController")
public class AiController {
}
