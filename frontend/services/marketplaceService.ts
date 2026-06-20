interface RentalPeriod{
    startDate: string; // LocalDate is returned
    endDate: string; // LocalDate is returned
}

// TODO: Change how you see fit
enum ListingStatus{
    AVAILABLE = "available",
    RENTED = "rented",
    SOLD = "sold",
}
export interface ListingResponse{
    listingId: string;
    username: string;
    gameTitle: string;
    itemType: string;
    listingType: string;
    price: number,
    description: string;
    imageUrl: string;
    genres: Array<string>,
    rentalPeriod: RentalPeriod;
    createdAt: string; // LocalDateTime is returned
    updatedAt: string; // LocalDateTime is returned
    status: ListingStatus;
}

export const MarketplaceService = {
    //GET ALL LISTINGS
    getListings(){
        const { $api } = useNuxtApp();
        return $api<Array<ListingResponse>>('marketplace/listings');
    },

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

        return $api<ListingResponse>(`marketplace/update/listing/${id}`,{
                method: 'PATCH',
                headers: { 'Content-Type': 'multipart/form-data' },
                body: formData
        });
    },

    //DELETE LISTING 
    deleteListing(id: string){
        const { $api } = useNuxtApp();
        return $api(`marketplace/delete/listing/${id}`, {
            method: 'DELETE'
        });
    },

    //GET LISTING BY ID
    getListingById(id: string){
        const { $api } = useNuxtApp();
        return $api<ListingResponse>(`marketplace/listings/${id}`);
    }

};