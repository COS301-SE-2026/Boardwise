import { computed, onMounted, onUnmounted, ref } from 'vue'
interface InstallEvent extends Event {
  prompt: () => Promise<void>
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>
}
export function usePwaInstall() {
  const deferred = ref<InstallEvent | null>(null)
  const installed = ref(false)
  const ios = ref(false)
  const dismissed = ref(false)
  const busy = ref(false)
  const error = ref('')
  let display: MediaQueryList | undefined
  const sync = () => { installed.value = Boolean(display?.matches || (navigator as Navigator & { standalone?: boolean }).standalone) }
  const capture = (event: Event) => { event.preventDefault(); deferred.value = event as InstallEvent }
  const complete = () => { installed.value = true; deferred.value = null }
  onMounted(() => {
    display = window.matchMedia('(display-mode: standalone)'); sync()
    ios.value = /iPad|iPhone|iPod/.test(navigator.userAgent) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1)
    try { dismissed.value = sessionStorage.getItem('boardwise-install-dismissed') === '1' } catch { /* storage may be unavailable */ }
    window.addEventListener('beforeinstallprompt', capture)
    window.addEventListener('appinstalled', complete)
    display.addEventListener('change', sync)
  })
  onUnmounted(() => {
    window.removeEventListener('beforeinstallprompt', capture)
    window.removeEventListener('appinstalled', complete)
    display?.removeEventListener('change', sync)
  })
  const dismiss = () => { dismissed.value = true; try { sessionStorage.setItem('boardwise-install-dismissed', '1') } catch {} }
  const install = async () => {
    if (!deferred.value || busy.value) return
    busy.value = true; error.value = ''
    try {
      await deferred.value.prompt()
      const result = await deferred.value.userChoice
      if (result.outcome === 'accepted') installed.value = true
      else dismiss()
      deferred.value = null
    } catch { error.value = 'Installation could not start. Try your browser’s install menu.' }
    finally { busy.value = false }
  }
  return { visible: computed(() => !installed.value && !dismissed.value && (Boolean(deferred.value) || ios.value)), ios, busy, error, install, dismiss }
}
