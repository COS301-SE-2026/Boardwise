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
    lockExpiresAt: string;
    uploadedAt: string; // Instant returned as a string
    updatedAt: string; // Instant returned as a string
    minPlayers: number;
    maxPlayers: number;
    minAge: number;
    duration: number;
}
interface PaginatedRulebookResponse{
    content: Array<RulebookSummaryResponse>;
    empty: boolean;
    first: boolean;
    last: boolean;
    number: number;
    numberOfElements: number;
    pageable: PageableResponse;
    size: number;
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
    minAge: number;
    duration: number;
}
interface DownloadUrlResponse{
    downloadUrl: string;
    expiresAt: string;
}
interface EditEventResponse{
    id: string;
    rulebookId: string;
    editor: string;
    chunkId: string;
    editType: string;
    previousContent: string;
    newContent: string;
    versionPostEdit: number;
    committedAt: string;
}

interface EditHistoryResponse{
    rulebookId: string;
    totalEdits: number;
    edits: EditEventResponse[];
}
interface AcquireWriteLockResponse{
    lockGranted: boolean;
    lockedBy: string;
    expiresAt: string;
    currentVersion: number;
}
interface CommitEditDeltaResponse{
    committed: boolean;
    newVersion: number;
    committedAt: string;
    lockExpiresAt: string;
}

interface UndoOrRedoActionResponse{
    done: boolean;
    newVersion: number;
    chunkId: string;
    doneAt: string;
    lockExpiresAt: string;
}

export const LibraryService = {
    fetchAllRulebooks(filters:{}) {
        const { $api } = useNuxtApp();

        const query = Object.fromEntries(
            Object.entries(filters).filter(([_, v]) => 
                v != null &&
                v !== '' &&
                v !== 'all' &&
                v !== 'All' &&
                !(Array.isArray(v) && v.length === 0)
            )
        );

        return $api<PaginatedRulebookResponse>('vault/rulebooks', {
            method: 'GET',
            query: query
        });
    },

    fetchRulebookById(id: string){
        const { $api } = useNuxtApp();
        return $api<RulebookResponse>(`vault/rulebooks/${id}`);
    },

    fetchRulebookText(id: string) {
        const { $api } = useNuxtApp();
        return $api<RulebookTextResponse>(`vault/rulebooks/${id}/text`);
    },

    fetchDownloadRulebook(id: string){
        const {$api} = useNuxtApp();
        return $api<DownloadUrlResponse>(`vault/rulebooks/${id}/download`);
    },

    fetchEditHistory(id: string){
        const {$api} = useNuxtApp();
        return $api<EditHistoryResponse>(`vault/rulebooks/${id}/history`);
    },

    acquireWriteLock(id: string){
        const {$api} = useNuxtApp();
        return $api<AcquireWriteLockResponse>(`vault/rulebooks/${id}/lock/acquire`,{ method: 'POST' });
    },

    commitEditDelta(id: string, data: any){
        const {$api} = useNuxtApp();

        return $api<CommitEditDeltaResponse>(`vault/rulebooks/${id}/chunk/update`, {
            method:'PATCH',
            body: {
                expectedVersion: data?.expectedVersion,
                content: data?.content,
                chunkId: data?.chunkId
            }
        });
    },

    releaseWriteLock(id: string){
        const {$api} = useNuxtApp();
        return $api<void>(`vault/rulebooks/${id}/lock/release`, { method: 'POST' });
    },

    releaseAllWriteLocks(){
        const {$api} = useNuxtApp();
        return $api<void>('vault/rulebooks/lock/release-all', { method: 'POST'});
    },

    undoEdit(id: string, data: any){
        const {$api} = useNuxtApp();
        return $api<UndoOrRedoActionResponse>(`vault/rulebooks/${id}/action/undo`, {
            method: 'POST',
            body: {
                expectedVersion: data?.expectedVersion,
                content: data?.content,
                chunkId: data?.chunkId
            }
        });
    },

    redoEdit(id: string, data: any){
        const {$api} = useNuxtApp();
        return $api<UndoOrRedoActionResponse>(`vault/rulebooks/${id}/action/redo`, {
            method: 'POST',
            body: {
                expectedVersion: data?.expectedVersion,
                content: data?.content,
                chunkId: data?.chunkId
            }
        });
    }
}