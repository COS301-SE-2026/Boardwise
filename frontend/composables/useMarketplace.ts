import { MarketplaceService, type ListingResponse } from '~/services/marketplaceService'
import { ref } from 'vue'
import { useSnackBar } from './useSnackbar'
import { createSharedComposable } from '@vueuse/core'
const { show } = useSnackBar()


const _useMarketplace = () =>{

    const firstPage = useState<{ listings: ListingResponse[]; hasMore: boolean } | null>(
        'marketplace-first-page', () => null
    )

    //page paramters
    const page = ref(0)
    const hasMore = ref(true)
    const pageSize = 25

    //storing listings
    const listings = ref<Array<ListingResponse>>([]); //listings in db
    
    //store users listings
    const userListings = ref<Array<ListingResponse>>([]); //listings in db  

    //checks if it loads
    const loading = ref(false);
    
    //error checking 
    const error = ref<string | null>(null);

    const activeFilters = ref({})

    const loadMore = () => {
        if (!loading.value && hasMore.value) fetchListings(activeFilters.value, false)
    }

    const refresh = () => {
        firstPage.value = null
        return Promise.allSettled([
            fetchUserListing(),
            fetchListings(activeFilters.value, true),
        ])
    }

    const fetchListings = async (filters?: {
        listingType?: string | null,
        genres?: string[] | null,
        conditions?: string[] | null,
        minPrice?: number | null,
        maxPrice?: number | null,
        page?: number,
        size?: number,
        gameTitle?: string | null,
        listingTitle?: string | null
    }, reset = false) => {

        const isDefault = !filters || Object.values(filters).every(v => v == null)
        const cached = reset && isDefault ? firstPage.value : null

        if (reset) {
            activeFilters.value = filters ?? {}
            page.value = 0
            hasMore.value = true
            listings.value = cached?.listings ?? []
        }

        if (!hasMore.value) return

        loading.value = !cached
        try {
            const res = await MarketplaceService.getListings({
                ...activeFilters.value,
                page: page.value,
                size: pageSize
            })

            listings.value = reset ? res.content : [...listings.value, ...res.content]
            hasMore.value = !res.last
            page.value += 1

            if (reset && isDefault) {
                firstPage.value = { listings: res.content, hasMore: !res.last }
            }
            
        } catch (err) {
            show("We couldn't find anything!", 'error')
            throw err
        } finally {
            loading.value = false
        }
    }

    const addListing = async (listingData: any, image: File)=>{
        loading.value = true;
        error.value = null;
        try{
            await MarketplaceService.createListing(listingData,image);
            show('Listing successfully created!');
            await refresh();
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
            userListings.value = res ?? [];
        } catch (err: any) {
            error.value = err.data?.message ?? 'Failed to fetch user listings';
            show("", 'error');
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
            await refresh()
            show('Successfully updated your listing!')
            return true;
        } catch (err: any) {
            console.error('Status:', err.status);
            console.error('Response data:', err.response?.data);
            error.value = err.response?.data?.message ?? 'Failed to update listing';
            show('Could not update your listing, Try Again', 'error');
            return false;
        } finally {
            loading.value = false;
        }
    }

    const removeListing = async (id: string) => {
        loading.value = true;
        error.value = null;
        try {
            await MarketplaceService.deleteListing(id);
            await refresh();
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

    const getOtherUsersListings = async (id: string) =>{
        loading.value = true;
        try{
            const res = await MarketplaceService.getOtherUsersListings(id);
            return res;
        }
        catch(err: any){
            error.value = err.data?.message ?? 'Failed to fetch other users listings'
            show('Failed to fetch other users listing','error');
            return null;
        }
        finally{
            loading.value = false;
        }
    }

    const rentOutListing = async(id: string) =>{
        loading.value = true;
        try{
            const res = await MarketplaceService.rentOutListing(id);
            await refresh();
            return res;
        }catch(err:any){
            show('Failed to rent out listing','error');
            return  null;
        }finally{
            loading.value = false;
        }
    }

    const returnRentedOutListing = async (id: string) =>{
        loading.value = true;
        try{
            const res = await MarketplaceService.returnRentedOutListing(id);
            await refresh();
            return res;
        }catch(err:any){
            show('Failed to rent out listing','error');
            return  null;
        }finally{
            loading.value = false;
        }
    }

    return { 
        listings, 
        loading, 
        error, 
        fetchListings, 
        fetchListingById, 
        addListing,
        fetchUserListing,
        editListing, 
        removeListing,
        page,
        loadMore,
        hasMore,
        userListings,
        getOtherUsersListings,
        rentOutListing,
        returnRentedOutListing
     }
}

export const useMarketplace = createSharedComposable(_useMarketplace)
