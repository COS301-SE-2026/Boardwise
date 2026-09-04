import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { AuthService } from '~/services/authService'

export const required = (message : string = 'This field is required') => (value: any) => {
  return (value !== null && value !== undefined && String(value).trim() !== '') || message 
}

export const isEmail = (message ?: string) => (value : any) => {
  if(!value) return true

  if (/\s/.test(value)){
    return message || 'Email cannot contain spaces'
  }

  const atIndex = value.indexOf('@')
  if(atIndex <= 0) {
    return message || 'Email must include an "@" before the domain'
  }

  if(value.indexOf('@', atIndex + 1) !== -1) {
    return message || 'Email can only contain one "@"'
  }

  const domain = value.slice(atIndex + 1) 
  if(!domain.includes('.')) {
    return message || 'Email domain must include a "." (e.g. name@example.com)'
  }

  if(domain.startsWith('.') || domain.endsWith('.')) {
    return message || 'Email domain is not valid'
  }

  return true
}

export const minLength = (length: number, message?: string) => (value: string): true | string => {
  if(!value) return true
  return value.length >= length || message ||'Must be at least 3 characters'
}

const token = ref<string|null>(
  import.meta.client ? localStorage.getItem('access_token') : null
)
const isAuthenticated = ref<boolean>(!!token.value)
const user = ref<any>(
  import.meta.client ? JSON.parse(localStorage.getItem('user_data') || 'null') : null
)
const error = ref<string>('');
const isLoading = ref<boolean>(false);

export const useAuth = () => {
  const router = useRouter();

  // Helper for session management
  const setSession = (newToken: string, userData?: any) => {
    if(import.meta.client){
      localStorage.setItem('access_token', newToken);
      if(userData){
        localStorage.setItem('user_data', JSON.stringify(userData));
      }
    }
    token.value = newToken;
    isAuthenticated.value = true;
    if(userData) user.value = userData;
  }

  const register = async (userData: any): Promise<boolean> => {
    error.value = '';
    isLoading.value = true;

    try{
      const response = await AuthService.register(userData);
      setSession(response.accessToken, {
        username: userData.username,
        email: userData.emailAddress,
        firstName: userData.firstName,
        lastName: userData.lastName
      });

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
      setSession(response.accessToken, response.user);
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
        localStorage.removeItem('user_data');
        isLoading.value = false;
      }
    }
    token.value = null
    user.value = null
    isAuthenticated.value = false;
    router.push('/auth/signin');
  }

  const forgotPassword = async (emailAddress: string): Promise<boolean> => {
    error.value = ''
    isLoading.value = true;

    try {
      await AuthService.forgotPassword(emailAddress);
      return true;
    } catch(err: any){
      error.value = err.data?.message || 'Could not send reset link. Please try again.';
      return false;
    } finally {
      isLoading.value = false;
    }
  }

  const resetPassword = async (payload: { token: string | string[], password: string }): Promise<boolean> => {
    error.value = ''
    isLoading.value = true;

    try {
      await AuthService.resetPasswprd(payload);
      return true;
    } catch(err: any){
      error.value = err.data?.message || 'Could not send reset link. Please try again.';
      return false;
    } finally {
      isLoading.value = false;
    }
  }

  return {
    token,
    user,
    isAuthenticated,
    error,
    isLoading,
    login,
    register,
    logout,
    forgotPassword,
    resetPassword
  }
}