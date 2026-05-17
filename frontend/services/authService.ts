import axios from "axios";

// create custom axios instance
const api = axios.create({
  baseURL: 'http://localhost:8080/api/auth/',
})

// Add a request interceptor
api.interceptors.request.use(
  (config) => {
    // Grab the token directly from local storage
    const token = localStorage.getItem('access_token')
    
    // If the token exists, attach it to the Authorization header
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

export default api;