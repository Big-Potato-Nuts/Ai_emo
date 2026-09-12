package org.example.AiSpringboot.Controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.example.AiSpringboot.Common.Result;
import org.example.AiSpringboot.DTO.Response.FileUploadResponseDTO;
import org.example.AiSpringboot.Service.FileService;
import org.example.AiSpringboot.Util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 * 提供文章封面等图片上传接口，登录用户可用
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 上传文件（multipart/form-data）
     * 请求参数：
     * - file:         上传的文件（表单文件字段）
     * - businessType: 业务类型，如 ARTICLE（文章）
     * - businessId:   业务对象ID，如文章ID（前端传 UUID）
     * - businessField:业务字段名，如 cover（封面）
     * 返回：{filePath: "/uploads/xxx.png"}，前端拼接 fileBaseUrl 后访问
     *
     * @param file          上传的文件
     * @param businessType  业务类型
     * @param businessId    业务对象ID
     * @param businessField 业务字段名
     * @return 上传结果（含 filePath）
     */
    @PostMapping("/upload")
    public Result<FileUploadResponseDTO> upload(@RequestParam("file") MultipartFile file,
                                                @RequestParam(required = false) String businessType,
                                                @RequestParam(required = false) String businessId,
                                                @RequestParam(required = false) String businessField) {
        // 1. 从 JWT 解析当前用户ID（记录文件上传人）
        DecodedJWT jwt = JwtTokenUtil.verifyToken(JwtTokenUtil.getCurrentToken());
        Long userId = jwt.getClaim("userId").asLong();
        // 2. 调用服务层保存文件并登记元信息
        return Result.ok(fileService.upload(file, businessType, businessId, businessField, userId));
    }
}
