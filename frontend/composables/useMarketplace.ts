import { MarketplaceService, type ListingResponse } from '@/services/marketplaceService'
import { ref } from 'vue'
import { useSnackBar } from './useSnackbar'
const { show } = useSnackBar()

export const useMarketplace = () =>{

    //page paramters
    const page = ref(1)
    const hasMore = ref(true)
    const pageSize = 10

    //storing listings
    const listings = ref<Array<ListingResponse>>([]); //listings in db
    
    //checks if it loads
    const loading = ref(false);
    
    //error checking 
    const error = ref(null);

    const loadMore = () => {
        if (!loading.value && hasMore.value) fetchListings(undefined, false)
    }

    const activeFilters = ref({})

    const fetchListings = async (filters?: {
        listingType?: string | null,
        genres?: string[] | null,
        conditions?: string[] | null,
        minPrice?: number | null,
        maxPrice?: number | null,
        page?: number,
        size?: number,
        search?: string | null

    }, reset = false)=> {
        if(reset){
            activeFilters.value = filters ?? {}
            page.value = 1;
            listings.value = [];
            hasMore.value = true;
        }
        if (!hasMore.value) return
        loading.value = true;
        try {
            const res = await MarketplaceService.getListings();
            listings.value = res;
            show('Successfully got all listings');
        } catch(err) {
            show('Failed to create a listing', 'error')
            console.error('Failed to fetch', err); 
        } finally {
            loading.value = false;
        }
    };

    const addListing = async (listingData: any, image: File)=>{
        loading.value = true;
        error.value = null;
        try{
            await MarketplaceService.createListing(listingData,image);
            await fetchListings();
            show('Listing successfully created!');
        }catch(err){
            console.error(err);
            show('Failed to create listing', 'error');
            return null;
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
            listings.value = res ?? null;
            if(listings.value)show('Successfully fetched your listings!');
            else show('You have no listings to fetch!')
        } catch (err: any) {
            error.value = err.data?.message ?? 'Failed to fetch user listings';
            show('Failed to fetch user listings', 'error');
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
        show('Successfully updated your listing!')
    } catch (err: any) {
        console.error('Status:', err.status);
        console.error('Response data:', err.response?.data);
        error.value = err.response?.data?.message ?? 'Failed to update listing';
        show('Could not update your listing, Try Again', 'error');
    } finally {
        loading.value = false;
    }
}

const removeListing = async (id: string) => {
  loading.value = true;
  error.value = null;
  try {
    await MarketplaceService.deleteListing(id);
    show('Listing deleted successfully!');
    await fetchUserListing(); // refresh list after delete
  } catch (err: any) {
     error.value = err.data?.message ?? 'Failed to delete listing';
     show('Failed to delete listing','error');
  } finally {
    loading.value = false;
  }
}

const fetchListingById = async (id: string) => {
  loading.value = true
  error.value = null
  try {
    const res = await MarketplaceService.getListingById(id)
    show('Successfully fetched listing details');
    return res
  } catch (err: any) {
      error.value = err.data?.message ?? 'Failed to fetch listing'
      show( 'Failed to fetch listing','error');
    return null
  } finally {
    loading.value = false
  }
}

return { listings, loading, error, fetchListings, fetchListingById, addListing, fetchUserListing, editListing, removeListing,page,loadMore,hasMore}
}
