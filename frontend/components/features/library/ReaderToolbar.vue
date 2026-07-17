<template>
  <v-app-bar flat border="b" color="surface" height="64" style="position: sticky; top: 0; z-index: 20;">

    <NuxtLink :to="`/library`" class="text-primary font-weight-bold text-decoration-none ml-4">
      <v-icon start>mdi-arrow-left</v-icon>
      Back
    </NuxtLink>

    <v-app-bar-title class="text-body-1 font-weight-medium">
      {{ rulebook?.title }}
    </v-app-bar-title>

    <template #append>
      <div class="d-flex align-center ga-2 mr-4">

        <!-- Lock indicator + countdown -->
         <template v-if="lockHeldBy || isEditing">
          <span 
            class="text-caption text-no-wrap d-flex align-center ga-1"
            :class="isEditing ? 'text-success' : 'text-error'"
          >
            <v-icon size="14" :color="isEditing ? 'success' : 'error'"> mdi-lock</v-icon>
            {{ isEditing? 'You are editing' : `@${lockHeldBy}` }}
            <template v-if="countdown > 0">
              {{  formattedCountdown }}
            </template>
          </span>
         </template>

         <!-- Undo/Redo - only while editing -->
          <template v-if="isEditing">
              <v-tooltip text="Undo" location="bottom">
                <template #activator="{ props: tooltipProps }">
                  <v-btn 
                    v-bind="tooltipProps"
                    icon size="small" variant="text"
                    :disabled="!canUndo"
                    @click="emit('undo')"
                  >
                    <v-icon>mdi-undo</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>

              <v-tooltip text="Redo" location="bottom">
                <template #activator="{ props: tooltipProps }">
                  <v-btn 
                    v-bind="tooltipProps"
                    icon size="small" variant="text"
                    :disabled="!canRedo"
                    @click="emit('redo')"
                  >
                    <v-icon>mdi-redo</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>
            </template>

            <!-- Edit Button -->
             <v-tooltip
              :text="lockHeldBy ? `Currently being edited by @${lockHeldBy}` : 'Edit this section'"
              location="bottom"
            >
              <template #activator="{ props: tooltipProps }">
                <v-btn
                  v-bind="tooltipProps"
                  size="small"
                  variant="outlined"
                  color="primary"
                  prepend-icon="mdi-pencil"
                  :disabled="!!lockHeldBy || isEditing"
                  @click="emit('edit')"
                >
                  Edit
                </v-btn>
              </template>
            </v-tooltip>

            <!-- Download Button -->
            <v-tooltip text="Download PDF" location="bottom">
              <template #activator="{ props: tooltipProps }">
                <v-btn
                  v-bind="tooltipProps"
                  icon size="small"
                  variant="text"
                  :loading="isDownloading"
                  :disabled="!rulebook?.id"
                  @click="handleDownload"
                >
                  <v-icon>mdi-download</v-icon>
                </v-btn>
              </template>
            </v-tooltip>

            <!-- History Button -->
            <v-btn icon size="small" variant="text" @click="emit('toggle-history')">
              <v-icon>mdi-history</v-icon>
            </v-btn>

            <!-- Search  -->
            <template v-if="showSearch">
              <BaseSearch 
                v-model="localQuery"
                placeholder="Search in this rulebook"
                autofocus
                style="width: 220px;"
                @update:model-value="emit('search', $event)"
                @keydown.enter="$emit('next-match')"
                @keydown.escape="closeSearch"
              />

              <span v-if="localQuery" class="text-caption text-medium-emphasis text-no-wrap">
                {{ matchCount > 0 ? `${currentMatch + 1} / ${matchCount}`: 'No matches' }}
              </span>

              <v-btn icon size="small" variant="text" :disabled="matchCount === 0" @click="$emit('prev-match')">
                <v-icon size="18">mdi-chevron-up</v-icon>
              </v-btn>

              <v-btn icon size="small" variant="text" :disabled="matchCount === 0" @click="$emit('next-match')">
                <v-icon size="18">mdi-chevron-down</v-icon>
              </v-btn>

              <v-btn icon size="small" variant="text" @click="closeSearch">
                <v-icon size="18">mdi-close</v-icon>
              </v-btn>

              <v-btn icon size="small" variant="text" @click="showSearch = !showSearch">
                <v-icon>mdi-magnify</v-icon>
              </v-btn>

              <span class="text-caption text-medium-emphasis text-no-wrap">
                {{ currentPage + 1 }} / {{ totalPages }}
              </span>
            </template>

          
      </div>
    </template>

  </v-app-bar>
</template>

<script setup>
import { ref, computed, onUnmounted ,watch } from 'vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import { useSnackBar } from '~/composables/useSnackbar.ts'
import { useLibrary } from '~/composables/useLibrary'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute();
const router = useRouter();
const { show } = useSnackBar();

const { getDownloadLink, downloadUrl } = useLibrary();

const props = defineProps({
  rulebook: Object,
  currentPage: Number,
  totalPages: Number,
  searchQuery: { type: String, default: ''},
  matchCount: { type: Number, default: 0},
  currentMatch: { type: Number, default: 0},
  isEditing:     { type: Boolean, default: false },
  isSaving:      { type: Boolean, default: false },
  lockHeldBy: { type: String, default: null },
  lockExpiresAt: { type: String, default: null },
  lockError:     { type: String,  default: ''    },
  canUndo: { type: Boolean, default: false },
  canRedo: { type: Boolean, default: false },
})

const emit = defineEmits(['search', 'next-match', 'prev-match', 'clear-search', 'edit', 'toggle-history', 'undo', 'redo'])

const showSearch = ref(false)
const localQuery = ref('')
const countdown = ref(0)
let countdownInterval = null

const closeSearch = () => {
  showSearch.value = false
  emit('clear-search')
}

const formattedCountdown = computed(() => {
  const mins = Math.floor(countdown.value/ 60)
  const secs = countdown.value % 60
  return `${mins}:${secs.toString().padStart(2,'0')}`
})

const startCountdown = (expiresAt) => {
  clearInterval(countdownInterval)
  const update = () => {
    const remaining = Math.max(0, Math.floor((new Date(expiresAt) - Date.now()) /1000))
    countdown.value = remaining
    if(remaining === 0) clearInterval(countdownInterval)
  }
  update()
  countdownInterval = setInterval(update, 1000)
}

watch(() => props.lockExpiresAt, (val) => {
  if (val) startCountdown(val)
  else {
    clearInterval(countdownInterval)
    countdown.value = 0
  }
}, { immediate: true })

onUnmounted(() => clearInterval(countdownInterval))

// ========== Download Logic ==========
const isDownloading = ref<boolean>(false);

const handleDownload = async () => {
  if (!props.rulebook?.id) return

  try {
    isDownloading.value = true;

    await getDownloadLink(route.params.id);

    if(downloadUrl.value?.downloadUrl){
      show('Download ready - opening PDF...', 'success');
      console.log('Download requested for:', props.rulebook.id);
      window.open(downloadUrl.value.downloadUrl, '_blank', 'noopener,noreferrer');
    }else{
      throw new Error("No URL returned from server");
    }
  } catch (err){
    console.error('Download failed:', err)
    show('Download failed. Please try again later.', 'error')
  }finally{
    isDownloading.value = false;
  }
}

const formattedExpiry = computed(() => {
  if(!props.lockExpiresAt) return ''
  return new Date(props.lockExpiresAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
})

</script>