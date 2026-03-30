package com.youda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youda.common.BusinessException;
import com.youda.dto.CommentCreateDTO;
import com.youda.dto.PostCreateDTO;
import com.youda.entity.Category;
import com.youda.entity.Comment;
import com.youda.entity.Post;
import com.youda.entity.PostCollect;
import com.youda.entity.PostLike;
import com.youda.entity.User;
import com.youda.mapper.CategoryMapper;
import com.youda.mapper.CommentMapper;
import com.youda.mapper.PostCollectMapper;
import com.youda.mapper.PostLikeMapper;
import com.youda.mapper.PostMapper;
import com.youda.mapper.UserMapper;
import com.youda.service.PointsService;
import com.youda.service.PostService;
import com.youda.utils.UserContext;
import com.youda.vo.CommentVO;
import com.youda.vo.PostDetailVO;
import com.youda.vo.PostListVO;
import com.youda.vo.UserSimpleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private PostCollectMapper postCollectMapper;

    @Autowired
    private PointsService pointsService;

    @Override
    public IPage<PostListVO> getPostList(Integer current, Integer size, Long categoryId, String keyword, String sortBy) {
        Page<PostListVO> page = new Page<>(current, size);
        return baseMapper.selectPostList(page, categoryId, keyword, sortBy);
    }

    @Override
    public PostDetailVO getPostDetail(Long postId) {
        Post post = this.getById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        Post updatePost = new Post();
        updatePost.setId(postId);
        updatePost.setViewCount(post.getViewCount() + 1);
        this.updateById(updatePost);

        PostDetailVO vo = new PostDetailVO();
        vo.setPostId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setCategoryId(post.getCategoryId());
        vo.setViewCount(post.getViewCount() + 1);
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setCollectCount(post.getCollectCount());
        vo.setCreateTime(post.getCreateTime());

        User author = userMapper.selectById(post.getUserId());
        if (author != null) {
            UserSimpleVO authorVO = new UserSimpleVO();
            authorVO.setUserId(author.getId());
            authorVO.setNickname(author.getNickname());
            authorVO.setAvatar(author.getAvatar());
            vo.setAuthor(authorVO);
        }

        Category category = categoryMapper.selectById(post.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        Long currentUserId = UserContext.getCurrentUserIdOrNull();
        if (currentUserId != null) {
            Long likeCount = postLikeMapper.selectCount(
                    new LambdaQueryWrapper<PostLike>()
                            .eq(PostLike::getUserId, currentUserId)
                            .eq(PostLike::getPostId, postId)
            );
            vo.setIsLiked(likeCount > 0);

            Long collectCount = postCollectMapper.selectCount(
                    new LambdaQueryWrapper<PostCollect>()
                            .eq(PostCollect::getUserId, currentUserId)
                            .eq(PostCollect::getPostId, postId)
            );
            vo.setIsCollected(collectCount > 0);
        } else {
            vo.setIsLiked(false);
            vo.setIsCollected(false);
        }

        return vo;
    }

    @Override
    @Transactional
    public Long createPost(PostCreateDTO dto) {
        Long userId = UserContext.getCurrentUserId();

        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCategoryId(dto.getCategoryId());
        post.setUserId(userId);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setIsTop(0);

        this.save(post);
        pointsService.rewardPostCreate(userId, post.getId());
        return post.getId();
    }

    @Override
    public void deletePost(Long postId) {
        Long userId = UserContext.getCurrentUserId();
        Post post = this.getById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        User user = userMapper.selectById(userId);
        if (!post.getUserId().equals(userId) && (user == null || user.getRole() != 1)) {
            throw new BusinessException("无权删除该帖子");
        }

        this.removeById(postId);
    }

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Long postId) {
        Long userId = UserContext.getCurrentUserId();
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getUserId, userId)
                .eq(PostLike::getPostId, postId);

        PostLike existing = postLikeMapper.selectOne(wrapper);
        Post post = this.getById(postId);
        Map<String, Object> result = new HashMap<>();

        if (existing != null) {
            postLikeMapper.delete(wrapper);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            result.put("isLiked", false);
        } else {
            PostLike like = new PostLike();
            like.setUserId(userId);
            like.setPostId(postId);
            postLikeMapper.insert(like);
            post.setLikeCount(post.getLikeCount() + 1);
            result.put("isLiked", true);
        }

        this.updateById(post);
        result.put("likeCount", post.getLikeCount());
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> toggleCollect(Long postId) {
        Long userId = UserContext.getCurrentUserId();
        LambdaQueryWrapper<PostCollect> wrapper = new LambdaQueryWrapper<PostCollect>()
                .eq(PostCollect::getUserId, userId)
                .eq(PostCollect::getPostId, postId);

        PostCollect existing = postCollectMapper.selectOne(wrapper);
        Post post = this.getById(postId);
        Map<String, Object> result = new HashMap<>();

        if (existing != null) {
            postCollectMapper.delete(wrapper);
            post.setCollectCount(Math.max(0, post.getCollectCount() - 1));
            result.put("isCollected", false);
        } else {
            PostCollect collect = new PostCollect();
            collect.setUserId(userId);
            collect.setPostId(postId);
            postCollectMapper.insert(collect);
            post.setCollectCount(post.getCollectCount() + 1);
            result.put("isCollected", true);
        }

        this.updateById(post);
        result.put("collectCount", post.getCollectCount());
        return result;
    }

    @Override
    public IPage<CommentVO> getComments(Long postId, Integer current, Integer size) {
        Page<Comment> page = new Page<>(current, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId)
                .orderByDesc(Comment::getCreateTime);
        IPage<Comment> commentPage = commentMapper.selectPage(page, wrapper);

        return commentPage.convert(comment -> {
            CommentVO vo = new CommentVO();
            vo.setCommentId(comment.getId());
            vo.setContent(comment.getContent());
            vo.setCreateTime(comment.getCreateTime());

            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                UserSimpleVO authorVO = new UserSimpleVO();
                authorVO.setUserId(user.getId());
                authorVO.setNickname(user.getNickname());
                authorVO.setAvatar(user.getAvatar());
                vo.setAuthor(authorVO);
            }
            return vo;
        });
    }

    @Override
    @Transactional
    public Long createComment(Long postId, CommentCreateDTO dto) {
        Long userId = UserContext.getCurrentUserId();

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        commentMapper.insert(comment);

        Post post = this.getById(postId);
        post.setCommentCount(post.getCommentCount() + 1);
        this.updateById(post);

        pointsService.rewardCommentCreate(userId, comment.getId());
        return comment.getId();
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Long userId = UserContext.getCurrentUserId();
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        User user = userMapper.selectById(userId);
        if (!comment.getUserId().equals(userId) && (user == null || user.getRole() != 1)) {
            throw new BusinessException("无权删除该评论");
        }

        commentMapper.deleteById(commentId);
        Post post = this.getById(comment.getPostId());
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        this.updateById(post);
    }
}
