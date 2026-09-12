package org.example.AiSpringboot.Controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.example.AiSpringboot.Common.Result;
import org.example.AiSpringboot.DTO.Response.AnalyticsOverviewDTO;
import org.example.AiSpringboot.Exception.BusinessException;
import org.example.AiSpringboot.Service.DataAnalyticsService;
import org.example.AiSpringboot.Util.JwtTokenUtil;
import org.example.AiSpringboot.enumClass.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据分析控制器
 * 提供后台数据看板接口（仅管理员）
 */
@RestController
@RequestMapping("/api/data-analytics")
public class DataAnalyticsController {

    @Autowired
    private DataAnalyticsService dataAnalyticsService;

    /**
     * 查询后台数据总览
     * 返回结构（对应 dashboard.vue）：
     * - systemOverview: 总用户/活跃用户/日记总数/今日日记/会话总数/今日会话/平均情绪
     * - consultationStats: 会话统计 + 近7天每日咨询趋势
     * - emotionTrend: 近7天情绪趋势
     * - userActivity: 近7天用户活跃度
     *
     * @return 数据总览 DTO
     */
    @GetMapping("/overview")
    public Result<AnalyticsOverviewDTO> overview() {
        // 1. 校验管理员权限（数据看板仅管理员可见）
        DecodedJWT jwt = JwtTokenUtil.verifyToken(JwtTokenUtil.getCurrentToken());
        Integer roleType = jwt.getClaim("roleType").asInt();
        if (!UserType.ADMIN.getCode().equals(roleType)) {
            throw new BusinessException("无权限访问，仅管理员可操作");
        }
        // 2. 调用服务层聚合统计并返回
        return Result.ok(dataAnalyticsService.overview());
    }
}
