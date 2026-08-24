import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mockNuxtImport } from '@nuxt/test-utils/runtime'

const fastApiMock = vi.fn();

mockNuxtImport('useNuxtApp', ()=>{
    return ()=>({$fastApi: fastApiMock});
});

const { useVaultService } = await import('~/services/vaultService')

describe('useVaultService', ()=>{
    beforeEach(()=>{
        fastApiMock.mockReset();
    });

    describe('uploadRulebook',()=>{
        it('calls $fastApi with POST and the FormData body', async ()=>{
            //ARRANGE

            const mockResponse = {
                message: 'uploaded',
                rulebook_id: 'someId-2313',
                job_id: 'job-456',
            }

            fastApiMock.mockResolvedValue(mockResponse);

            const formData  = new FormData();
            formData.append('file', new File(['pdf nonsense'], 'rules.pdf', {type: 'application/pdf'}));

            //ACT
            const res = await useVaultService.uploadRulebook(formData);

            //ASSERT
            expect(fastApiMock).toHaveBeenCalledWith('vault/rulebooks/upload', {
                method: 'POST',
                body: formData,
            });
            expect(res).toEqual(mockResponse);
        })     
    })
    describe('getJobStatus', () => {
        it('calls $fastApi with GET and the correct job status URL', async () => {
        // ARRANGE
        const mockJob = {
            _id: 'ing-1',
            rulebookId: 'rb-123',
            stage: 'parsing',
            jobStatus: 'in_progress',
            failureReason: null,
            startedAt: '2026-07-22T10:00:00Z',
            completedAt: '',
        }
        fastApiMock.mockResolvedValue(mockJob)

        // ACT
        const result = await useVaultService.getJobStatus('job-456')

        // ASSERT
        expect(fastApiMock).toHaveBeenCalledWith('vault/rulebooks/status/job-456', {
            method: 'GET',
        })
        expect(result).toEqual(mockJob)
        })

        it('appends the jobId correctly into the URL', async () => {
        fastApiMock.mockResolvedValue({})

        await useVaultService.getJobStatus('some-weird-id-999')

        expect(fastApiMock).toHaveBeenCalledWith(
            'vault/rulebooks/status/some-weird-id-999',
            { method: 'GET' }
        )
        })

        it('propagates errors from $fastApi', async () => {
        fastApiMock.mockRejectedValue(new Error('Job not found'))

        await expect(useVaultService.getJobStatus('bad-id')).rejects.toThrow('Job not found')
        })
    })
})