package com.youda.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youda.dto.WrongQuestionSaveDTO;
import com.youda.vo.NotebookStatsVO;
import com.youda.vo.WrongQuestionVO;

public interface NotebookService {

    IPage<WrongQuestionVO> getQuestionPage(Integer current, Integer size, Long subjectId, Long gradeId, String keyword);

    Long createQuestion(WrongQuestionSaveDTO dto);

    void updateQuestion(Long id, WrongQuestionSaveDTO dto);

    void deleteQuestion(Long id);

    WrongQuestionVO getRandomReviewQuestion(Long subjectId);

    void updateMasteryStatus(Long id, Integer masteryStatus);

    NotebookStatsVO getStats();
}
