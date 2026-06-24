import { getListings, createListing, getUserListings, updateListing, deleteListing,getListingById } from '@/services/marketplaceService'
import { ref } from 'vue'
import axios from 'axios'


export const useMarketplace = () =>{

    //page paramters
    const page = ref(1)
    const hasMore = ref(true)
    const pageSize = 10

    //storing listings
    const listings = ref([]); //listings in db
    
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
            const res = await getListings({ ...(activeFilters.value ?? {}), page: page.value, size: pageSize })
            const data = res.data
            const incoming = data.content ?? data
            listings.value = reset ? incoming : [...listings.value, ...incoming]
            hasMore.value = data.last === false
            page.value++

        } catch(err) {//TODO: ADD SNACKBAR
            console.error('Failed to fetch', err); 
        } finally {
            loading.value = false;
        }
    };

    const addListing = async (listingData: any, image: File)=>{
        loading.value = true;
        try{
            await createListing(listingData,image);
            await fetchListings();
        }catch(err){//TODO: ADD SNACKBAR
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
            const res = await getUserListings();
            console.log(res.data);
            listings.value = res.data.content ?? res.data;
        } catch (err) {//TODO: ADD SNACKBAR
            if (axios.isAxiosError(err)) {
                error.value = err.response?.data?.message ?? 'Failed to fetch user listings';
            } else {
            console.error(err);
            }
        } finally {
            loading.value = false;
        }
    }

const editListing = async (id: string, listingData: any, image?: File) => {
    loading.value = true;
    error.value = null;
    try {
        await updateListing(id, listingData, image);
        await fetchListings(); // refresh the list
    } catch (err) {//TODO: ADD SNACKBAR
        if (axios.isAxiosError(err)) {
            console.error('Status:', err.response?.status);
            console.error('Response data:', err.response?.data);
            error.value = err.response?.data?.message ?? 'Failed to update listing';
        }
    } finally {
        loading.value = false;
    }
}

const removeListing = async (id: string) => {
  loading.value = true;
  error.value = null;
  try {
    await deleteListing(id);
    await fetchUserListing(); // refresh list after delete
  } catch (err) { //TODO: ADD SNACKBAR
    if (axios.isAxiosError(err)) {
      error.value = err.response?.data?.message ?? 'Failed to delete listing';
    }
  } finally {
    loading.value = false;
  }
}

const fetchListingById = async (id: string) => {
  loading.value = true
  error.value = null
  try {
    const res = await getListingById(id)
    return res.data
  } catch (err) { //TODO: ADD SNACKBAR
    if (axios.isAxiosError(err)) {
      error.value = err.response?.data?.message ?? 'Failed to fetch listing'
    }
    return null
  } finally {
    loading.value = false
  }
}

return { listings, loading, error, fetchListings, fetchListingById, addListing, fetchUserListing, editListing, removeListing,page,loadMore }
}
