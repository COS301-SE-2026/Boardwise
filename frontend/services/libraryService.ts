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
// TODO: Change this to rulebookSummary response instead such that less data is returned
interface RulebookResponse{
    id: string;
    title: string;
    edition: string;
    genres: string;
    version: number;
    status: string;
    contributorUsername: string;
    description: string;
    language: string;
    lockHeldBy: string;
    uploadedAt: string; // Instant returned as a string
    updatedAt: string; // Instant returned as a string
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
    title: string;
    language: string;
    edition: string;
    version: number;
    genres: string[];
}

export const LibraryService = {
    // Optional parameters to control the pagination/search
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
    }
}