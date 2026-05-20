// nuxt.config.js
export default defineNuxtConfig({
  // 1. Load your design tokens explicitly
  css: ['~/assets/theme.css'],

  modules: ['vuetify-nuxt-module'],

  vuetify: {
    moduleOptions: {
      // Directs Vuetify to load variable configurations before template compiling
      styles: { configFile: 'assets/settings.scss' }
    },
    vuetifyOptions: {
      theme: {
        defaultTheme: 'boardwise',
        themes: {
          boardwise: {
            dark: false,
            colors: {
              // Direct mappings to the exact variables present in your theme.css
              primary: 'var(--bw-maroon)',
              'primary-dark': 'var(--bw-maroon-deep)',
              secondary: 'var(--bw-navy)',
              accent: 'var(--bw-gold)',
              error: 'var(--bw-accent-coral)',
              background: 'var(--color-bg)',
              surface: 'var(--color-surface)',
            }
          }
        }
      }
    }
  }
})