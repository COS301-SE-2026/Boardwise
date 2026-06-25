import api from './api' // import API

//GET ALL LISTINGS
export const getListings = (filters:{
    listingType?: string | null,
    genres?: string[] | null,
    conditions?: string[] | null,
    minPrice?: number | null,
    maxPrice?: number | null,
    page?: number,
    size?: number,
    search?:string,
}) =>{ 

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

    if(!hasFilters) return api.get('marketplace/listings');
    else return api.get('marketplace/listings/search', {params:applied_filters} );
}

    //CREATE LISTING
    createListing(data: any, image : File){
        const { $api } = useNuxtApp();
        const formData = new FormData();
        formData.append('data', new Blob([JSON.stringify(data)],{type:'application/json'}));
        formData.append('image',image);

        return $api<ListingResponse>('marketplace/listings',{
            method: 'POST',
            headers:{'Content-Type': 'multipart/form-data'},
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

    return api.patch(`marketplace/listing/${id}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
}

//DELETE LISTING 
export const deleteListing = (id: string) => 
  api.delete(`marketplace/listing/${id}`)

//GET LISTING BY ID
export const getListingById = (id: string) => 
  api.get(`marketplace/listing/${id}`)
