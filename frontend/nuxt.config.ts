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
          rounded: 'pill',
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
              primary: '#C7286E',      
              secondary: '#4E1E5C',    
              accent: '#EF5B27',       
              error: '#C62828',        

              background: '#FBF6F0',   
              surface: '#FFFFFF',      

              success: '#2E7D5B',      
              warning: '#B7791F',      
              info: '#4E1E5C'          
            }
          },

          boardwiseDark: {
            dark: true,

            colors: {
              primary: '#E64C86',      
              secondary: '#F0D9EC',    
              accent: '#EF5B27',       
              error: '#C62828',        

              background: '#17101C',   
              surface: '#241629',      

              success: '#2E7D5B',      
              warning: '#B7791F',      
              info: '#F0D9EC'          
            }
          }
        }
      }
    }
  },
  runtimeConfig: {
    public: {
      // Once backend is deployed, we must change the URL to match
      apiBase: process.env.NODE_ENV === 'prod' ? 'https://api.our-production-domain.com' : 'http://localhost:8080/api/',
      wsBaseUrl: process.env.NODE_ENV === 'prod' ? 'wss://api.our-production-domain.com/api/stomp' : 'ws://localhost:8080/api/stomp',
      fastApiBase: process.env.NODE_ENV === 'prod' ? 'https://fastapi.our-production-domain.com' : 'http://localhost:8000/api/'
    }
  }
})