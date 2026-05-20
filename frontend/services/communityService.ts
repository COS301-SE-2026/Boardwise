import api from './api'

export const CommunityService = {
    getAllGroups(){
        return api.get('social/groups');
    },
}
