<template>
  <div>
    <h1>발주 ID: {{ stockOrderId }} 상세</h1>
    <router-link to="/order/warehouse-orders">← 목록으로 돌아가기</router-link>
    <table style="margin-top:20px;">
      <thead><tr><th>발주 그룹 ID</th><th>지점명</th><th>SKU</th><th>수량</th><th>메모</th><th>상태</th></tr></thead>
      <tbody>
        <tr v-for="d in details" :key="d.id">
          <td>{{ d.orderGroupId }}</td><td>{{ d.storeName }}</td>
          <td>{{ d.skuName }}</td><td>{{ d.quantity }}</td><td>{{ d.memo }}</td><td>{{ d.statusDescription }}</td>
        </tr>
        <tr v-if="!details.length"><td colspan="6" style="text-align:center;">내역이 없습니다.</td></tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { http } from '@/api/http'

const route = useRoute()
const stockOrderId = route.params.id
const details = ref([])
onMounted(async () => { details.value = await http.get('/api/orders/warehouse/' + stockOrderId + '/details') })
</script>
