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
      <button class="btn btn-primary" @click="onFilterSearch">조회</button>
      <button class="btn" @click="resetFilter">초기화</button>
    </div>

    <!-- 재고 현황 -->
    <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:8px;">
      <h2 style="margin:0;">재고 현황</h2>
      <span style="font-size:0.85rem; color:#666;">페이지 {{ invPage + 1 }} / {{ invTotalPages }}</span>
    </div>
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
    <!-- 재고 현황 페이지 네비게이션 -->
    <div style="display:flex; justify-content:center; align-items:center; gap:12px; margin:12px 0 28px;">
      <button class="btn" :disabled="invPage === 0" @click="invPage--; loadInventory()">◀</button>
      <span>{{ invPage + 1 }} / {{ invTotalPages }}</span>
      <button class="btn" :disabled="invPage >= invTotalPages - 1" @click="invPage++; loadInventory()">▶</button>
    </div>

    <!-- 재고 변동 기록 -->
    <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:8px;">
      <h2 style="margin:0;">재고 변동 기록</h2>
      <span style="font-size:0.85rem; color:#666;">{{ txCurrentIdx + 1 }} 페이지</span>
    </div>
    <table>
      <thead><tr><th>ID</th><th>일시</th><th>창고</th><th>구역</th><th>SKU</th><th>유형</th><th>변동</th><th>사유</th></tr></thead>
      <tbody>
        <tr v-for="t in transactions" :key="t.id">
          <td>{{ t.id }}</td><td>{{ formatDate(t.createdAt) }}</td>
          <td>{{ t.warehouseName }}</td><td>{{ t.sectionName }}</td><td>{{ t.skuName }}</td>
          <td>{{ t.transactionTypeTitle }}</td>
          <td>{{ t.beforeQuantity }} → {{ t.afterQuantity }} ({{ t.quantity }})</td>
          <td>{{ t.reason }}</td>
        </tr>
        <tr v-if="!transactions.length"><td colspan="8" style="text-align:center;">조회된 변동 기록이 없습니다.</td></tr>
      </tbody>
    </table>
    <!-- 커서 기반 네비게이션 -->
    <div style="display:flex; justify-content:center; align-items:center; gap:12px; margin:12px 0;">
      <button class="btn" :disabled="txCurrentIdx === 0" @click="txPrev()">◀</button>
      <span>{{ txCurrentIdx + 1 }} 페이지</span>
      <button class="btn" :disabled="!txHasNext" @click="txNext()">▶</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { http } from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const PAGE_SIZE = 15

const auth = useAuthStore()
const warehouses = ref([])
const allSections = ref([])
const skus = ref([])
const inventories = ref([])
const transactions = ref([])
const filter = ref({ warehouseId: '', sectionId: '', skuId: '' })

// 재고 현황 페이지 상태 (Page — 전체 페이지 수 있음)
const invPage = ref(0)
const invTotalPages = ref(1)

// 재고 변동 기록 커서 기반 페이지네이션 상태
// cursorStack: 각 페이지 진입 시 사용한 커서를 쌓아둠 (뒤로 가기용)
// cursorStack[0] = 첫 페이지 (커서 없음), cursorStack[n] = n번째 페이지 커서
const txCursorStack = ref([{ cursorCreatedAt: null, cursorId: null }])
const txCurrentIdx = ref(0)
const txHasNext = ref(false)

const filteredSections = computed(() =>
  filter.value.warehouseId
    ? allSections.value.filter(s => s.warehouseId == filter.value.warehouseId)
    : allSections.value
)

function formatDate(d) { return d ? d.replace('T', ' ').substring(0, 16) : '-' }
function onWarehouseChange() { filter.value.sectionId = '' }

function resetFilter() {
  filter.value = { warehouseId: '', sectionId: '', skuId: '' }
  invPage.value = 0
  txCursorStack.value = [{ cursorCreatedAt: null, cursorId: null }]
  txCurrentIdx.value = 0
  loadInventory()
  loadTransactions()
}

function onFilterSearch() {
  invPage.value = 0
  txCursorStack.value = [{ cursorCreatedAt: null, cursorId: null }]
  txCurrentIdx.value = 0
  loadInventory()
  loadTransactions()
}

function buildQuery(page) {
  const p = new URLSearchParams()
  if (filter.value.warehouseId) p.append('warehouseId', filter.value.warehouseId)
  if (filter.value.sectionId)   p.append('sectionId',   filter.value.sectionId)
  if (filter.value.skuId)       p.append('skuId',       filter.value.skuId)
  p.append('page', page)
  p.append('size', PAGE_SIZE)
  return '?' + p.toString()
}

async function loadInventory() {
  const data = await http.get('/api/inventory' + buildQuery(invPage.value))
  // Page 응답: { content: [...], totalPages: N, ... }
  inventories.value = data.content ?? data
  invTotalPages.value = data.totalPages ?? 1
}

async function loadTransactions() {
  const cursor = txCursorStack.value[txCurrentIdx.value]
  const p = new URLSearchParams()
  if (filter.value.warehouseId) p.append('warehouseId', filter.value.warehouseId)
  if (filter.value.sectionId)   p.append('sectionId',   filter.value.sectionId)
  if (filter.value.skuId)       p.append('skuId',       filter.value.skuId)
  if (cursor.cursorCreatedAt)   p.append('cursorCreatedAt', cursor.cursorCreatedAt)
  if (cursor.cursorId)          p.append('cursorId',    cursor.cursorId)
  p.append('size', PAGE_SIZE)

  const data = await http.get('/api/inventory/transactions?' + p.toString())
  transactions.value = data.content ?? []
  txHasNext.value = data.hasNext ?? false

  // 다음 페이지 커서를 스택에 미리 저장 (▶ 버튼 클릭 시 사용)
  if (data.hasNext && data.nextCursorCreatedAt) {
    const nextCursor = { cursorCreatedAt: data.nextCursorCreatedAt, cursorId: data.nextCursorId }
    // 현재 위치 이후 스택을 잘라내고 추가 (필터 변경 없이 앞뒤 이동 시 스택 오염 방지)
    txCursorStack.value = [
      ...txCursorStack.value.slice(0, txCurrentIdx.value + 1),
      nextCursor
    ]
  }
}

function txNext() {
  txCurrentIdx.value++
  loadTransactions()
}

function txPrev() {
  txCurrentIdx.value--
  loadTransactions()
}

onMounted(async () => {
  const warehouseUrl = auth.isWarehouseManager ? '/api/warehouses/my' : '/api/warehouses'
  const [ws, ss, sk] = await Promise.all([
    http.get(warehouseUrl),
    http.get('/api/warehouses/sections'),
    http.get('/api/products/skus'),
  ])
  warehouses.value = ws
  if (auth.isWarehouseManager) {
    const myWarehouseIds = ws.map(w => w.id)
    allSections.value = ss.filter(s => myWarehouseIds.includes(s.warehouseId))
  } else {
    allSections.value = ss
  }
  skus.value = sk
  await Promise.all([loadInventory(), loadTransactions()])
})
</script>
