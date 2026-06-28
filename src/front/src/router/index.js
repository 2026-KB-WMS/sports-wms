import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import HomePage from '@/views/HomePage.vue'
import LoginPage from '@/views/LoginPage.vue'
import SignupPage from '@/views/SignupPage.vue'
import WarehousePage from '@/views/WarehousePage.vue'
import InventoryPage from '@/views/InventoryPage.vue'
import InboundPage from '@/views/InboundPage.vue'
import InboundDetailPage from '@/views/InboundDetailPage.vue'
import OutboundPage from '@/views/OutboundPage.vue'
import OutboundDetailPage from '@/views/OutboundDetailPage.vue'
import OrderPage from '@/views/OrderPage.vue'
import WarehouseOrderPage from '@/views/WarehouseOrderPage.vue'
import WarehouseOrderDetailPage from '@/views/WarehouseOrderDetailPage.vue'
import ProductPage from '@/views/ProductPage.vue'
import SkuPage from '@/views/SkuPage.vue'
import StorePage from '@/views/StorePage.vue'
import DeliveryPage from '@/views/DeliveryPage.vue'
import DeliveryDetailPage from '@/views/DeliveryDetailPage.vue'

const ROLES = {
  GM: 'ROLE_GENERAL_MANAGER',
  WM: 'ROLE_WAREHOUSE_MANAGER',
  USER: 'ROLE_USER',
}

const routes = [
  { path: '/',                           component: HomePage },
  { path: '/login',                      component: LoginPage,               meta: { public: true } },
  { path: '/signup',                     component: SignupPage,              meta: { public: true } },

  // 본사 관리자 전용
  { path: '/product',                    component: ProductPage,             meta: { roles: [ROLES.GM] } },
  { path: '/sku',                        component: SkuPage,                 meta: { roles: [ROLES.GM] } },
  { path: '/store',                      component: StorePage,               meta: { roles: [ROLES.GM] } },
  { path: '/order/assign',               component: OrderPage,               meta: { roles: [ROLES.GM] } },

  // 본사 관리자 + 창고 관리자
  { path: '/warehouse',                  component: WarehousePage,           meta: { roles: [ROLES.GM, ROLES.WM] } },
  { path: '/inbound',                    component: InboundPage,             meta: { roles: [ROLES.GM, ROLES.WM] } },
  { path: '/inbound/:id',               component: InboundDetailPage,       meta: { roles: [ROLES.GM, ROLES.WM] } },
  { path: '/outbound',                   component: OutboundPage,            meta: { roles: [ROLES.GM, ROLES.WM] } },
  { path: '/outbound/:id',              component: OutboundDetailPage,      meta: { roles: [ROLES.GM, ROLES.WM] } },
  { path: '/inventory',                  component: InventoryPage,           meta: { roles: [ROLES.GM, ROLES.WM] } },

  // 창고 관리자 전용
  { path: '/order/warehouse-orders',     component: WarehouseOrderPage,      meta: { roles: [ROLES.WM] } },
  { path: '/order/warehouse-orders/:id', component: WarehouseOrderDetailPage, meta: { roles: [ROLES.WM] } },

  // 발주: 본사 관리자(전체 조회/위임) + 점주(발주 등록/내역)
  { path: '/order',                      component: OrderPage,               meta: { roles: [ROLES.GM, ROLES.USER] } },

  // 점주 전용
  { path: '/delivery',                   component: DeliveryPage,            meta: { roles: [ROLES.USER] } },
  { path: '/delivery/:id',              component: DeliveryDetailPage,      meta: { roles: [ROLES.USER] } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  // 공개 페이지는 통과
  if (to.meta.public) return true

  // 로그인 안 됐으면 로그인 페이지로
  if (!auth.isLoggedIn) return '/login'

  // 역할 제한이 있는 페이지인데 역할이 안 맞으면 홈으로
  if (to.meta.roles && !to.meta.roles.includes(auth.user?.role)) return '/'

  return true
})

export default router
