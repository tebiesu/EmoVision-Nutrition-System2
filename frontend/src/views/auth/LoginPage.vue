<template>
  <div class="auth-shell">
    <div class="auth-card glass-card">
      <section class="auth-hero">
        <div>
          <div class="page-eyebrow">Natural Healing UI</div>
          <h1>回到你的饮食与情绪节律</h1>
          <p>从每一餐、每一个情绪片段，构建可解释的健康闭环。</p>
        </div>
        <div>
          <div class="badge-chip">识别 / 评估 / 建议 / 趋势</div>
        </div>
      </section>
      <section class="auth-panel">
        <div class="page-eyebrow">Sign In</div>
        <h2>登录系统</h2>
        <el-form :model="form" label-position="top" @submit.prevent="handleSubmit">
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-button class="soft-button" style="width: 100%" @click="handleSubmit">登录并进入工作台</el-button>
        </el-form>
        <div style="margin-top: 16px; color: var(--color-text-sub)">
          还没有账号？<RouterLink to="/register" style="color: var(--color-primary-deep)">去注册</RouterLink>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const form = reactive({ username: 'demo', password: '123456' })

const handleSubmit = async () => {
  await authStore.login(form)
  ElMessage.success('登录成功')
  router.push('/dashboard')
}
</script>
