interface GroupInfo{
    groupId: string;
    name: string;
    description: string;
    owner: string;
    visibility: string;
    memberCount: number;
}

interface Groups{
    groups: Array<GroupInfo>;
}

export const CommunityService = {
    getAllGroups(){
        const { $api } = useNuxtApp();
        return $api<Groups>('social/groups');
    },
}
