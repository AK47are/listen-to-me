package com.github.listen_to_me.controller.creator;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 创作者端 - 时间槽管理
 */
@Slf4j
@RestController
@RequestMapping("/creator")
@RequiredArgsConstructor
@Tag(name = "时间槽管理")
public class CreatorSlotController {
}
