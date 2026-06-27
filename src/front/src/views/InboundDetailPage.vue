<template>
  <div>
    <h1>입고 ID: {{ inboundId }} 상세 내역</h1>
    <router-link to="/inbound">← 목록으로 돌아가기</router-link>
    <p v-if="error" class="error">{{ error }}</p>

    <!-- 상태 바 -->
    <div v-if="inbound" style="display:flex; align-items:center; gap:16px; margin:16px 0; flex-wrap:wrap;">
      <span>현재 상태:</span>
      <span style="padding:6px 14px; border-radius:20px; background:#e9ecef; font-weight:bold;">
        {{ inbound.statusDescription }}
      </span>

      <!-- 본사 관리자: 상태 진행 -->
      <template v-if="auth.isGeneralManager && inbound.nextStatus">
        <button class="btn btn-primary" @click="advanceStatus">{{ inbound.nextStatusDescription }}(으)로 변경</button>
      </template>

      <!-- 창고 관리자 -->
      <template v-if="auth.isWarehouseManager">
        <button v-if="inbound.status === 'DELIVERED'" class="btn btn-primary" @click="startInspection">검수 시작</button>
        <button v-if="inbound.status === 'INSPECTING'" class="btn btn-success" @click="completeInbound">입고 완료</button>
      </template>
    </div>

    <!-- 상세 테이블 -->
    <table v-if="details.length">
      <thead>
        <tr>
          <th>상세 ID</th><th>품목명</th><th>입고 수량</th><th>불량 수량</th><th>정상 수량</th>
          <th>정상 구역</th><th>불량 구역</th>
          <template v-if="isInspecting">
            <th>불량 등록</th><th>정상 구역 배정</th><th>불량 구역 배정</th>
          </template>
        </tr>
      </thead>
      <tbody>
        <tr v-for="d in details" :key="d.id">
          <td>{{ d.id }}</td>
          <td>{{ d.skuName }}</td>
          <td>{{ d.quantity }}</td>
          <td style="color:#dc3545;">{{ d.defectQuantity }}</td>
          <td style="color:#28a745;">{{ d.normalQuantity }}</td>
          <td>{{ d.sectionName || '미배정' }}</td>
          <td>{{ d.defectSectionName || '-' }}</td>

          <template v-if="isInspecting">
            <!-- 불량 수량 등록 -->
            <td>
              <template v-if="!d.defectRecorded">
                <div style="display:flex; gap:6px;">
                  <input v-model.number="d._defectQty" type="number" :min="0" :max="d.quantity" style="width:70px;" />
                  <button class="btn btn-sm" style="background:#fd7e14;color:white;" @click="recordDefect(d)">등록</button>
                </div>
              </template>
              <template v-else>
                <span style="color:#888;">{{ d.defectQuantity }}개 (확정)</span>
                <button class="btn btn-sm" style="background:#6c757d;color:white;margin-left:6px;"
                        @click="resetDefect(d)">초기화</button>
              </template>
            </td>

            <!-- 정상 구역 배정 -->
            <td>
              <template v-if="!d.defectRecorded">
                <span style="color:#aaa;">불량 수량 확정 후 배정 가능</span>
              </template>
              <template v-else-if="!d.sectionId">
                <div style="display:flex; gap:6px;">
                  <select v-model="d._sectionId">
                    <option value="">구역 선택 (정상: {{ d.normalQuantity }})</option>
                    <option v-for="s in assignableSections" :key="s.id" :value="s.id">
                      {{ s.name }} ({{ s.sectionCode }}) - 잔여 {{ s.effectiveRemaining }}
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

            <!-- 불량 구역 배정 -->
            <td>
              <template v-if="d.defectQuantity > 0 && !d.defectSectionId">
                <div style="display:flex; gap:6px;">
                  <select v-model="d._defectSectionId">
                    <option value="">불량 구역 선택 (불량: {{ d.defectQuantity }})</option>
                    <option v-for="s in defectSections" :key="s.id" :value="s.id">
                      {{ s.name }} ({{ s.sectionCode }}) - 잔여 {{ s.effectiveRemaining }}
                    </option>
                  </select>
                  <button class="btn btn-sm" style="background:#fd7e14;color:white;" @click="assignDefectSection(d)">배정</button>
                </div>
              </template>
              <template v-else-if="d.defectSectionId">
                <span style="color:#fd7e14;font-weight:bold;">✓ {{ d.defectSectionName }}</span>
                <button class="btn btn-sm btn-danger" style="margin-left:6px;" @click="clearDefectSection(d)">초기화</button>
              </template>
              <template v-else><span style="color:#888;">-</span></template>
            </td>
          </template>
        </tr>
      </tbody>
    </table>
    <p v-else>상세 내역이 없습니다.</p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { http } from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()
