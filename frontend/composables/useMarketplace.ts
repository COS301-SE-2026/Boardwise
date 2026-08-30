import { MarketplaceService, type ListingResponse } from '~/services/marketplaceService'
import { ref } from 'vue'
import { useSnackBar } from './useSnackbar'
import { createSharedComposable } from '@vueuse/core'
const { show } = useSnackBar()


const _useMarketplace = () =>{

    //page paramters
    const page = ref(1)
    const hasMore = ref(true)
    const pageSize = 15

    //storing listings
    const listings = ref<Array<ListingResponse>>([]); //listings in db
    
    //checks if it loads
    const loading = ref(false);
    
    //error checking 
    const error = ref(null);

    const activeFilters = ref({})

    //page for personalised listings
    const persPage = ref(0);

    const loadMore = () => {
        if (!loading.value && hasMore.value) fetchListings(activeFilters.value, false)
    }


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
            const res = await MarketplaceService.getListings({
                ...activeFilters.value,
                page: page.value,
                size: pageSize
            });
            listings.value = reset ? res.content : [...listings.value, ...res.content];
            hasMore.value = !res.last;
            page.value += 1;
        } catch(err) {
            if(!activeFilters.value){
                show('Failed to find any listings!', 'error');
            }
        } finally {
            loading.value = false;
        }
    };

    const addListing = async (listingData: any, image: File)=>{
        loading.value = true;
        error.value = null;
        try{
            await MarketplaceService.createListing(listingData,image);
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
  } catch (err: any) {
     error.value = err.data?.message ?? 'Failed to delete listing';
     show('Failed to delete listing','error');
  } finally {
    loading.value = false;
  }
}

const fetchListingById = async (id: string) => {
  error.value = null
  try {
    const res = await MarketplaceService.getListingById(id)
    return res
  } catch (err: any) {
      error.value = err.data?.message ?? 'Failed to fetch listing'
      show( 'Failed to fetch listing','error');
    return null
  } finally {
    loading.value = false
  }
}



return { listings, loading, error, fetchListings, fetchListingById, addListing, fetchUserListing, editListing, removeListing,page,loadMore,hasMore, }
}

export const useMarketplace = createSharedComposable(_useMarketplace)
