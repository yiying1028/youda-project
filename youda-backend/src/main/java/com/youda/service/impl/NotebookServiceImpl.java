package com.youda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youda.common.BusinessException;
import com.youda.dto.WrongQuestionSaveDTO;
import com.youda.entity.Grade;
import com.youda.entity.Subject;
import com.youda.entity.WrongQuestion;
import com.youda.mapper.GradeMapper;
import com.youda.mapper.SubjectMapper;
import com.youda.mapper.WrongQuestionMapper;
import com.youda.service.NotebookService;
import com.youda.service.PointsService;
import com.youda.utils.UserContext;
import com.youda.vo.NotebookStatsVO;
import com.youda.vo.WrongQuestionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class NotebookServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion> implements NotebookService {

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private PointsService pointsService;

    @Override
    public IPage<WrongQuestionVO> getQuestionPage(Integer current, Integer size, Long subjectId, Long gradeId, String keyword) {
        Long userId = UserContext.getCurrentUserId();
        Page<WrongQuestion> page = new Page<>(current, size);
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId)
                .orderByDesc(WrongQuestion::getCreateTime);
        if (subjectId != null) {
            wrapper.eq(WrongQuestion::getSubjectId, subjectId);
        }
        if (gradeId != null) {
            wrapper.eq(WrongQuestion::getGradeId, gradeId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(q -> q.like(WrongQuestion::getQuestionContent, keyword)
                    .or()
                    .like(WrongQuestion::getCorrectAnswer, keyword));
        }

        IPage<WrongQuestion> questionPage = this.page(page, wrapper);
        return questionPage.convert(this::buildVO);
    }

    @Override
    @Transactional
    public Long createQuestion(WrongQuestionSaveDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        WrongQuestion question = new WrongQuestion();
        question.setUserId(userId);
        question.setSubjectId(dto.getSubjectId());
        question.setGradeId(dto.getGradeId());
        question.setQuestionContent(dto.getQuestionContent());
        question.setQuestionImage(dto.getQuestionImage());
        question.setMyAnswer(dto.getMyAnswer());
        question.setCorrectAnswer(dto.getCorrectAnswer());
        question.setErrorReason(dto.getErrorReason());
        question.setMasteryStatus(0);
        this.save(question);
        pointsService.rewardWrongQuestionCreate(userId, question.getId());
        return question.getId();
    }

    @Override
    public void updateQuestion(Long id, WrongQuestionSaveDTO dto) {
        WrongQuestion question = requireOwnedQuestion(id);
        question.setSubjectId(dto.getSubjectId());
        question.setGradeId(dto.getGradeId());
        question.setQuestionContent(dto.getQuestionContent());
        question.setQuestionImage(dto.getQuestionImage());
        question.setMyAnswer(dto.getMyAnswer());
        question.setCorrectAnswer(dto.getCorrectAnswer());
        question.setErrorReason(dto.getErrorReason());
        this.updateById(question);
    }

    @Override
    public void deleteQuestion(Long id) {
        requireOwnedQuestion(id);
        this.removeById(id);
    }

    @Override
    public WrongQuestionVO getRandomReviewQuestion(Long subjectId) {
        Long userId = UserContext.getCurrentUserId();
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId)
                .ne(WrongQuestion::getMasteryStatus, 1);
        if (subjectId != null) {
            wrapper.eq(WrongQuestion::getSubjectId, subjectId);
        }
        List<WrongQuestion> questions = this.list(wrapper);
        if (questions.isEmpty()) {
            return null;
        }
        WrongQuestion question = questions.get(ThreadLocalRandom.current().nextInt(questions.size()));
        return buildVO(question);
    }

    @Override
    public void updateMasteryStatus(Long id, Integer masteryStatus) {
        if (masteryStatus == null || masteryStatus < 0 || masteryStatus > 2) {
            throw new BusinessException("掌握状态不合法");
        }
        WrongQuestion question = requireOwnedQuestion(id);
        question.setMasteryStatus(masteryStatus);
        this.updateById(question);
    }

    @Override
    public NotebookStatsVO getStats() {
        Long userId = UserContext.getCurrentUserId();
        long totalCount = this.count(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId));
        long masteredCount = this.count(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId)
                .eq(WrongQuestion::getMasteryStatus, 1));

        NotebookStatsVO vo = new NotebookStatsVO();
        vo.setTotalCount(totalCount);
        vo.setMasteredCount(masteredCount);
        vo.setReviewCount(totalCount - masteredCount);
        return vo;
    }

    private WrongQuestion requireOwnedQuestion(Long id) {
        Long userId = UserContext.getCurrentUserId();
        WrongQuestion question = this.getById(id);
        if (question == null) {
            throw new BusinessException("错题不存在");
        }
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该错题");
        }
        return question;
    }

    private WrongQuestionVO buildVO(WrongQuestion question) {
        WrongQuestionVO vo = new WrongQuestionVO();
        vo.setId(question.getId());
        vo.setSubjectId(question.getSubjectId());
        vo.setGradeId(question.getGradeId());
        vo.setQuestionContent(question.getQuestionContent());
        vo.setQuestionImage(question.getQuestionImage());
        vo.setMyAnswer(question.getMyAnswer());
        vo.setCorrectAnswer(question.getCorrectAnswer());
        vo.setErrorReason(question.getErrorReason());
        vo.setMasteryStatus(question.getMasteryStatus());
        vo.setMasteryStatusText(getMasteryStatusText(question.getMasteryStatus()));
        vo.setCreateTime(question.getCreateTime());

        Subject subject = subjectMapper.selectById(question.getSubjectId());
        if (subject != null) {
            vo.setSubjectName(subject.getName());
        }
        Grade grade = gradeMapper.selectById(question.getGradeId());
        if (grade != null) {
            vo.setGradeName(grade.getName());
        }
        return vo;
    }

    private String getMasteryStatusText(Integer masteryStatus) {
        if (masteryStatus == null || masteryStatus == 0) {
            return "待复习";
        }
        if (masteryStatus == 1) {
            return "已掌握";
        }
        return "仍不会";
    }
}
