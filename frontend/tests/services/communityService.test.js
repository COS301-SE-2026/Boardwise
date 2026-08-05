import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mockNuxtImport } from '@nuxt/test-utils/runtime'

const apiMock = vi.fn();

mockNuxtImport('useNuxtApp', () => {
  return () => ({ $api: apiMock });
});

const { CommunityService } = await import('~/services/communityService')

describe('CommunityService', () => {
  beforeEach(() => {
    apiMock.mockReset();
  });

  describe('createCommunity', () => {
    it('sends a POST request with FormData containing groupInfo and groupImage', async () => {
      // Arrange
      const image = new File(['fake'], 'photo.png', { type: 'image/png' })
      const community = {
        name: 'Board Game Buddies',
        description: 'A group for board gamers',
        category: 'Strategy',
        visibility: 'PUBLIC',
        communityPfp: image
      };

      apiMock.mockResolvedValue({ message: 'created', group: { id: '1' } });

      // Act
      const result = CommunityService.createCommunity(community);

      // Assert
      expect(apiMock).toHaveBeenCalledWith(
        'social/groups',
        expect.objectContaining({
          method: 'POST',
          body: expect.any(FormData)
        })
      );
      await expect(result).resolves.toEqual({ message: 'created', group: { id: '1' } });
    });

    it('serializes name, description, category, and visibility into the groupInfo blob', async () => {
      // Arrange
      const image = new File(['fake'], 'photo.png', { type: 'image/png' });
      apiMock.mockResolvedValue({ message: 'created', group: {} });

      // Act
      CommunityService.createCommunity({
        name: 'Catan Fans',
        description: 'For Catan lovers',
        category: 'Strategy',
        visibility: 'PRIVATE',
        communityPfp: image
      });

      // Assert
      const [, callArgs] = apiMock.mock.calls[0];
      const groupInfoBlob = callArgs.body.get('groupInfo');
      const parsed = JSON.parse(await groupInfoBlob.text());
      expect(parsed).toEqual({
        name: 'Catan Fans',
        description: 'For Catan lovers',
        category: 'Strategy',
        visibility: 'PRIVATE'
      });
    });

    it('includes the image file under groupImage', () => {
      // Arrange
      const image = new File(['fake'], 'photo.png', { type: 'image/png' });
      apiMock.mockResolvedValue({ message: 'created', group: {} });

      // Act
      CommunityService.createCommunity({
        name: 'X',
        description: 'Y',
        category: 'Z',
        visibility: 'PUBLIC',
        communityPfp: image
      });

      // Assert
      const [, callArgs] = apiMock.mock.calls[0];
      expect(callArgs.body.get('groupImage')).toBe(image);
    })
  })

  describe('getAllGroups', () => {
    it('fetches groups and returns the groups array', async () => {
      // Arrange
      const mockGroups = [{ id: '1', name: 'Chess Club' }];
      apiMock.mockResolvedValue({ groups: mockGroups });

      // Act
      const result = await CommunityService.getAllGroups();

      // Assert
      expect(apiMock).toHaveBeenCalledWith('social/groups');
      expect(result).toEqual(mockGroups);
    })
  })

  describe('getCommunityDetails', () => {
    it('fetches details for the given group id', () => {
      // Arrange
      const id = 'group-1';
      const mockDetails = { id, name: 'Chess Club', members: [] };
      apiMock.mockResolvedValue(mockDetails);

      // Act
      const result = CommunityService.getCommunityDetails(id);

      // Assert
      expect(apiMock).toHaveBeenCalledWith(`social/groups/${id}`);
      return expect(result).resolves.toEqual(mockDetails);
    });
  });

  describe('searchForCommunity', () => {
    it('calls the base groups endpoint when query is empty string', async () => {
      // Arrange
      apiMock.mockResolvedValue({ groups: [] });

      // Act
      await CommunityService.searchForCommunity('');

      // Assert
      expect(apiMock).toHaveBeenCalledWith('social/groups');
    });

    it('calls the base groups endpoint when query is null', async () => {
      // Arrange
      apiMock.mockResolvedValue({ groups: [] })

      // Act
      await CommunityService.searchForCommunity(null);

      // Assert
      expect(apiMock).toHaveBeenCalledWith('social/groups');
    });

    it('calls the search endpoint with the query when provided', async () => {
      // Arrange
      const mockGroups = [{ id: '1', name: 'Catan Fans' }];
      apiMock.mockResolvedValue({ groups: mockGroups });

      // Act
      const result = await CommunityService.searchForCommunity('Catan');

      // Assert
      expect(apiMock).toHaveBeenCalledWith('social/groups/search/Catan');
      expect(result).toEqual(mockGroups);
    });
  });

  describe('joinCommunity', () => {
    it('sends a POST request to join the group', () => {
      // Arrange
      const id = 'group-1'
      apiMock.mockResolvedValue({ message: 'joined', data: { memberCount: 5, isMember: true, members: [] } });

      // Act
      const result = CommunityService.joinCommunity(id);

      // Assert
      expect(apiMock).toHaveBeenCalledWith(`social/groups/${id}`, {
        method: 'POST'
      });

      return expect(result).resolves.toEqual({
        message: 'joined',
        data: { memberCount: 5, isMember: true, members: [] }
      });
    });
  });

  describe('leaveCommunity', () => {
    it('sends a DELETE request to leave the group', () => {
      // Arrange
      const id = 'group-1'
      apiMock.mockResolvedValue({ message: 'left', data: { memberCount: 4, isMember: false, members: [] } });

      // Act
      const result = CommunityService.leaveCommunity(id);

      // Assert
      expect(apiMock).toHaveBeenCalledWith(`social/groups/${id}`, {
        method: 'DELETE'
      });
      return expect(result).resolves.toEqual({
        message: 'left',
        data: { memberCount: 4, isMember: false, members: [] }
      });
    });
  });

  describe('editCommunity', () => {
    it('sends a PATCH request with FormData containing groupInfo and groupImage', () => {
      // Arrange
      const id = 'group-1';
      const newPic = new File(['fake'], 'newpic.png', { type: 'image/png' });
      const updateData = { name: 'New Name', visibility: 'PRIVATE' };
      apiMock.mockResolvedValue({
        message: 'updated',
        data: { name: 'New Name', description: '', visibility: 'PRIVATE', imageUrl: '' }
      });

      // Act
      CommunityService.editCommunity(id, newPic, updateData);

      // Assert
      expect(apiMock).toHaveBeenCalledWith(
        `social/groups/${id}`,
        expect.objectContaining({
          method: 'PATCH',
          body: expect.any(FormData)
        })
      );
    });

    it('serializes only the provided update fields into the groupInfo blob', async () => {
      // Arrange
      const id = 'group-1'
      const newPic = new File(['fake'], 'newpic.png', { type: 'image/png' });
      apiMock.mockResolvedValue({ message: 'updated', data: {} });

      // Act
      CommunityService.editCommunity(id, newPic, { name: 'New Name' });

      // Assert
      const [, callArgs] = apiMock.mock.calls[0];
      const groupInfoBlob = callArgs.body.get('groupInfo');
      const parsed = JSON.parse(await groupInfoBlob.text());
      expect(parsed).toEqual({ name: 'New Name' });
    });

    it('includes the new image file under groupImage', () => {
      // Arrange
      const id = 'group-1';
      const newPic = new File(['fake'], 'newpic.png', { type: 'image/png' });
      apiMock.mockResolvedValue({ message: 'updated', data: {} });

      // Act
      CommunityService.editCommunity(id, newPic, { name: 'X' });

      // Assert
      const [, callArgs] = apiMock.mock.calls[0];
      expect(callArgs.body.get('groupImage')).toBe(newPic);
    });
  });
});