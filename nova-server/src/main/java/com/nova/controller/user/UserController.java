package com.nova.controller.user;

import com.nova.constant.JwtClaimsConstant;
import com.nova.context.BaseContext;
import com.nova.dto.UserLoginDTO;
import com.nova.dto.UserRegisterDTO;
import com.nova.dto.UserUpdateDTO;
import com.nova.dto.UserWebLoginDTO;
import com.nova.entity.User;
import com.nova.properties.JwtProperties;
import com.nova.result.Result;
import com.nova.service.FileService;
import com.nova.service.UserService;
import com.nova.utils.JwtUtil;
import com.nova.vo.UserLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户端-用户相关接口")
public class UserController {

    private final UserService userService;
    private final JwtProperties jwtProperties;
    private final FileService fileService;

    @PostMapping("/login")
    @Operation(summary = "微信登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO dto) {
        User user = userService.wxLogin(dto);
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);
        UserLoginVO vo = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .name(user.getName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .token(token).build();
        return Result.success(vo);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<UserLoginVO> register(@Valid @RequestBody UserRegisterDTO dto) {
        User user = userService.register(dto);
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);
        UserLoginVO vo = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .name(user.getName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .token(token).build();
        return Result.success(vo);
    }

    @PostMapping("/webLogin")
    @Operation(summary = "网页端密码登录")
    public Result<UserLoginVO> webLogin(@Valid @RequestBody UserWebLoginDTO dto,
                                        HttpServletRequest request) {
        String clientIp = getClientIp(request);
        User user = userService.webLogin(dto, clientIp);
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);
        UserLoginVO vo = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .name(user.getName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .token(token).build();
        return Result.success(vo);
    }

    @PutMapping("/profile")
    @Operation(summary = "修改个人资料")
    public Result<UserLoginVO> updateProfile(@RequestBody UserUpdateDTO dto) {
        Long userId = BaseContext.getCurrentId();
        User user = userService.updateProfile(userId, dto);
        UserLoginVO vo = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .name(user.getName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .build();
        return Result.success(vo);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文件到OSS")
    public Result<Map<String, String>> upload(
            MultipartFile file,
            @RequestParam(defaultValue = "user") String category) {
        Map<String, String> result = fileService.upload(file, category);
        return Result.success(result);
    }

    /**
     * 从请求中提取客户端 IP，考虑反向代理场景下的 X-Forwarded-For 头部。
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
