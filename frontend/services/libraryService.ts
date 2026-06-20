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
    gameName: string;
    edition: string;
    status: string;
    version: number;
    contributorId: string;
    lockHeldBy: string;
    uploadedAt: string; // Instant returned as a string
    updatedAt: string; // Instant returned as a string
}
interface PaginatedRulebookResponse{
    content: Array<RulebookResponse>;
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
interface RulebookTextResponse{
    rulebookId: string;
    content: string;
    version: number;
    lockHeldBy: string;
    updatedAt: string;
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

/* {
  "content": [
    {
      "contributorId": "6a0c9b15fd78b5b69bb47671",
      "edition": "Classic",
      "gameName": "Monopoly",
      "id": "6a0c9b15fd78b5b69bb47673",
      "lockHeldBy": null,
      "status": "Ready",
      "updatedAt": "2026-05-19T17:17:09.482Z",
      "uploadedAt": "2026-05-19T17:17:09.482Z",
      "version": 1
    },
    {
      "contributorId": "6a0c9b15fd78b5b69bb47671",
      "edition": "Standard",
      "gameName": "Scrabble",
      "id": "6a0c9b15fd78b5b69bb47674",
      "lockHeldBy": null,
      "status": "Ready",
      "updatedAt": "2026-05-19T17:17:09.482Z",
      "uploadedAt": "2026-05-19T17:17:09.482Z",
      "version": 1
    },
    {
      "contributorId": "6a0c9b15fd78b5b69bb47672",
      "edition": "5th Edition",
      "gameName": "Catan",
      "id": "6a0c9b15fd78b5b69bb47675",
      "lockHeldBy": null,
      "status": "Ready",
      "updatedAt": "2026-05-19T17:17:09.482Z",
      "uploadedAt": "2026-05-19T17:17:09.482Z",
      "version": 2
    }
  ],
  "empty": false,
  "first": true,
  "last": true,
  "number": 0,
  "numberOfElements": 3,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 20,
    "paged": true,
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "unpaged": false
  },
  "size": 20,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "totalElements": 3,
  "totalPages": 1
} 
*/