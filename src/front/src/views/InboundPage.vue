<template>
  <div>
    <h1>입고 관리</h1>
    <div style="display: flex; gap: 40px;">

      <!-- 신규 입고 폼 (창고 관리자) -->
      <div v-if="auth.isWarehouseManager" style="width: 400px;">
        <h2>신규 입고 요청</h2>
        <p v-if="error" class="error">{{ error }}</p>
        <div class="form-group">
          <label>창고 선택</label>
          <select v-model="form.warehouseId">
            <option value="">-- 창고를 선택하세요 --</option>
            <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
          </select>
        </div>
        <h4>입고 품목</h4>
        <div v-for="(item, i) in form.items" :key="i" style="display:flex; gap:8px; margin-bottom:8px;">
          <select v-model="item.skuId" style="flex:3;">
            <option value="">SKU 선택</option>
            <option v-for="s in skus" :key="s.id" :value="s.id">{{ s.name }} ({{ s.skuCode }})</option>
          </select>
          <input v-model.number="item.quantity" type="number" placeholder="수량" style="flex:1; width:80px;" />
          <button class="btn btn-danger btn-sm" @click="removeItem(i)">삭제</button>
        </div>
        <button class="btn" @click="addItem">품목 추가</button>
        <br><br>
        <button class="btn btn-primary" @click="submitInbound">입고 요청 생성</button>
      </div>

      <!-- 입고 목록 -->
      <div style="flex: 1;">
        <h2>입고 요청 내역</h2>
        <table>
          <thead><tr><th>ID</th><th>창고</th><th>요청 시간</th><th>상태</th><th>상세</th></tr></thead>
          <tbody>
            <tr v-for="ib in inbounds" :key="ib.id">
              <td>{{ ib.id }}</td>
              <td>{{ ib.warehouseName }}</td>
              <td>{{ formatDate(ib.requestTime) }}</td>
              <td>{{ ib.statusDescription }}</td>
              <td><router-link :to="'/inbound/' + ib.id">보기</router-link></td>
            </tr>
            <tr v-if="!inbounds.length"><td colspan="5" style="text-align:center;">입고 내역이 없습니다.</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const inbounds = ref([])
const warehouses = ref([])
const skus = ref([])
const error = ref('')
const form = ref({ warehouseId: '', items: [{ skuId: '', quantity: 1 }] })

function addItem() { form.value.items.push({ skuId: '', quantity: 1 }) }
function removeItem(i) { form.value.items.splice(i, 1) }
function formatDate(d) { return d ? d.replace('T', ' ').substring(0, 16) : '-' }

async function load() {
  const warehouseUrl = auth.isWarehouseManager ? '/api/warehouses/my' : '/api/warehouses'
  const [ibs, ws, ss] = await Promise.all([
    http.get('/api/inbounds'),
    http.get(warehouseUrl),
    http.get('/api/products/skus'),
  ])
  inbounds.value = ibs
  warehouses.value = ws
  skus.value = ss
}

async function submitInbound() {
  error.value = ''
  const skuIds = form.value.items.map(i => i.skuId).filter(Boolean)
  if (new Set(skuIds).size !== skuIds.length) {
    error.value = '동일한 SKU를 중복 선택할 수 없습니다.'
    return
  }
  try {
    await http.post('/api/inbounds', form.value)
    form.value = { warehouseId: '', items: [{ skuId: '', quantity: 1 }] }
    await load()
  } catch (e) { error.value = e.message }
}

onMounted(load)
</script>
