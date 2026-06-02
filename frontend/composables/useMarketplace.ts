import { MarketplaceService, type ListingResponse } from '@/services/marketplaceService'
import { ref } from 'vue'


export const useMarketplace = () =>{
    //storing listings
    const listings = ref<Array<ListingResponse>>([]); //listings in db
    
    //checks if it loads
    const loading = ref(false);
    
    //error checking 
    const error = ref(null);

    const fetchListings = async () => {
        loading.value = true;
        try {
            const res = await MarketplaceService.getListings();
            listings.value = res;
        } catch(err) {
            console.error('Failed to fetch', err);
        } finally {
            loading.value = false;
        }
    }

    const addListing = async (listingData: any, image: File)=>{
        loading.value = true;
        try{
            await MarketplaceService.createListing(listingData,image);
            await fetchListings();
        }catch(err){
            console.error(err);
        }
        finally{
            loading.value = false;
        }
    }

    const fetchUserListing = async () => {
        loading.value = true;
        error.value = null;
        try {
            const res = await MarketplaceService.getUserListings();
            listings.value = res ?? [];
        } catch (err: any) {
            error.value = err.data?.message ?? 'Failed to fetch user listings';
            console.error(err);
        } finally {
            loading.value = false;
        }
        }

const editListing = async (id: string, listingData: any, image?: File) => {
    loading.value = true;
    error.value = null;
    try {
        await MarketplaceService.updateListing(id, listingData, image);
        await fetchListings(); // refresh the list
    } catch (err: any) {
        console.error('Status:', err.status);
        console.error('Response data:', err.response?.data);
        error.value = err.response?.data?.message ?? 'Failed to update listing';
    } finally {
        loading.value = false;
    }
}

const removeListing = async (id: string) => {
  loading.value = true;
  error.value = null;
  try {
    await MarketplaceService.deleteListing(id);
    await fetchUserListing(); // refresh list after delete
  } catch (err: any) {
      error.value = err.data?.message ?? 'Failed to delete listing';
  } finally {
    loading.value = false;
  }
}

const fetchListingById = async (id: string) => {
  loading.value = true
  error.value = null
  try {
    const res = await MarketplaceService.getListingById(id)
    return res
  } catch (err: any) {
      error.value = err.data?.message ?? 'Failed to fetch listing'
    return null
  } finally {
    loading.value = false
  }
}

return { listings, loading, error, fetchListings, fetchListingById, addListing, fetchUserListing, editListing, removeListing }
}
