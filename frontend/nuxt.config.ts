// nuxt.config.js
export default defineNuxtConfig({
  css: ['~/assets/theme.css'],

  modules: ['vuetify-nuxt-module'],

  vuetify: {
    moduleOptions: {
      styles: {
        configFile: 'assets/settings.scss'
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

  // Add this block to proxy requests to Spring Boot
  routeRules: {
    '/api/**': {
      proxy: 'http://localhost:8080/api/**'
    }
  }
})