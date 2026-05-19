export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: {
    enabled: true
  },
  components: [
    {
      path: '~/components',
      pathPrefix: false,  
    }
  ],
  modules: ['@vite-pwa/nuxt'],
  pwa: {
    manifest: {
      name: 'Boardwise',
      short_name: 'Boardwise',
      theme_color: '#7B2CBF',
      background_color: '#FFF8F0',
      display: 'standalone'
    }
  },

  routeRules: {
    // Route all requests starting with /api to Spring Boot
    '/api/**': {
      proxy: 'http://localhost:8080/api/**'
    }
  }
})