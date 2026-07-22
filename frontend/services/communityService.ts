export interface GroupInfo{
    id: string;
    name: string;
    imageUrl: string;
    description: string;
    owner: string;
    visibility: string;
    category: string;
    memberCount: number;
}

interface Member{
    username: string;
    profilePicture: string;
}

interface GroupResponse{
    id: string;
    name: string;
    imageUrl: string;
    description: string;
    owner: string;
    memberCount: number;
    members: Array<Member>
    isMember: boolean;
}

interface Groups{
    groups: Array<GroupInfo>
}


export const CommunityService = {
    async getAllGroups(){
        const { $api } = useNuxtApp();
        const response = await $api<Groups>('social/groups');
        return response.groups;
    },

    getCommunityDetails(id: string){
        const { $api } = useNuxtApp();
        return $api<GroupResponse>('social/groups/' + id);
    },

    async searchForCommunity(query: string){
        const { $api } = useNuxtApp();
        const endpoint = query === null || query === '' ? 'social/groups' : 'social/groups/search/' + query
        const response = await $api<Groups>(endpoint);
        return response.groups;
    }
}
