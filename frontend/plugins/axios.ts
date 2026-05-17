import axios from 'axios'

export default defineNuxtPlugin((nuxtApp) => {
  const api = axios.create({
    // We point to '/api' so that Nuxt can proxy the request to Spring Boot
    baseURL: '/api' 
  })

  // 1. Request Interceptor (Attach Token)
  api.interceptors.request.use((config) => {
    // import.meta.client ensures this only runs in the browser, not on the server
    if (import.meta.client) {
      const token = localStorage.getItem('access_token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    return config
  })

  // 2. Response Interceptor (Handle Expired Tokens)
  api.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401 && import.meta.client) {
        localStorage.removeItem('access_token')
        const router = useRouter()
        router.push('/auth/signin')
      }
      return Promise.reject(error)
    }
  )

  // Provide the api instance to the rest of your Nuxt app
  return {
    provide: {
      api
    }
  }
})