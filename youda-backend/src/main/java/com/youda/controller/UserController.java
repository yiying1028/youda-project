package com.youda.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youda.common.Result;
import com.youda.dto.PasswordUpdateDTO;
import com.youda.dto.UserLoginDTO;
import com.youda.dto.UserRegisterDTO;
import com.youda.dto.UserUpdateDTO;
import com.youda.service.UserService;
import com.youda.utils.FileUtils;
import com.youda.vo.LoginVO;
import com.youda.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户Controller
 * 处理注册、登录、个人信息修改、我的帖子/收藏等
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileUtils fileUtils;

    // ============ 注册登录 ============

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Map<String, Long>> register(@Valid @RequestBody UserRegisterDTO dto) {
        Long userId = userService.register(dto);
        Map<String, Long> data = new HashMap<>();
        data.put("userId", userId);
        return Result.success("注册成功", data);
    }

    /**
     * 用户登录，返回token和用户基本信息
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        LoginVO loginVO = userService.login(dto);
        return Result.success("登录成功", loginVO);
    }

    // ============ 个人信息 ============

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        UserVO userVO = userService.getCurrentUser();
        return Result.success(userVO);
    }

    /**
     * 更新用户昵称/头像
     */
    @PutMapping("/info")
    public Result<?> updateUserInfo(@RequestBody UserUpdateDTO dto) {
        userService.updateUserInfo(dto);
        return Result.success("修改成功", null);
    }

    /**
     * 修改密码（需要验证原密码）
     */
    @PutMapping("/password")
    public Result<?> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        userService.updatePassword(dto);
        return Result.success("修改成功", null);
    }

    /**
     * 上传头像
     * @return 头像访问URL
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        String url = fileUtils.uploadFile(file, "avatar");
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success("上传成功", data);
    }

    // ============ 个人中心 ============

    /**
     * 获取我发布的帖子列表
     */
    @GetMapping("/posts")
    public Result<IPage<Map<String, Object>>> getMyPosts(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<Map<String, Object>> page = userService.getMyPosts(current, size);
        return Result.success(page);
    }

    /**
     * 获取我收藏的帖子列表
     */
    @GetMapping("/favorites")
    public Result<IPage<Map<String, Object>>> getMyFavorites(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<Map<String, Object>> page = userService.getMyFavorites(current, size);
        return Result.success(page);
    }
}
