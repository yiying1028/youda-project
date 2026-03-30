package com.youda.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youda.common.BusinessException;
import com.youda.dto.PasswordUpdateDTO;
import com.youda.dto.UserLoginDTO;
import com.youda.dto.UserRegisterDTO;
import com.youda.dto.UserUpdateDTO;
import com.youda.entity.Post;
import com.youda.entity.PostCollect;
import com.youda.entity.User;
import com.youda.mapper.PostCollectMapper;
import com.youda.mapper.PostMapper;
import com.youda.mapper.UserMapper;
import com.youda.service.UserService;
import com.youda.utils.JwtUtils;
import com.youda.utils.UserContext;
import com.youda.vo.LoginVO;
import com.youda.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostCollectMapper postCollectMapper;

    @Override
    public Long register(UserRegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        Long count = this.lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setNickname(dto.getUsername());
        user.setRole(0);
        user.setStatus(1);
        user.setPoints(0);

        this.save(user);
        return user.getId();
    }

    @Override
    public LoginVO login(UserLoginDTO dto) {
        User user = this.lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();

        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(jwtUtils.generateToken(user.getId()));
        loginVO.setUserInfo(buildUserVO(user));
        return loginVO;
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContext.getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return buildUserVO(user);
    }

    @Override
    public void updateUserInfo(UserUpdateDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        User user = new User();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        user.setAvatar(dto.getAvatar());
        user.setPhone(dto.getPhone());
        user.setBio(dto.getBio());
        this.updateById(user);
    }

    @Override
    public void updatePassword(PasswordUpdateDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        User user = this.getById(userId);
        if (user == null || !BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        this.updateById(updateUser);
    }

    @Override
    public IPage<Map<String, Object>> getMyPosts(Integer current, Integer size) {
        Long userId = UserContext.getCurrentUserId();
        Page<Post> page = new Page<>(current, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .orderByDesc(Post::getCreateTime);
        IPage<Post> postPage = postMapper.selectPage(page, wrapper);

        return postPage.convert(post -> {
            Map<String, Object> map = new HashMap<>();
            map.put("postId", post.getId());
            map.put("title", post.getTitle());
            map.put("viewCount", post.getViewCount());
            map.put("likeCount", post.getLikeCount());
            map.put("commentCount", post.getCommentCount());
            map.put("createTime", post.getCreateTime());
            return map;
        });
    }

    @Override
    public IPage<Map<String, Object>> getMyFavorites(Integer current, Integer size) {
        Long userId = UserContext.getCurrentUserId();
        Page<PostCollect> page = new Page<>(current, size);
        LambdaQueryWrapper<PostCollect> wrapper = new LambdaQueryWrapper<PostCollect>()
                .eq(PostCollect::getUserId, userId)
                .orderByDesc(PostCollect::getCreateTime);
        IPage<PostCollect> collectPage = postCollectMapper.selectPage(page, wrapper);

        return collectPage.convert(collect -> {
            Map<String, Object> map = new HashMap<>();
            Post post = postMapper.selectById(collect.getPostId());
            if (post != null) {
                map.put("postId", post.getId());
                map.put("title", post.getTitle());
                map.put("viewCount", post.getViewCount());
                map.put("likeCount", post.getLikeCount());
                map.put("collectTime", collect.getCreateTime());
                map.put("createTime", post.getCreateTime());
            }
            return map;
        });
    }

    private UserVO buildUserVO(User user) {
        UserVO userVO = new UserVO();
        userVO.setUserId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setPhone(user.getPhone());
        userVO.setBio(user.getBio());
        userVO.setPoints(user.getPoints());
        userVO.setRole(user.getRole());
        userVO.setCreateTime(user.getCreateTime());
        return userVO;
    }
}
