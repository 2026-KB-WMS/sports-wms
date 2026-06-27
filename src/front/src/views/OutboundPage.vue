<template>
  <div>
    <h1>출고 관리</h1>
    <table>
      <thead><tr><th>ID</th><th>창고</th><th>배송 지점</th><th>요청 시간</th><th>상태</th><th>상세</th></tr></thead>
      <tbody>
        <tr v-for="o in outbounds" :key="o.id">
          <td>{{ o.id }}</td><td>{{ o.warehouseName }}</td><td>{{ o.storeName }}</td>
          <td>{{ formatDate(o.stockOrderId) }}</td>
          <td>{{ o.statusDescription }}</td>
          <td><router-link :to="'/outbound/' + o.id">보기</router-link></td>
        </tr>
        <tr v-if="!outbounds.length"><td colspan="6" style="text-align:center;">출고 내역이 없습니다.</td></tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/api/http'

const outbounds = ref([])
function formatDate(d) { return d ? String(d) : '-' }

onMounted(async () => {
  outbounds.value = await http.get('/api/outbounds')
})
</script>
