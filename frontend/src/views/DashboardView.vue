<template>
  <div class="panel" style="margin-bottom: 20px;">
    <h2 style="margin-top: 0;">质量监控总览</h2>
    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="subtle">今日检测量</div>
        <h3>1,284</h3>
      </div>
      <div class="kpi-card">
        <div class="subtle">缺陷检出率</div>
        <h3>3.2%</h3>
      </div>
      <div class="kpi-card">
        <div class="subtle">实时报警</div>
        <h3>2</h3>
      </div>
      <div class="kpi-card">
        <div class="subtle">模型版本</div>
        <h3>v1.0.0</h3>
      </div>
    </div>
  </div>
  <div class="panel">
    <h3 style="margin-top: 0;">运行状态</h3>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="推理服务">运行中</el-descriptions-item>
      <el-descriptions-item label="边缘节点">3 / 3 在线</el-descriptions-item>
      <el-descriptions-item label="报警策略">启用</el-descriptions-item>
      <el-descriptions-item label="数据同步">正常</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup lang="ts">
</script>
