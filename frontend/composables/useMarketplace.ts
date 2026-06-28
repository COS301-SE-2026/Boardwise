import { MarketplaceService, type ListingResponse } from '@/services/marketplaceService'
import { ref } from 'vue'


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
            const res = await MarketplaceService.getListings({ ...(activeFilters.value ?? {}), page: page.value, size: pageSize })
            const incoming = res.content ?? res
            listings.value = reset ? incoming : [...listings.value, ...incoming]
            hasMore.value = res.last === false
            page.value++

        } catch(err) {
            console.error('Failed to fetch', err); 
        } finally {
            loading.value = false;
        }
    };

    const addListing = async (listingData: any, image: File)=>{
        loading.value = true;
        error.value = null;
        try{
            return await MarketplaceService.createListing(listingData,image);
        }catch(err){
            console.error(err);
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
            listings.value = (res as any).content ?? res
        } catch (err:any) {
            error.value = err.response?.data?.message ?? 'Failed to fetch user listings';
        } finally {
            loading.value = false;
        }
    }

const editListing = async (id: string, listingData: any, image?: File) => {
    loading.value = true;
    error.value = null;
    try {
        await MarketplaceService.updateListing(id, listingData, image);
        await fetchListings(undefined, true); // refresh the list
    } catch (err:any) {
            console.error('Status:', err.response?.status);
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
  } catch (err:any) {
      error.value = err.response?.data?.message ?? 'Failed to delete listing';
    
  } finally {
    loading.value = false;
  }
}

const fetchListingById = async (id: string) => {
  loading.value = true
  error.value = null
  try {
    return await MarketplaceService.getListingById(id)
  } catch (err:any) { 
      error.value = err.response?.data?.message ?? 'Failed to fetch listing'
    return null
  } finally {
    loading.value = false
  }
}

return { listings, loading, error, fetchListings, fetchListingById, addListing, fetchUserListing, editListing, removeListing,page,loadMore }
}
