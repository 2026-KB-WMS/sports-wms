<template>
  <div>
    <h1>SKU 관리</h1>
    <hr>
    <h2>SKU 등록</h2>
    <p v-if="skuError" class="error">{{ skuError }}</p>
    <div class="form-group">
      <label>상품명</label>
      <select v-model="form.productId" @change="loadOptionGroups">
        <option value="">상품 선택</option>
        <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
      </select>
    </div>
    <div v-if="optionGroups.length">
      <div v-for="g in optionGroups" :key="g.id" class="form-group">
        <label>{{ g.name }} (필수)</label>
        <div style="display:flex; gap:12px; flex-wrap:wrap;">
          <label v-for="v in g.optionValues" :key="v.id">
            <input type="radio" :name="'opt_' + g.id" :value="v.id" v-model="optionSelections[g.id]" />
            {{ v.name }}
          </label>
        </div>
      </div>
    </div>
    <button class="btn btn-primary" :disabled="!allGroupsSelected" @click="createSku">등록</button>
    <hr>

    <h2>등록된 SKU 목록</h2>
    <table>
      <thead><tr><th>카테고리</th><th>브랜드</th><th>상품명</th><th>SKU명</th><th>SKU 코드</th><th>가격</th></tr></thead>
      <tbody>
        <tr v-for="s in skus" :key="s.id">
          <td>{{ s.categoryName }}</td>
          <td>{{ s.brandName }}</td>
          <td>{{ s.productName }}</td>
          <td>{{ s.name }}</td>
          <td><strong>{{ s.skuCode }}</strong></td>
          <td>{{ s.price }}원</td>
        </tr>
        <tr v-if="!skus.length"><td colspan="6" style="text-align:center;">등록된 SKU가 없습니다.</td></tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { http } from '@/api/http'

const products = ref([])
const skus = ref([])
const optionGroups = ref([])
const optionSelections = reactive({})
const skuError = ref('')
const form = ref({ productId: '', optionValueIds: [] })

const allGroupsSelected = computed(() =>
  optionGroups.value.length > 0 &&
  optionGroups.value.every(g => optionSelections[g.id])
)

async function loadOptionGroups() {
  if (!form.value.productId) { optionGroups.value = []; return }
  optionGroups.value = await http.get('/api/products/' + form.value.productId + '/option-groups')
  Object.keys(optionSelections).forEach(k => delete optionSelections[k])
}

async function createSku() {
  skuError.value = ''
  form.value.optionValueIds = Object.values(optionSelections).filter(Boolean)
  try {
    await http.post('/api/products/skus', form.value)
    form.value = { productId: '', optionValueIds: [] }
    optionGroups.value = []
    skus.value = await http.get('/api/products/skus')
  } catch (e) { skuError.value = e.message }
}

onMounted(async () => {
  const [ps, ss] = await Promise.all([http.get('/api/products'), http.get('/api/products/skus')])
  products.value = ps
  skus.value = ss
})
</script>
