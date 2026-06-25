<template>
  <div v-if="loading"></div>
  <div v-else-if="listing">

    <ContentSection>
      <SpecificItemHero :listing="listing" />
    </ContentSection>

    <ContentSection>
      <SpecificItemGameInfo :rulebook="rulebook" />
    </ContentSection>

    <ContentSection v-if="rulebook">
      <SpecificItemRulebook :rulebook="rulebook" />
    </ContentSection>


  </div>
</template>

<script setup>
import { computed,ref,onMounted } from 'vue'
import { rulebooks } from '~/services/mockData/rulebooks'
import ContentSection from '~/components/layout/ContentSection.vue'
import SpecificItemHero from './SpecificItemHero.vue'
import { useRoute } from 'vue-router'
import SpecificItemGameInfo from './SpecificItemGameInfo.vue'
import SpecificItemRulebook from './SpecificItemRulebook.vue'
import { getListingById } from '~/services/marketplaceService.js'
const route = useRoute()
const router = useRouter()

const listing = ref(null)
const loading = ref(true)


onMounted(async ()=>{
  const itemId = route.params.id;
  res = await getListingById(itemId);

  if(!res){
    //TODO: add an error
    router.push('/marketplace');
    return;
  }
  else{
    listing.value = res;
    loading.value=false;
  }
})

const props = defineProps ({
    listing: {
        type: Object,
        required: true
    }
})

const rulebook = computed(() =>
  rulebooks.find(r => r.id === props.listing.rulebookId) ?? null
)
</script>
