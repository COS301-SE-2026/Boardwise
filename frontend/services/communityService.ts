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
    visibility: string;
    memberCount: number;
    members: Array<Member>
    isMember: boolean;
}

interface GroupCreationResponse{
    message: string,
    group: GroupInfo
}

interface Groups{
    groups: Array<GroupInfo>
}


export const CommunityService = {
    createCommunity(community: {
      name: string,
      description: string,
      category: string,
      visibility: string,
      communityPfp: File
    }){
        const formdata = new FormData();
        
        formdata.append("groupInfo", new Blob([JSON.stringify({
            name: community.name,
            description: community.description,
            category: community.category,
            visibility: community.visibility
        })],{
            type: 'application/json'
        }));
        formdata.append("groupImage", community.communityPfp);

        const { $api } = useNuxtApp();
        return $api<GroupCreationResponse>('social/groups', {
            method: 'POST',
            body: formdata
        });
    },
    
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
    },
}
