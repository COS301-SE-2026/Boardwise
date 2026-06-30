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
import {ref,onMounted } from 'vue'
import ContentSection from '~/components/layout/ContentSection.vue'
import SpecificItemHero from './SpecificItemHero.vue'
import { useRoute, useRouter  } from 'vue-router'
import SpecificItemGameInfo from './SpecificItemGameInfo.vue'
import SpecificItemRulebook from './SpecificItemRulebook.vue'
import { MarketplaceService } from '~/services/marketplaceService.js'
const route = useRoute()
const router = useRouter()

const listing = ref(null)
const loading = ref(true)
const error = ref('')

onMounted(async ()=>{
  const itemId = route.params.id;
  const res = await MarketplaceService.getListingById(itemId);

  if(res === null){
    error.value = 'Listing not found'
    router.push('/marketplace');
  }
  else{
    listing.value = res;
    loading.value=false;
  }
})


// const rulebook = computed(() =>
//   rulebooks.find(r => r.id === props.listing.rulebookId) ?? null
// )
</script>
