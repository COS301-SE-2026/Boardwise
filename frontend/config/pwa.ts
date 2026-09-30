import type { ModuleOptions } from '@vite-pwa/nuxt'

export const pwaConfig: Partial<ModuleOptions> = {
  registerType: 'prompt',

  manifest: {
    id: '/',
    name: 'Boardwise',
    short_name: 'Boardwise',
    description: 'A social platform for board game enthusiasts.',
    lang: 'en',
    theme_color: '#C7286E',
    background_color: '#FBF6F0',
    display: 'standalone',
    start_url: '/library',
    scope: '/',
    icons: [
      {
        src: '/icon-192.png',
        sizes: '192x192',
        type: 'image/png',
        purpose: 'any'
      },
      {
        src: '/icon-512.png',
        sizes: '512x512',
        type: 'image/png',
        purpose: 'any'
      },
      {
        src: '/icon-maskable-512.png',
        sizes: '512x512',
        type: 'image/png',
        purpose: 'maskable'
      }
    ]
  },

  includeAssets: [
    'icon-192.png',
    'icon-512.png',
    'icon-maskable-512.png',
    'apple-touch-icon.png',
    'offline.html'
  ],

  workbox: {
    cleanupOutdatedCaches: true,
    navigateFallback: undefined,
    globPatterns: ['**/*.{js,css,html,ico,png,svg,woff,woff2}'],
    ignoreURLParametersMatching: [/^utm_/, /^fbclid$/],
    runtimeCaching: [
      {
        urlPattern: ({ request, url }) =>
          request.mode === 'navigate' &&
          url.origin === self.location.origin &&
          !url.search &&
          !/^\/(api|auth)(\/|$)/.test(url.pathname),
        handler: 'NetworkFirst',
        options: {
          cacheName: 'boardwise-pages-v1',
          networkTimeoutSeconds: 4,
          cacheableResponse: {
            statuses: [200]
          },
          expiration: {
            maxEntries: 40,
            maxAgeSeconds: 7 * 24 * 60 * 60
          },
          precacheFallback: {
            fallbackURL: '/offline.html'
          }
        }
      }
    ]
  },

  devOptions: {
    enabled: false
  }
}