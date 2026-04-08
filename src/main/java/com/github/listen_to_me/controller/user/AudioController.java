package com.github.listen_to_me.controller.user;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.domain.query.AudioSearchQuery;
import com.github.listen_to_me.domain.query.PageQuery;

import com.github.listen_to_me.domain.vo.FolderVO;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.github.listen_to_me.domain.vo.AudioDetailVO;
import org.springframework.web.bind.annotation.*;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.FavoriteActionDTO;
import com.github.listen_to_me.domain.dto.LikeActionDTO;
import com.github.listen_to_me.domain.vo.AudioVO;
import com.github.listen_to_me.service.IAudioFolderRelationService;
import com.github.listen_to_me.service.IAudioInfoService;
import com.github.listen_to_me.service.IAudioLikeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@RequestMapping("/user/audio")
@RestController("userAudioController")
@Tag(name = "音频稿件管理", description = "包含收藏、点赞音频等接口")
public class AudioController {

    private final IAudioLikeService audioLikeService;
    private final IAudioInfoService audioInfoService;
    private final IAudioFolderRelationService audioFolderRelationService;

    @PostMapping("/action")
    @Operation(summary = "收藏/取消收藏音频")
    public Result<Void> saveAudioAction(@RequestBody FavoriteActionDTO favoriteActionDTO) {
        audioFolderRelationService.saveAudioAction(favoriteActionDTO);
        return Result.success();
    }

    @PostMapping("/like")
    @Operation(summary = "喜欢/取消喜欢音频")
    public Result<Void> saveAudioLike(@RequestBody LikeActionDTO likeActionDTO) {
        audioLikeService.modifyAudioLike(likeActionDTO);
        return Result.success();
    }

    @GetMapping("/hot")
    @Operation(summary = "热榜")
    public Result<List<AudioVO>> getHotList() {
        return Result.success(audioInfoService.getHotList());
    }

    @GetMapping("/search")
    @Operation(summary = "音频搜索")
    public Result<IPage<AudioVO>> searchAudio(@Valid @ParameterObject AudioSearchQuery audioSearchQuery) {
        return Result.success(audioInfoService.searchAudio(audioSearchQuery));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取音频详情")
    public Result<AudioDetailVO> getAudioDetail(@PathVariable Long id) {
        return Result.success(audioInfoService.getAudioDetail(id));
    }

    @GetMapping("/creator/{creatorId}/page")
    @Operation(summary = "分页查询创作者作品")
    public Result<IPage<AudioVO>> getCreatorAudioPage(@PathVariable Long creatorId,
            @ParameterObject PageQuery pageQuery) {
        return Result.success(audioInfoService.getCreatorAudioPage(creatorId, pageQuery));
    }

    @GetMapping("/recommend")
    @Operation(summary = "推荐音频")
    public Result<IPage<AudioVO>> getRecommendList(@ParameterObject PageQuery pageQuery) {
        return Result.success(audioInfoService.getRecommendList(pageQuery));
    }


    @GetMapping("/{audioId}/folders")
    @Operation(summary = "获取音频收藏文件夹列表")
    public Result<List<FolderVO>> getAudioFolders(@PathVariable Long audioId) {
        return Result.success(audioFolderRelationService.getAudioFolders(audioId));
    }
}
