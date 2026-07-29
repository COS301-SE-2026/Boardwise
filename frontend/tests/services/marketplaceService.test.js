import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mockNuxtImport } from '@nuxt/test-utils/runtime'

const apiMock = vi.fn()

mockNuxtImport('useNuxtApp', () => {
  return () => ({ $api: apiMock })
})

const { MarketplaceService } = await import('~/services/marketplaceService')

describe('MarketplaceService', () => {
    beforeEach(() => {
        apiMock.mockReset()
    })

    describe('getListings', () => {
        it('calls the base listings endpoint when no filters are applied', () => {
            // Arrange
            const mockResponse = { listings: [], total: 0 };
            apiMock.mockResolvedValue(mockResponse);

            // Act
            const result = MarketplaceService.getListings();

            // Assert
            expect(apiMock).toHaveBeenCalledWith('marketplace/listings', {
                method: 'GET',
                query: {}
            });
            await expect(result).resolves.toEqual(mockResponse);
        });

        it('calls the search endpoint and includes gameTitle when search is provided', () => {
            // Arrange
            apiMock.mockResolvedValue({ listings: [] });

            // Act
            MarketplaceService.getListings({ search: 'Catan' });

            // Assert
            expect(apiMock).toHaveBeenCalledWith('marketplace/listings/search', {
                method: 'GET',
                query: { gameTitle: 'Catan' }
            });
        });

        it('applies listingType, genres, conditions, price range, and pagination filters', () => {
            // Arrange
            apiMock.mockResolvedValue({ listings: [] });
            const filters = {
                listingType: 'SALE',
                genres: ['Strategy', 'Party'],
                conditions: ['NEW', 'USED'],
                minPrice: 10,
                maxPrice: 100,
                page: 2,
                size: 20
            }

            // Act
            MarketplaceService.getListings(filters);

            // Assert
            expect(apiMock).toHaveBeenCalledWith('marketplace/listings/search', {
                method: 'GET',
                query: filters
            });
        });

        it('omits empty genre/condition arrays and null price values from the query', () => {
            // Arrange
            apiMock.mockResolvedValue({ listings: [] });

            // Act
            MarketplaceService.getListings({
                genres: [],
                conditions: [],
                minPrice: null,
                maxPrice: null
            });

            // Assert
            expect(apiMock).toHaveBeenCalledWith('marketplace/listings', {
                method: 'GET',
                query: {}
            });
        });
    });

    describe('createListing', () => {
        it('sends a POST request with a FormData body containing data and image', () => {
        // Arrange
        const listingData = { listingTitle: 'Catan', price: 25 };
        const image = new File(['fake'], 'photo.png', { type: 'image/png' });
        apiMock.mockResolvedValue({ listingId: '123' });

        // Act
        MarketplaceService.createListing(listingData, image);

        // Assert
        expect(apiMock).toHaveBeenCalledWith(
            'marketplace/listings',
            expect.objectContaining({
            method: 'POST',
            body: expect.any(FormData)
            })
        );
        });
    });

    describe('getUserListings', () => {
        it('calls the user listings endpoint with no query params', () => {
        // Arrange
        apiMock.mockResolvedValue([]);

        // Act
        MarketplaceService.getUserListings();

        // Assert
        expect(apiMock).toHaveBeenCalledWith('marketplace/listings/user');
        });
    });

    describe('updateListing', () => {
        it('sends a PATCH request with FormData when no image is provided', () => {
            // Arrange
            const id = 'abc-123';
            const data = { price: 30 };
            apiMock.mockResolvedValue({ listingId: id });

            // Act
            MarketplaceService.updateListing(id, data);

            // Assert
            expect(apiMock).toHaveBeenCalledWith(
                `marketplace/listing/${id}`,
                expect.objectContaining({
                method: 'PATCH',
                body: expect.any(FormData)
                })
            );
        });

        it('includes the image in FormData when one is provided', () => {
            // Arrange
            const id = 'abc-123';
            const data = { price: 30 };
            const image = new File(['fake'], 'update.png', { type: 'image/png' });
            apiMock.mockResolvedValue({ listingId: id });

            // Act
            MarketplaceService.updateListing(id, data, image);

            // Assert
            const [, callArgs] = apiMock.mock.calls[0];
            expect(callArgs.body.get('image')).toBe(image);
        });
    });

    describe('deleteListing', () => {
        it('sends a DELETE request to the correct listing endpoint', () => {
            // Arrange
            const id = 'xyz-789'
            apiMock.mockResolvedValue(undefined);

            // Act
            MarketplaceService.deleteListing(id);

            // Assert
            expect(apiMock).toHaveBeenCalledWith(`marketplace/listing/${id}`, {
                method: 'DELETE'
            });
        });
    });

    describe('getListingById', () => {
        it('fetches a single listing by id', () => {
            // Arrange
            const id = 'listing-1'
            const mockListing = { listingId: id, listingTitle: 'Ticket to Ride' };
            apiMock.mockResolvedValue(mockListing);

            // Act
            const result = MarketplaceService.getListingById(id);

            // Assert
            expect(apiMock).toHaveBeenCalledWith(`marketplace/listing/${id}`);
            await expect(result).resolves.toEqual(mockListing);
        });
    });
});
