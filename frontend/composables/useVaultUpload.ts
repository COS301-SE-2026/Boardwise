import {ref} from 'vue';
import {useVaultService} from '~/services/vaultService';

export const useVaultUpload = () => {
    // UI reactive state
    const isUploading = ref<boolean>(false);
    const error = ref<string | null>(null);
    const uploadData = ref<any>(null);

    const triggerUpload = async (rulebook: {
        title: string,
        language: string,
        edition?: string,
        file: File
    }) => {
        isUploading.value = true;
        error.value = null;

        const formData = new FormData();
        formData.append('title', rulebook.title);
        formData.append('language', rulebook.language);
        formData.append('file', rulebook.file);
        if(rulebook.edition){
            formData.append('edition', rulebook.edition);
        }

        try{
            const response = await useVaultService.uploadRulebook(formData);
            uploadData.value = response;
            return response;
        }catch(err: any){
            error.value = err.response?._data?.detail || 'An unexpected upload error occured.';
            throw err;
        }finally{
            isUploading.value = false;
        }
    };

    return{
        triggerUpload,
        isUploading,
        error,
        uploadData
    }
}