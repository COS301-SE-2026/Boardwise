interface SortResponse{
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
}
interface PageableResponse{
    offset: number;
    pageNumber: number;
    pageSize: number;
    paged: boolean;
    sort: SortResponse;
    unpaged: boolean;
}
interface RulebookResponse{
    id: string;
    coverUrl: string;
    title: string;
    edition: string;
    genres: string[];
    version: number;
    status: string;
    contributorUsername: string;
    description: string;
    language: string;
    lockHeldBy: string;
    uploadedAt: string; // Instant returned as a string
    updatedAt: string; // Instant returned as a string
    minPlayers: number;
    maxPlayers: number;
}
interface PaginatedRulebookResponse{
    content: Array<RulebookSummaryResponse>;
    empty: boolean;
    first: boolean;
    last: boolean;
    number: 0;
    numberOfElements: 3;
    pageable: PageableResponse;
    size: 20;
    sort: SortResponse;
    totalElements: number;
    totalPages: number;
}
interface Chunk{
  chunkId: string;
  index: number;
  content: string;
}
interface RulebookTextResponse{
    rulebookId: string;
    chunks: Chunk[];
    version: number;
    lockHeldBy: string;
    updatedAt: string;
}
interface RulebookSummaryResponse{
    id: string;
    coverUrl: string;
    title: string;
    language: string;
    edition: string;
    version: number;
    genres: string[];
    minPlayers: number;
    maxPlayers: number;
}

export const LibraryService = {
    fetchAllRulebooks(search = '', page = 1, limit = 20) {
        const { $api } = useNuxtApp();
        return $api<PaginatedRulebookResponse>('vault/rulebooks', {
            params: {
                search: search,
                page: page,
                limit: limit
            }
        });
    },

    fetchRulebookById(id: string){
      const { $api } = useNuxtApp();
      return $api<RulebookResponse>(`vault/rulebooks/${id}`)
    },

    fetchRulebookText(id: string) {
      const { $api } = useNuxtApp();
      return $api<RulebookTextResponse>(`vault/rulebooks/${id}/text`)
    },

    // TODO: Please double check added services. Most of it based documents given in regards to requests

    acquireLock(id: string) {
        const { $api } = useNuxtApp()
        return $api<{
            lockGranted: boolean
            lockedBy: string | null
            expiresAt: string | null
        }>(`vault/rulebooks/${id}/lock`, { method: 'POST'})
    }, 

    releaseLock(id: string) {
        const { $api } = useNuxtApp()
        return $api<{
            message: string 
        }>(`vault/rulebooks/${id}/lock`, { method: 'DELETE'})
    },

    releaseAllLocks(id: string) {
        const { $api } = useNuxtApp()
        return $api<{
            message: string 
        }>(`vault/rulebooks/${id}/lock/all`, { method: 'DELETE'})
    }, 

    commitDelta(id: string, payload: {
        chunkId: string
        deltaContent: string
        expectedVersion: number
    }) {
        const { $api } = useNuxtApp()
        return $api<{
            newVersion: number
            chunkId: string
            updatedAt: string
        }>(`vault/rulebooks/${id}/text`, { method: 'PATCH', body: payload })
    },

    insertChunk(id: string, payload: {
        content: string
        insertIndex: number 
        expectedVersion: number
    }) {
        const { $api } = useNuxtApp()
        return $api<{
            newVersion: number
            chunkId: string
            actualIndex: number
        }>(`vault/rulebooks/${id}/chunk`, { method: 'POST', body: payload })
    },

    removeChunk(id: string, payload: {
        chunkId: string
        expectedVersion: number
    }) {
        const { $api } = useNuxtApp()
        return $api<{
            newVersion: number
        }>(`vault/rulebooks/${id}/chunk`, { method: 'DELETE', body: payload })
    },

    undoEdit(id: string, payload: {
        expectedVersion: number
    }) {
        const { $api } = useNuxtApp()
        return $api<{
            newVersion: number
        }>(`vault/rulebooks/${id}/text/undo`, { method: 'POST', body: payload })
    },

    redoEdit(id: string, payload: {
        expectedVersion: number
    }) {
        const { $api } = useNuxtApp()
        return $api<{
            newVersion: number
        }>(`vault/rulebooks/${id}/text/redo`, { method: 'POST', body: payload })
    }
}