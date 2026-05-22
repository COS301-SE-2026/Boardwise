import axios from "axios";

// 1. Use a relative path so the Nuxt Proxy handles it and prevents CORS
const api = axios.create({
  baseURL: '/api/', 
})

api.interceptors.request.use(
  (config) => {
    // 2. SSR Check: Only attempt to read localStorage if running in the browser
    if (import.meta.client) {
      const token = localStorage.getItem('access_token')
      
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

export default api;