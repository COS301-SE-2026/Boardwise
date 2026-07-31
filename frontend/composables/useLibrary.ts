import { ref } from 'vue'
import { LibraryService } from '~/services/libraryService'

const token = ref<string|null>(import.meta.client ? localStorage.getItem('access_token') : null)
const error = ref<string>('');
const isLoading = ref<boolean>(false);
const rulebooks = ref<any[]>([]);
const featuredRulebooks = ref<any[]>([]);
const currentRulebook = ref<any>(null);
const downloadUrl = ref<any>(null);
const rulebookText = ref<any>(null);

const page = ref(1);
const hasMore = ref(true);
const activeFilters = ref({});

export const useLibrary = () => {
    const getAllRulebooks = async (filters = {}, reset = false) => {
        if(reset){
            activeFilters.value = filters;
            page.value = 1;
            rulebooks.value = [];
            hasMore.value = true;
        }
        
        if(!hasMore.value) return;

        error.value = '';
        isLoading.value = true;

        try {
            const response = await LibraryService.fetchAllRulebooks({
                ...activeFilters.value,
                page: page.value,
                limit: 20
            });
            
            const newContent = response?.content || [];

            rulebooks.value = reset ? newContent : [...rulebooks.value, ...newContent];
            hasMore.value = response? !response.last : false;
            page.value += 1;
            // console.log('fetched', response.content);
        } catch(err: any) {
            error.value = err.data?.message || 'No rulebooks found'
            if (reset) rulebooks.value = []
        } finally {
            isLoading.value = false;
        }
    }

    const getRulebookById = async (id: string) => {
        error.value = '';
        isLoading.value = true;
        currentRulebook.value = null;

        try{
            const rawData = await LibraryService.fetchRulebookById(id);
            currentRulebook.value = rawData;
        }catch(err: any){
            console.error(`Failed to fetch rulebook ${id}:`, err);
            error.value = err.data?.message || 'Failed to load rulebook details';
        }finally{
            isLoading.value = false;
        }
    };

    const loadMore = () => {
        if(!isLoading.value && hasMore.value){
            getAllRulebooks(activeFilters.value, false);
        }
    };

    const fetchFeaturedRulebooks = async () => {
        try{
            const response = await LibraryService.fetchAllRulebooks({page:1, limit:10});
            featuredRulebooks.value = response?.content || [];
        }catch(err){
            console.error("Failed to fetch featured rulebooks", err);
        }
    }

    const getRulebookText = async (id: string) => {
        error.value = '';
        isLoading.value = true;
        try {
            const response = await LibraryService.fetchRulebookText(id)
            rulebookText.value = response;
        } catch (err: any) {
            console.error(`Failed to fetch rulebook text ${id}:`, err)
            rulebookText.value = null
            error.value = err.data?.message || "Failed to load rulebook text.";
        }finally{
            isLoading.value = false;
        }
    }

    const getDownloadLink = async (id: string) =>{
        error.value = '';
        isLoading.value = true;
        try{
            const response = await LibraryService.fetchDownloadRulebook(id);
            downloadUrl.value = response;
        }catch(err:any){
            console.error(`Failed to get download link for rulebook ${id}:`, err);
            downloadUrl.value = null;
            error.value = err.data?.message || "Failed to generate download link";
        }finally{
            isLoading.value = false;
        }
    }


    return {
        token,
        error,
        isLoading,
        rulebooks,
        featuredRulebooks,
        currentRulebook,
        rulebookText,
        downloadUrl,
        getAllRulebooks,
        loadMore,
        hasMore,
        fetchFeaturedRulebooks,
        getRulebookById,
        getRulebookText,
        getDownloadLink
    }
}