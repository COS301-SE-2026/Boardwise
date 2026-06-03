export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig();

  const api = $fetch.create({
    baseURL: config.public.apiBase as string,

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
          navigateTo('auth/signin');
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