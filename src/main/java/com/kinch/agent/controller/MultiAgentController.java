package com.kinch.agent.controller;

import com.kinch.agent.service.MultiAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多Agent控制器
 * 处理多Agent协作请求
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/multi-agent")
@RequiredArgsConstructor
public class MultiAgentController {

    private final MultiAgentService multiAgentService;

    /**
     * 监督者模式
     */
    @PostMapping("/supervisor")
    public Map<String, Object> supervisor(@RequestBody Map<String, String> request) {
        String task = request.get("task");
        log.info("监督者模式 - 任务: {}", task);

        try {
            return multiAgentService.supervisorPattern(task);
        } catch (Exception e) {
            log.error("监督者模式失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    /**
     * 并行执行模式
     */
    @PostMapping("/parallel")
    public Map<String, Object> parallel(@RequestBody Map<String, List<String>> request) {
        List<String> tasks = request.get("tasks");
        log.info("并行执行模式 - 任务数: {}", tasks.size());

        try {
            return multiAgentService.parallelExecution(tasks);
        } catch (Exception e) {
            log.error("并行执行失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    /**
     * Agent间通信模式
     */
    @PostMapping("/communication")
    public Map<String, Object> communication(@RequestBody Map<String, String> request) {
        String initialMessage = request.get("message");
        log.info("Agent间通信 - 消息: {}", initialMessage);

        try {
            return multiAgentService.agentCommunication(initialMessage);
        } catch (Exception e) {
            log.error("Agent通信失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }
}



