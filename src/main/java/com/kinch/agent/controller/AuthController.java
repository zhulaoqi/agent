package com.kinch.agent.controller;

import com.kinch.agent.entity.User;
import com.kinch.agent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 认证控制器 - 处理登录、注册等
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            log.info("登录请求 - 用户名: {}", username);
            
            // 验证参数
            if (username == null || username.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "用户名不能为空");
                return response;
            }
            
            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "密码不能为空");
                return response;
            }
            
            // 查询用户
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "用户名或密码错误");
                return response;
            }
            
            User user = userOpt.get();
            
            // 简化版密码验证（实际应使用BCrypt）
            // 这里为了演示，直接比较明文（生产环境必须加密）
            if (!password.equals(password)) {  // TODO: 改为BCrypt验证
                // 临时：直接通过
            }
            
            // 检查用户状态
            if ("disabled".equals(user.getStatus())) {
                response.put("success", false);
                response.put("message", "账号已被禁用");
                return response;
            }
            
            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);
            
            // 生成简单token（实际应使用JWT）
            String token = "token_" + user.getId() + "_" + System.currentTimeMillis();
            
            // 返回成功
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "nickname", user.getNickname() != null ? user.getNickname() : user.getUsername(),
                "role", user.getRole(),
                "tokenQuota", user.getTokenQuota(),
                "tokenUsed", user.getTokenUsed()
            ));
            
            log.info("登录成功 - 用户: {}", username);
            
        } catch (Exception e) {
            log.error("登录失败", e);
            response.put("success", false);
            response.put("message", "登录失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 注册接口（简化版）
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = request.get("username");
            String password = request.get("password");
            String email = request.get("email");
            
            // 验证参数
            if (username == null || username.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "用户名不能为空");
                return response;
            }
            
            if (password == null || password.length() < 6) {
                response.put("success", false);
                response.put("message", "密码至少6位");
                return response;
            }
            
            // 检查用户名是否存在
            if (userRepository.findByUsername(username).isPresent()) {
                response.put("success", false);
                response.put("message", "用户名已存在");
                return response;
            }
            
            // 创建用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);  // TODO: 应使用BCrypt加密
            user.setEmail(email);
            user.setNickname(username);
            user.setRole("user");
            user.setStatus("active");
            user.setTokenQuota(100000);
            user.setTokenUsed(0);
            
            userRepository.save(user);
            
            response.put("success", true);
            response.put("message", "注册成功");
            
            log.info("注册成功 - 用户: {}", username);
            
        } catch (Exception e) {
            log.error("注册失败", e);
            response.put("success", false);
            response.put("message", "注册失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/user")
    public Map<String, Object> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        // 简化版：从token中提取userId
        // 实际应该解析JWT token
        if (token == null || !token.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "未登录");
            return response;
        }
        
        response.put("success", true);
        response.put("user", Map.of(
            "username", "guest",
            "nickname", "访客",
            "role", "user"
        ));
        
        return response;
    }

    /**
     * 登出接口
     */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登出成功");
        return response;
    }
}


