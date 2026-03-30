package com.youda.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youda.common.BusinessException;
import com.youda.common.Result;
import com.youda.entity.*;
import com.youda.mapper.*;
import com.youda.utils.FileUtils;
import com.youda.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台Controller
 * 仅管理员（role=1）可访问
 * 涵盖：用户管理、帖子管理、资料管理、课程管理、公告管理、数据统计
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseChapterMapper chapterMapper;

    @Autowired
    private CourseVideoMapper videoMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private FileUtils fileUtils;

    /**
     * 公共：校验当前用户是否为管理员（role=1）
     */
    private void checkAdmin() {
        Long userId = UserContext.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null || user.getRole() != 1) {
            throw new BusinessException(403, "无权限访问，仅管理员可操作");
        }
    }

    // ==================== 用户管理 ====================

    /**
     * 获取用户列表（支持关键词搜索用户名/昵称）
     */
    @GetMapping("/user/list")
    public Result<IPage<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        checkAdmin();

        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword).or().like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return Result.success(userMapper.selectPage(page, wrapper));
    }

    /**
     * 禁用/启用用户账号
     * @param status 0=禁用 1=启用
     */
    @PutMapping("/user/{userId}/status")
    public Result<?> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        checkAdmin();

        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success(status == 1 ? "启用成功" : "禁用成功", null);
    }

    // ==================== 帖子管理 ====================

    /**
     * 获取帖子列表（管理后台，含软删除数据除外）
     */
    @GetMapping("/post/list")
    public Result<IPage<Post>> getPostList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        checkAdmin();

        Page<Post> page = new Page<>(current, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Post::getTitle, keyword);
        }
        wrapper.orderByDesc(Post::getCreateTime);
        return Result.success(postMapper.selectPage(page, wrapper));
    }

    /**
     * 删除帖子（管理员强制删除）
     */
    @DeleteMapping("/post/{postId}")
    public Result<?> deletePost(@PathVariable Long postId) {
        checkAdmin();

        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException("帖子不存在");
        postMapper.deleteById(postId);
        return Result.success("删除成功", null);
    }

    /**
     * 设置/取消帖子置顶
     * @param isTop 1=置顶 0=取消置顶
     */
    @PutMapping("/post/{postId}/top")
    public Result<?> setPostTop(@PathVariable Long postId, @RequestParam Integer isTop) {
        checkAdmin();

        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException("帖子不存在");

        Post updatePost = new Post();
        updatePost.setId(postId);
        updatePost.setIsTop(isTop);
        postMapper.updateById(updatePost);
        return Result.success(isTop == 1 ? "置顶成功" : "取消置顶成功", null);
    }

    // ==================== 资料管理 ====================

    /**
     * 获取资料列表（管理后台）
     */
    @GetMapping("/resource/list")
    public Result<IPage<Resource>> getResourceList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        checkAdmin();

        Page<Resource> page = new Page<>(current, size);
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Resource::getName, keyword);
        }
        wrapper.orderByDesc(Resource::getCreateTime);
        return Result.success(resourceMapper.selectPage(page, wrapper));
    }

    /**
     * 删除资料（管理员）
     */
    @DeleteMapping("/resource/{resourceId}")
    public Result<?> deleteResource(@PathVariable Long resourceId) {
        checkAdmin();

        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) throw new BusinessException("资料不存在");
        resourceMapper.deleteById(resourceId);
        return Result.success("删除成功", null);
    }

    // ==================== 课程管理 ====================

    /**
     * 获取课程列表（管理后台）
     */
    @GetMapping("/course/list")
    public Result<IPage<Course>> getCourseList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        checkAdmin();

        Page<Course> page = new Page<>(current, size);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Course::getName, keyword);
        }
        wrapper.orderByDesc(Course::getCreateTime);
        return Result.success(courseMapper.selectPage(page, wrapper));
    }

    /**
     * 新增课程
     */
    @PostMapping("/course")
    public Result<Map<String, Long>> addCourse(@RequestBody Course course) {
        checkAdmin();

        course.setStudentCount(0);
        course.setChapterCount(0);
        courseMapper.insert(course);

        Map<String, Long> data = new HashMap<>();
        data.put("courseId", course.getId());
        return Result.success("添加成功", data);
    }

    /**
     * 编辑课程基本信息
     */
    @PutMapping("/course/{courseId}")
    public Result<?> updateCourse(@PathVariable Long courseId, @RequestBody Course course) {
        checkAdmin();

        Course existing = courseMapper.selectById(courseId);
        if (existing == null) throw new BusinessException("课程不存在");

        course.setId(courseId);
        courseMapper.updateById(course);
        return Result.success("修改成功", null);
    }

    /**
     * 删除课程（同时删除章节和视频）
     */
    @DeleteMapping("/course/{courseId}")
    public Result<?> deleteCourse(@PathVariable Long courseId) {
        checkAdmin();

        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new BusinessException("课程不存在");

        // 查出该课程下所有章节，再删除章节下视频
        LambdaQueryWrapper<CourseChapter> chapterWrapper = new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, courseId);
        java.util.List<CourseChapter> chapters = chapterMapper.selectList(chapterWrapper);
        for (CourseChapter chapter : chapters) {
            videoMapper.delete(new LambdaQueryWrapper<CourseVideo>()
                    .eq(CourseVideo::getChapterId, chapter.getId()));
        }
        chapterMapper.delete(chapterWrapper);
        courseMapper.deleteById(courseId);
        return Result.success("删除成功", null);
    }

    // ==================== 章节管理 ====================

    /**
     * 为课程新增章节
     */
    @PostMapping("/course/{courseId}/chapter")
    public Result<Map<String, Long>> addChapter(@PathVariable Long courseId, @RequestBody CourseChapter chapter) {
        checkAdmin();

        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new BusinessException("课程不存在");

        chapter.setCourseId(courseId);
        chapterMapper.insert(chapter);

        // 更新课程章节数统计
        course.setChapterCount(course.getChapterCount() + 1);
        courseMapper.updateById(course);

        Map<String, Long> data = new HashMap<>();
        data.put("chapterId", chapter.getId());
        return Result.success("添加成功", data);
    }

    /**
     * 删除章节（同时删除章节下所有视频）
     */
    @DeleteMapping("/chapter/{chapterId}")
    public Result<?> deleteChapter(@PathVariable Long chapterId) {
        checkAdmin();

        CourseChapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) throw new BusinessException("章节不存在");

        // 级联删除视频
        videoMapper.delete(new LambdaQueryWrapper<CourseVideo>()
                .eq(CourseVideo::getChapterId, chapterId));
        chapterMapper.deleteById(chapterId);

        // 更新课程章节数
        Course course = courseMapper.selectById(chapter.getCourseId());
        if (course != null) {
            course.setChapterCount(Math.max(0, course.getChapterCount() - 1));
            courseMapper.updateById(course);
        }

        return Result.success("删除成功", null);
    }

    // ==================== 视频管理 ====================

    /**
     * 为章节上传视频
     * 接收视频文件和标题、排序号
     */
    @PostMapping("/chapter/{chapterId}/video")
    public Result<Map<String, Long>> uploadVideo(
            @PathVariable Long chapterId,
            @RequestParam("file") MultipartFile file,
            @RequestParam String title,
            @RequestParam(defaultValue = "0") Integer sort) throws IOException {
        checkAdmin();

        CourseChapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) throw new BusinessException("章节不存在");

        // 保存视频文件
        String videoUrl = fileUtils.uploadFile(file, "video");

        CourseVideo video = new CourseVideo();
        video.setCourseId(chapter.getCourseId());
        video.setChapterId(chapterId);
        video.setTitle(title);
        video.setVideoUrl(videoUrl);
        video.setSort(sort);
        // 时长暂时为0，实际项目可通过ffprobe解析
        video.setDuration(0);
        videoMapper.insert(video);

        Map<String, Long> data = new HashMap<>();
        data.put("videoId", video.getId());
        return Result.success("上传成功", data);
    }

    /**
     * 删除视频
     */
    @DeleteMapping("/video/{videoId}")
    public Result<?> deleteVideo(@PathVariable Long videoId) {
        checkAdmin();

        CourseVideo video = videoMapper.selectById(videoId);
        if (video == null) throw new BusinessException("视频不存在");

        // 删除文件
        fileUtils.deleteFile(video.getVideoUrl());
        videoMapper.deleteById(videoId);
        return Result.success("删除成功", null);
    }

    // ==================== 分类管理 ====================

    /**
     * 新增帖子分类
     */
    @PostMapping("/category")
    public Result<Map<String, Long>> addCategory(@RequestBody Category category) {
        checkAdmin();
        category.setStatus(1);
        categoryMapper.insert(category);

        Map<String, Long> data = new HashMap<>();
        data.put("categoryId", category.getId());
        return Result.success("添加成功", data);
    }

    /**
     * 修改帖子分类
     */
    @PutMapping("/category/{categoryId}")
    public Result<?> updateCategory(@PathVariable Long categoryId, @RequestBody Category category) {
        checkAdmin();
        Category existing = categoryMapper.selectById(categoryId);
        if (existing == null) throw new BusinessException("分类不存在");
        category.setId(categoryId);
        categoryMapper.updateById(category);
        return Result.success("修改成功", null);
    }

    /**
     * 删除帖子分类
     */
    @DeleteMapping("/category/{categoryId}")
    public Result<?> deleteCategory(@PathVariable Long categoryId) {
        checkAdmin();
        categoryMapper.deleteById(categoryId);
        return Result.success("删除成功", null);
    }

    // ==================== 公告管理 ====================

    /**
     * 获取公告列表（管理后台，分页）
     */
    @GetMapping("/announcement/list")
    public Result<IPage<Announcement>> getAnnouncementList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        checkAdmin();

        Page<Announcement> page = new Page<>(current, size);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .orderByDesc(Announcement::getCreateTime);
        return Result.success(announcementMapper.selectPage(page, wrapper));
    }

    /**
     * 发布公告
     */
    @PostMapping("/announcement")
    public Result<Map<String, Long>> addAnnouncement(@RequestBody Announcement announcement) {
        checkAdmin();
        announcement.setStatus(1);
        announcementMapper.insert(announcement);

        Map<String, Long> data = new HashMap<>();
        data.put("announcementId", announcement.getId());
        return Result.success("发布成功", data);
    }

    /**
     * 修改公告内容
     */
    @PutMapping("/announcement/{announcementId}")
    public Result<?> updateAnnouncement(@PathVariable Long announcementId, @RequestBody Announcement announcement) {
        checkAdmin();
        Announcement existing = announcementMapper.selectById(announcementId);
        if (existing == null) throw new BusinessException("公告不存在");
        announcement.setId(announcementId);
        announcementMapper.updateById(announcement);
        return Result.success("修改成功", null);
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/announcement/{announcementId}")
    public Result<?> deleteAnnouncement(@PathVariable Long announcementId) {
        checkAdmin();
        announcementMapper.deleteById(announcementId);
        return Result.success("删除成功", null);
    }

    // ==================== 数据统计 ====================

    /**
     * 获取平台整体统计数据
     * 包含用户总数、帖子总数、资料总数、课程总数
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        checkAdmin();

        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userMapper.selectCount(null));
        stats.put("postCount", postMapper.selectCount(null));
        stats.put("resourceCount", resourceMapper.selectCount(null));
        stats.put("courseCount", courseMapper.selectCount(null));
        stats.put("commentCount", commentMapper.selectCount(null));
        return Result.success(stats);
    }
}
