import { ref, watch, onUnmounted, toValue, type MaybeRefOrGetter } from 'vue'

export function useElapsedTimer(startedAt: MaybeRefOrGetter<number>) {
    const elapsed = ref('00:00:00')
    let intervalId: ReturnType<typeof setInterval> | undefined

    const tick = () => {
        const start = toValue(startedAt)
        if (!start) {
            elapsed.value = '00:00:00'
            return
        }
        const totalSeconds = Math.max(0, Math.floor((Date.now() - start) / 1000))
        const h = String(Math.floor(totalSeconds / 3600)).padStart(2, '0')
        const m = String(Math.floor((totalSeconds % 3600) / 60)).padStart(2, '0')
        const s = String(totalSeconds % 60).padStart(2, '0')
        elapsed.value = `${h}:${m}:${s}`
    }

    watch(() => toValue(startedAt), tick, { immediate: true })
    intervalId = setInterval(tick, 1000)

    onUnmounted(() => {
        if (intervalId) clearInterval(intervalId)
    })

    return elapsed
}