const inboundId = route.params.id
const inbound = ref(null)
const details = ref([])
const assignableSections = ref([])
const defectSections = ref([])
const error = ref('')

const isInspecting = computed(() => inbound.value?.status === 'INSPECTING' && auth.isWarehouseManager)

async function loadInbound() {
  const list = await http.get('/api/inbounds')
  inbound.value = list.find(i => i.id == inboundId) || null
}

async function loadDetails() {
  const ds = await http.get('/api/inbounds/' + inboundId + '/details')
  details.value = ds.map(d => ({ ...d, _defectQty: d.defectQuantity, _sectionId: '', _defectSectionId: '' }))
}

async function advanceStatus() {
  try {
    await http.patch('/api/inbounds/' + inboundId + '/status?nextStatus=' + inbound.value.nextStatus)
    await loadInbound()
  } catch (e) { error.value = e.message }
}

async function startInspection() {
  try {
    await http.patch('/api/inbounds/' + inboundId + '/inspect')
    await loadInbound()
  } catch (e) { error.value = e.message }
}

async function completeInbound() {
  if (!confirm('입고를 완료 처리하시겠습니까?')) return
  try {
    await http.patch('/api/inbounds/' + inboundId + '/complete')
    await loadInbound()
  } catch (e) { error.value = e.message }
}

async function recordDefect(d) {
  try {
    await http.patch(`/api/inbounds/${inboundId}/details/${d.id}/defect?defectQuantity=${d._defectQty}`)
    await loadDetails()
  } catch (e) { error.value = e.message }
}

async function resetDefect(d) {
  if (!confirm('불량 수량 확정을 초기화하시겠습니까?')) return
  try {
    await http.delete(`/api/inbounds/${inboundId}/details/${d.id}/defect`)
    await loadDetails()
  } catch (e) { error.value = e.message }
}

async function assignSection(d) {
  try {
    await http.patch(`/api/inbounds/${inboundId}/details/${d.id}/section?sectionId=${d._sectionId}`)
    await loadDetails()
  } catch (e) { error.value = e.message }
}

async function clearSection(d) {
  if (!confirm('구역 배정을 초기화하시겠습니까?')) return
  try {
    await http.delete(`/api/inbounds/${inboundId}/details/${d.id}/section`)
    await loadDetails()
  } catch (e) { error.value = e.message }
}

async function assignDefectSection(d) {
  try {
    await http.patch(`/api/inbounds/${inboundId}/details/${d.id}/defect-section?sectionId=${d._defectSectionId}`)
    await loadDetails()
  } catch (e) { error.value = e.message }
}

async function clearDefectSection(d) {
  if (!confirm('불량 구역 배정을 초기화하시겠습니까?')) return
  try {
    await http.delete(`/api/inbounds/${inboundId}/details/${d.id}/defect-section`)
    await loadDetails()
  } catch (e) { error.value = e.message }
}

onMounted(async () => {
  await loadInbound()
  await loadDetails()
  if (inbound.value) {
    const [as, ds] = await Promise.all([
      http.get('/api/inbounds/' + inboundId + '/assignable-sections'),
      http.get('/api/inbounds/' + inboundId + '/defect-sections'),
    ])
    assignableSections.value = as
    defectSections.value = ds
  }
})
</script>
