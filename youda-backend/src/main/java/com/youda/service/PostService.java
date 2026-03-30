package com.youda.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youda.dto.CommentCreateDTO;
import com.youda.dto.PostCreateDTO;
import com.youda.entity.Post;
import com.youda.vo.CommentVO;
import com.youda.vo.PostDetailVO;
import com.youda.vo.PostListVO;

import java.util.Map;

public interface PostService extends IService<Post> {

    /**
     * 获取帖子列表
     */
    IPage<PostListVO> getPostList(Integer current, Integer size, Long categoryId, String keyword, String sortBy);

    /**
     * 获取帖子详情
     */
    PostDetailVO getPostDetail(Long postId);

    /**
     * 发布帖子
     */
    Long createPost(PostCreateDTO dto);

    /**
     * 删除帖子
     */
    void deletePost(Long postId);

    /**
     * 点赞/取消点赞
     */
    Map<String, Object> toggleLike(Long postId);

    /**
     * 收藏/取消收藏
     */
    Map<String, Object> toggleCollect(Long postId);

    /**
     * 获取评论列表
     */
    IPage<CommentVO> getComments(Long postId, Integer current, Integer size);

    /**
     * 发表评论
     */
    Long createComment(Long postId, CommentCreateDTO dto);

    /**
     * 删除评论
     */
    void deleteComment(Long commentId);
}
