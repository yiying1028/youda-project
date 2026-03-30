package com.youda.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youda.dto.PasswordUpdateDTO;
import com.youda.dto.UserLoginDTO;
import com.youda.dto.UserRegisterDTO;
import com.youda.dto.UserUpdateDTO;
import com.youda.entity.User;
import com.youda.vo.LoginVO;
import com.youda.vo.PostListVO;
import com.youda.vo.UserVO;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /** 用户注册 */
    Long register(UserRegisterDTO dto);

    /** 用户登录，返回token和用户信息 */
    LoginVO login(UserLoginDTO dto);

    /** 获取当前登录用户信息 */
    UserVO getCurrentUser();

    /** 修改昵称/头像等基本信息 */
    void updateUserInfo(UserUpdateDTO dto);

    /** 修改密码 */
    void updatePassword(PasswordUpdateDTO dto);

    /**
     * 获取我发布的帖子列表
     * @param current 页码
     * @param size    每页条数
     */
    IPage<Map<String, Object>> getMyPosts(Integer current, Integer size);

    /**
     * 获取我收藏的帖子列表
     * @param current 页码
     * @param size    每页条数
     */
    IPage<Map<String, Object>> getMyFavorites(Integer current, Integer size);
}
