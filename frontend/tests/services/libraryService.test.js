import { describe, it, expect, vi, beforeEach } from "vitest";
import { mockNuxtImport } from '@nuxt/test-utils/runtime'

const apiMock = vi.fn();

mockNuxtImport('useNuxtApp', () => {
    return () => ({ $api: apiMock });
});

const { LibraryService } = await import('~/services/libraryService');

describe('LibraryService', () => {
    beforeEach(() => { apiMock.mockReset() });

    describe('fetchAllRulebooks', () => {
        it('sends the full query as-is when all filters are meaningful', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ content: [] });
            const filters = { genre: 'Strategy', minPlayers: 2 };

            // ACT
            await LibraryService.fetchAllRulebooks(filters);

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks', {
                method: 'GET',
                query: { genre: 'Strategy', minPlayers: 2 },
            });
        });

        it('strips null and undefined values from the query', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ content: [] });
            const filters = { genre: null, edition: undefined, minPlayers: 2 };

            // ACT
            await LibraryService.fetchAllRulebooks(filters);

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks', {
                method: 'GET',
                query: { minPlayers: 2 },
            });
        });

        it('strips empty string values from the query', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ content: [] });
            const filters = { title: '', genre: 'Party' };

            // ACT
            await LibraryService.fetchAllRulebooks(filters);

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks', {
                method: 'GET',
                query: { genre: 'Party' },
            });
        });

        it('strips "all" and "All" sentinel values from the query', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ content: [] });
            const filters = { genre: 'all', language: 'All', edition: 'First' };

            // ACT
            await LibraryService.fetchAllRulebooks(filters);

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks', {
                method: 'GET',
                query: { edition: 'First' },
            });
        });

        it('strips empty array values but keeps non-empty ones', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ content: [] });
            const filters = { genres: [], languages: ['English'] };

            // ACT
            await LibraryService.fetchAllRulebooks(filters);

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks', {
                method: 'GET',
                query: { languages: ['English'] },
            });
        });

        it('keeps falsy-but-meaningful values like 0 and false', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ content: [] });
            const filters = { minPlayers: 0, isAvailable: false };

            // ACT
            await LibraryService.fetchAllRulebooks(filters);

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks', {
                method: 'GET',
                query: { minPlayers: 0, isAvailable: false },
            });
        });

        it('sends an empty query when filters is an empty object', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ content: [] });

            // ACT
            await LibraryService.fetchAllRulebooks({});

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks', {
                method: 'GET',
                query: {},
            });
        });

        it('returns the paginated response from $api', async () => {
            // ARRANGE
            const mockResponse = { content: [{ id: '1', title: 'Catan' }], totalElements: 1 };
            apiMock.mockResolvedValue(mockResponse);

            // ACT
            const result = await LibraryService.fetchAllRulebooks({});

            // ASSERT
            expect(result).toEqual(mockResponse);
        })
    })

    describe('fetchRulebookById', () => {
        it('calls $api with the id appended to the endpoint, no options', async () => {
            // ARRANGE
            const mockRulebook = { id: '1', title: 'Catan' };
            apiMock.mockResolvedValue(mockRulebook);

            // ACT
            const result = await LibraryService.fetchRulebookById('1');

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks/1');
            expect(result).toEqual(mockRulebook);
        })
    })

    describe('fetchRulebookText', () => {
        it('calls $api on the /text endpoint, no options', async () => {
            // ARRANGE
            const mockText = { rulebookId: '1', chunks: [] };
            apiMock.mockResolvedValue(mockText);

            // ACT
            const result = await LibraryService.fetchRulebookText('1');

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks/1/text');
            expect(result).toEqual(mockText);
        });
    });

    describe('fetchDownloadRulebook', () => {
        it('calls $api on the /download endpoint, no options', async () => {
            // ARRANGE
            const mockDownload = { downloadUrl: 'https://example.com/file.pdf', expiresAt: '2026-01-01' };
            apiMock.mockResolvedValue(mockDownload);

            // ACT
            const result = await LibraryService.fetchDownloadRulebook('1');

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks/1/download');
            expect(result).toEqual(mockDownload);
        });
    });

    describe('fetchEditHistory', () => {
        it('calls $api on the /history endpoint, no options', async () => {
            // ARRANGE
            const mockHistory = { rulebookId: '1', totalEdits: 0, edits: [] };
            apiMock.mockResolvedValue(mockHistory);

            // ACT
            const result = await LibraryService.fetchEditHistory('1');

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks/1/history');
            expect(result).toEqual(mockHistory);
        });
    });

    describe('acquireWriteLock', () => {
        it('calls $api with POST on the /lock/acquire endpoint', async () => {
            // ARRANGE
            const mockLock = { lockGranted: true, lockedBy: 'user1', expiresAt: '2026-01-01', currentVersion: 1 };
            apiMock.mockResolvedValue(mockLock);

            // ACT
            const result = await LibraryService.acquireWriteLock('1');

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks/1/lock/acquire', {
                method: 'POST',
            });
            expect(result).toEqual(mockLock);
        })
    })

    describe('commitEditDelta', () => {
        it('calls $api with PATCH and the expected body fields', async () => {
            // ARRANGE
            const mockCommit = { committed: true, newVersion: 2, committedAt: '2026-01-01', lockExpiresAt: '2026-01-02' };
            apiMock.mockResolvedValue(mockCommit);
            const data = { expectedVersion: 1, content: 'new text', chunkId: 'chunk-1' };

            // ACT
            const result = await LibraryService.commitEditDelta('1', data);

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks/1/chunk/update', {
                method: 'PATCH',
                body: { expectedVersion: 1, content: 'new text', chunkId: 'chunk-1' },
            });
            expect(result).toEqual(mockCommit);
        });

        it('sends a body of undefined fields when data is not provided', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ committed: false });

            // ACT
            await LibraryService.commitEditDelta('1', undefined);

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks/1/chunk/update', {
                method: 'PATCH',
                body: { expectedVersion: undefined, content: undefined, chunkId: undefined },
            });
        });
    });

    describe('releaseWriteLock', () => {
        it('calls $api with POST on the /lock/release endpoint', async () => {
            // ARRANGE
            apiMock.mockResolvedValue(undefined);

            // ACT
            await LibraryService.releaseWriteLock('1');

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks/1/lock/release', {
                method: 'POST',
            });
        });
    });

    describe('releaseAllWriteLocks', () => {
        it('calls $api with POST on the lock/release-all endpoint', async () => {
            // ARRANGE
            apiMock.mockResolvedValue(undefined);

            // ACT
            await LibraryService.releaseAllWriteLocks();

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks/lock/release-all', {
                method: 'POST',
            });
        });
    });

    describe('undoEdit', () => {
        it('calls $api with POST and the expected body fields', async () => {
            // ARRANGE
            const mockUndo = { done: true, newVersion: 1, chunkId: 'chunk-1', doneAt: '2026-01-01', lockExpiresAt: '2026-01-02' };
            apiMock.mockResolvedValue(mockUndo);
            const data = { expectedVersion: 2, content: 'old text', chunkId: 'chunk-1' };

            // ACT
            const result = await LibraryService.undoEdit('1', data);

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks/1/action/undo', {
                method: 'POST',
                body: { expectedVersion: 2, content: 'old text', chunkId: 'chunk-1' },
            })
            expect(result).toEqual(mockUndo);
        });

        it('sends a body of undefined fields when data is not provided', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ done: false });

            // ACT
            await LibraryService.undoEdit('1', undefined);

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks/1/action/undo', {
                method: 'POST',
                body: { expectedVersion: undefined, content: undefined, chunkId: undefined },
            });
        });
    });

    describe('redoEdit', () => {
        it('calls $api with POST and the expected body fields', async () => {
            // ARRANGE
            const mockRedo = { done: true, newVersion: 3, chunkId: 'chunk-1', doneAt: '2026-01-01', lockExpiresAt: '2026-01-02' };
            apiMock.mockResolvedValue(mockRedo);
            const data = { expectedVersion: 2, content: 'new text', chunkId: 'chunk-1' };

            // ACT
            const result = await LibraryService.redoEdit('1', data);

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks/1/action/redo', {
                method: 'POST',
                body: { expectedVersion: 2, content: 'new text', chunkId: 'chunk-1' },
            });
            expect(result).toEqual(mockRedo);
        });

        it('sends a body of undefined fields when data is not provided', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ done: false });

            // ACT
            await LibraryService.redoEdit('1', undefined);

            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('vault/rulebooks/1/action/redo', {
                method: 'POST',
                body: { expectedVersion: undefined, content: undefined, chunkId: undefined },
            });
        });
    });
});