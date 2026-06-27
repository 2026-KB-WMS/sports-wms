<template>
  <div>
    <h1>WMS 홈</h1>
    <hr>
    <p>안녕하세요, <strong>{{ auth.user?.name }}</strong>님</p>

    <div style="display: flex; gap: 12px; flex-wrap: wrap; margin-top: 20px;">
      <template v-if="auth.isGeneralManager">
        <router-link to="/order"><button class="btn btn-primary">발주 요청 조회</button></router-link>
        <router-link to="/product"><button class="btn btn-primary">상품 조회/등록</button></router-link>
        <router-link to="/sku"><button class="btn btn-primary">SKU 조회/등록</button></router-link>
        <router-link to="/inbound"><button class="btn btn-primary">입고 관리</button></router-link>
        <router-link to="/outbound"><button class="btn btn-primary">출고 관리</button></router-link>
        <router-link to="/warehouse"><button class="btn btn-primary">창고 조회</button></router-link>
        <router-link to="/inventory"><button class="btn btn-primary">재고 조회</button></router-link>
        <router-link to="/store"><button class="btn btn-primary">지점 조회</button></router-link>
      </template>

      <template v-if="auth.isWarehouseManager">
        <router-link to="/order/warehouse-orders"><button class="btn btn-primary">발주 요청 조회</button></router-link>
        <router-link to="/inbound"><button class="btn btn-primary">입고 관리</button></router-link>
        <router-link to="/outbound"><button class="btn btn-primary">출고 관리</button></router-link>
        <router-link to="/inventory"><button class="btn btn-primary">재고 조회</button></router-link>
      </template>

      <template v-if="auth.isUser">
        <router-link to="/order"><button class="btn btn-primary">발주 등록 및 내역 조회</button></router-link>
        <router-link to="/delivery"><button class="btn btn-primary">배송 조회</button></router-link>
      </template>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()
</script>
