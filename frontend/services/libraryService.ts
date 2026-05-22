import api from './api'

export const LibraryService = {
    // Optional parameters to control the pagination/search
    getAllRulebooks(search = '', page = 1, limit = 20) {
        return api.get('vault/rulebooks', {
            params: {
                search: search,
                page: page,
                limit: limit
            }
        });
    }
}