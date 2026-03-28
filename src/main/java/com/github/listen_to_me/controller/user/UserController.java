package com.github.listen_to_me.controller.user;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.UserProfileUpdateDTO;
import com.github.listen_to_me.domain.dto.FavoriteActionDTO;
import com.github.listen_to_me.domain.dto.FavoriteDeleteDTO;
import com.github.listen_to_me.domain.dto.FolderDTO;
import com.github.listen_to_me.domain.vo.UserVO;
import com.github.listen_to_me.domain.query.FavoriteQuery;
import com.github.listen_to_me.domain.vo.AudioVO;
import com.github.listen_to_me.domain.vo.FolderVO;
import com.github.listen_to_me.service.AudioFolderRelationService;
import com.github.listen_to_me.service.IAudioInfoService;
import com.github.listen_to_me.service.ISysUserService;
import com.github.listen_to_me.service.SysUserFolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
@Slf4j
@Tag(name = "用户管理", description = "包含登录、注册、用户信息管理等接口")
public class UserController {
    private final AudioFolderRelationService audioFolderRelationService;
    private final SysUserFolderService sysUserFolderService;
    private final IAudioInfoService audioInfoService;
    private ISysUserService sysUserService;

    @Operation(summary = "获取当前登录用户资料")
    @GetMapping("/profile")
    public Result<UserVO> getProfile(){
        return Result.success(sysUserService.findProfile());
    }

    @Operation(summary = "修改个人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserProfileUpdateDTO updateDTO){
        sysUserService.modifyProfile(updateDTO);
        return Result.success();
    }
    
    @PostMapping("/audio/action")
    @Operation(summary = "收藏/取消收藏音频")
    public Result<Void> saveAudioAction(@RequestBody FavoriteActionDTO favoriteActionDTO) {
        audioFolderRelationService.saveAudioAction(favoriteActionDTO);
        return Result.success();
    }

    @PostMapping("/favorite/folder")
    @Operation(summary = "创建收藏夹")
    public Result<String> saveFavoriteFolder(@RequestBody FolderDTO folderDTO) {
        sysUserFolderService.createFolder(folderDTO);
        return Result.success("创建收藏夹成功");
    }

    @GetMapping("/favorite/folder/list")
    @Operation(summary = "获取收藏夹列表")
    public Result<List<FolderVO>> getFolderList() {
        return Result.success(sysUserFolderService.getFolderList());
    }

    @GetMapping("/favorite/page")
    @Operation(summary = "获取收藏音频列表")
    public IPage<AudioVO> getFavoritePage(@ParameterObject FavoriteQuery favoriteQuery) {
        //转换改到service层
        return audioInfoService.getFavoriteAudioPage(favoriteQuery);
    }
    @DeleteMapping("/favorite")
    @Operation(summary = "删除收藏音频")
    public Result<String> deleteFavorite(@RequestBody FavoriteDeleteDTO favoriteDeleteDTO) {
        audioFolderRelationService.deleteFavorite(favoriteDeleteDTO);
        return Result.success("删除收藏音频成功");
    }
}