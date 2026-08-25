export interface Citation {
    chunkId: string;
    index: number;
    content: string;
    relevanceScore: number;
}

export interface QueryResponse {
    answer: string;
    citations: Citation[];
}

export const ragService {
    // TODO: Fix integration 
    queryRulebook(rulebookId: string, query: string) {
        const { $api } = useNuxtApp()
        return $api<QueryResponse>(`vault/rulebooks/${rulebookId}/query`, {
            method: 'POST',
            body: { query }
        })
    }
}