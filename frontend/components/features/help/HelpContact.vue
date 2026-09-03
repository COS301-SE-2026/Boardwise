<template>
  <section class="help-contact py-12" aria-labelledby="support-heading">
    <v-container>
      <div class="text-center mb-8">
        <h2 id="support-heading">Additional help</h2>

        <p class="text-medium-emphasis mt-3">
          Contact the Boardwise team, submit a complaint, or report a problem.
        </p>
      </div>

      <BaseGrid
        cols="260px"
        gap="24px"
      >
        <BaseCard
          v-for="option in requestOptions"
          :key="option.type"
          class="pa-6 d-flex flex-column"
        >
          <v-icon
            :icon="option.icon"
            :color="option.color"
            size="38"
            class="mb-4"
          />

          <h3 class="text-h6">
            {{ option.title }}
          </h3>

          <p class="text-medium-emphasis mt-2 mb-6">
            {{ option.description }}
          </p>

          <BaseButton
            class="mt-auto"
            :variant="option.buttonVariant"
            block
            @click="openForm(option.type)"
          >
            {{ option.buttonText }}
          </BaseButton>
        </BaseCard>
      </BaseGrid>

      <p class="text-center text-medium-emphasis mt-6">
        Prefer email?
        <a href="mailto:support@boardwise.co.za">
          support@boardwise.co.za
        </a>
      </p>
    </v-container>

    <BaseModal
          v-model="showForm"
          :title="currentRequest.title"
          :max-width="720"
          closable
        >
          <p class="text-medium-emphasis mb-6">
            {{ currentRequest.formDescription }}
          </p>

  <v-form ref="formRef" @submit.prevent="submitRequest">
            <v-text-field
              v-model.trim="form.name"
              label="Your name"
              autocomplete="name"
              :rules="requiredRules"
              class="mb-3"
            />

            <v-text-field
              v-model.trim="form.email"
              label="Email address"
              type="email"
              autocomplete="email"
              :rules="emailRules"
              class="mb-3"
            />

            <v-select
              v-model="form.category"
              label="Category"
              :items="currentRequest.categories"
              :rules="requiredRules"
              class="mb-3"
            />

            <v-text-field
              v-if="selectedType === 'report'"
              v-model.trim="form.relatedItem"
              label="Username, listing, community, or page URL"
              hint="Tell us what this report relates to"
              persistent-hint
              class="mb-3"
            />

            <v-text-field
              v-model.trim="form.subject"
              label="Subject"
              :rules="requiredRules"
              maxlength="100"
              counter
              class="mb-3"
            />

            <v-textarea
              v-model.trim="form.message"
              label="How can we help?"
              :rules="messageRules"
              rows="5"
              maxlength="2000"
              counter
              class="mb-3"
            />

            <v-file-input
              v-model="form.attachment"
              label="Attach a screenshot (optional)"
              accept="image/png,image/jpeg,application/pdf"
              prepend-icon="mdi-paperclip"
              show-size
              clearable
              class="mb-3"
            />

            <v-checkbox
              v-model="form.consent"
              :rules="consentRules"
              label="I confirm that the information provided is accurate."
            />

            <v-alert
              type="info"
              variant="tonal"
              density="compact"
              class="mb-5"
            >
               Your request will be recorded after submission.
            </v-alert>

            <div class="d-flex flex-wrap justify-end ga-3">
              <BaseButton
                variant="text"
                @click="closeForm"
              >
                Cancel
              </BaseButton>

              <BaseButton
                type="submit"
                variant="primary"
                :loading="isSubmitting"
              >
                Submit request
              </BaseButton>
            </div>
          </v-form>
          </BaseModal>

    <BaseModal
        v-model="showSuccess"
        title="Request received"
        :max-width="500"
      >
        <div class="text-center">
          <v-icon
            icon="mdi-check-circle"
            color="success"
            size="64"
            class="mb-4"
          />

          <p class="text-medium-emphasis mt-3">
            Your request has been recorded. Keep this reference number for
            follow-up:
          </p>

          <v-chip
            color="primary"
            variant="tonal"
            size="large"
            class="my-5"
          >
            {{ referenceNumber }}
          </v-chip>

          <p class="text-caption text-medium-emphasis mb-5">
            Keep your reference number if you need to follow up with the Boardwise team.
          </p>

          <BaseButton
            variant="primary"
            block
            @click="showSuccess = false"
          >
            Done
          </BaseButton>
        </div>
      </BaseModal>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseGrid from '~/components/ui/BaseGrid.vue'
