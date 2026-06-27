<template>
  <div>
    <h1>내 창고 발주 목록</h1>
    <table>
      <thead><tr><th>발주 ID</th><th>창고명</th><th>요청 시간</th><th>완료 시간</th><th>상태</th><th>상세 보기</th></tr></thead>
      <tbody>
        <tr v-for="o in orders" :key="o.id">
          <td>{{ o.id }}</td><td>{{ o.warehouseName }}</td>
          <td>{{ formatDate(o.requestTime) }}</td>
          <td>{{ o.completeTime ? formatDate(o.completeTime) : '(미완료)' }}</td>
          <td>{{ o.statusDescription }}</td>          <td><router-link :to="'/order/warehouse-orders/' + o.id">상세 보기</router-link></td>
        </tr>
        <tr v-if="!orders.length"><td colspan="6" style="text-align:center;">배정된 발주 요청이 없습니다.</td></tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/api/http'

const orders = ref([])
function formatDate(d) { return d ? d.replace('T', ' ').substring(0, 16) : '-' }
onMounted(async () => { orders.value = await http.get('/api/orders/warehouse') })
</script>
