<template>
  <div>
    <nav v-if="auth.isLoggedIn" style="padding: 10px; border-bottom: 1px solid #ccc; margin-bottom: 20px; display: flex; gap: 12px; flex-wrap: wrap;">
      <strong>WMS</strong>
      <span style="color:#888">|</span>

      <template v-if="auth.isGeneralManager">
        <router-link to="/order">발주 조회</router-link>
        <router-link to="/product">상품 관리</router-link>
        <router-link to="/sku">SKU 관리</router-link>
        <router-link to="/inbound">입고 관리</router-link>
        <router-link to="/outbound">출고 관리</router-link>
        <router-link to="/warehouse">창고 관리</router-link>
        <router-link to="/inventory">재고 조회</router-link>
        <router-link to="/store">지점 관리</router-link>
      </template>

      <template v-if="auth.isWarehouseManager">
        <router-link to="/order/warehouse-orders">발주 조회</router-link>
        <router-link to="/inbound">입고 관리</router-link>
        <router-link to="/outbound">출고 관리</router-link>
        <router-link to="/inventory">재고 조회</router-link>
      </template>

      <template v-if="auth.isUser">
        <router-link to="/order">발주 등록/내역</router-link>
        <router-link to="/delivery">배송 조회</router-link>
      </template>

      <span style="margin-left: auto;">{{ auth.user?.name }}</span>
      <button @click="logout" style="cursor: pointer;">로그아웃</button>
    </nav>

    <div style="padding: 0 20px;">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

async function logout() {
  await fetch('/logout', { method: 'POST', credentials: 'include' })
  auth.clearUser()
  router.push('/login')
}
</script>

<style>
* { box-sizing: border-box; }
body { font-family: sans-serif; margin: 0; }
a { color: #007bff; text-decoration: none; }
a:hover { text-decoration: underline; }
table { width: 100%; border-collapse: collapse; margin-top: 10px; margin-bottom: 20px; }
th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
th { background-color: #f2f2f2; }
.btn { padding: 6px 14px; border: none; border-radius: 4px; cursor: pointer; font-size: 0.9em; }
.btn-primary { background-color: #007bff; color: white; }
.btn-success { background-color: #28a745; color: white; }
.btn-danger  { background-color: #dc3545; color: white; }
.btn-sm { padding: 4px 10px; font-size: 0.85em; }
.error { color: red; margin: 8px 0; }
.form-group { margin-bottom: 12px; }
.form-group label { display: block; margin-bottom: 4px; font-weight: bold; }
.form-group input,
.form-group select { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
</style>
