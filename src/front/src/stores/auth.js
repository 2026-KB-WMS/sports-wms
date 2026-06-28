import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  // Spring 세션 기반이라 로그인 여부를 로컬에서 관리
  // 실제 인증은 Spring Security가 세션 쿠키로 처리
  const user = ref(JSON.parse(sessionStorage.getItem('wms_user') || 'null'))

  const isLoggedIn = computed(() => !!user.value)
  const isGeneralManager = computed(() => user.value?.role === 'ROLE_GENERAL_MANAGER')
  const isWarehouseManager = computed(() => user.value?.role === 'ROLE_WAREHOUSE_MANAGER')
  const isUser = computed(() => user.value?.role === 'ROLE_USER')

  function setUser(userData) {
    user.value = userData
    sessionStorage.setItem('wms_user', JSON.stringify(userData))
  }

  function clearUser() {
    user.value = null
    sessionStorage.removeItem('wms_user')
  }

  return { user, isLoggedIn, isGeneralManager, isWarehouseManager, isUser, setUser, clearUser }
})
