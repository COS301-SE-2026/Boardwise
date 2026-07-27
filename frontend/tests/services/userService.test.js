import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mockNuxtImport } from '@nuxt/test-utils/runtime'

const apiMock = vi.fn();

mockNuxtImport('useNuxtApp',()=>{
    return ()=>({$api:apiMock});
});

const { userService } = await import('~/services/userService')

describe('userServce',()=>{
    beforeEach(()=>{
        apiMock.mockReset();
    });

    describe('getCurrentUser', ()=>{
        it('calls $api with the current user endpoint', async () => {
            const mockProfile = { username: 'jdoe', fullName: 'J Doe' }
            apiMock.mockResolvedValue(mockProfile)

            const result = await userService.getCurrentUser()

            expect(apiMock).toHaveBeenCalledWith('/users/')
            expect(result).toEqual(mockProfile)
        });
    })

    describe('getUser', () => {
        it('calls $api with the username appended to the endpoint', async () => {
        const mockProfile = { username: 'someoneelse' }
        apiMock.mockResolvedValue(mockProfile)

        const result = await userService.getUser('someoneelse')

        expect(apiMock).toHaveBeenCalledWith('/users/someoneelse')
        expect(result).toEqual(mockProfile)
        })
    })

    describe('updateProfile',()=>{
        it('splits a full name into firstName and lastName', async ()=>{
            apiMock.mockResolvedValue({username:'jdoe', email: 'janedoe@email.com'});

            await userService.updateProfile({name: 'Jane Doe'});

            expect(apiMock).toHaveBeenCalledWith('/users/',{
                method:'PATCH',
                body:{
                    firstName: 'Jane',
                    lastName: 'Doe',
                    username: undefined,
                    location: undefined,
                },
            })
        })
    })

    it('sets lastName to undefined when only one name is given', async () => {
        apiMock.mockResolvedValue({})

        await userService.updateProfile({ name: 'Cher' })

        expect(apiMock).toHaveBeenCalledWith('/users/', {
            method: 'PATCH',
            body: {
            firstName: 'Cher',
            lastName: undefined,
            username: undefined,
            location: undefined,
            },
        })
    })

    it('sets firstName and lastName to null when no name is given', async () => {
        apiMock.mockResolvedValue({})

        await userService.updateProfile({ username: 'newname', location: 'Pretoria' })

        expect(apiMock).toHaveBeenCalledWith('/users/', {
            method: 'PATCH',
            body: {
            firstName: null,
            lastName: null,
            username: 'newname',
            location: 'Pretoria',
            },
        })
    });

    describe('updateProfilePicture', () => {
        it('builds FormData with the profilePicture key and calls $api with POST', async () => {
        const mockResponse = { message: 'ok', profilePictureUrl: 'https://cdn/pfp.jpg' }
        apiMock.mockResolvedValue(mockResponse)

        const fakeImage = new File(['img'], 'pfp.png', { type: 'image/png' })
        const result = await userService.updateProfilePicture(fakeImage)

        expect(apiMock).toHaveBeenCalledTimes(1)
        const [endpoint, options] = apiMock.mock.calls[0]

        expect(endpoint).toBe('/users/profilePicture')
        expect(options.method).toBe('POST')
        expect(options.body).toBeInstanceOf(FormData)
        expect(options.body.get('profilePicture')).toBe(fakeImage)
        expect(result).toEqual(mockResponse)
        })
    })

    describe('addExistingGameToInventory', () => {
        it('calls $api with POST and the gameId in the URL', async () => {
        const mockResponse = { message: 'added', ownedGamesCount: 1, games: [] }
        apiMock.mockResolvedValue(mockResponse)

        const result = await userService.addExistingGameToInventory('67og9b07tb69rvrvtb0');

        expect(apiMock).toHaveBeenCalledWith('/users/gameInventory/67og9b07tb69rvrvtb0', {
            method: 'POST',
        })
        expect(result).toEqual(mockResponse)
        })
    })

    describe('addGameToInventory', () => {
        it('builds FormData with gameInfo and gameImage, calls $api with POST', async () => {
        const mockResponse = { message: 'added', ownedGamesCount: 2, games: [] }
        apiMock.mockResolvedValue(mockResponse)

        const gameInfo = {
            title: 'New Game',
            description: 'Fun',
            minPlayers: 2,
            maxPlayers: 4,
            minAge: 8,
            duration: 30,
            genres: ['strategy'],
        }
        const fakeImage = new File(['img'], 'game.png', { type: 'image/png' })

        const result = await userService.addGameToInventory(gameInfo, fakeImage)

        const [endpoint, options] = apiMock.mock.calls[0]
        expect(endpoint).toBe('/users/gameInventory')
        expect(options.method).toBe('POST')
        expect(options.body).toBeInstanceOf(FormData)
        expect(options.body.has('gameInfo')).toBe(true)
        expect(options.body.has('gameImage')).toBe(true)
        expect(result).toEqual(mockResponse)
        })

        it('combines gameInfo correctly as a JSON blob', async () => {
        apiMock.mockResolvedValue({ message: 'ok', ownedGamesCount: 1, games: [] })

        const gameInfo = {
            title: 'Test',
            description: 'Desc',
            minPlayers: 1,
            maxPlayers: 2,
            minAge: 5,
            duration: 10,
            genres: ['party'],
        }
        const fakeImage = new File(['x'], 'x.png', { type: 'image/png' })

        await userService.addGameToInventory(gameInfo, fakeImage)

        const [, options] = apiMock.mock.calls[0]
        const blob = options.body.get('gameInfo')
        const parsed = JSON.parse(await blob.text())

        expect(parsed).toEqual(gameInfo)
        })
    })

    describe('removeGameFromInventory', () => {
        it('calls $api with DELETE and the gameId in the URL', async () => {
        const mockResponse = { message: 'removed', ownedGamesCount: 0, games: [] };
        apiMock.mockResolvedValue(mockResponse);

        const result = await userService.removeGameFromInventory('game-456')

        expect(apiMock).toHaveBeenCalledWith('/users/gameInventory/game-456', {
            method: 'DELETE',
        })
        expect(result).toEqual(mockResponse);
        })
    });

    describe('error handling', () => {
        it('propagates errors from $api for getCurrentUser', async () => {
        apiMock.mockRejectedValue(new Error('Unauthorized'))
        await expect(userService.getCurrentUser()).rejects.toThrow('Unauthorized')
        })

        it('propagates errors from $api for addGameToInventory', async () => {
        apiMock.mockRejectedValue(new Error('Upload failed'))
        const fakeImage = new File(['x'], 'x.png', { type: 'image/png' })
        await expect(
            userService.addGameToInventory(
            { title: 'x', description: 'x', minPlayers: 1, maxPlayers: 2, minAge: 1, duration: 1, genres: [] },
            fakeImage
            )
        ).rejects.toThrow('Upload failed')
        })
  })

})