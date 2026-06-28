<template>
  <div>
    <h1>상품 마스터 관리</h1>
    <hr>

    <section>
      <h2>브랜드 등록</h2>
      <p v-if="brandError" class="error">{{ brandError }}</p>
      <div class="form-group"><label>브랜드명</label><input v-model="brandForm.name" placeholder="예) Yonex" /></div>
      <div class="form-group"><label>브랜드 코드</label><input v-model="brandForm.code" placeholder="예) YNX" /></div>
      <button class="btn btn-primary" @click="createBrand">브랜드 등록</button>
    </section>
    <hr>

    <section>
      <h2>상품 등록</h2>
      <p v-if="productError" class="error">{{ productError }}</p>
      <div class="form-group"><label>상품명</label><input v-model="productForm.name" /></div>
      <div class="form-group"><label>상품 코드</label><input v-model="productForm.code" /></div>
      <div class="form-group"><label>가격</label><input v-model.number="productForm.price" type="number" /></div>
      <div class="form-group"><label>브랜드</label>
        <select v-model="productForm.brandId">
          <option value="">브랜드 선택</option>
          <option v-for="b in brands" :key="b.id" :value="b.id">{{ b.name }}</option>
        </select>
      </div>
      <div class="form-group"><label>카테고리</label>
        <select v-model="productForm.categoryId" @change="loadSpecGroups">
          <option value="">카테고리 선택</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
      </div>
      <div v-if="specGroups.length">
        <div v-for="g in specGroups" :key="g.id" class="form-group">
          <label>{{ g.name }} (필수)</label>
          <div style="display:flex; gap:12px; flex-wrap:wrap;">
            <label v-for="v in g.optionValues" :key="v.id">
              <input type="radio" :name="'spec_' + g.id" :value="v.id" v-model="specSelections[g.id]" />
              {{ v.name }}
            </label>
          </div>
        </div>
      </div>
      <button class="btn btn-primary" @click="createProduct">상품 등록</button>
    </section>
    <hr>

    <section>
      <h2>등록된 브랜드 목록</h2>
      <table>
        <thead><tr><th>ID</th><th>브랜드명</th><th>코드</th></tr></thead>
        <tbody>
          <tr v-for="b in brands" :key="b.id"><td>{{ b.id }}</td><td>{{ b.name }}</td><td>{{ b.code }}</td></tr>
          <tr v-if="!brands.length"><td colspan="3" style="text-align:center;">등록된 브랜드가 없습니다.</td></tr>
        </tbody>
      </table>
    </section>
    <hr>

    <section>
      <h2>등록된 상품 목록</h2>
      <table>
        <thead><tr><th>ID</th><th>카테고리</th><th>브랜드</th><th>상품명</th><th>코드</th><th>가격</th></tr></thead>
        <tbody>
          <tr v-for="p in products" :key="p.id">
            <td>{{ p.id }}</td><td>{{ p.categoryName }}</td><td>{{ p.brandName }}</td>
            <td>{{ p.name }}</td><td>{{ p.code }}</td><td>{{ p.price }}원</td>
          </tr>
          <tr v-if="!products.length"><td colspan="6" style="text-align:center;">등록된 상품이 없습니다.</td></tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { http } from '@/api/http'

const brands = ref([])
const categories = ref([])
const products = ref([])
const specGroups = ref([])
const specSelections = reactive({})
const brandError = ref('')
const productError = ref('')

const brandForm = ref({ name: '', code: '' })
const productForm = ref({ name: '', code: '', price: null, brandId: '', categoryId: '', specOptionValueIds: [] })

async function load() {
  const [bs, cs, ps] = await Promise.all([
    http.get('/api/products/brands'),
    http.get('/api/products/categories'),
    http.get('/api/products'),
  ])
  brands.value = bs; categories.value = cs; products.value = ps
}

async function loadSpecGroups() {
  if (!productForm.value.categoryId) { specGroups.value = []; return }
  specGroups.value = await http.get('/api/categories/' + productForm.value.categoryId + '/spec-option-groups')
}

async function createBrand() {
  brandError.value = ''
  try {
    await http.post('/api/products/brands', brandForm.value)
    brandForm.value = { name: '', code: '' }
    await load()
  } catch (e) { brandError.value = e.message }
}

async function createProduct() {
  productError.value = ''
  productForm.value.specOptionValueIds = Object.values(specSelections).filter(Boolean)
  try {
    await http.post('/api/products', productForm.value)
    productForm.value = { name: '', code: '', price: null, brandId: '', categoryId: '', specOptionValueIds: [] }
    specGroups.value = []
    await load()
  } catch (e) { productError.value = e.message }
}

onMounted(load)
</script>
