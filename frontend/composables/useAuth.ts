import {ref} from 'vue'
import { useRouter } from 'vue-router'
import { AuthService } from '@/services/authService'

export const useAuth = () => {
  const router = useRouter();

  // Safe state initialization
  const token = ref<string|null>(
    import.meta.client ? localStorage.getItem('access_token') : null
  )
  const isAuthenticated = ref<boolean>(!!token.value)
  const error = ref<string>('');
  const isLoading = ref<boolean>(false);

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
    isLoading.value = true;

    try{
      const response = await AuthService.register(userData);
      setSession(response.accessToken);
      return true;
    }catch(err: any){
      error.value = err.data?.message || 'Registration failed';
      return false;
    }finally{
      isLoading.value = false;
    }
  }

  const login = async (credentials: any): Promise<boolean> => {
    error.value = '';
    isLoading.value = true;
    try{
      const response = await AuthService.login(credentials);
      setSession(response.accessToken);
      return true;
    }catch(err: any){
      error.value = err.data?.message || 'Invalid credentials';
      return false;
    } finally {
      isLoading.value = false;
    }
  }

  const logout = async () => {
    error.value = '';
    isLoading.value = true;
    if(import.meta.client){
      try{
        const response = await AuthService.logout();
        return true;
      }catch(err: any){
        error.value = err.data?.message || 'Invalid credentials';
      }finally{
        localStorage.removeItem('access_token');
        isLoading.value = false;
      }
    }
    token.value = null
    isAuthenticated.value = false;
    router.push('/login');
  }
  return {
    token,
    isAuthenticated,
    error,
    isLoading,
    login,
    register,
    logout
  }
}