// nuxt.config.js
export default defineNuxtConfig({
  ssr: false,
  
  app: {
    head: {
      link: [
        {
          rel: 'manifest',
          href: '/manifest.webmanifest'
        }
      ]
    }
  },

  css: ['~/assets/theme.css'],

  modules: ['vuetify-nuxt-module', '@vite-pwa/nuxt'],

  pwa: {
  registerType: 'autoUpdate',

  manifest: {
    id: '/',
    name: 'Boardwise',
    short_name: 'Boardwise',
    description: 'A social platform for board game enthusiasts.',
    theme_color: '#C7286E',
    background_color: '#FBF6F0',
    display: 'standalone',
    start_url: '/',
    scope: '/',
    icons: [
      {
        src: '/icon-192.png',
        sizes: '192x192',
        type: 'image/png'
      },
      {
        src: '/icon-512.png',
        sizes: '512x512',
        type: 'image/png'
      }
    ]
  },

  workbox: {
    navigateFallback: '/'
  },

  devOptions: {
    enabled: true
  }
},
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
      apiBase: process.env.APP_ENV === 'prod' ? process.env.PROD_API_BASE : process.env.DEV_API_BASE,
      wsBaseUrl: process.env.APP_ENV === 'prod' ? process.env.PROD_WS_API_BASE : process.env.DEV_WS_API_BASE,
      fastApiBase: process.env.APP_ENV === 'prod' ? process.env.PROD_FAST_API_BASE : process.env.DEV_FAST_API_BASE
    }
  }

  
})