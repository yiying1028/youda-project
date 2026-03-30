package com.youda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youda.common.BusinessException;
import com.youda.dto.ProgressUpdateDTO;
import com.youda.entity.Course;
import com.youda.entity.CourseChapter;
import com.youda.entity.CourseVideo;
import com.youda.entity.Grade;
import com.youda.entity.LearningProgress;
import com.youda.entity.Subject;
import com.youda.mapper.CourseChapterMapper;
import com.youda.mapper.CourseMapper;
import com.youda.mapper.CourseVideoMapper;
import com.youda.mapper.GradeMapper;
import com.youda.mapper.LearningProgressMapper;
import com.youda.mapper.SubjectMapper;
import com.youda.service.CourseService;
import com.youda.service.PointsService;
import com.youda.utils.UserContext;
import com.youda.vo.ChapterVO;
import com.youda.vo.CourseDetailVO;
import com.youda.vo.CourseListVO;
import com.youda.vo.ProgressVO;
import com.youda.vo.VideoPlayVO;
import com.youda.vo.VideoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Autowired
    private CourseChapterMapper chapterMapper;

    @Autowired
    private CourseVideoMapper videoMapper;

    @Autowired
    private LearningProgressMapper progressMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private PointsService pointsService;

    @Override
    public IPage<CourseListVO> getCourseList(Integer current, Integer size, Long subjectId, Long gradeId, String keyword) {
        Page<CourseListVO> page = new Page<>(current, size);
        return baseMapper.selectCourseList(page, subjectId, gradeId, keyword);
    }

    @Override
    public CourseDetailVO getCourseDetail(Long courseId) {
        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        CourseDetailVO vo = new CourseDetailVO();
        vo.setCourseId(course.getId());
        vo.setName(course.getName());
        vo.setDescription(course.getDescription());
        vo.setCoverImage(course.getCoverImage());
        vo.setTeacherName(course.getTeacherName());
        vo.setStudentCount(course.getStudentCount());

        Subject subject = subjectMapper.selectById(course.getSubjectId());
        if (subject != null) {
            vo.setSubjectName(subject.getName());
        }

        Grade grade = gradeMapper.selectById(course.getGradeId());
        if (grade != null) {
            vo.setGradeName(grade.getName());
        }

        List<CourseChapter> chapters = chapterMapper.selectList(
                new LambdaQueryWrapper<CourseChapter>()
                        .eq(CourseChapter::getCourseId, courseId)
                        .orderByAsc(CourseChapter::getSort)
        );

        List<ChapterVO> chapterVOList = new ArrayList<>();
        int totalVideos = 0;
        int completedVideos = 0;
        Long currentUserId = UserContext.getCurrentUserIdOrNull();

        for (CourseChapter chapter : chapters) {
            ChapterVO chapterVO = new ChapterVO();
            chapterVO.setChapterId(chapter.getId());
            chapterVO.setTitle(chapter.getTitle());

            List<CourseVideo> videos = videoMapper.selectList(
                    new LambdaQueryWrapper<CourseVideo>()
                            .eq(CourseVideo::getChapterId, chapter.getId())
                            .orderByAsc(CourseVideo::getSort)
            );

            List<VideoVO> videoVOList = new ArrayList<>();
            for (CourseVideo video : videos) {
                VideoVO videoVO = new VideoVO();
                videoVO.setVideoId(video.getId());
                videoVO.setTitle(video.getTitle());
                videoVO.setDuration(video.getDuration());
                videoVOList.add(videoVO);
                totalVideos++;

                if (currentUserId != null) {
                    LearningProgress progress = progressMapper.selectOne(
                            new LambdaQueryWrapper<LearningProgress>()
                                    .eq(LearningProgress::getUserId, currentUserId)
                                    .eq(LearningProgress::getVideoId, video.getId())
                    );
                    if (progress != null && progress.getIsCompleted() == 1) {
                        completedVideos++;
                    }
                }
            }

            chapterVO.setVideos(videoVOList);
            chapterVOList.add(chapterVO);
        }

        vo.setChapters(chapterVOList);

        ProgressVO progressVO = new ProgressVO();
        progressVO.setTotalCount(totalVideos);
        progressVO.setCompletedCount(completedVideos);
        progressVO.setPercentage(totalVideos > 0 ? completedVideos * 100 / totalVideos : 0);
        vo.setProgress(progressVO);
        return vo;
    }

    @Override
    public VideoPlayVO getVideoPlayInfo(Long videoId) {
        CourseVideo video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }

        VideoPlayVO vo = new VideoPlayVO();
        vo.setVideoId(video.getId());
        vo.setTitle(video.getTitle());
        vo.setVideoUrl(video.getVideoUrl());
        vo.setDuration(video.getDuration());

        Long currentUserId = UserContext.getCurrentUserIdOrNull();
        if (currentUserId != null) {
            LearningProgress progress = progressMapper.selectOne(
                    new LambdaQueryWrapper<LearningProgress>()
                            .eq(LearningProgress::getUserId, currentUserId)
                            .eq(LearningProgress::getVideoId, videoId)
            );
            vo.setLastPosition(progress != null ? progress.getPosition() : 0);
        } else {
            vo.setLastPosition(0);
        }

        List<CourseVideo> allVideos = videoMapper.selectList(
                new LambdaQueryWrapper<CourseVideo>()
                        .eq(CourseVideo::getCourseId, video.getCourseId())
                        .orderByAsc(CourseVideo::getChapterId)
                        .orderByAsc(CourseVideo::getSort)
        );

        for (int i = 0; i < allVideos.size(); i++) {
            if (allVideos.get(i).getId().equals(videoId)) {
                vo.setPrevVideoId(i > 0 ? allVideos.get(i - 1).getId() : null);
                vo.setNextVideoId(i < allVideos.size() - 1 ? allVideos.get(i + 1).getId() : null);
                break;
            }
        }

        return vo;
    }

    @Override
    public Map<String, Object> updateProgress(Long videoId, ProgressUpdateDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        CourseVideo video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }

        boolean isCompleted = dto.getDuration() > 0 && dto.getPosition() >= dto.getDuration() * 0.9;
        LearningProgress progress = progressMapper.selectOne(
                new LambdaQueryWrapper<LearningProgress>()
                        .eq(LearningProgress::getUserId, userId)
                        .eq(LearningProgress::getVideoId, videoId)
        );
        boolean wasCompleted = progress != null && progress.getIsCompleted() == 1;

        if (progress == null) {
            progress = new LearningProgress();
            progress.setUserId(userId);
            progress.setCourseId(video.getCourseId());
            progress.setVideoId(videoId);
            progress.setPosition(dto.getPosition());
            progress.setIsCompleted(isCompleted ? 1 : 0);
            progressMapper.insert(progress);

            Long currentCourseProgressCount = progressMapper.selectCount(
                    new LambdaQueryWrapper<LearningProgress>()
                            .eq(LearningProgress::getUserId, userId)
                            .eq(LearningProgress::getCourseId, video.getCourseId())
            );
            if (currentCourseProgressCount == 1) {
                Course course = this.getById(video.getCourseId());
                course.setStudentCount(course.getStudentCount() + 1);
                this.updateById(course);
            }
        } else {
            progress.setPosition(dto.getPosition());
            if (isCompleted) {
                progress.setIsCompleted(1);
            }
            progressMapper.updateById(progress);
        }

        if (!wasCompleted && progress.getIsCompleted() == 1 && isChapterCompleted(userId, video.getChapterId())) {
            pointsService.rewardChapterComplete(userId, video.getChapterId());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("isCompleted", isCompleted);
        result.put("progress", dto.getDuration() > 0 ? dto.getPosition() * 100 / dto.getDuration() : 0);
        return result;
    }

    @Override
    public List<Map<String, Object>> getLearningRecords() {
        Long userId = UserContext.getCurrentUserId();
        List<LearningProgress> progressList = progressMapper.selectList(
                new LambdaQueryWrapper<LearningProgress>()
                        .eq(LearningProgress::getUserId, userId)
                        .orderByDesc(LearningProgress::getUpdateTime)
        );

        Map<Long, LearningProgress> courseLatestMap = new LinkedHashMap<>();
        for (LearningProgress progress : progressList) {
            courseLatestMap.putIfAbsent(progress.getCourseId(), progress);
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (Map.Entry<Long, LearningProgress> entry : courseLatestMap.entrySet()) {
            Long courseId = entry.getKey();
            LearningProgress latestProgress = entry.getValue();
            Course course = this.getById(courseId);
            if (course == null) {
                continue;
            }

            long totalCount = videoMapper.selectCount(
                    new LambdaQueryWrapper<CourseVideo>()
                            .eq(CourseVideo::getCourseId, courseId)
            );
            long completedCount = progressMapper.selectCount(
                    new LambdaQueryWrapper<LearningProgress>()
                            .eq(LearningProgress::getUserId, userId)
                            .eq(LearningProgress::getCourseId, courseId)
                            .eq(LearningProgress::getIsCompleted, 1)
            );

            Map<String, Object> map = new HashMap<>();
            map.put("courseId", course.getId());
            map.put("courseName", course.getName());
            map.put("coverImage", course.getCoverImage());
            map.put("progress", totalCount > 0 ? (int) (completedCount * 100 / totalCount) : 0);
            map.put("lastStudyTime", latestProgress.getUpdateTime());
            records.add(map);
        }

        return records;
    }

    private boolean isChapterCompleted(Long userId, Long chapterId) {
        List<CourseVideo> chapterVideos = videoMapper.selectList(
                new LambdaQueryWrapper<CourseVideo>()
                        .eq(CourseVideo::getChapterId, chapterId)
                        .select(CourseVideo::getId)
        );
        if (chapterVideos.isEmpty()) {
            return false;
        }

        List<Long> videoIds = new ArrayList<>();
        for (CourseVideo chapterVideo : chapterVideos) {
            videoIds.add(chapterVideo.getId());
        }

        Long completedCount = progressMapper.selectCount(
                new LambdaQueryWrapper<LearningProgress>()
                        .eq(LearningProgress::getUserId, userId)
                        .eq(LearningProgress::getIsCompleted, 1)
                        .in(LearningProgress::getVideoId, videoIds)
        );
        return completedCount == chapterVideos.size();
    }
}
