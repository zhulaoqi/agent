package com.kinch.agent.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 数据库查询工具
 * 安全地执行SQL查询（只读）
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseQueryTool implements Function<String, String> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public String apply(String sql) {
        log.info("执行数据库查询: {}", sql);

        try {
            // 安全检查：只允许SELECT查询
            String sqlLower = sql.trim().toLowerCase();
            if (!sqlLower.startsWith("select")) {
                return "❌ 安全限制：只允许执行SELECT查询";
            }

            // 检查危险关键词
            if (sqlLower.contains("drop") || sqlLower.contains("delete") || 
                sqlLower.contains("update") || sqlLower.contains("insert")) {
                return "❌ 安全限制：检测到危险SQL关键词";
            }

            // 执行查询
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            if (results.isEmpty()) {
                return "查询结果为空";
            }

            // 格式化结果
            StringBuilder output = new StringBuilder();
            output.append("查询成功，共").append(results.size()).append("条记录：\n\n");

            // 最多显示10条记录
            int limit = Math.min(results.size(), 10);
            for (int i = 0; i < limit; i++) {
                Map<String, Object> row = results.get(i);
                output.append("记录 ").append(i + 1).append(":\n");
                row.forEach((key, value) -> 
                    output.append("  ").append(key).append(": ").append(value).append("\n")
                );
                output.append("\n");
            }

            if (results.size() > 10) {
                output.append("... 还有 ").append(results.size() - 10).append(" 条记录未显示");
            }

            return output.toString();

        } catch (Exception e) {
            log.error("数据库查询失败", e);
            return "❌ 查询失败: " + e.getMessage();
        }
    }
}

