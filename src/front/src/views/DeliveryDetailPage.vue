<template>
  <div>
    <h1>배송 상세 — 출고 ID: {{ outboundId }}</h1>
    <router-link to="/delivery">← 배송 현황으로 돌아가기</router-link>
    <table style="margin-top:20px;">
      <thead><tr><th>상세 ID</th><th>품목명</th><th>수량</th></tr></thead>
      <tbody>
        <tr v-for="d in details" :key="d.id">
          <td>{{ d.id }}</td><td>{{ d.skuName }}</td><td>{{ d.quantity }}</td>
        </tr>
        <tr v-if="!details.length"><td colspan="3" style="text-align:center;">상세 내역이 없습니다.</td></tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { http } from '@/api/http'

const route = useRoute()
const outboundId = route.params.id
const details = ref([])

onMounted(async () => {
  details.value = await http.get('/api/outbounds/' + outboundId + '/details')
})
</script>