import BaseModal from '~/components/ui/BaseModal.vue'

const showForm = ref(false)
const showSuccess = ref(false)
const isSubmitting = ref(false)
const selectedType = ref('support')
const referenceNumber = ref('')
const formRef = ref(null)

const requestOptions = [
  {
    type: 'support',
    title: 'Contact Support',
    description: 'Get help with your account or a Boardwise feature.',
    formDescription: 'Tell us what you need help with.',
    buttonText: 'Get support',
    icon: 'mdi-lifebuoy',
    color: 'primary',
    categories: [
      'Account and profile',
      'Technical problem',
      'Accessibility',
      'Rulebooks and library',
      'Marketplace',
      'Other'
    ],
    buttonVariant: 'primary'
  },
  {
    type: 'complaint',
    title: 'Submit a Complaint',
    description: 'Tell us about a service or moderation concern.',
    formDescription: 'Describe your complaint and what outcome you expect.',
    buttonText: 'Submit complaint',
    icon: 'mdi-message-alert-outline',
    color: 'secondary',
    categories: [
      'Service experience',
      'Moderation decision',
      'Privacy concern',
      'Accessibility concern',
      'Other'
    ],
    buttonVariant: 'secondary'
  },
  {
    type: 'report',
    title: 'Report Something',
    description: 'Report inappropriate content, users, or listings.',
    formDescription: 'Provide enough information for the team to investigate.',
    buttonText: 'Make a report',
    icon: 'mdi-flag-outline',
    color: 'error',
    categories: [
      'User behaviour',
      'Community content',
      'Marketplace listing',
      'Event',
      'Spam or scam',
      'Copyright concern',
      'Other'
    ],
    buttonVariant: 'error'
  }
]

const currentRequest = computed(() => {
  return (
    requestOptions.find(option => option.type === selectedType.value) ??
    requestOptions[0]
  )
})

const form = reactive({
  name: '',
  email: '',
  category: null,
  relatedItem: '',
  subject: '',
  message: '',
  attachment: null,
  consent: false
})

const requiredRules = [
  value => Boolean(value?.toString().trim()) || 'This field is required'
]

const isValidEmail = value => {
  const email = value?.trim()

  if (!email || email.includes(' ')) return false

  const atIndex = email.indexOf('@')
  const lastAtIndex = email.lastIndexOf('@')
  const dotIndex = email.indexOf('.', atIndex + 2)

  return (
    atIndex > 0 &&
    atIndex === lastAtIndex &&
    dotIndex > atIndex + 1 &&
    dotIndex < email.length - 1
  )
}

const emailRules = [
  value => Boolean(value?.trim()) || 'Email address is required',
  value => isValidEmail(value) || 'Enter a valid email address'
]

const messageRules = [
  value => Boolean(value?.trim()) || 'Please enter a message',
  value =>
    value?.trim().length >= 20 ||
    'Please provide at least 20 characters'
]

const consentRules = [
  value => value === true || 'Please confirm before submitting'
]

const resetForm = () => {
  form.name = ''
  form.email = ''
  form.category = null
  form.relatedItem = ''
  form.subject = ''
  form.message = ''
  form.attachment = null
  form.consent = false
  formRef.value?.resetValidation()
}

const openForm = type => {
  resetForm()
  selectedType.value = type
  showForm.value = true
}

const closeForm = () => {
  showForm.value = false
}

const createReferenceNumber = () => {
  const date = new Date()
    .toISOString()
    .slice(0, 10)
    .replaceAll('-', '')

  const randomValues = new Uint32Array(1)
  crypto.getRandomValues(randomValues)

  const randomNumber = 1000 + (randomValues[0] % 9000)

  return `BW-${date}-${randomNumber}`
}

const submitRequest = async () => {
  const validation = await formRef.value?.validate()

  if (!validation?.valid) return

  isSubmitting.value = true

  await new Promise(resolve => setTimeout(resolve, 700))

  referenceNumber.value = createReferenceNumber()
  isSubmitting.value = false
  showForm.value = false
  showSuccess.value = true
}
</script>