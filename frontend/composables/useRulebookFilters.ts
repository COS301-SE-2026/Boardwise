import { duration } from 'happy-dom/lib/PropertySymbol';
import {reactive, watch} from 'vue';

const filters = reactive({
    genre: ['all'],
    playerCount: '',
    duration: '',
    minAge: ''
});

watch(
    () => filters.genre,
    (newGenreArray) => {
        if(Array.isArray(newGenreArray) && newGenreArray.length > 1){
            filters.genre = [newGenreArray[newGenreArray.length - 1] ?? 'all'];
        }else if(!newGenreArray || newGenreArray.length === 0){
            filters.genre = ['all'];
        }
    }
);

export const useRulebookFilters = () => {
    const resetFilters = () => {
      filters.genre = ['all']
      filters.playerCount = ''
      filters.duration = ''
      filters.minAge = ''
    }
    
    return{
        filters,
        resetFilters
    }
}