package com.github.listen_to_me.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "用户管理")
@RequiredArgsConstructor
@RequestMapping("/admin/user")
@RestController("adminUserController")
public class UserController {
}
