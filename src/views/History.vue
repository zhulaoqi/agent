<template>
  <div class="history-container">
    <aside class="sidebar">
      <div class="logo">
        <el-icon :size="32"><Clock /></el-icon>
        <h2>历史记录</h2>
      </div>
    </aside>

    <main class="main-content">
      <div class="content-wrapper">
        <h1>对话历史</h1>
        <el-empty description="暂无历史记录" />
      </div>
    </main>
  </div>
</template>

<script setup>
</script>

<style scoped>
.history-container {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 240px;
  background: #2c2c2c;
  color: #ffffff;
}

.logo {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.main-content {
  flex: 1;
  background: #f5f5f5;
  padding: 40px;
}
</style>



