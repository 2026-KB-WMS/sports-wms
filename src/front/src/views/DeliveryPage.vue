<template>
  <div>
    <h1>배송 현황</h1>
    <p style="color:#6c757d; font-size:0.9em;">배송이 도착하면 "수령 완료" 버튼을 눌러주세요.</p>
    <p v-if="error" class="error">{{ error }}</p>
    <table>
      <thead>
        <tr><th>출고 ID</th><th>출발 창고</th><th>발주 요청 시간</th><th>품목</th><th>배송 상태</th><th>수령 확인</th></tr>
      </thead>
      <tbody>
        <tr v-for="o in outbounds" :key="o.id">
          <td>{{ o.id }}</td>
          <td>{{ o.warehouseName }}</td>
          <td>{{ o.stockOrderId }}</td>
          <td><router-link :to="'/delivery/' + o.id">보기</router-link></td>
          <td><span :class="'badge badge-' + o.status">{{ o.statusDescription }}</span></td>
          <td>
            <button v-if="o.status === 'SHIPPED'" class="btn btn-success btn-sm"
                    @click="deliver(o.id)">수령 완료</button>
            <span v-else style="color:#6c757d;">—</span>
          </td>
        </tr>
        <tr v-if="!outbounds.length"><td colspan="6" style="text-align:center;">배송 내역이 없습니다.</td></tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/api/http'

const outbounds = ref([])
const error = ref('')

async function load() { outbounds.value = await http.get('/api/outbounds') }

async function deliver(id) {
  if (!confirm('상품을 수령하셨습니까? 배송 완료로 처리됩니다.')) return
  try { await http.patch('/api/outbounds/' + id + '/deliver'); await load() }
  catch (e) { error.value = e.message }
}

onMounted(load)
</script>

<style scoped>
.badge { padding: 3px 10px; border-radius: 12px; font-size: 0.85em; font-weight: bold; }
.badge-ASSIGNED  { background: #cfe2ff; color: #084298; }
.badge-APPROVED  { background: #d1e7dd; color: #0a3622; }
.badge-PICKING   { background: #fff3cd; color: #664d03; }
.badge-PACKING   { background: #fff3cd; color: #664d03; }
.badge-SHIPPED   { background: #f0d9ff; color: #4a0072; }
.badge-DELIVERED { background: #d1e7dd; color: #0a3622; }
</style>
