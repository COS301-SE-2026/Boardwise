interface GroupInfo{
    id: string;
    name: string;
    imageUrl: string;
    description: string;
    owner: string;
    visibility: string;
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
    groups: Array<GroupInfo>;
}

export const CommunityService = {
    getAllGroups(){
        const { $api } = useNuxtApp();
        return $api<Groups>('social/groups');
    },

    getCommunityDetails(id: string){
        const { $api } = useNuxtApp();
        return $api<GroupResponse>('social/groups/' + id);
    }
}
