<template>
  <div style="max-width: 360px; margin: 80px auto;">
    <h1>로그인</h1>
    <hr>
    <p v-if="error" class="error">{{ error }}</p>
    <div class="form-group">
      <label>아이디</label>
      <input v-model="loginId" type="text" @keyup.enter="login" />
    </div>
    <div class="form-group">
      <label>비밀번호</label>
      <input v-model="password" type="password" @keyup.enter="login" />
    </div>
    <button class="btn btn-primary" style="width:100%;" @click="login">로그인</button>
    <p style="margin-top:12px; text-align:center;">
      계정이 없으신가요? <router-link to="/signup">회원가입</router-link>
    </p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const loginId = ref('')
const password = ref('')
const error = ref('')
const router = useRouter()
const auth = useAuthStore()

async function login() {
  error.value = ''
  try {
    const params = new URLSearchParams()
    params.append('loginId', loginId.value)
    params.append('password', password.value)

    const res = await fetch('/login', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params,
    })

    if (!res.ok) {
      error.value = '아이디 또는 비밀번호가 올바르지 않습니다.'
      return
    }

    const userRes = await fetch('/api/users/me', { credentials: 'include' })
    if (!userRes.ok) {
      error.value = '사용자 정보를 불러올 수 없습니다.'
      return
    }

    const user = await userRes.json()
    auth.setUser(user)
    router.push('/')
  } catch (e) {
    error.value = '로그인 중 오류가 발생했습니다.'
  }
}
</script>
