export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig();

  const fetchOptions = {
    onRequest({options}){
      if(import.meta.client){
        const token = localStorage.getItem('access_token');
        if(token){
          options.headers = new Headers(options.headers || {});
          options.headers.set('Authorization', `Bearer ${token}`);
        }
      }
    },
    onResponseError({response}){
      if(response.status === 401){
        console.error('Unauthorized: Invalid or expired token');
        if(import.meta.client){
          localStorage.removeItem('access_token');
          navigateTo('/auth/signin');
        }
      }
    }
  };

  const api = $fetch.create({
    baseURL: config.public.apiBase as string,
    ...fetchOptions
  });

  const fastApi = $fetch.create({
    baseURL: config.public.fastApiBase as string,
    ...fetchOptions
  });

  return {
    provide: { // these will be globally injected as $api and $fastApi
      api,
      fastApi
    }
  };
});