package com.youda.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youda.vo.PointsOverviewVO;
import com.youda.vo.PointsRecordVO;

import java.util.Map;

public interface PointsService {

    void rewardPostCreate(Long userId, Long postId);

    void rewardCommentCreate(Long userId, Long commentId);

    void rewardWrongQuestionCreate(Long userId, Long questionId);

    void rewardChapterComplete(Long userId, Long chapterId);

    PointsOverviewVO getOverview();

    Map<String, Object> checkIn();

    Map<String, Object> getRanking();

    IPage<PointsRecordVO> getRecords(Integer current, Integer size);
}
