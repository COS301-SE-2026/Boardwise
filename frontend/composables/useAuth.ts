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
  
  return {

  }
}