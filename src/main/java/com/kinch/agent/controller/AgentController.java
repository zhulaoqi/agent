package com.kinch.agent.controller;

import com.kinch.agent.entity.Message;
import com.kinch.agent.service.AgentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent对话控制器
 * 处理普通对话请求
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    /**
     * 普通对话（非流式）
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String message = request.get("message");
        String conversationIdStr = request.get("conversationId");

        log.info("接收到对话请求: {}", message);

        Map<String, Object> response = new HashMap<>();

        try {
            // 使用默认用户ID
            Long userId = 1L;

            // 解析会话ID（可选）
            Long conversationId = null;
            if (conversationIdStr != null && !conversationIdStr.isEmpty()) {
                try {
                    conversationId = Long.parseLong(conversationIdStr);
                } catch (NumberFormatException e) {
                    log.warn("无效的conversationId: {}", conversationIdStr);
                }
            }

            // 获取IP地址
            String ipAddress = getClientIP(httpRequest);

            // 调用AgentService处理对话 - 传入所有4个参数
            Message replyMessage = agentService.chat(userId, message, conversationId, ipAddress);

            response.put("success", true);
            response.put("message", replyMessage.getContent());
            response.put("conversationId", replyMessage.getConversationId());
            response.put("messageId", replyMessage.getId());
            log.info("对话处理成功");

        } catch (Exception e) {
            log.error("对话处理失败", e);
            response.put("success", false);
            response.put("message", "对话处理失败: " + e.getMessage());
        }

        return response;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

