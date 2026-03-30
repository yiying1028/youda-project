package com.youda.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youda.common.Result;
import com.youda.dto.CommentCreateDTO;
import com.youda.dto.PostCreateDTO;
import com.youda.service.PostService;
import com.youda.vo.CommentVO;
import com.youda.vo.PostDetailVO;
import com.youda.vo.PostListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PostController {

    @Autowired
    private PostService postService;

    /**
     * 获取帖子列表
     */
    @GetMapping("/post/list")
    public Result<IPage<PostListVO>> getPostList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sortBy) {
        IPage<PostListVO> page = postService.getPostList(current, size, categoryId, keyword, sortBy);
        return Result.success(page);
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/post/{postId}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long postId) {
        PostDetailVO vo = postService.getPostDetail(postId);
        return Result.success(vo);
    }

    /**
     * 发布帖子
     */
    @PostMapping("/post")
    public Result<Map<String, Long>> createPost(@Valid @RequestBody PostCreateDTO dto) {
        Long postId = postService.createPost(dto);
        Map<String, Long> data = new HashMap<>();
        data.put("postId", postId);
        return Result.success("发布成功", data);
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/post/{postId}")
    public Result<?> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return Result.success("删除成功", null);
    }

    /**
     * 点赞帖子
     */
    @PostMapping("/post/{postId}/like")
    public Result<Map<String, Object>> likePost(@PathVariable Long postId) {
        Map<String, Object> result = postService.toggleLike(postId);
        return Result.success("操作成功", result);
    }

    /**
     * 收藏帖子
     */
    @PostMapping("/post/{postId}/collect")
    public Result<Map<String, Object>> collectPost(@PathVariable Long postId) {
        Map<String, Object> result = postService.toggleCollect(postId);
        return Result.success("操作成功", result);
    }

    /**
     * 获取评论列表
     */
    @GetMapping("/post/{postId}/comments")
    public Result<IPage<CommentVO>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size) {
        IPage<CommentVO> page = postService.getComments(postId, current, size);
        return Result.success(page);
    }

    /**
     * 发表评论
     */
    @PostMapping("/post/{postId}/comment")
    public Result<Map<String, Long>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateDTO dto) {
        Long commentId = postService.createComment(postId, dto);
        Map<String, Long> data = new HashMap<>();
        data.put("commentId", commentId);
        return Result.success("评论成功", data);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comment/{commentId}")
    public Result<?> deleteComment(@PathVariable Long commentId) {
        postService.deleteComment(commentId);
        return Result.success("删除成功", null);
    }
}
