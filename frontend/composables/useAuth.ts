import { ref } from 'vue'

export const useAuth = () => {
  // Grab the Axios instance from the Nuxt plugin
  const { $api } = useNuxtApp()
  const router = useRouter()

  // Safely initialize state 
  const token = ref<string | null>(
    import.meta.client ? localStorage.getItem('access_token') : null
  )
  const isAuthenticated = ref<boolean>(!!token.value)
  const error = ref<string>('')
  const loading = ref<boolean>(false)

  // Helper function to update both storage and reactive state
  const setSession = (newToken: string) => {
    if (import.meta.client) {
      localStorage.setItem('access_token', newToken)
    }
    token.value = newToken
    isAuthenticated.value = true
  }

  const login = async (credentials: any): Promise<boolean> => {
    error.value = ''
    loading.value = true
    
    try {
      const response = await $api.post('/auth/login', credentials)
      setSession(response.data.accessToken)
      return true
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Invalid credentials'
      return false
    } finally {
      loading.value = false
    }
  }

  const register = async (userData: any): Promise<boolean> => {
    error.value = ''
    loading.value = true
    
    try {
      const response = await $api.post('/auth/register', userData)
      setSession(response.data.accessToken)
      return true
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Registration failed'
      return false
    } finally {
      loading.value = false
    }
  }

  const logout = () => {
    if (import.meta.client) {
      localStorage.removeItem('access_token')
    }
    token.value = null
    isAuthenticated.value = false
    router.push('/auth/signin')
  }

  return {
    token,
    isAuthenticated,
    error,
    loading,
    login,
    register,
    logout
  }
}