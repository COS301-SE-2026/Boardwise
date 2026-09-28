import type { ModuleOptions } from '@vite-pwa/nuxt'

/** Precache public assets and static page shells. Never cache API responses or user uploads. */
export const pwaConfig: Partial<ModuleOptions> = {
  registerType: 'autoUpdate',
  manifest: false,
  includeAssets: ['manifest.webmanifest', 'icon-192.png', 'icon-512.png',
    'icon-maskable-512.png', 'apple-touch-icon.png', 'offline.html'],
  workbox: {
    cleanupOutdatedCaches: true,
    navigateFallback: undefined,
    globPatterns: ['**/*.{js,css,html,ico,png,svg,woff,woff2}'],
    globIgnores: ['**/manifest.webmanifest'],
    ignoreURLParametersMatching: [/^utm_/, /^fbclid$/],
    runtimeCaching: [{
      // Cache only clean, same-origin page navigations, never API/auth callbacks.
      urlPattern: ({ request, url }) => request.mode === 'navigate' &&
        url.origin === self.location.origin && !url.search &&
        !/^\/(api|auth)(\/|$)/.test(url.pathname),
      handler: 'NetworkFirst',
      options: {
        cacheName: 'boardwise-pages-v1',
        networkTimeoutSeconds: 4,
        cacheableResponse: { statuses: [200] },
        expiration: { maxEntries: 40, maxAgeSeconds: 7 * 24 * 60 * 60 },
        precacheFallback: { fallbackURL: '/offline.html' }
      }
    }]
  },
  devOptions: { enabled: false }
}
