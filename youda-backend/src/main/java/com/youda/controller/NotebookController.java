package com.youda.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youda.common.Result;
import com.youda.dto.WrongQuestionSaveDTO;
import com.youda.service.NotebookService;
import com.youda.vo.NotebookStatsVO;
import com.youda.vo.WrongQuestionVO;
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

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notebook")
public class NotebookController {

    @Autowired
    private NotebookService notebookService;

    @GetMapping("/list")
    public Result<IPage<WrongQuestionVO>> getQuestionPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) String keyword) {
        return Result.success(notebookService.getQuestionPage(current, size, subjectId, gradeId, keyword));
    }

    @PostMapping
    public Result<Map<String, Long>> createQuestion(@Valid @RequestBody WrongQuestionSaveDTO dto) {
        Long id = notebookService.createQuestion(dto);
        Map<String, Long> data = new HashMap<>();
        data.put("id", id);
        return Result.success("保存成功", data);
    }

    @PutMapping("/{id}")
    public Result<?> updateQuestion(@PathVariable Long id, @Valid @RequestBody WrongQuestionSaveDTO dto) {
        notebookService.updateQuestion(id, dto);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteQuestion(@PathVariable Long id) {
        notebookService.deleteQuestion(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/review/random")
    public Result<WrongQuestionVO> getRandomReviewQuestion(@RequestParam(required = false) Long subjectId) {
        return Result.success(notebookService.getRandomReviewQuestion(subjectId));
    }

    @PutMapping("/{id}/mastery-status")
    public Result<?> updateMasteryStatus(@PathVariable Long id, @RequestParam Integer status) {
        notebookService.updateMasteryStatus(id, status);
        return Result.success("状态已更新", null);
    }

    @GetMapping("/stats")
    public Result<NotebookStatsVO> getStats() {
        return Result.success(notebookService.getStats());
    }
}
