import { ref } from 'vue'

export type SnackBarType =
  | 'success'
  | 'error'
  | 'info'
  | 'warning'

const message = ref('')
const color = ref<SnackBarType>('success')
const visible = ref(false)

export const useSnackBar = () => {
  const show = (
    msg: string,
    type: SnackBarType = 'success'
  ) => {
    message.value = msg
    color.value = type
    visible.value = true
  }

  const hide = () => {
    visible.value = false
  }

  return {
    message,
    visible,
    color,
    show,
    hide
  }
}