import type ForgotPassword from "~/components/features/auth/ForgotPassword.vue";

interface AuthResponse{
  message: string;
  accessToken: string;
}

interface LogoutResponse{}

interface ForgotPasswordResponse {
  message: string;
}

interface ResetPasswordResponse {
  message: string;
}

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

  forgotPassword(emailAddress: string){
    const { $api } = useNuxtApp();
    return $api<ForgotPasswordResponse>('auth/forgotPassword', {
      method: 'POST',
      body: { emailAddress }
    });
  },

  resetPasswprd(payload: { token: string | string[], password: string }){
    const { $api } = useNuxtApp();
    return $api<ResetPasswordResponse>('auth/resetPassword', {
      method: 'POST',
      body: payload
    });
  },
}