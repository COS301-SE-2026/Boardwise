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

        // Trigger router navigation to login.
      }
    }
  });

  return{
    provide:{ // Returning  inside a provide block makes Nuxt inject it globally
      api
    }
  };
});