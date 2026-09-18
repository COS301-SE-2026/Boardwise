<template>
  <div>
    <v-app-bar flat border="b" color="surface" height="64" style="position: sticky; top: 0; z-index: 20;">

      <NuxtLink :to="`/library`" class="text-primary font-weight-bold text-decoration-none ml-4 d-flex align-center">
        <v-icon start>mdi-arrow-left</v-icon>
        <span class="d-none d-sm-inline">Back</span>
      </NuxtLink>

      <v-app-bar-title class="text-body-1 font-weight-medium text-truncate">
        {{ rulebook?.title }}
      </v-app-bar-title>

      <template #append>
        <div class="d-flex align-center ga-2 mr-4">

          <!-- Lock indicator + countdown -->
           <template v-if="lockHeldBy || isEditing">
            <span 
              class="text-caption text-no-wrap d-none d-sm-flex align-center ga-1"
              :class="isEditing ? 'text-success' : 'text-error'"
            >
              <v-icon size="14" :color="isEditing ? 'success' : 'error'"> mdi-lock</v-icon>
              {{ isEditing? 'You are editing' : `@${lockHeldBy}` }}
              <template v-if="countdown > 0">
                {{  formattedCountdown }}
              </template>
            </span>
           </template>

           <!-- Desktop: Undo/Redo inline. Mobile: tucked into the overflow menu below -->
            <template v-if="isEditing && mdAndUp">
                <v-tooltip text="Undo" location="bottom">
                  <template #activator="{ props: tooltipProps }">
                    <BaseButton
                      v-bind="tooltipProps"
                      icon size="small" variant="text"
                      :disabled="!canUndo"
                      @click="emit('undo')"
                    >
                      <v-icon>mdi-undo</v-icon>
                    </BaseButton>
                  </template>
                </v-tooltip>

                <v-tooltip text="Redo" location="bottom">
                  <template #activator="{ props: tooltipProps }">
                    <BaseButton
                      v-bind="tooltipProps"
                      icon size="small" 
                      variant="text"
                      :disabled="!canRedo"
                      @click="emit('redo')"
                    >
                      <v-icon>mdi-redo</v-icon>
                    </BaseButton>
                  </template>
                </v-tooltip>
            </template>

              <!-- Edit/ Done Editing Button -->
              <template v-if="isEditing">
                <BaseButton
                  size="sm"
                  variant="secondary"
                  :prepend-icon="mdAndUp ? 'mdi-check' : undefined"
                  :icon="!mdAndUp ? true : undefined"
                  @click="emit('stop-editing')"
                >
                  <v-icon v-if="!mdAndUp">mdi-check</v-icon>
                  <span v-if="mdAndUp">Done Editing</span>
                </BaseButton>
              </template>
              <template v-else>
                <v-tooltip
                  :text="lockHeldBy ? `Currently being edited by @${lockHeldBy}` : 'Edit this section'"
                  location="bottom"
                >
                  <template #activator="{ props: tooltipProps }">
                    <BaseButton
                      v-bind="tooltipProps"
                      size="sm"
                      variant="secondary"
                      :prepend-icon="mdAndUp ? 'mdi-pencil' : undefined"
                      :icon="!mdAndUp ? true : undefined"
                      :disabled="!!lockHeldBy"
                      @click="emit('edit')"
                    >
                      <v-icon v-if="!mdAndUp">mdi-pencil</v-icon>
                      <span v-if="mdAndUp">Edit</span>
                    </BaseButton>
                  </template>
                </v-tooltip>
              </template>

              <!-- Desktop: Download + History inline. Mobile: overflow menu -->
              <template v-if="mdAndUp">
                <v-tooltip text="Download PDF" location="bottom">
                  <template #activator="{ props: tooltipProps }">
                    <BaseButton
                      v-bind="tooltipProps"
                      icon size="sm"
                      variant="text"
                      :loading="isDownloading"
                      :disabled="!rulebook?.id"
                      @click="handleDownload"
                    >
                      <v-icon>mdi-download</v-icon>
                    </BaseButton>
                  </template>
                </v-tooltip>

                <BaseButton 
                  icon size="small" 
                  variant="text"
                  @click="emit('toggle-history')"
                >
                  <v-icon>mdi-history</v-icon>
                </BaseButton>
              </template>

              <!-- Mobile overflow menu: Undo/Redo/Download/History -->
              <v-menu v-if="!mdAndUp" location="bottom end">
                <template #activator="{ props: menuProps }">
                  <BaseButton icon size="small" variant="text" v-bind="menuProps" aria-label="More actions">
                    <v-icon>mdi-dots-vertical</v-icon>
                  </BaseButton>
                </template>

                <v-list density="compact" min-width="180">
                  <v-list-item
                    v-if="isEditing"
                    prepend-icon="mdi-undo"
                    title="Undo"
                    :disabled="!canUndo"
                    @click="emit('undo')"
                  />
                  <v-list-item
                    v-if="isEditing"
                    prepend-icon="mdi-redo"
                    title="Redo"
                    :disabled="!canRedo"
                    @click="emit('redo')"
                  />
                  <v-list-item
                    prepend-icon="mdi-download"
                    title="Download PDF"
                    :disabled="!rulebook?.id || isDownloading"
                    @click="handleDownload"
                  />
                  <v-list-item
                    prepend-icon="mdi-history"
                    title="History"
                    @click="emit('toggle-history')"
                  />
                </v-list>
              </v-menu>

              <!-- Search (desktop: inline) -->
              <template v-if="mdAndUp">
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

                  <BaseButton icon size="small" variant="text" :disabled="matchCount === 0" @click="$emit('prev-match')">
                    <v-icon size="18">mdi-chevron-up</v-icon>
                  </BaseButton>

                  <BaseButton icon size="small" variant="text" :disabled="matchCount === 0" @click="$emit('next-match')">
                    <v-icon size="18">mdi-chevron-down</v-icon>
                  </BaseButton>

                  <BaseButton icon size="small" variant="text" @click="closeSearch">
                    <v-icon size="18">mdi-close</v-icon>
                  </BaseButton>

                  <span class="text-caption text-medium-emphasis text-no-wrap">
                    {{ currentPage + 1 }} / {{ totalPages }}
                  </span>
                </template>
                <template v-else>
                  <BaseButton icon size="small" variant="text" @click="showSearch = !showSearch">
                    <v-icon>mdi-magnify</v-icon>
                  </BaseButton>
                </template>
              </template>

              <!-- Search toggle (mobile: opens the row below instead of inline) -->
              <BaseButton v-else icon size="small" variant="text" @click="showSearch = !showSearch">
                <v-icon>{{ showSearch ? 'mdi-close' : 'mdi-magnify' }}</v-icon>
              </BaseButton>
        </div>
      </template>
    </v-app-bar>

    <!-- Mobile search row: its own full-width bar below the toolbar,
         instead of trying to cram into the fixed-height app-bar -->
    <div v-if="!mdAndUp && showSearch" class="mobile-search-row d-flex align-center ga-2 pa-2">
      <BaseSearch 
        v-model="localQuery"
        placeholder="Search in this rulebook"
        autofocus
        class="flex-grow-1"
        @update:model-value="emit('search', $event)"
        @keydown.enter="$emit('next-match')"
        @keydown.escape="closeSearch"
      />

      <span v-if="localQuery" class="text-caption text-medium-emphasis text-no-wrap">
        {{ matchCount > 0 ? `${currentMatch + 1} / ${matchCount}`: 'No matches' }}
      </span>

      <BaseButton icon size="small" variant="text" :disabled="matchCount === 0" @click="$emit('prev-match')">
        <v-icon size="18">mdi-chevron-up</v-icon>
      </BaseButton>

      <BaseButton icon size="small" variant="text" :disabled="matchCount === 0" @click="$emit('next-match')">
        <v-icon size="18">mdi-chevron-down</v-icon>
      </BaseButton>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted ,watch } from 'vue'
import { useDisplay } from 'vuetify'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import { useSnackBar } from '~/composables/useSnackbar.ts'
import { useLibrary } from '~/composables/useLibrary'
import { useRoute } from 'vue-router'

const route = useRoute();
const { show } = useSnackBar();
const { mdAndUp } = useDisplay();

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

const emit = defineEmits(['search', 'next-match', 'prev-match', 'clear-search', 'edit', 'stop-editing','toggle-history', 'undo', 'redo'])

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
const isDownloading = ref(false);

const handleDownload = async () => {
  if (!props.rulebook?.id) return

  try {
    isDownloading.value = true;

    await getDownloadLink(route.params.id);

    if(downloadUrl.value?.downloadUrl){
      show('Download ready - opening PDF...', 'success');
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
</script>