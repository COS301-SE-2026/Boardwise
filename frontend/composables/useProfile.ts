import { userService } from "~/services/userService";
import { useRouter } from "vue-router";

export const useProfile = () => {
    const isLoading = ref(false)
    const router = useRouter()
    const error = ref('')

    const fetchCurrentUser = async () => {
        isLoading.value = true;

        try{
            const res = await userService.getCurrentUser()
            console.log(res)
            return res
        }
        catch(err: any){
            error.value = err.data?.message || "This user does not exist"
            if(err.response?.status === 401){
                localStorage.removeItem("access_token")
                router.push('/auth/signin')
            }
                
        }
        finally{
            isLoading.value = false
        }
    }

    const updateProfile = async (username: string) => {
        isLoading.value = true;

        try{
            const res = await userService.updateProfile(username)
            console.log(res)
            return res
        }
        catch(err: any){
            error.value = err.data?.message || "Profile update failed"
            if(err.response?.status === 401)
                router.push('/auth/signin')
        }
        finally{
            isLoading.value = false
        }
    };

    return { isLoading, fetchCurrentUser, updateProfile }
}