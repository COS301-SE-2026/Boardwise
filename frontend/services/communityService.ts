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
    isOwner: boolean;
}

interface GroupCreationResponse{
    message: string;
    group: GroupInfo;
}

interface GroupUpdateResponse{
    message: string;
    data: { 
        name: string,
        description: string,
        visibility: string,
        imageUrl: string
    }
}

interface GroupMembershipResponse{
    message: string;
    data: {
        memberCount: number,
        isMember: boolean,
        members: Array<Member>
    }
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

    joinCommunity(id: string){
        const { $api } = useNuxtApp();
        return $api<GroupMembershipResponse>('social/groups/' + id, {
            method: 'POST'
        })
    },

    leaveCommunity(id: string){
        const { $api } = useNuxtApp();
        return $api<GroupMembershipResponse>('social/groups/' + id, {
            method: 'DELETE'
        })
    },

    editCommunity(id: string, newPic: File , updateData: {
        name?: string,
        description?: string,
        visibility?: string
    }){
        const formData = new FormData();

        formData.append("groupInfo", new Blob([JSON.stringify(updateData)],{
            type: 'application/json'
        }));

        formData.append("groupImage", newPic);

        const { $api } = useNuxtApp();
        return $api<GroupUpdateResponse>('social/groups/' + id, {
            method: 'PATCH',
            body: formData
        })
    }
}
