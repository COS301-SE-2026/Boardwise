export interface Citation {
    chunkId: string;
    index: number;
    content: string;
}

export interface QueryResponse {
    answer: string;
    citations: Citation[];
}

export const ragService = {
    queryRulebook(rulebookId: string, query: string) {
        const { $fastApi } = useNuxtApp()
        return $fastApi<QueryResponse>(`vault/rulebooks/${rulebookId}/query`, {
            method: 'POST',
            body: { query }
        })
    }
}