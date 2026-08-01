package com.nova.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nova.dto.UserLoginDTO;
import com.nova.dto.UserRegisterDTO;
import com.nova.dto.UserUpdateDTO;
import com.nova.dto.UserWebLoginDTO;
import com.nova.entity.User;
import com.nova.exception.BaseException;
import com.nova.mapper.UserMapper;
import com.nova.properties.WeChatProperties;
import com.nova.service.UserService;
import com.nova.utils.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final WeChatProperties weChatProperties;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String IP_LIMIT_KEY = "login:fail:ip:";
    private static final int MAX_IP_ATTEMPTS = 10;
    private static final long IP_LIMIT_SECONDS = 60;

    @Override
    public User wxLogin(UserLoginDTO dto) {
        Map<String, String> param = new HashMap<>();
        param.put("appid", weChatProperties.getAppid());
        param.put("secret", weChatProperties.getSecret());
        param.put("js_code", dto.getCode());
        param.put("grant_type", "authorization_code");

        String resp;
        try {
            resp = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/jscode2session", param);
        } catch (RuntimeException e) {
            log.error("调用微信接口失败", e);
            throw new BaseException("微信登录失败，无法连接微信服务器：" + e.getMessage());
        }
        JSONObject json = JSONObject.parseObject(resp);
        String openid = json.getString("openid");
        if (openid == null) {
            throw new BaseException("微信登录失败：" + resp);
        }

        User user = userMapper.getByOpenid(openid);
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        return user;
    }

    @Override
    public User register(UserRegisterDTO dto) {
        User existing = userMapper.getByPhone(dto.getPhone());
        if (existing != null) {
            throw new BaseException("该手机号已注册");
        }

        User user = User.builder()
                .openid("web_" + dto.getPhone())
                .name(dto.getName())
                .phone(dto.getPhone())
                .password(passwordEncoder.encode(dto.getPassword()))
                .createTime(LocalDateTime.now())
                .build();
        userMapper.insert(user);
        return user;
    }

    @Override
    public User webLogin(UserWebLoginDTO dto, String clientIp) {
        // 检查 IP 限频
        if (clientIp != null && !clientIp.isBlank()) {
            String count = stringRedisTemplate.opsForValue().get(IP_LIMIT_KEY + clientIp);
            if (count != null && Integer.parseInt(count) >= MAX_IP_ATTEMPTS) {
                log.warn("用户端 IP 被限流: {}", clientIp);
                throw new BaseException("操作过于频繁，请稍后重试");
            }
        }

        User user = userMapper.getByPhone(dto.getPhone());
        if (user == null) {
            recordIpFailure(clientIp);
            throw new BaseException("账号不存在");
        }
        if (user.getPassword() == null) {
            throw new BaseException("该账号未设置密码，请使用微信登录");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            recordIpFailure(clientIp);
            throw new BaseException("密码错误");
        }
        return user;
    }

    private void recordIpFailure(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) return;
        Long count = stringRedisTemplate.opsForValue().increment(IP_LIMIT_KEY + clientIp);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(IP_LIMIT_KEY + clientIp, IP_LIMIT_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    public User updateProfile(Long userId, UserUpdateDTO dto) {
        User updateUser = User.builder()
                .id(userId)
                .name(dto.getName())
                .avatar(dto.getAvatar())
                .build();
        userMapper.update(updateUser);
        return userMapper.getById(userId);
    }
}
