<template>
  <div>
    <h1>지점 관리</h1>
    <hr>
    <div style="display:flex; gap:40px;">
      <div style="flex:1;">
        <h2>지점 목록</h2>
        <table>
          <thead><tr><th>ID</th><th>지점명</th><th>주소</th><th>전화번호</th></tr></thead>
          <tbody>
            <tr v-for="s in stores" :key="s.id">
              <td>{{ s.id }}</td><td>{{ s.name }}</td><td>{{ s.address }}</td><td>{{ s.callNum }}</td>
            </tr>
            <tr v-if="!stores.length"><td colspan="4" style="text-align:center;">등록된 지점이 없습니다.</td></tr>
          </tbody>
        </table>

        <h2 style="margin-top:20px;">지점 담당자 배정 현황</h2>
        <table>
          <thead><tr><th>지점명</th><th>담당자</th><th>권한</th></tr></thead>
          <tbody>
            <tr v-for="m in pagedManagers" :key="m.id">
              <td>{{ m.storeName }}</td><td>{{ m.userName }}</td><td>{{ m.managementTypeTitle }}</td>
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

      <div style="width:360px;">
        <h2>신규 지점 등록</h2>
        <p v-if="storeError" class="error">{{ storeError }}</p>
        <div class="form-group"><label>지점명</label><input v-model="storeForm.name" /></div>
        <div class="form-group"><label>주소</label><input v-model="storeForm.address" /></div>
        <div class="form-group"><label>전화번호</label><input v-model="storeForm.callNum" /></div>
        <button class="btn btn-primary" @click="createStore">지점 등록</button>

        <h2>지점 담당자 배정</h2>
        <p v-if="assignError" class="error">{{ assignError }}</p>
        <div class="form-group"><label>대상 지점</label>
          <select v-model="assignForm.storeId">
            <option value="">지점 선택</option>
            <option v-for="s in stores" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div class="form-group"><label>담당 직원</label>
          <select v-model="assignForm.userId">
            <option value="">직원 선택</option>
            <option v-for="u in users" :key="u.id" :value="u.id">{{ u.name }} ({{ u.loginId }})</option>
          </select>
        </div>
        <div class="form-group"><label>권한</label>
          <select v-model="assignForm.storeManagementType">
            <option value="">권한 선택</option>
            <option v-for="t in managementTypes" :key="t.name" :value="t.name">{{ t.title }}</option>
          </select>
        </div>
        <button class="btn btn-primary" @click="assignStore">담당자 배정</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { http } from '@/api/http'

const PAGE_SIZE = 15

const stores = ref([])
const managers = ref([])
const users = ref([])
const managementTypes = ref([])
const storeError = ref('')
const assignError = ref('')
const storeForm = ref({ name: '', address: '', callNum: '' })
const assignForm = ref({ storeId: '', userId: '', storeManagementType: '' })

const managerPage = ref(0)
const managerTotalPages = computed(() => Math.max(1, Math.ceil(managers.value.length / PAGE_SIZE)))
const pagedManagers = computed(() => {
  const start = managerPage.value * PAGE_SIZE
  return managers.value.slice(start, start + PAGE_SIZE)
})

async function load() {
  const [ss, mg, mt] = await Promise.all([
    http.get('/api/orders/stores'),
    http.get('/api/orders/stores/managers'),
    http.get('/api/orders/stores/management-types'),
  ])
  stores.value = ss
  managers.value = mg
  managementTypes.value = mt
  try { users.value = await http.get('/api/users?role=ROLE_USER') } catch { users.value = [] }
}

async function createStore() {
  storeError.value = ''
  try {
    await http.post('/api/orders/stores', storeForm.value)
    storeForm.value = { name: '', address: '', callNum: '' }
    await load()
  } catch (e) { storeError.value = e.message }
}

async function assignStore() {
  assignError.value = ''
  try {
    await http.post('/api/orders/stores/assign', assignForm.value)
    assignForm.value = { storeId: '', userId: '', storeManagementType: '' }
    await load()
  } catch (e) { assignError.value = e.message }
}

onMounted(load)
</script>
