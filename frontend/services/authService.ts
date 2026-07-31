interface AuthResponse{
  message: string;
  accessToken: string;
}

interface LogoutResponse{}

export const AuthService = {
  register(userData: any){
    const { $api } = useNuxtApp();
    return $api<AuthResponse>('auth/register', {
      method: 'POST',
      body: userData
    });
  },

  login(credentials: any){
    const { $api } = useNuxtApp();
    return $api<AuthResponse>('auth/login', {
      method: 'POST',
      body: credentials
    });
  },
  
  logout(){
    const { $api } = useNuxtApp();
    return $api<LogoutResponse>('auth/logout',{
      method: 'DELETE'
    });
  },
}