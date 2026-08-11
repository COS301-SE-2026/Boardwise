import { describe, it, vi, beforeEach ,expect } from "vitest";
import { useEvents } from '~/services/eventService'

const apiMock = vi.fn();

mockNuxtImport('useNuxtApp',() =>{
    return () =>({$api:apiMock})
});

const { EventService } = await import('~/services/eventService');

describe('EventService', () => {
    beforeEach(() => {
        apiMock.mockReset()
    })

    describe('getAllEvents', ()=>{
        it('calls $api with GET and name/page in query', async ()=>{
            //ARRANGE
            apiMock.mockResolvedValue({message: 'ok', result: []});

            //ACT 
            const result = await EventService.getAllEvents('board game night', 2);

            //ASSERT

            expect(apiMock).toHaveBeenCalled('community/',{
                method:'GET',
                query:{name: 'board game night', page:2},
            });

            expect(result.result).toEqual([]);
        });

        it('calls $api with undefined name/page when none provided', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ message: 'ok', result: [] });
 
            // ACT
            await EventService.getAllEvents();
 
            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('community/', {
                method: 'GET',
                query: { name: undefined, page: undefined },
            });
        });
    });

    describe('getEvent', () => {
        it('calls $api with the id appended to the endpoint, no options', async () => {
            // ARRANGE
            const mockEvent = { id: '1', name: 'Catan Night' };
            apiMock.mockResolvedValue({ message: 'ok', data: mockEvent });
 
            // ACT
            const result = await EventService.getEvent('1');
 
            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('community/1');
            expect(result.data).toEqual(mockEvent);
        })
    })

    describe('createEvent', () => {
        it('builds FormData with EventInfo and EventImage and calls $api with POST', async () => {
            // ARRANGE
            const mockEvent = { id: '2', name: 'New Event' };
            apiMock.mockResolvedValue({ message: 'created', data: mockEvent });
 
            const eventInfo = { name: 'New Event', description: 'A new event' };
            const fakeImage = new File(['fake image'], 'img.png', { type: 'image/png' });
 
            // ACT
            const result = await EventService.createEvent(eventInfo, fakeImage);
 
            // ASSERT
            expect(apiMock).toHaveBeenCalledTimes(1);
            const [endpoint, options] = apiMock.mock.calls[0];
 
            expect(endpoint).toBe('community/');
            expect(options.method).toBe('POST');
            expect(options.body).toBeInstanceOf(FormData);
            expect(options.body.has('EventInfo')).toBe(true);
            expect(options.body.has('EventImage')).toBe(true);
            expect(result.data).toEqual(mockEvent);
        })
 
        it('omits EventImage when no image is provided', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ message: 'created', data: {} });
            const eventInfo = { name: 'No Image Event' };
 
            // ACT
            await EventService.createEvent(eventInfo);
 
            // ASSERT
            const [, options] = apiMock.mock.calls[0];
            expect(options.body.has('EventInfo')).toBe(true);
            expect(options.body.has('EventImage')).toBe(false);
        });
 
        it('appends EventInfo as a JSON blob matching the original object', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ message: 'created', data: {} });
            const eventInfo = { name: 'Test Event', description: 'Desc' };
 
            // ACT
            await EventService.createEvent(eventInfo);
 
            // ASSERT
            const [, options] = apiMock.mock.calls[0];
            const blob = options.body.get('EventInfo');
            const parsed = JSON.parse(await blob.text());
            expect(parsed).toEqual(eventInfo);
        });
    });

    describe('updateEvent', () => {
        it('builds FormData with EventInfo and EventImage and calls $api with PATCH', async () => {
            // ARRANGE
            const mockEvent = { id: '1', name: 'Updated Event' };
            apiMock.mockResolvedValue({ message: 'ok', data: mockEvent });
            const eventInfo = { name: 'Updated Event' };
            const fakeImage = new File(['x'], 'img.png', { type: 'image/png' });
 
            // ACT
            const result = await EventService.updateEvent('1', eventInfo, fakeImage);
 
            // ASSERT
            const [endpoint, options] = apiMock.mock.calls[0];
            expect(endpoint).toBe('community/1');
            expect(options.method).toBe('PATCH');
            expect(options.body).toBeInstanceOf(FormData);
            expect(options.body.has('EventInfo')).toBe(true);
            expect(options.body.has('EventImage')).toBe(true);
            expect(result.data).toEqual(mockEvent);
        })
 
        it('omits EventInfo when no eventInfo is provided', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ message: 'ok', data: {} });
            const fakeImage = new File(['x'], 'img.png', { type: 'image/png' });
 
            // ACT
            await EventService.updateEvent('1', undefined, fakeImage);
 
            // ASSERT
            const [, options] = apiMock.mock.calls[0];
            expect(options.body.has('EventInfo')).toBe(false);
            expect(options.body.has('EventImage')).toBe(true);
        })
 
        it('sends an empty FormData when neither eventInfo nor image are provided', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ message: 'ok', data: {} });
 
            // ACT
            await EventService.updateEvent('1');
 
            // ASSERT
            const [, options] = apiMock.mock.calls[0];
            expect(options.body.has('EventInfo')).toBe(false);
            expect(options.body.has('EventImage')).toBe(false);
        })
    })
 
    describe('cancelEvent', () => {
        it('calls $api with DELETE on the event-scoped endpoint', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ message: 'cancelled' });
 
            // ACT
            const result = await EventService.cancelEvent('1');
 
            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('community/1', {
                method: 'DELETE',
            });
            expect(result.message).toBe('cancelled');
        });
    });
 
    describe('rsvpToEvent', () => {
        it('calls $api with POST on the event-scoped endpoint, no body', async () => {
            // ARRANGE
            const mockEvent = { id: '1', rsvpStatus: 'ATTENDING' };
            apiMock.mockResolvedValue({ message: 'ok', data: mockEvent });
 
            // ACT
            const result = await EventService.rsvpToEvent('1');
 
            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('community/1', {
                method: 'POST',
            });
            expect(result.data).toEqual(mockEvent);
        });
    });
 
    describe('deRsvpToEvent', () => {
        it('calls $api with PATCH on the generic endpoint, eventId in body', async () => {
            // ARRANGE
            const mockEvent = { id: '1', rsvpStatus: 'NOT_ATTENDING' };
            apiMock.mockResolvedValue({ message: 'ok', data: mockEvent });
 
            // ACT
            const result = await EventService.deRsvpToEvent('1');
 
            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('community/', {
                method: 'PATCH',
                body: { eventId: '1' },
            });
            expect(result.data).toEqual(mockEvent);
        });
    });
 
    describe('getUserInvites', () => {
        it('calls $api with GET on the invite endpoint', async () => {
            // ARRANGE
            const mockInvites = [{ status: 'INVITED', event: { id: 'a' } }];
            apiMock.mockResolvedValue({ message: 'ok', inviteCount: 1, invites: mockInvites });
 
            // ACT
            const result = await EventService.getUserInvites()
 
            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('community/invite', {
                method: 'GET',
            });
            expect(result.invites).toEqual(mockInvites);
            expect(result.inviteCount).toBe(1);
        })
    })
 
    describe('inviteUser', () => {
        it('calls $api with POST and invitee/eventId in body', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ message: 'invited' });
 
            // ACT
            const result = await EventService.inviteUser('friend@example.com', 'event-1');
 
            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('community/invite', {
                method: 'POST',
                body: { invitee: 'friend@example.com', eventId: 'event-1' },
            });
            expect(result.message).toBe('invited');
        });
    });
 
    describe('respondToInvite', () => {
        it('calls $api with PATCH and status in query, for accept', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ message: 'accepted' });
 
            // ACT
            const result = await EventService.respondToInvite('event-1', 'accept');
 
            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('community/invite/event-1', {
                method: 'PATCH',
                query: { status: 'accept' },
            });
            expect(result.message).toBe('accepted');
        });
 
        it('calls $api with PATCH and status in query, for decline', async () => {
            // ARRANGE
            apiMock.mockResolvedValue({ message: 'declined' });
 
            // ACT
            const result = await EventService.respondToInvite('event-1', 'decline');
 
            // ASSERT
            expect(apiMock).toHaveBeenCalledWith('community/invite/event-1', {
                method: 'PATCH',
                query: { status: 'decline' },
            });
            expect(result.message).toBe('declined');
        });
    });

})