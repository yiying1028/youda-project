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
import java.math.BigDecimal;
import java.math.RoundingMode;
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
     * 闂傚倸鍊风粈渚€骞夐敓鐘冲殞闁绘劦鍓﹀▓鑺ユ叏濠靛棜顫﹀ù婊冪秺閺岀喓绱掗姀鐘崇亪缂備胶濮垫繛濠囧蓟閺囩喎绶為柛顐ｇ箓婵海绱撻崒姘毙ｉ柟绋垮⒔濡叉劙骞樼€涙ê顎撻梺鍛婄箓鐎氼參鎮楅搹鍦＝濞达綀顫夐埛鎰版煙缁嬪灝鏆辨い鏇稻缁傛帞鈧綆鍋呭▍銏ゆ⒑缂佹﹫鑰挎繛浣冲洦鍎楁俊銈呭暟绾捐棄霉閿濆懏鎯堟い搴㈡尵缁辨帗娼忛妸锔绢槹濡ょ姷鍋涢崯顖滄崲濠靛鐐婄憸宥囩玻濞戞ǚ鏀介柍钘夋閻忕姵绻涚涵椋庣瘈闁诡噯绻濋崺鈩冨閸楃偟绉洪柟顔规櫅椤斿繘顢欓悾宀€鈼ラ梻?
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
     * 闂傚倸鍊风粈渚€骞夐敓鐘冲殞闁绘劦鍓﹀▓鑺ユ叏濠靛棜顫﹀ù婊冪秺閹妫冨☉娆忔殘闁诲孩纰嶅畝鎼佸蓟濞戙垹绠绘俊鐐额嚙娴滈箖姊洪崫鍕潶闁稿﹥绻堝濠氭晸閻樿尙锛滃┑鐐村灦閻熝囧礄閳ユ剚娓婚柕鍫濆暙閳ь剚娲熷畷顖炲箻椤斿吋鐎悗骞垮劚濞诧絽鈻介鍫熺厾婵炴潙顑嗗▍鍥ㄣ亜閺冣偓濡啴寮婚敐鍡樺劅闁靛繆鎳囨慨鍥⒑閹稿氦澹樻い顓″劵椤︽潙鈹戦鈧ˉ鎾澄ｉ幇鏉跨婵°倓绀佹禍婊堟⒑閸涘﹥灏紒鍨涘墲鐎靛ジ宕惰閺€浠嬫煟閹邦剛鎽犻悘蹇ｅ弮閺岀喖宕橀懠顒傤唺缂備緡鍠栭悧鎾崇暦閹烘垟妲堟慨妯哄悑缁侇偅绻濈喊妯活潑闁搞劋鍗抽幃妯衡攽鐎ｎ偄鈧灚銇勯幘鍗炵仾闁抽攱鍨块弻鈩冨緞鎼淬垻銆婇柤鍙夌墵濮婂搫效閸パ€鍋撻弴鐐嶆稑鈹戦崶锔剧畾?
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
     * 闂傚倸鍊风粈渚€骞夐敓鐘冲殞闁绘劦鍓﹀▓鑺ユ叏濠靛棜顫﹀ù婊冪秺閹妫冨☉娆忔殘闁诲孩纰嶅畝鎼佸蓟濞戙垹绠绘俊鐐额嚙娴滈箖姊洪崫鍕潶闁稿﹥顨堝Σ鎰板箻鐠囪尙锛滃┑鐐村灦閼归箖鍩涙繝鍕＝濞撴埃鍋撴い銈呭€垮畷鎴炵節閸パ勭€悗骞垮劚閹虫劙寮抽崱娑欑厱闁哄洢鍔嬬花鐣岀磼鏉堛劌鍝烘慨濠呮缁瑧鎹勯妸褜鍞剁紓鍌欑椤︻垶鎮樺顓犫攳濠电姴娲ょ粻鐟懊归敐鍫殐婵☆偄鍟村铏圭磼濡搫顫屽銈嗘处閸欏啯淇婄€涙绡€闁稿鍨扮紞濠囧箖閳哄啰纾兼俊顖滅帛濞堟悂姊绘担铏瑰笡妞ゃ劌鎳橀獮妤€顭ㄩ崨顕呮綗闂佸湱鍎ら〃鍛矆鐎ｎ偁浜滈柟鏉垮閸掍即鏌嶈閸忔瑩宕愬┑瀣摕闁挎繂顦悡娑樏归敐鍥у妺闁规彃銈搁弻锝夊閳轰胶浠梺鍦焾閸熷潡鎮惧畡鎷旂喓鎮伴埄鍐偓濠氭⒑閸︻厼鍔嬫慨濠傤煼椤㈡瑩骞掑Δ浣叉嫼闁哄鍋炴刊浠嬪礂鐏炵瓔鐔嗙憸搴ㄣ€冮崨瀛樺仼闁割煈鍋呮刊鎾偡濞嗗繐顏╅柛鏂挎嚇濮婃椽宕烽鐐板婵犫拃鍐弰鐎?
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
     * 闂傚倷娴囧畷鍨叏閺夋嚚娲Χ閸℃ɑ鐝锋繛瀵稿Т椤戝懘鎮″┑瀣厱闊洦鑹炬禍瑙勩亜閳哄啫鍘存慨濠冩そ瀹曞綊顢氶崨顓炲濠电姰鍨奸～澶愬礉濡ゅ懎绠熼柟闂寸劍閸嬪鏌涢锝囩畼闁荤喐鍔楃槐鎾存媴鐟欏嫧鎷归梺鍦焾閸熷潡鎮鹃悜鑺ュ亹閻犲洦褰冮崬銊╂⒑闂堟侗妲堕柛銊潐缁?
     * 闂傚倸鍊烽懗鍫曗€﹂崼銏″床闁归偊鍠氶惌鎾绘煟閹达絾顥夐柛銊ュ€块弻娑氫沪閸撗呯厑闁诲孩纰嶅畝鎼佸蓟濞戙垹绠绘俊鐐额嚙娴滈箖姊洪崫鍕潶闁稿﹥娲熷﹢渚€姊虹紒姗嗙劷缂侇噮鍨跺顐︽焼瀹ュ棛鍘藉┑掳鍊曢崰姘舵倿閻愵兙浜滈柡鍥朵簽缁夘喚鈧娲﹂崑濠傜暦閻旂⒈鏁冮柕鍫濇噹缁犳垶绻?0闂傚倸鍊烽悞锔锯偓绗涘懐鐭欓柟杈鹃檮閸ゆ劖銇勯弽銊х細濞存粌婀遍幉鎼佸棘濞嗘儳娈ㄩ梺鍓茬厛閸嬪嫮娆㈤悙娴嬫斀闁绘ɑ褰冮顐︽煥濞戞瑧娲存慨濠呮缁瑧鎹勯妸褜鍟堟繝鐢靛仜閹冲繐煤濮椻偓瀵煡宕奸弴鐔告珖闂侀€炲苯澧存い銏″哺閺佹劖寰勬繝鍕Е婵＄偑鍊栫敮濠囨嚄閸洖鐓濋柡鍐ㄧ墛閻撴盯鏌涚仦鎹愬闁抽攱姊归〃銉╂倷閸欏妫﹂梺鍝勫閳ь剚鍓氶崥瀣煕閵夋垵鍟╃划顖炴⒒娓氣偓濞佳兠洪妶鍚ゆ椽鏁傞崜褏鐒?0 闂傚倸鍊风粈浣虹礊婵犲偆鐒界憸蹇曞垝閺冨牆閱囬柡鍥╁枎娴犺偐绱撻崒娆戝妽妞ゎ厼娲妴鍛存煥鐎ｂ晝绠氬銈嗙墬濮樸劍绂掗姀銈嗙厽閹兼惌鍠栧顕€鏌?
     */
    private void normalizeCoursePricing(Course course) {
        if (course == null) {
            throw new BusinessException(400, "Course payload cannot be null");
        }
        BigDecimal normalizedPrice = course.getPriceAmount() == null
                ? BigDecimal.ZERO
                : course.getPriceAmount().max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        course.setPriceAmount(normalizedPrice);
        course.setRequiresPoints(0);
        course.setPointsCost(0);
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


