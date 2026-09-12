package org.example.AiSpringboot.DTO.Command;

// 文章状态变更请求 DTO

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文章上架/下架请求体
 * status: 0-草稿 1-已发布 2-已下线（前端发布传 1，下线传 2）
 */
@Data
public class ArticleStatusDTO {

    // 目标状态
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 2, message = "状态值不合法")
    private Integer status;
}
