import {ref} from 'vue'
import { CommunityService } from '~/services/communityService'

export const useCommunity = () => {
    const token = ref<string|null>(
        import.meta.client ? localStorage.getItem('access_token') : null
    )
    const error = ref<string>('');
    const loading = ref<boolean>(false);

    const createCommunity = async (community: {
        name: string,
        description: string,
        category: string,
        visibility: string,
        communityPfp: File
    }) => {
        error.value = ''
        loading.value = true

        try{
            const response = await CommunityService.createCommunity(community);
            return response;
        }
        catch(err: any){
            error.value = err.data?.message || "Could not create community."
            throw err;
        }
        finally{
            loading.value = false;
        }
    }

    const getAllCommunities = async () => {
        error.value = '';
        loading.value = true;

        try{
            const response = await CommunityService.getAllGroups();
            return response;
        }catch(err: any){
            error.value = err.data?.message || 'No communities found'
            throw err;
        }finally{
            loading.value = false;
        }
    }

    const getCommunityDetails = async (id: string) => {
        error.value = ''
        loading.value = true

        try{
            const response = await CommunityService.getCommunityDetails(id);
            return response;
        }
        catch(err: any){
            error.value = err.data?.message || "Could not get community."
            throw err;
        }
    }

    const searchForCommunity = async (query: string) => {
        error.value = ''
        loading.value = true

        try{
            const response = await CommunityService.searchForCommunity(query)
            return response;
        }
        catch(err: any){
            error.value = err.data?.message || "Could not make search query."
            throw err;
        }
        finally{
            loading.value = false;
        }
    }

    const joinCommunity = async (id: string) => {
        error.value = ''
        loading.value = true

        try{
            const response = await CommunityService.joinCommunity(id);
            return response;
        }
        catch(err: any){
            error.value = err.data?.message || "Could not add user to community."
            throw err;
        }
        finally{
            loading.value = false;
        }
    }

    const leaveCommunity = async (id: string) => {
        error.value = ''
        loading.value = true

        try{
            const response = await CommunityService.leaveCommunity(id);
            return response;
        }
        catch(err: any){
            error.value = err.data?.message || "Could not remove user from community."
            throw err;
        }
        finally{
            loading.value = false;
        }
    }

    const editCommunity = async (id: string, newPic: File,  updateData: {
        name?: string,
        description?: string,
        visibility?: string
    }) => {
        error.value = ''
        loading.value = true

        try{
            const response = await CommunityService.editCommunity(id, newPic, updateData);
            return response;
        }
        catch(err: any){
            error.value = err.data?.message || "Could not edit community details."
            throw err;
        }
        finally{
            loading.value = false;
        }
    }

    return {
        token,
        error,
        loading,
        getAllCommunities,
        getCommunityDetails,
        searchForCommunity,
        createCommunity,
        joinCommunity,
        leaveCommunity,
        editCommunity
    }
}

/*{
  "groups": [
    {
      "groupId": "6a0cba33e844a6c626c70da5",
      "name": "Board Game Enthusiasts",
      "description": "A group for all board game lovers.",
      "owner": "IAmR3al",
      "visibility": "public",
      "memberCount": 4
    },
    {
      "groupId": "6a0cba33e844a6c626c70da6",
      "name": "Strategy Masters",
      "description": "Deep strategy games discussion.",
      "owner": "sarah_dev",
      "visibility": "public",
      "memberCount": 3
    },
    {
      "groupId": "6a0cba33e844a6c626c70da7",
      "name": "Casual Gamers",
      "description": "Laid back gaming sessions and trades.",
      "owner": "bob",
      "visibility": "public",
      "memberCount": 3
    },
    {
      "groupId": "6a0cba33e844a6c626c70da8",
      "name": "RPG Adventurers",
      "description": "Tabletop RPG and dungeon crawler fans.",
      "owner": "alex_games",
      "visibility": "private",
      "memberCount": 3
    },
    {
      "groupId": "6a0cba33e844a6c626c70da9",
      "name": "Card & Tile Collectors",
      "description": "For fans of card and tile-based games.",
      "owner": "jane_doe",
      "visibility": "private",
      "memberCount": 3
    }
  ]
}*/