import { ref, onUnmounted, type Ref } from 'vue'

export function useElapsedTimer(startedAt: Ref<number>) {
    const elapsed = ref('00:00:00')
    let intervalId: ReturnType<typeof setInterval>

    const tick = () => {
        const totalSeconds = Math.max(0, Math.floor((Date.now() - startedAt.value) /1000))
        const h = String(Math.floor(totalSeconds / 3600)).padStart(2, '0')
        const m = String(Math.floor((totalSeconds % 3600) / 60)).padStart(2, '0')
        const s = String(totalSeconds % 60).padStart(2, '0')
        elapsed.value = `${h}:${m}:${s}`
    }

    tick()
    intervalId = setInterval(tick, 1000)
    onUnmounted(() => { if (intervalId) clearInterval(intervalId) })

    return elapsed
}