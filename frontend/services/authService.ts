import api from './api'

export const AuthService = {
  register(userData: any){
    return api.post('auth/register', userData);
  },

  login(credentials: any){
    return api.post('auth/login', credentials);
  },
  
  // logout(){
  //   return api.delete('auth/logout');
  // },
}