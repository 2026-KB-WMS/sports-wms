<template>
  <div>
    <h1>지점 발주 관리</h1>
    <p v-if="error" class="error">{{ error }}</p>

    <!-- 점주: 신규 발주 -->
    <template v-if="auth.isUser">
      <h2>신규 발주 신청</h2>
      <div class="form-group">
        <label>발주 지점</label>
        <select v-model="orderForm.storeId">
          <option value="">-- 배정된 지점 선택 --</option>
          <option v-for="s in stores" :key="s.id" :value="s.id">{{ s.name }} ({{ s.address }})</option>
        </select>
      </div>
      <div style="display:flex; gap:8px; margin-bottom:8px;">
        <select v-model="newItem.skuId" style="flex:3;">
          <option value="">-- 상품(SKU) 선택 --</option>
          <option v-for="s in skus" :key="s.id" :value="s.id">{{ s.skuCode }} ({{ s.name }})</option>
        </select>
        <input v-model.number="newItem.quantity" type="number" placeholder="수량" style="width:80px;" />
        <input v-model="newItem.memo" type="text" placeholder="메모 (선택)" style="flex:1;" />
        <button class="btn" @click="addItem">추가</button>
      </div>
      <table v-if="orderForm.items.length">
        <thead><tr><th>상품명</th><th>수량</th><th>메모</th><th>관리</th></tr></thead>
        <tbody>
          <tr v-for="(item, i) in orderForm.items" :key="i">
            <td>{{ skuName(item.skuId) }}</td><td>{{ item.quantity }}</td><td>{{ item.memo }}</td>
            <td><button class="btn btn-sm btn-danger" @click="removeItem(i)">삭제</button></td>
          </tr>
        </tbody>
      </table>
      <button class="btn btn-primary" @click="submitOrder">본사로 발주 전송하기</button>
      <hr style="margin: 40px 0;">
    </template>

    <!-- 본사 관리자: 창고 위임 -->
    <template v-if="auth.isGeneralManager">
      <h2>전체 지점 발주 내역</h2>
      <div style="border:1px solid #ccc; padding:15px; margin-bottom:20px; border-radius:4px;">
        <h3>창고 위임</h3>
        <div class="form-group">
          <label>위임 대상 창고</label>
          <select v-model="assignWarehouseId">
            <option value="">-- 창고 선택 --</option>
            <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
          </select>
        </div>
        <button class="btn btn-primary" @click="assignOrders">선택한 발주들을 창고에 위임하기</button>
      </div>
    </template>

    <!-- 발주 내역 테이블 -->
    <table>
      <thead>
        <tr>
          <th v-if="auth.isGeneralManager" style="width:40px;"><input type="checkbox" @change="toggleAll" /></th>
          <th>발주 그룹 ID</th><th>지점명</th><th>SKU</th><th>수량</th><th>메모</th><th>상태</th><th>오더 ID</th>
          <th v-if="auth.isUser">취소</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="d in orderDetails" :key="d.id">
          <td v-if="auth.isGeneralManager">
            <input v-if="!d.stockOrderId && d.status !== 'CANCELED'" type="checkbox" v-model="selectedIds" :value="d.id" />
          </td>
          <td>{{ d.orderGroupId }}</td><td>{{ d.storeName }}</td>
          <td>{{ d.skuName }}</td><td>{{ d.quantity }}</td><td>{{ d.memo }}</td>
          <td><span :class="'badge badge-' + d.status">{{ d.statusDescription }}</span></td>
          <td>{{ d.stockOrderId || '-' }}</td>
          <td v-if="auth.isUser">
            <button v-if="d.status === 'PENDING'" class="btn btn-sm btn-danger"
                    @click="cancelOrder(d.orderGroupId)">취소</button>
          </td>
        </tr>
        <tr v-if="!orderDetails.length">
          <td :colspan="auth.isGeneralManager ? 8 : 7" style="text-align:center;">발주 내역이 없습니다.</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const orderDetails = ref([])
const stores = ref([])
const skus = ref([])
const warehouses = ref([])
const selectedIds = ref([])
const assignWarehouseId = ref('')
const error = ref('')

const orderForm = ref({ storeId: '', items: [] })
const newItem = ref({ skuId: '', quantity: 1, memo: '' })

function skuName(id) { return skus.value.find(s => s.id === id)?.name || id }
function addItem() {
  if (!newItem.value.skuId) return
  orderForm.value.items.push({ ...newItem.value })
  newItem.value = { skuId: '', quantity: 1, memo: '' }
}
function removeItem(i) { orderForm.value.items.splice(i, 1) }
function toggleAll(e) {
  selectedIds.value = e.target.checked
    ? orderDetails.value.filter(d => !d.stockOrderId).map(d => d.id)
    : []
}

async function load() {
  const [details, ws] = await Promise.all([
    auth.isGeneralManager ? http.get('/api/orders/details') : http.get('/api/orders/my'),
    auth.isGeneralManager ? http.get('/api/warehouses') : Promise.resolve([]),
  ])
  orderDetails.value = details
  warehouses.value = ws

  if (auth.isUser) {
    const [ss, sk] = await Promise.all([http.get('/api/orders/stores'), http.get('/api/products/skus')])
    stores.value = ss
    skus.value = sk
  }
}

async function submitOrder() {
  error.value = ''
  if (!orderForm.value.items.length) { error.value = '발주할 품목을 추가해주세요.'; return }
  try {
    await http.post('/api/orders', orderForm.value)
    orderForm.value = { storeId: '', items: [] }
    await load()
  } catch (e) { error.value = e.message }
}

async function assignOrders() {
  error.value = ''
  if (!assignWarehouseId.value) { error.value = '창고를 선택해주세요.'; return }
  if (!selectedIds.value.length) { error.value = '위임할 발주를 선택해주세요.'; return }
  try {
    await http.post('/api/orders/assign', { warehouseId: assignWarehouseId.value, orderDetailIds: selectedIds.value })
    selectedIds.value = []
    await load()
  } catch (e) { error.value = e.message }
}

async function cancelOrder(groupId) {
  if (!confirm(`발주 그룹 ${groupId} 전체를 취소하시겠습니까?`)) return
  try {
    await http.delete('/api/orders/' + groupId)
    await load()
  } catch (e) { error.value = e.message }
}

onMounted(load)
</script>

<style scoped>
.badge { padding: 3px 10px; border-radius: 12px; font-size: 0.85em; font-weight: bold; }
.badge-PENDING    { background: #e9ecef; color: #6c757d; }
.badge-ASSIGNED   { background: #cfe2ff; color: #084298; }
.badge-DELIVERING { background: #fff3cd; color: #664d03; }
.badge-COMPLETED  { background: #d1e7dd; color: #0a3622; }
.badge-CANCELED   { background: #f8d7da; color: #842029; }
</style>
