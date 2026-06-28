<template>
  <div>
    <h1>재고 관리</h1>
    <hr>
    <!-- 필터 -->
    <div style="display:flex; gap:12px; align-items:flex-end; margin-bottom:20px; flex-wrap:wrap;">
      <div class="form-group" style="margin:0;">
        <label>창고별</label>
        <select v-model="filter.warehouseId" @change="onWarehouseChange">
          <option value="">전체 창고</option>
          <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
        </select>
      </div>
      <div class="form-group" style="margin:0;">
        <label>구역별</label>
        <select v-model="filter.sectionId">
          <option value="">전체 구역</option>
          <option v-for="s in filteredSections" :key="s.id" :value="s.id">{{ s.warehouseName }} - {{ s.name }}</option>
        </select>
      </div>
      <div class="form-group" style="margin:0;">
        <label>SKU별</label>
        <select v-model="filter.skuId">
          <option value="">전체 SKU</option>
          <option v-for="s in skus" :key="s.id" :value="s.id">{{ s.skuCode }}</option>
        </select>
      </div>
      <button class="btn btn-primary" @click="loadInventory">조회</button>
      <button class="btn" @click="resetFilter">초기화</button>
    </div>

    <h2>재고 현황</h2>
    <table>
      <thead><tr><th>재고 ID</th><th>소속 창고</th><th>보관 구역</th><th>SKU 코드</th><th>실재고</th><th>할당</th><th>가용</th></tr></thead>
      <tbody>
        <tr v-for="i in inventories" :key="i.id">
          <td>{{ i.id }}</td><td>{{ i.warehouseName }}</td><td>{{ i.sectionName }}</td>
          <td>{{ i.skuName }}</td><td>{{ i.actualQuantity }}</td><td>{{ i.allocatedQuantity }}</td><td>{{ i.availableQuantity }}</td>
        </tr>
        <tr v-if="!inventories.length"><td colspan="7" style="text-align:center;">조회된 재고가 없습니다.</td></tr>
      </tbody>
    </table>

    <h2>재고 변동 기록</h2>
    <table>
      <thead><tr><th>ID</th><th>일시</th><th>창고</th><th>구역</th><th>SKU</th><th>유형</th><th>변동</th><th>사유</th></tr></thead>
      <tbody>
        <tr v-for="t in transactions" :key="t.id">
          <td>{{ t.id }}</td><td>{{ formatDate(t.createdAt) }}</td>
          <td>{{ t.sectionName }}</td><td>{{ t.sectionName }}</td><td>{{ t.skuName }}</td>
          <td>{{ t.transactionTypeTitle }}</td>
          <td>{{ t.beforeQuantity }} → {{ t.afterQuantity }} ({{ t.quantity }})</td>
          <td>{{ t.reason }}</td>
        </tr>
        <tr v-if="!transactions.length"><td colspan="8" style="text-align:center;">조회된 변동 기록이 없습니다.</td></tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { http } from '@/api/http'

const warehouses = ref([])
const allSections = ref([])
const skus = ref([])
const inventories = ref([])
const transactions = ref([])
const filter = ref({ warehouseId: '', sectionId: '', skuId: '' })

const filteredSections = computed(() =>
  filter.value.warehouseId
    ? allSections.value.filter(s => s.warehouseId == filter.value.warehouseId)
    : allSections.value
)

function formatDate(d) { return d ? d.replace('T', ' ').substring(0, 16) : '-' }
function onWarehouseChange() { filter.value.sectionId = '' }
function resetFilter() { filter.value = { warehouseId: '', sectionId: '', skuId: '' }; loadInventory() }

function buildQuery() {
  const p = new URLSearchParams()
  if (filter.value.warehouseId) p.append('warehouseId', filter.value.warehouseId)
  if (filter.value.sectionId)   p.append('sectionId',   filter.value.sectionId)
  if (filter.value.skuId)       p.append('skuId',       filter.value.skuId)
  return p.toString() ? '?' + p.toString() : ''
}

async function loadInventory() {
  const q = buildQuery()
  const [inv, tx] = await Promise.all([
    http.get('/api/inventory' + q),
    http.get('/api/inventory/transactions' + q),
  ])
  inventories.value = inv
  transactions.value = tx
}

onMounted(async () => {
  const [ws, ss, sk] = await Promise.all([
    http.get('/api/warehouses'),
    http.get('/api/warehouses/sections'),
    http.get('/api/products/skus'),
  ])
  warehouses.value = ws
  allSections.value = ss
  skus.value = sk
  await loadInventory()
})
</script>
