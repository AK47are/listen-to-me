package com.github.listen_to_me.controller.creator;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/creator/consult")
@Tag(name = "预约订单管理")
@RestController("creatorConsultController")
public class ConsultController {
}
