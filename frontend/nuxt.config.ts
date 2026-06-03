// nuxt.config.js
export default defineNuxtConfig({
  ssr: false,
  
  css: ['~/assets/theme.css'],

  modules: ['vuetify-nuxt-module'],

  vuetify: {
    moduleOptions: {
      styles: {
        configFile: process.env.NODE_ENV === 'prod' ? 'assets/settings.scss' : 'assets/empty.scss'
      }
    },

    vuetifyOptions: {
      defaults: {
        VBtn: {
          rounded: 'lg',
          elevation: 0,
          class: 'text-none'
        },

        VTextField: {
          variant: 'outlined',
          density: 'compact',
          hideDetails: true,
          rounded: 'lg',
        },

        VSelect: {
          variant: 'outlined',
          density: 'compact',
          hideDetails: true,
          rounded: 'lg',
        },

        VTextarea: {
          variant: 'outlined',
          density: 'compact',
          hideDetails: true,
          rounded: 'lg',
        },

        VCard: {
          rounded: 'xl',
          elevation: 1
        }
      },

      theme: {
        defaultTheme: 'boardwise',

        themes: {
          boardwise: {
            dark: false,

            colors: {
              primary: '#6D0037',
              secondary: '#1A1430',
              accent: '#C9A86A',
              error: '#E4572E',

              background: '#F9FAFB',
              surface: '#FFFFFF',

              success: '#2E7D5B',
              warning: '#B7791F',
              info: '#7C3AED'
            }
          }
        }
      }
    }
  },
  runtimeConfig: {
    public: {
      // Once backend is deployed, we must change the URL to match
      apiBase: process.env.NODE_ENV === 'prod' ? 'https://api.our-production-domain.com' : 'http://127.0.0.1:8080/api/'
    }
  },

  // Proxy for requests to Spring Boot
  routeRules: {
    '/api/**': {
      proxy: 'http://127.0.0.1:8080/api/**'
    }
  }
})