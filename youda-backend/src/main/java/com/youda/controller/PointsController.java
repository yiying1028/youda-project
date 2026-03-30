package com.youda.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youda.common.Result;
import com.youda.service.PointsService;
import com.youda.vo.PointsOverviewVO;
import com.youda.vo.PointsRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @GetMapping("/overview")
    public Result<PointsOverviewVO> getOverview() {
        return Result.success(pointsService.getOverview());
    }

    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkIn() {
        return Result.success("签到成功", pointsService.checkIn());
    }

    @GetMapping("/ranking")
    public Result<Map<String, Object>> getRanking() {
        return Result.success(pointsService.getRanking());
    }

    @GetMapping("/records")
    public Result<IPage<PointsRecordVO>> getRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(pointsService.getRecords(current, size));
    }
}
