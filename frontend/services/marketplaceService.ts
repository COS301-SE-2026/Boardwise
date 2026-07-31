export interface ListingResponse {
    listingId: string;
    listingTitle: string;
    username: string;
    userId: string;
    gameTitle: string;
    itemType: string;
    listingType: string;
    price: number;
    description: string;
    imageUrl?: string;
    location: string;
    isNegotiable: boolean;
    condition: string;
    version: string;
    genres: string[];
    rentalPeriod?: {
        startDate: string;
        endDate: string;
    };
    status: string;
}

export const MarketplaceService = {
    //GET ALL LISTINGS
    getListings(filters:{
        listingType?: string | null,
        genres?: string[] | null,
        conditions?: string[] | null,
        minPrice?: number | null,
        maxPrice?: number | null,
        page?: number,
        size?: number,
        search?:string|null,
    } ={}){ 
        const {$api} = useNuxtApp()
        const applied_filters: Record<string,any> = {};

        if (filters?.listingType) applied_filters.listingType = filters.listingType
        if (filters?.genres?.length) applied_filters.genres = filters.genres
        if (filters?.conditions?.length) applied_filters.conditions = filters.conditions
        if (filters?.minPrice != null) applied_filters.minPrice = filters.minPrice
        if (filters?.maxPrice != null) applied_filters.maxPrice = filters.maxPrice
        if (filters?.page != null) applied_filters.page = filters.page
        if (filters?.size != null) applied_filters.size = filters.size

        if (filters?.search) {
            applied_filters.gameTitle = filters.search
        }
        
        const hasFilters = Object.keys(applied_filters).length > 0;
        const url = hasFilters ? 'marketplace/listings/search' : 'marketplace/listings';
        return $api<any>(url, { method: 'GET', query: applied_filters });
    },

    //CREATE LISTING
    createListing(data: any, image : File){
        const { $api } = useNuxtApp();
        const formData = new FormData();
        formData.append('data', new Blob([JSON.stringify(data)],{type:'application/json'}));
        formData.append('image',image);
        return $api<ListingResponse>('marketplace/listings',{
            method: 'POST',
            body: formData,
        });
    },

    //GET USER LISTINGS 
    getUserListings(){
        const { $api } = useNuxtApp();
        return $api<Array<ListingResponse>>('marketplace/listings/user');
    },

    // UPDATE LISTING BY ID
    
    updateListing(id: string, data: any, image?: File){
        const { $api } = useNuxtApp();
        const formData = new FormData();
        formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));

        if (image) {
            formData.append('image', image);
        }

        return $api<ListingResponse>(`marketplace/listing/${id}`,{method:'PATCH'
            ,body:formData
        });
    },

    //DELETE LISTING 
    deleteListing(id: string){
        const { $api } = useNuxtApp();
        return $api(`marketplace/listing/${id}`,{method: 'DELETE'})
    },

    //GET LISTING BY ID
    getListingById(id: string){ 
        const { $api } = useNuxtApp();
        return $api<ListingResponse>(`marketplace/listing/${id}`)
    }
}