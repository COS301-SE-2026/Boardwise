import {getListings , createListing, getUserListings} from '@/services/marketplaceService'
import { ref } from 'vue'
import axios from 'axios'


export const useMarketplace = () =>{
    //storing listings
    const listings = ref([]); //listings in db
    
    //checks if it loads
    const loading = ref(false);
    
    //error checking 
    const error = ref(null);

    const fetchListings = async () => {
        loading.value = true;// so it can refresh 
        try{
            const res = await getListings(); //store response of call
            listings.value = res.data; // store data 
        }
        catch(err){
            console.error('Failed to fetch ' + err);
        } finally{
            loading.value = false; // set loading to false 
        }
    }

    const addListing = async (listingData: any, image: File)=>{
        loading.value = true;
        try{
            await createListing(listingData,image);
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
        const res = await getUserListings();
        listings.value = res.data ?? [];
    } catch (err) {
        if (axios.isAxiosError(err)) {
            console.error('Status:', err.response?.status);
            console.error('Response data:', err.response?.data);
            error.value = err.response?.data?.message ?? 'Failed to fetch user listings';
        }
    } finally {
        loading.value = false;
    }
}

    return { listings, loading, error, fetchListings, addListing, fetchUserListing };
}
