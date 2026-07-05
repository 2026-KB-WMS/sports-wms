<template>
  <div>
    <h1>창고/구역 관리</h1>
    <hr>

    <div style="display: flex; gap: 40px;">
      <!-- 목록 -->
      <div style="flex: 1;">
        <h2>창고 목록</h2>
        <table>
          <thead><tr><th>ID</th><th>창고명</th><th>주소</th><th>최대 수용량</th><th>현재 수용량</th></tr></thead>
          <tbody>
            <tr v-for="w in warehouses" :key="w.id">
              <td>{{ w.id }}</td><td>{{ w.name }}</td><td>{{ w.address }}</td>
              <td>{{ w.totalCapacity }}</td><td>{{ w.currentSectionCapacity }}</td>
            </tr>
            <tr v-if="!warehouses.length"><td colspan="5" style="text-align:center;">등록된 창고가 없습니다.</td></tr>
          </tbody>
        </table>

        <!-- 구역 목록 — 창고별 필터 -->
        <div style="display:flex; align-items:center; gap:12px; margin:20px 0 8px;">
          <h2 style="margin:0;">구역 목록</h2>
          <select v-model="sectionFilterWarehouseId" @change="sectionPage = 0" style="min-width:160px;">
            <option value="">전체 창고</option>
            <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
          </select>
        </div>
        <table>
          <thead><tr><th>소속 창고</th><th>구역명</th><th>구역 코드</th><th>타입</th><th>최대</th><th>사용</th><th>작업</th></tr></thead>
          <tbody>
            <tr v-for="s in pagedSections" :key="s.id">
              <td>{{ s.warehouseName }}</td><td>{{ s.name }}</td><td>{{ s.sectionCode }}</td>
              <td>{{ s.sectionTypeTitle }}</td><td>{{ s.totalCapacity }}</td><td>{{ s.currentUsage }}</td>
              <td>
                <button class="btn btn-danger btn-sm"
                        :disabled="s.currentUsage > 0"
                        @click="deleteSection(s.id)">삭제</button>
              </td>
            </tr>
            <tr v-if="!pagedSections.length"><td colspan="7" style="text-align:center;">등록된 구역이 없습니다.</td></tr>
          </tbody>
        </table>
        <div style="display:flex; justify-content:center; align-items:center; gap:12px; margin:8px 0 24px;">
          <button class="btn" :disabled="sectionPage === 0" @click="sectionPage--">◀</button>
          <span>{{ sectionPage + 1 }} / {{ sectionTotalPages }}</span>
          <button class="btn" :disabled="sectionPage >= sectionTotalPages - 1" @click="sectionPage++">▶</button>
        </div>

        <!-- 창고 담당자 배정 현황 -->
        <div style="display:flex; align-items:center; gap:12px; margin:20px 0 8px;">
          <h2 style="margin:0;">창고 담당자 배정 현황</h2>
        </div>
        <table>
          <thead><tr><th>창고명</th><th>담당자</th><th>권한</th></tr></thead>
          <tbody>
            <tr v-for="m in pagedManagers" :key="m.id">
              <td>{{ m.warehouseName }}</td><td>{{ m.userName }}</td><td>{{ m.managementTypeTitle }}</td>
            </tr>
            <tr v-if="!pagedManagers.length"><td colspan="3" style="text-align:center;">배정된 담당자가 없습니다.</td></tr>
          </tbody>
        </table>
        <div style="display:flex; justify-content:center; align-items:center; gap:12px; margin:8px 0 24px;">
          <button class="btn" :disabled="managerPage === 0" @click="managerPage--">◀</button>
          <span>{{ managerPage + 1 }} / {{ managerTotalPages }}</span>
          <button class="btn" :disabled="managerPage >= managerTotalPages - 1" @click="managerPage++">▶</button>
        </div>
      </div>

      <!-- 폼 -->
      <div style="width: 360px;">
        <h2>신규 창고 등록</h2>
        <p v-if="warehouseError" class="error">{{ warehouseError }}</p>
        <div class="form-group"><label>창고명</label><input v-model="warehouseForm.name" type="text" /></div>
        <div class="form-group"><label>우편번호</label>
          <div style="display:flex; gap:8px;">
            <input v-model="warehouseForm.postcode" readonly style="width:100px;" />
            <button class="btn" @click="openDaumPostcode">주소 검색</button>
          </div>
        </div>
        <div class="form-group"><label>주소</label><input v-model="warehouseForm.address" readonly /></div>
        <div class="form-group"><label>상세주소</label><input v-model="warehouseForm.detailAddress" /></div>
        <div class="form-group"><label>최대 수용량</label><input v-model.number="warehouseForm.totalCapacity" type="number" /></div>
        <button class="btn btn-primary" @click="createWarehouse">창고 등록</button>

        <h2>신규 구역 등록</h2>
        <p v-if="sectionError" class="error">{{ sectionError }}</p>
        <div class="form-group"><label>소속 창고</label>
          <select v-model="sectionForm.warehouseId">
            <option value="">창고 선택</option>
            <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
          </select>
        </div>
        <div class="form-group"><label>구역명</label><input v-model="sectionForm.name" type="text" /></div>
        <div class="form-group"><label>구역 타입</label>
          <select v-model="sectionForm.sectionType">
            <option value="">타입 선택</option>
            <option v-for="t in sectionTypes" :key="t.name" :value="t.name">{{ t.title }}</option>
          </select>
        </div>
        <div class="form-group"><label>최대 수용량</label><input v-model.number="sectionForm.totalCapacity" type="number" /></div>
        <button class="btn btn-primary" @click="createSection">구역 등록</button>

        <h2>창고 담당자 배정</h2>
        <p v-if="assignError" class="error">{{ assignError }}</p>
        <div class="form-group"><label>대상 창고</label>
          <select v-model="assignForm.warehouseId">
            <option value="">창고 선택</option>
            <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
          </select>
        </div>
        <div class="form-group"><label>직원</label>
          <select v-model="assignForm.userId">
            <option value="">직원 선택</option>
            <option v-for="u in users" :key="u.id" :value="u.id">{{ u.name }} ({{ u.loginId }})</option>
          </select>
        </div>
        <div class="form-group"><label>권한</label>
          <select v-model="assignForm.managementType">
            <option value="">권한 선택</option>
            <option v-for="t in managementTypes" :key="t.name" :value="t.name">{{ t.roleName }}</option>
          </select>
        </div>
        <button class="btn btn-primary" @click="assignManager">담당자 배정</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { http } from '@/api/http'

