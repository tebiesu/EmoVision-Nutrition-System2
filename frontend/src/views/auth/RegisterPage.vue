<template>
  <div class="auth-shell">
    <div class="auth-card glass-card">
      <section class="auth-hero">
        <div>
          <div class="page-eyebrow">Create Account</div>
          <h1>创建你的健康画像</h1>
          <p>注册后即可开始维护档案、记录餐次、识别食物并跟踪趋势。</p>
        </div>
      </section>
      <section class="auth-panel">
        <div class="page-eyebrow">Sign Up</div>
        <h2>注册账号</h2>
        <el-form :model="form" label-position="top">
          <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
          <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
          <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
          <el-button class="soft-button" style="width: 100%" @click="handleRegister">注册并返回登录</el-button>
        </el-form>
        <div style="margin-top: 16px; color: var(--color-text-sub)">
          已有账号？<RouterLink to="/login" style="color: var(--color-primary-deep)">返回登录</RouterLink>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { registerApi } from '@/api/auth'

const router = useRouter()
const form = reactive({ username: '', nickname: '', email: '', password: '' })

const handleRegister = async () => {
  await registerApi(form)
  ElMessage.success('注册成功，请登录')
  router.push('/login')
}
</script>
