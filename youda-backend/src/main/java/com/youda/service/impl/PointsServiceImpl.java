package com.youda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youda.common.BusinessException;
import com.youda.entity.PointsRecord;
import com.youda.entity.User;
import com.youda.entity.UserCheckin;
import com.youda.mapper.PointsRecordMapper;
import com.youda.mapper.UserCheckinMapper;
import com.youda.mapper.UserMapper;
import com.youda.service.PointsService;
import com.youda.utils.UserContext;
import com.youda.vo.PointsOverviewVO;
import com.youda.vo.PointsRankingVO;
import com.youda.vo.PointsRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PointsServiceImpl implements PointsService {

    private static final String ACTION_CHECKIN = "CHECKIN";
    private static final String ACTION_CHECKIN_BONUS = "CHECKIN_BONUS";
    private static final String ACTION_POST_CREATE = "POST_CREATE";
    private static final String ACTION_COMMENT_CREATE = "COMMENT_CREATE";
    private static final String ACTION_CHAPTER_COMPLETE = "CHAPTER_COMPLETE";
    private static final String ACTION_WRONG_QUESTION_CREATE = "WRONG_QUESTION_CREATE";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserCheckinMapper userCheckinMapper;

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Override
    @Transactional
    public void rewardPostCreate(Long userId, Long postId) {
        rewardWithDailyLimit(userId, ACTION_POST_CREATE, String.valueOf(postId), 3, "发布帖子", 3);
    }

    @Override
    @Transactional
    public void rewardCommentCreate(Long userId, Long commentId) {
        rewardWithDailyLimit(userId, ACTION_COMMENT_CREATE, String.valueOf(commentId), 1, "发表评论", 5);
    }

    @Override
    @Transactional
    public void rewardWrongQuestionCreate(Long userId, Long questionId) {
        rewardWithDailyLimit(userId, ACTION_WRONG_QUESTION_CREATE, String.valueOf(questionId), 2, "添加错题", 5);
    }

    @Override
    @Transactional
    public void rewardChapterComplete(Long userId, Long chapterId) {
        rewardWithDailyLimit(userId, ACTION_CHAPTER_COMPLETE, String.valueOf(chapterId), 10, "完成课程章节", null);
    }

    @Override
    public PointsOverviewVO getOverview() {
        Long userId = UserContext.getCurrentUserId();
        User user = requireUser(userId);
        LocalDate today = LocalDate.now();

        PointsOverviewVO vo = new PointsOverviewVO();
        vo.setPoints(user.getPoints() == null ? 0 : user.getPoints());
        vo.setTodayEarnedPoints(sumTodayPoints(userId));
        vo.setTodayCheckedIn(hasCheckedIn(userId, today));
        vo.setContinuousDays(vo.getTodayCheckedIn() ? calculateContinuousDays(userId, today) : calculateContinuousDays(userId, today.minusDays(1)));
        vo.setTotalCheckinDays(Math.toIntExact(userCheckinMapper.selectCount(
                new LambdaQueryWrapper<UserCheckin>().eq(UserCheckin::getUserId, userId)
        )));
        vo.setCurrentRank(getRank(userId));
        return vo;
    }

    @Override
    @Transactional
    public Map<String, Object> checkIn() {
        Long userId = UserContext.getCurrentUserId();
        LocalDate today = LocalDate.now();
        if (hasCheckedIn(userId, today)) {
            throw new BusinessException("今天已经签到过了");
        }

        int streakDays = calculateContinuousDays(userId, today.minusDays(1)) + 1;
        int bonusPoints = streakDays % 7 == 0 ? 20 : 0;

        UserCheckin checkin = new UserCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setPointsReward(5);
        checkin.setBonusPoints(bonusPoints);
        checkin.setStreakDays(streakDays);
        userCheckinMapper.insert(checkin);

        addPoints(userId, ACTION_CHECKIN, today.toString(), 5, "每日签到");
        if (bonusPoints > 0) {
            addPoints(userId, ACTION_CHECKIN_BONUS, today.toString(), bonusPoints, "连续签到奖励");
        }

        PointsOverviewVO overview = getOverview();
        Map<String, Object> result = new HashMap<>();
        result.put("continuousDays", streakDays);
        result.put("bonusPoints", bonusPoints);
        result.put("overview", overview);
        return result;
    }

    @Override
    public Map<String, Object> getRanking() {
        Long currentUserId = UserContext.getCurrentUserId();
        Page<User> page = new Page<>(1, 20);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 1)
                .eq(User::getRole, 0)
                .orderByDesc(User::getPoints)
                .orderByAsc(User::getCreateTime);
        IPage<User> rankingPage = userMapper.selectPage(page, wrapper);

        List<PointsRankingVO> rankingList = new ArrayList<>();
        int rank = 1;
        for (User user : rankingPage.getRecords()) {
            PointsRankingVO vo = new PointsRankingVO();
            vo.setUserId(user.getId());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setPoints(user.getPoints() == null ? 0 : user.getPoints());
            vo.setRank(rank++);
            vo.setCurrentUser(user.getId().equals(currentUserId));
            rankingList.add(vo);
        }

        User currentUser = requireUser(currentUserId);
        Map<String, Object> result = new HashMap<>();
        result.put("rankingList", rankingList);
        result.put("currentUserRank", getRank(currentUserId));
        result.put("currentUserPoints", currentUser.getPoints() == null ? 0 : currentUser.getPoints());
        result.put("currentUserNickname", currentUser.getNickname());
        return result;
    }

    @Override
    public IPage<PointsRecordVO> getRecords(Integer current, Integer size) {
        Long userId = UserContext.getCurrentUserId();
        Page<PointsRecord> page = new Page<>(current, size);
        IPage<PointsRecord> recordPage = pointsRecordMapper.selectPage(
                page,
                new LambdaQueryWrapper<PointsRecord>()
                        .eq(PointsRecord::getUserId, userId)
                        .orderByDesc(PointsRecord::getCreateTime)
        );

        return recordPage.convert(record -> {
            PointsRecordVO vo = new PointsRecordVO();
            vo.setActionType(record.getActionType());
            vo.setActionLabel(getActionLabel(record.getActionType()));
            vo.setPoints(record.getPoints());
            vo.setRemark(record.getRemark());
            vo.setCreateTime(record.getCreateTime());
            return vo;
        });
    }

    private void rewardWithDailyLimit(Long userId, String actionType, String bizId, int points, String remark, Integer dailyLimit) {
        PointsRecord existing = pointsRecordMapper.selectOne(
                new LambdaQueryWrapper<PointsRecord>()
                        .eq(PointsRecord::getUserId, userId)
                        .eq(PointsRecord::getActionType, actionType)
                        .eq(PointsRecord::getBizId, bizId)
        );
        if (existing != null) {
            return;
        }

        if (dailyLimit != null) {
            LocalDateTime start = LocalDate.now().atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            Long todayCount = pointsRecordMapper.selectCount(
                    new LambdaQueryWrapper<PointsRecord>()
                            .eq(PointsRecord::getUserId, userId)
                            .eq(PointsRecord::getActionType, actionType)
                            .ge(PointsRecord::getCreateTime, start)
                            .lt(PointsRecord::getCreateTime, end)
            );
            if (todayCount >= dailyLimit) {
                return;
            }
        }

        addPoints(userId, actionType, bizId, points, remark);
    }

    private void addPoints(Long userId, String actionType, String bizId, int points, String remark) {
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setActionType(actionType);
        record.setBizId(bizId);
        record.setPoints(points);
        record.setRemark(remark);
        pointsRecordMapper.insert(record);

        User user = requireUser(userId);
        user.setPoints((user.getPoints() == null ? 0 : user.getPoints()) + points);
        userMapper.updateById(user);
    }

    private boolean hasCheckedIn(Long userId, LocalDate date) {
        return userCheckinMapper.selectCount(
                new LambdaQueryWrapper<UserCheckin>()
                        .eq(UserCheckin::getUserId, userId)
                        .eq(UserCheckin::getCheckinDate, date)
        ) > 0;
    }

    private int calculateContinuousDays(Long userId, LocalDate endDate) {
        if (endDate == null) {
            return 0;
        }
        List<UserCheckin> checkins = userCheckinMapper.selectList(
                new LambdaQueryWrapper<UserCheckin>()
                        .eq(UserCheckin::getUserId, userId)
                        .le(UserCheckin::getCheckinDate, endDate)
                        .orderByDesc(UserCheckin::getCheckinDate)
        );
        int count = 0;
        LocalDate cursor = endDate;
        for (UserCheckin checkin : checkins) {
            if (checkin.getCheckinDate().isEqual(cursor)) {
                count++;
                cursor = cursor.minusDays(1);
            } else if (checkin.getCheckinDate().isBefore(cursor)) {
                break;
            }
        }
        return count;
    }

    private int sumTodayPoints(Long userId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<PointsRecord> records = pointsRecordMapper.selectList(
                new LambdaQueryWrapper<PointsRecord>()
                        .eq(PointsRecord::getUserId, userId)
                        .ge(PointsRecord::getCreateTime, start)
                        .lt(PointsRecord::getCreateTime, end)
        );
        int total = 0;
        for (PointsRecord record : records) {
            total += record.getPoints() == null ? 0 : record.getPoints();
        }
        return total;
    }

    private Integer getRank(Long userId) {
        User user = requireUser(userId);
        int points = user.getPoints() == null ? 0 : user.getPoints();
        Long higherCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getStatus, 1)
                        .eq(User::getRole, 0)
                        .gt(User::getPoints, points)
        );
        return Math.toIntExact(higherCount + 1);
    }

    private String getActionLabel(String actionType) {
        switch (actionType) {
            case ACTION_CHECKIN:
                return "每日签到";
            case ACTION_CHECKIN_BONUS:
                return "连续签到奖励";
            case ACTION_POST_CREATE:
                return "发布帖子";
            case ACTION_COMMENT_CREATE:
                return "发表评论";
            case ACTION_CHAPTER_COMPLETE:
                return "完成课程章节";
            case ACTION_WRONG_QUESTION_CREATE:
                return "添加错题";
            default:
                return actionType;
        }
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
}