const PAGE_SIZE = 15

const warehouses = ref([])
const allSections = ref([])
const managers = ref([])
const users = ref([])
const sectionTypes = ref([])
const managementTypes = ref([])
const warehouseError = ref('')
const sectionError = ref('')
const assignError = ref('')

// 구역 필터 + 페이지
const sectionFilterWarehouseId = ref('')
const sectionPage = ref(0)

// 담당자 페이지
const managerPage = ref(0)

const warehouseForm = ref({ name: '', postcode: '', address: '', detailAddress: '', totalCapacity: null })
const sectionForm = ref({ warehouseId: '', name: '', sectionType: '', totalCapacity: null })
const assignForm = ref({ warehouseId: '', userId: '', managementType: '' })

const filteredSections = computed(() =>
  sectionFilterWarehouseId.value
    ? allSections.value.filter(s => s.warehouseId == sectionFilterWarehouseId.value)
    : allSections.value
)

const sectionTotalPages = computed(() => Math.max(1, Math.ceil(filteredSections.value.length / PAGE_SIZE)))
const pagedSections = computed(() => {
  const start = sectionPage.value * PAGE_SIZE
  return filteredSections.value.slice(start, start + PAGE_SIZE)
})

const managerTotalPages = computed(() => Math.max(1, Math.ceil(managers.value.length / PAGE_SIZE)))
const pagedManagers = computed(() => {
  const start = managerPage.value * PAGE_SIZE
  return managers.value.slice(start, start + PAGE_SIZE)
})

async function load() {
  const [ws, ss, st, mt, mg] = await Promise.all([
    http.get('/api/warehouses'),
    http.get('/api/warehouses/sections'),
    http.get('/api/warehouses/section-types'),
    http.get('/api/warehouses/management-types'),
    http.get('/api/warehouses/managers'),
  ])
  warehouses.value = ws
  allSections.value = ss
  sectionTypes.value = st
  managementTypes.value = mt
  managers.value = mg

  // 첫 번째 창고로 미리 필터링
  if (ws.length > 0 && !sectionFilterWarehouseId.value) {
    sectionFilterWarehouseId.value = ws[0].id
  }

  try { users.value = await http.get('/api/users?role=ROLE_WAREHOUSE_MANAGER') } catch { users.value = [] }
}

async function createWarehouse() {
  warehouseError.value = ''
  try {
    await http.post('/api/warehouses', warehouseForm.value)
    warehouseForm.value = { name: '', postcode: '', address: '', detailAddress: '', totalCapacity: null }
    await load()
  } catch (e) { warehouseError.value = e.message }
}

async function createSection() {
  sectionError.value = ''
  try {
    await http.post('/api/warehouses/sections', sectionForm.value)
    sectionForm.value = { warehouseId: '', name: '', sectionType: '', totalCapacity: null }
    await load()
  } catch (e) { sectionError.value = e.message }
}

async function deleteSection(id) {
  if (!confirm('구역을 삭제하시겠습니까?')) return
  try {
    await http.delete('/api/warehouses/sections/' + id)
    await load()
  } catch (e) { alert(e.message) }
}

async function assignManager() {
  assignError.value = ''
  try {
    await http.post('/api/warehouses/managers', assignForm.value)
    assignForm.value = { warehouseId: '', userId: '', managementType: '' }
    await load()
  } catch (e) { assignError.value = e.message }
}

function openDaumPostcode() {
  new window.daum.Postcode({
    oncomplete(data) {
      warehouseForm.value.postcode = data.zonecode
      warehouseForm.value.address = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress
    }
  }).open()
}

onMounted(load)
</script>

