// https://nuxt.com/docs/api/configuration/nuxt-config

export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',

  devtools: {
    enabled: true
  },

  modules: ['@vite-pwa/nuxt'],

  pwa: {
    manifest: {
      name: 'Boardwise',
      short_name: 'Boardwise',
      theme_color: '#7B2CBF',
      background_color: '#FFF8F0',
      display: 'standalone'
    }
  }
})
