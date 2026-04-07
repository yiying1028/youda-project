package com.youda.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youda.common.BusinessException;
import com.youda.common.Result;
import com.youda.entity.Announcement;
import com.youda.entity.Category;
import com.youda.entity.Comment;
import com.youda.entity.Course;
import com.youda.entity.CourseChapter;
import com.youda.entity.CourseVideo;
import com.youda.entity.Post;
import com.youda.entity.Resource;
import com.youda.entity.User;
import com.youda.mapper.AnnouncementMapper;
import com.youda.mapper.CategoryMapper;
import com.youda.mapper.CommentMapper;
import com.youda.mapper.CourseChapterMapper;
import com.youda.mapper.CourseMapper;
import com.youda.mapper.CourseVideoMapper;
import com.youda.mapper.GradeMapper;
import com.youda.mapper.PostMapper;
import com.youda.mapper.ResourceMapper;
import com.youda.mapper.SubjectMapper;
import com.youda.mapper.UserMapper;
import com.youda.service.CourseService;
import com.youda.utils.FileUtils;
import com.youda.utils.UserContext;
import com.youda.vo.CourseDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private CourseService courseService;

    /**
     * 鍚庡彴鎺ュ彛缁熶竴鏍￠獙绠＄悊鍛樿韩浠姐€?
     */
    private void checkAdmin() {
        Long userId = UserContext.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null || user.getRole() != 1) {
            throw new BusinessException(403, "Admin only");
        }
    }

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

    @PutMapping("/user/{userId}/status")
    public Result<?> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        checkAdmin();

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success(status == 1 ? "User enabled" : "User disabled", null);
    }

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

    @DeleteMapping("/post/{postId}")
    public Result<?> deletePost(@PathVariable Long postId) {
        checkAdmin();

        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("Post not found");
        }
        postMapper.deleteById(postId);
        return Result.success("Delete successful", null);
    }

    @PutMapping("/post/{postId}/top")
    public Result<?> setPostTop(@PathVariable Long postId, @RequestParam Integer isTop) {
        checkAdmin();

        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("Post not found");
        }

        Post updatePost = new Post();
        updatePost.setId(postId);
        updatePost.setIsTop(isTop);
        postMapper.updateById(updatePost);
        return Result.success(isTop == 1 ? "Top set" : "Top removed", null);
    }

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

    @DeleteMapping("/resource/{resourceId}")
    public Result<?> deleteResource(@PathVariable Long resourceId) {
        checkAdmin();

        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException("Resource not found");
        }
        resourceMapper.deleteById(resourceId);
        return Result.success("Delete successful", null);
    }

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

    @GetMapping("/course/{courseId}")
    public Result<CourseDetailVO> getCourseDetail(@PathVariable Long courseId) {
        checkAdmin();
        return Result.success(courseService.getCourseDetail(courseId));
    }

    @PostMapping("/course/cover")
    public Result<Map<String, String>> uploadCourseCover(@RequestParam("file") MultipartFile file) throws IOException {
        checkAdmin();

        String url = fileUtils.uploadFile(file, "course-cover");
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success("Upload successful", data);
    }

    /**
     * 鍚庡彴璇剧▼鏂板锛屼繚瀛樺墠缁熶竴娓呮礂浠樿垂閰嶇疆銆?
     */
    @PostMapping("/course")
    public Result<Map<String, Long>> addCourse(@RequestBody Course course) {
        checkAdmin();

        normalizeCoursePricing(course);
        course.setStudentCount(0);
        course.setChapterCount(0);
        courseMapper.insert(course);

        Map<String, Long> data = new HashMap<>();
        data.put("courseId", course.getId());
        return Result.success("Create successful", data);
    }

    /**
     * 鍚庡彴璇剧▼缂栬緫锛岄伩鍏嶆妸闈炴硶浠锋牸缁勫悎鐩存帴鍐欒繘鏁版嵁搴撱€?
     */
    @PutMapping("/course/{courseId}")
    public Result<?> updateCourse(@PathVariable Long courseId, @RequestBody Course course) {
        checkAdmin();

        Course existing = courseMapper.selectById(courseId);
        if (existing == null) {
            throw new BusinessException("Course not found");
        }

        normalizeCoursePricing(course);
        course.setId(courseId);
        courseMapper.updateById(course);
        return Result.success("Update successful", null);
    }

    @DeleteMapping("/course/{courseId}")
    public Result<?> deleteCourse(@PathVariable Long courseId) {
        checkAdmin();

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("Course not found");
        }

        LambdaQueryWrapper<CourseChapter> chapterWrapper = new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, courseId);
        List<CourseChapter> chapters = chapterMapper.selectList(chapterWrapper);
        for (CourseChapter chapter : chapters) {
            videoMapper.delete(new LambdaQueryWrapper<CourseVideo>()
                    .eq(CourseVideo::getChapterId, chapter.getId()));
        }
        chapterMapper.delete(chapterWrapper);
        courseMapper.deleteById(courseId);
        return Result.success("Delete successful", null);
    }

    @PostMapping("/course/{courseId}/chapter")
    public Result<Map<String, Long>> addChapter(@PathVariable Long courseId, @RequestBody CourseChapter chapter) {
        checkAdmin();

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("Course not found");
        }
        if (chapter == null || chapter.getTitle() == null || chapter.getTitle().trim().isEmpty()) {
            throw new BusinessException(400, "Chapter title cannot be empty");
        }

        chapter.setCourseId(courseId);
        chapter.setTitle(chapter.getTitle().trim());
        if (chapter.getSort() == null || chapter.getSort() <= 0) {
            chapter.setSort(getNextChapterSort(courseId));
        }
        chapterMapper.insert(chapter);

        course.setChapterCount((course.getChapterCount() == null ? 0 : course.getChapterCount()) + 1);
        courseMapper.updateById(course);

        Map<String, Long> data = new HashMap<>();
        data.put("chapterId", chapter.getId());
        return Result.success("Create successful", data);
    }

    @DeleteMapping("/chapter/{chapterId}")
    public Result<?> deleteChapter(@PathVariable Long chapterId) {
        checkAdmin();

        CourseChapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException("Chapter not found");
        }

        videoMapper.delete(new LambdaQueryWrapper<CourseVideo>()
                .eq(CourseVideo::getChapterId, chapterId));
        chapterMapper.deleteById(chapterId);

        Course course = courseMapper.selectById(chapter.getCourseId());
        if (course != null) {
            course.setChapterCount(Math.max(0, (course.getChapterCount() == null ? 0 : course.getChapterCount()) - 1));
            courseMapper.updateById(course);
        }

        return Result.success("Delete successful", null);
    }

    @PostMapping("/chapter/{chapterId}/video")
    public Result<Map<String, Long>> uploadVideo(
            @PathVariable Long chapterId,
            @RequestParam("file") MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) Integer sort) throws IOException {
        checkAdmin();

        CourseChapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException("Chapter not found");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(400, "Video title cannot be empty");
        }

        String videoUrl = fileUtils.uploadFile(file, "video");

        CourseVideo video = new CourseVideo();
        video.setCourseId(chapter.getCourseId());
        video.setChapterId(chapterId);
        video.setTitle(title.trim());
        video.setVideoUrl(videoUrl);
        video.setSort(sort != null && sort > 0 ? sort : getNextVideoSort(chapterId));
        video.setDuration(0);
        videoMapper.insert(video);

        Map<String, Long> data = new HashMap<>();
        data.put("videoId", video.getId());
        return Result.success("Upload successful", data);
    }

    @DeleteMapping("/video/{videoId}")
    public Result<?> deleteVideo(@PathVariable Long videoId) {
        checkAdmin();

        CourseVideo video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new BusinessException("Video not found");
        }

        fileUtils.deleteFile(video.getVideoUrl());
        videoMapper.deleteById(videoId);
        return Result.success("Delete successful", null);
    }

    @PostMapping("/category")
    public Result<Map<String, Long>> addCategory(@RequestBody Category category) {
        checkAdmin();
        category.setStatus(1);
        categoryMapper.insert(category);

        Map<String, Long> data = new HashMap<>();
        data.put("categoryId", category.getId());
        return Result.success("Create successful", data);
    }

    @PutMapping("/category/{categoryId}")
    public Result<?> updateCategory(@PathVariable Long categoryId, @RequestBody Category category) {
        checkAdmin();
        Category existing = categoryMapper.selectById(categoryId);
        if (existing == null) {
            throw new BusinessException("Category not found");
        }
        category.setId(categoryId);
        categoryMapper.updateById(category);
        return Result.success("Update successful", null);
    }

    @DeleteMapping("/category/{categoryId}")
    public Result<?> deleteCategory(@PathVariable Long categoryId) {
        checkAdmin();
        categoryMapper.deleteById(categoryId);
        return Result.success("Delete successful", null);
    }

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

    @PostMapping("/announcement")
    public Result<Map<String, Long>> addAnnouncement(@RequestBody Announcement announcement) {
        checkAdmin();
        announcement.setStatus(1);
        announcementMapper.insert(announcement);

        Map<String, Long> data = new HashMap<>();
        data.put("announcementId", announcement.getId());
        return Result.success("Publish successful", data);
    }

    @PutMapping("/announcement/{announcementId}")
    public Result<?> updateAnnouncement(@PathVariable Long announcementId, @RequestBody Announcement announcement) {
        checkAdmin();
        Announcement existing = announcementMapper.selectById(announcementId);
        if (existing == null) {
            throw new BusinessException("Announcement not found");
        }
        announcement.setId(announcementId);
        announcementMapper.updateById(announcement);
        return Result.success("Update successful", null);
    }

    @DeleteMapping("/announcement/{announcementId}")
    public Result<?> deleteAnnouncement(@PathVariable Long announcementId) {
        checkAdmin();
        announcementMapper.deleteById(announcementId);
        return Result.success("Delete successful", null);
    }

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

    /**
     * 璇剧▼浠锋牸褰掍竴鍖栥€?
     * 鍏嶈垂璇剧▼浠锋牸寮哄埗涓?0锛屽彧鏈夊紑鍏虫墦寮€涓斾环鏍煎ぇ浜?0 鎵嶇畻浠樿垂銆?
     */
    private void normalizeCoursePricing(Course course) {
        if (course == null) {
            throw new BusinessException(400, "Course payload cannot be null");
        }
        int normalizedCost = course.getPointsCost() == null ? 0 : Math.max(course.getPointsCost(), 0);
        boolean paidCourse = course.getRequiresPoints() != null && course.getRequiresPoints() == 1 && normalizedCost > 0;
        course.setRequiresPoints(paidCourse ? 1 : 0);
        course.setPointsCost(paidCourse ? normalizedCost : 0);
    }

    private Integer getNextChapterSort(Long courseId) {
        List<CourseChapter> chapters = chapterMapper.selectList(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, courseId)
                .orderByDesc(CourseChapter::getSort)
                .last("LIMIT 1"));
        if (chapters.isEmpty() || chapters.get(0).getSort() == null) {
            return 1;
        }
        return chapters.get(0).getSort() + 1;
    }

    private Integer getNextVideoSort(Long chapterId) {
        List<CourseVideo> videos = videoMapper.selectList(new LambdaQueryWrapper<CourseVideo>()
                .eq(CourseVideo::getChapterId, chapterId)
                .orderByDesc(CourseVideo::getSort)
                .last("LIMIT 1"));
        if (videos.isEmpty() || videos.get(0).getSort() == null) {
            return 1;
        }
        return videos.get(0).getSort() + 1;
    }
}


