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

        VCard: {
          rounded: 'xl',
          elevation: 1
        },

        VTextField: {
          variant: 'outlined',
          rounded: 'lg'
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
  }
})