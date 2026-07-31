import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mockNuxtImport } from '@nuxt/test-utils/runtime'

const apiMock = vi.fn();

mockNuxtImport('useNuxtApp',() =>{
    return () =>({$api:apiMock})
});

const {BoardGameService} = await import('~/services/boardgameService');

describe('BoardGameService', () =>{
    //ARRANGE
    beforeEach(()=>{apiMock.mockReset()});

   describe('getBoardgames', () => { 
        it('calls $api with GET and empty query when none provided',
            async()=>{
                // ARRANGE
                apiMock.mockResolvedValue({ message: 'ok', boardGames: [] });

                //ACT 
                const result = await BoardGameService.getBoardgames()

                //ASSERT
                expect(apiMock).toHaveBeenCalledWith('boardgames/', {
                    method: 'GET',
                    query: {},
                })
                expect(result.boardGames).toEqual([])
            }
        );

        it('calls $api with the query param when provided', async () => {
            //ARRANGE
            const mockGame = { id: '1', title: 'Catan' }
            apiMock.mockResolvedValue({ message: 'ok', boardGames: [mockGame] });

            //ACT
            const result = await BoardGameService.getBoardgames('catan');

            //ASSERT
            expect(apiMock).toHaveBeenCalledWith('boardgames/', {
                method: 'GET',
                query: { query: 'catan' },
            })

            expect(result.boardGames).toEqual([mockGame]);
        });
    });

    describe('getGenres', ()=>{
        it('calls $api with GET and empty query',async ()=>{
            //ARRANGE
            const mockGenres = ['strategy','party','card game', 'card']
            apiMock.mockResolvedValue({message: 'ok',result: mockGenres});

            //ACT
            const res = await BoardGameService.getGenres();

            //ASSERT
            expect(apiMock).toHaveBeenCalledWith('boardgames/genres', {
                method: 'GET',
                query: {},
            });

            expect(res.result).toEqual(mockGenres);

        });

        it('calls $api with GET and query param provided', async ()=>{
            //ARRANGE 
            const mockGenres = ['action', 'adventure'];
            apiMock.mockResolvedValue({message:"ok", result: mockGenres});
            //ACT
            const res = await BoardGameService.getGenres('a');

            //ASSERT
            expect(apiMock).toHaveBeenCalledWith('boardgames/genres', {
                method: 'GET',
                query:{
                    query: "a"
                },
            });

            expect(res.result).toEqual(mockGenres);
        });
    });

    describe('addBoardgame', () => {
        it('builds FormData correctly and calls $api with POST', async () => {
            // ARRANGE
            const mockGame = { id: '2', title: 'New Game' }
            apiMock.mockResolvedValue({ message: 'created', data: mockGame })

            const gameInfo = {
            title: 'Some game',
            description: 'A  new game',
            genres: ['Strategy'],
            }
            const fakeImage = new File(['fake image'], 'img.png', { type: 'image/png' })

            // ACT
            const result = await BoardGameService.addBoardgame(gameInfo, fakeImage)

            // ASSERT
            expect(apiMock).toHaveBeenCalledTimes(1)
            const [endpoint, options] = apiMock.mock.calls[0]

            expect(endpoint).toBe('boardgames/')
            expect(options.method).toBe('POST')
            expect(options.body).toBeInstanceOf(FormData)
            expect(options.body.has('gameInfo')).toBe(true)
            expect(options.body.has('gameImage')).toBe(true)

            expect(result.data).toEqual(mockGame)
        })

        it('appends gameInfo as a JSON blob', async () => {
            apiMock.mockResolvedValue({ message: 'created', data: {} })

            const gameInfo = { title: 'Test', description: 'Desc', genres: ['Party'] }
            const fakeImage = new File(['x'], 'img.png', { type: 'image/png' })

            await BoardGameService.addBoardgame(gameInfo, fakeImage)

            const [, options] = apiMock.mock.calls[0]
            const blob = options.body.get('gameInfo')
            const parsed = JSON.parse(await blob.text())

            expect(parsed).toEqual(gameInfo)
        })
        })
});