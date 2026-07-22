<template>
  <BaseModal v-model="open">
    <div class="d-flex flex-column ga-6">
      <h2> Invite Your Friends</h2>

      <BaseInput
        v-model="searchQuery"
        label="Search"
        placeholder="Search by username"
      />

      <div class="d-flex flex-column ga-2">
        <div
          v-if="isSearching"
          class="text-body-2 text-medium-emphasis text-center py-4"
        >
        Searching...
      </div>

      <div
          v-else-if="searchQuery.trim() && results.length === 0"
          class="text-body-2 text-medium-emphasis text-center py-4"
      >
        No users found
      </div>

      <div
          v-for="user in results"
          :key="user.id"
          class="d-flex align-center justify-space-between"
      >

      <div class="d-flex align-center ga-3">
            <BaseImage
              :src="user.profilePicture ?? '/default-avatar.png'"
              :alt="user.username"
              width="36px"
              height="36px"
              fit="cover"
              style="border-radius: 50%;"
            />

        <div>
              <p class="text-body-2 font-weight-bold mb-0">{{ user.username }}</p>
              <p class="text-caption text-medium-emphasis mb-0">{{ user.fullName }}</p>
            </div>
          </div>

          <v-btn
            icon
            variant="text"
            :disabled="invitedUsernames.has(user.username)"
            :loading="invitingUsername === user.username"
            @click="handleInvite(user)"
          >
            <v-icon>
              {{ invitedUsernames.has(user.username) ? 'mdi-check' : 'mdi-plus' }}
            </v-icon>
          </v-btn>
        </div>
      </div>

      <div class="d-flex justify-end">
        <BaseButton variant="secondary" @click="closeModal">
          Close
        </BaseButton>
      </div>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref, watch } from 'vue'

import BaseButton from '~/components/ui/BaseButton.vue';
import BaseInput from '~/components/ui/BaseInput.vue';
import BaseModal from '~/components/ui/BaseModal.vue';
import BaseImage from '~/components/ui/BaseImage.vue';


import { userService } from '~/services/userService'
import { useEvents } from '~/composables/useEvents'
import { useSnackBar } from '~/composables/useSnackbar'

const { show } = useSnackBar()
const { inviteUser } = useEvents()


const open = defineModel({
    type: Boolean,
    default: false
})

const props = defineProps({
  event:{
    type: Object,
    default: null
  }
});

const searchQuery = ref('');
const results = ref([]);

const isSearching = ref(false);
const invitingUsername = ref(null);
const invitedUsernames = ref(new Set());

let searchTimeout;
watch(searchQuery, (query) =>{
  clearTimeout(searchTimeout);

  if(!query.trim()){
    results.value = [];
    return;
  }

  searchTimeout = setTimeout(async () => {
    isSearching.value = true
    try {
      results.value = await userService.searchForUser(query.trim())
    } catch {
      results.value = []
    } finally {
      isSearching.value = false
    }
  }, 400)
});

watch(open, (isOpen) => {
  if (isOpen) {
    searchQuery.value = ''
    results.value = []
    invitedUsernames.value = new Set()
    invitingUsername.value = null
  }
})



const closeModal = () => {
  open.value = false
}

const handleInvite  = async (user) => {
  if (!props.event) return

  invitingUsername.value = user.username
  try {
    await inviteUser(user.username, props.event.id)
    invitedUsernames.value.add(user.username)
    show(`Invite was sent to: ${user.username}`, 'success')
  } catch (err) {
    show(err?.data?.message || `Failed to invite ${user.username}.`, 'error')
  } finally {
    invitingUsername.value = null
  }
}

</script>
