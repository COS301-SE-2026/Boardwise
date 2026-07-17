interface UploadResponse{
    message: string;
    rulebook_id: string;
    job_id: string;
}
interface IngestionJob{
    _id: string;
    rulebookId: string;
    stage: string;
    jobStatus: string;
    failureReason: string | null;
    startedAt: string;
    completedAt: string;
}
export const useVaultService = {
    uploadRulebook(formData: FormData){
        const {$fastApi} = useNuxtApp();
        return $fastApi<UploadResponse>('vault/rulebooks/upload',{
            method: 'POST',
            body: formData
        });
    },
    getJobStatus(jobId: string){
        const {$fastApi} = useNuxtApp();
        return $fastApi<IngestionJob>(`vault/rulebooks/status/${jobId}`,{
            method: 'GET'
        });
    }
}