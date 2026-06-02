export default defineNuxtPlugin(() => {
  const api = $fetch.create({
    baseURL: '/api/',

    onRequest({options}){
      if(import.meta.client){
        const token = localStorage.getItem('access_token');

        if(token){
          options.headers = new Headers(options.headers || {});
          options.headers.set('Authorization', `Bearer ${token}`); // Secure token append
        }
      }
    },

    onResponseError({response}){
      if(response.status === 401){
        console.error('Unauthorized: Token may be invalid or expired');

        if(import.meta.client){
          // Remove old token
          localStorage.removeItem('access_token');
          // Trigger router navigation to login.
          navigateTo('/login');
        }
      }
    }
  });

  return{
    provide:{ // Returning  inside a provide block makes Nuxt inject it globally
      api
    }
  };
});