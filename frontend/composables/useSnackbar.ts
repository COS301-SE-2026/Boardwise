const message = ref('')
const color = ref('success')
const visible = ref(false)

export const useSnackBar = () =>{
    const show = (msg:string, type: 'success'|'error'| 'info' = 'success')=>{
        message.value = msg;
        color.value = type;
        visible.value = true;
    }
    return {message, visible, color, show}
}
