import {ref} from 'vue'
import { useRouter } from 'vue-router'
import { AuthService } from '~/services/authService'

export const useAuth = () => {
  const router = useRouter();

  // Safe state initialization
  const token = ref<string|null>(
    import.meta.client ? localStorage.getItem('access_token') : null
  )
  const isAuthenticated = ref<boolean>(!!token.value)
  const error = ref<string>('');
  const loading = ref<boolean>(false);

  // Helper for session management
  const setSession = (newToken: string) => {
    if(import.meta.client){
      localStorage.setItem('access_token', newToken);
    }
    token.value = newToken;
    isAuthenticated.value = true;
  }

  const register = async (userData: any): Promise<boolean> => {
    error.value = '';
    loading.value = true;

    try{
      const response = await AuthService.register(userData);
      setSession(response.data.accessToken);
      return true;
    }catch(err: any){
      error.value = err.response?.data?.message || 'Registration failed';
      return false;
    }finally{
      loading.value = false;
    }
  }

  const login = async (credentials: any): Promise<boolean> => {
    error.value = '';
    loading.value = true;
    try{
      const response = await AuthService.login(credentials);
      setSession(response.data.accessToken);
      return true;
    }catch(err: any){
      error.value = err.response?.data?.message || 'Invalid credentials'
      return false
    } finally {
      loading.value = false;
    }
  }

  const logout = () => {
    if(import.meta.client){
      localStorage.removeItem('access_token');
    }
    token.value = null
    isAuthenticated.value = false;
    router.push('/login');
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