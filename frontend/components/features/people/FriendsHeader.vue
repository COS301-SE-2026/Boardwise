<template>
    <div class="d-flex flex-column ga-5" data-test="friends-header">
        <div class="friend-header__top">
            <SectionTitle
                data-test="friend-title"
                title="Friends & Players"
                subtitle="Find tabletop players, connect with friends and grow your gaming circle"
            />

            <BaseButton 
                data-test="invite-frined-button"
                variant="primary"
                :block="mobile"
                prepend-icon="mdi-account-plus-outline"
                @clikc="$emit('invite')"
            >
                Invite Friend
            </BaseButton>

            <BaseSearch
                data-test="friends-search"
                v-model="search"
                placeholder="Search by username or name..."
                aria-label="Search players"
                @update:model-value="$emit('search', $event)"
            />
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { useDisplay } from 'vuetify'

import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'

const { mobile } = useDisplay()
const search = ref('')

defineEmits(['search', 'invite'])
</script>