<template>
  <div>
    <h1>출고 ID: {{ outboundId }} 상세 내역</h1>
    <router-link to="/outbound">← 목록으로 돌아가기</router-link>
    <p v-if="error" class="error">{{ error }}</p>

    <div v-if="outbound" style="display:flex; align-items:center; gap:16px; margin:16px 0; flex-wrap:wrap;">
      <span>현재 상태:</span>
      <span style="padding:6px 14px; border-radius:20px; background:#e9ecef; font-weight:bold;">
        {{ outbound.statusDescription }}
      </span>
      <template v-if="auth.isWarehouseManager">
        <button v-if="outbound.status === 'ASSIGNED'" class="btn btn-primary" @click="approve">출고 승인</button>
        <button v-if="outbound.status === 'APPROVED'" class="btn btn-primary" @click="startPicking">피킹 시작</button>
        <button v-if="outbound.status === 'PICKING'" class="btn btn-primary" @click="completePicking">피킹 완료</button>
        <button v-if="outbound.status === 'PACKING'" class="btn btn-success" @click="ship">배송 출발</button>
      </template>
    </div>

    <table>
      <thead>
        <tr>
          <th>상세 ID</th><th>품목명</th><th>수량</th><th>배정 구역</th>
          <th v-if="isAssigning">구역 배정</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="d in details" :key="d.id">
          <td>{{ d.id }}</td><td>{{ d.skuName }}</td><td>{{ d.quantity }}</td>
          <td>
            <span v-if="d.sectionName" style="color:#28a745; font-weight:bold;">{{ d.sectionName }}</span>
            <span v-else>미배정</span>
          </td>
          <td v-if="isAssigning">
            <template v-if="!d.sectionId">
              <div style="display:flex; gap:6px;">
                <select v-model="d._sectionId">
                  <option value="">구역 선택 (수량: {{ d.quantity }})</option>
                  <option v-for="s in d.sections" :key="s.id" :value="s.id">
                    {{ s.name }} ({{ s.sectionCode }}) - 가용 {{ s.availableQuantity }}
                  </option>
                </select>
                <button class="btn btn-sm btn-primary" @click="assignSection(d)">배정</button>
              </div>
            </template>
            <template v-else>
              <span style="color:#28a745; font-weight:bold;">✓ {{ d.sectionName }}</span>
              <button class="btn btn-sm btn-danger" style="margin-left:6px;" @click="clearSection(d)">초기화</button>
            </template>
          </td>
        </tr>
        <tr v-if="!details.length"><td colspan="5" style="text-align:center;">상세 내역이 없습니다.</td></tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { http } from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()
const outboundId = route.params.id
const outbound = ref(null)
const details = ref([])
const error = ref('')

const isAssigning = computed(() => outbound.value?.status === 'ASSIGNED' && auth.isWarehouseManager)

async function loadOutbound() {
  const list = await http.get('/api/outbounds')
  outbound.value = list.find(o => o.id == outboundId) || null
}

async function loadDetails() {
  const ds = await http.get('/api/outbounds/' + outboundId + '/details')
  details.value = ds.map(d => ({ ...d, _sectionId: '', sections: [] }))

  // ASSIGNED 상태일 때 각 품목별 배정 가능 구역 로드
  if (outbound.value?.status === 'ASSIGNED' && auth.isWarehouseManager) {
    await loadSections()
  }
}

async function loadSections() {
  for (const d of details.value) {
    if (!d.sectionId) {
      try {
        d.sections = await http.get(`/api/outbounds/${outboundId}/details/${d.id}/assignable-sections`)
      } catch { d.sections = [] }
    }
  }
}

async function action(fn) {
  error.value = ''
  try { await fn(); await loadOutbound(); await loadDetails() }
  catch (e) { error.value = e.message }
}

const approve        = () => action(() => http.patch('/api/outbounds/' + outboundId + '/approve'))
const startPicking   = () => action(() => http.patch('/api/outbounds/' + outboundId + '/picking/start'))
const completePicking = () => action(() => http.patch('/api/outbounds/' + outboundId + '/picking/complete'))
const ship           = () => action(() => http.patch('/api/outbounds/' + outboundId + '/ship'))

async function assignSection(d) {
  await action(() => http.patch(`/api/outbounds/${outboundId}/details/${d.id}/section?sectionId=${d._sectionId}`))
}
async function clearSection(d) {
  if (!confirm('구역 배정을 초기화하시겠습니까?')) return
  await action(() => http.delete(`/api/outbounds/${outboundId}/details/${d.id}/section`))
}

onMounted(async () => {
  await loadOutbound()
  await loadDetails()
})
</script>
