<template>
  <div style="max-width: 400px; margin: 60px auto;">
    <h1>회원가입</h1>
    <hr>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="success" style="color:green;">{{ success }}</p>
    <div class="form-group"><label>아이디</label><input v-model="form.loginId" type="text" /></div>
    <div class="form-group"><label>비밀번호</label><input v-model="form.password" type="password" /></div>
    <div class="form-group"><label>이름</label><input v-model="form.name" type="text" /></div>
    <div class="form-group"><label>이메일</label><input v-model="form.email" type="email" /></div>
    <div class="form-group"><label>전화번호</label><input v-model="form.phoneNum" type="text" /></div>
    <div class="form-group"><label>주소</label><input v-model="form.address" type="text" /></div>
    <button class="btn btn-primary" style="width:100%;" @click="signup">가입하기</button>
    <p style="margin-top:12px; text-align:center;"><router-link to="/login">로그인으로 돌아가기</router-link></p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { http } from '@/api/http'

const form = ref({ loginId: '', password: '', name: '', email: '', phoneNum: '', address: '' })
const error = ref('')
const success = ref('')

async function signup() {
  error.value = ''; success.value = ''
  try {
    await http.post('/api/users/signup', form.value)
    success.value = '가입이 완료됐습니다. 관리자 승인 후 로그인 가능합니다.'
    form.value = { loginId: '', password: '', name: '', email: '', phoneNum: '', address: '' }
  } catch (e) {
    error.value = e.message
  }
}
</script>
