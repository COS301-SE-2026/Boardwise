<template>
  <v-card rounded="lg" class="pa-4" style="position: sticky; top: 80px;">

    <p class="text-caption text-uppercase font-weight-bold text-medium-emphasis mb-3">
      Contents
    </p>

    <v-list density="compact" nav>
      <v-list-item
        v-for="(page, index) in pages"
        :key="index"
        :active="currentPage === index"
        active-color="primary"
        rounded="lg"
        @click="$emit('change', index)"
      >
        <template #prepend>
          <v-avatar
            size="24"
            :color="currentPage === index ? 'primary' : 'grey-lighten-2'"
          >
            <span class="text-caption font-weight-bold" :class="currentPage === index ? 'text-white' : 'text-grey'">
              {{ index + 1 }}
            </span>
          </v-avatar>
        </template>
        <v-list-item-title class="text-body-2">{{ page.title }}</v-list-item-title>
      </v-list-item>
    </v-list>

  </v-card>
</template>

<script setup>
defineProps({
  pages: {
    type: Array,
    default: () => []
  },
  currentPage: Number
})

defineEmits(['change'])
</script>