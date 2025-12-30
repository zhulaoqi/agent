#!/bin/bash

# 重启前端开发服务器脚本

echo "🛑 停止前端服务..."
pkill -f "vite" 2>/dev/null || echo "   没有运行的Vite进程"

sleep 1

echo ""
echo "🚀 启动前端服务..."
echo ""
echo "======================================"
echo "  前端代码已更新！"
echo "  请在浏览器中按 Ctrl+Shift+R 强制刷新"
echo "======================================"
echo ""

npm run dev


