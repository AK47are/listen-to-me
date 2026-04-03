package com.github.listen_to_me.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "审核创作者申请")
@RequestMapping("/admin/creator/apply/audit")
@RestController
public class AuditApplyController {
}
