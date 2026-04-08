package com.github.listen_to_me.controller.user;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.FolderDTO;
import com.github.listen_to_me.domain.query.FavoriteQuery;
import com.github.listen_to_me.domain.vo.AudioVO;
import com.github.listen_to_me.domain.vo.FolderVO;
import com.github.listen_to_me.service.IAudioInfoService;
import com.github.listen_to_me.service.ISysUserFolderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/user/favorite")
@Tag(name = "收藏夹管理", description = "包含增删查收藏夹等接口")
public class FavoriteController {

    private final ISysUserFolderService sysUserFolderService;
    private final IAudioInfoService audioInfoService;

    @PostMapping("/folder")
    @Operation(summary = "创建收藏夹")
    public Result<Void> saveFavoriteFolder(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody FolderDTO folderDTO) {
        sysUserFolderService.createFolder(userId, folderDTO);
        return Result.success();
    }

    @GetMapping("/folder/list")
    @Operation(summary = "获取收藏夹列表")
    public Result<List<FolderVO>> getFolderList() {
        return Result.success(sysUserFolderService.getFolderList());
    }

    @GetMapping("/page")
    @Operation(summary = "获取收藏音频列表")
    public Result<IPage<AudioVO>> getFavoritePage(@ParameterObject FavoriteQuery favoriteQuery) {
        return Result.success(audioInfoService.getFavoriteAudioPage(favoriteQuery));
    }

    @DeleteMapping("/folder/{folderId}")
    @Operation(summary = "删除收藏夹")
    public Result<String> deleteFavoriteFolder(@PathVariable Long folderId) {
        sysUserFolderService.deleteFolder(folderId);
        return Result.success("删除收藏夹成功");
    }

}
