<template>
  <section class="help-contact py-12" aria-labelledby="support-heading">
    <v-container>
      <div class="text-center mb-8">
        <h2 id="support-heading">Additional help</h2>

        <p class="text-medium-emphasis mt-3">
          Contact the Boardwise team, submit a complaint, or report a problem.
        </p>
      </div>

      <v-row justify="center">
        <v-col
          v-for="option in requestOptions"
          :key="option.type"
          cols="12"
          sm="6"
          md="4"
        >
          <v-card
            class="request-card pa-6 d-flex flex-column"
            rounded="xl"
            variant="outlined"
            height="100%"
          >
            <v-icon
              :icon="option.icon"
              :color="option.color"
              size="38"
              class="mb-4"
            />

            <h3 class="text-h6">{{ option.title }}</h3>

            <p class="text-medium-emphasis mt-2 mb-6">
              {{ option.description }}
            </p>

            <v-btn
              class="mt-auto"
              :color="option.color"
              variant="tonal"
              block
              @click="openForm(option.type)"
            >
              {{ option.buttonText }}
            </v-btn>
          </v-card>
        </v-col>
      </v-row>

      <p class="text-center text-medium-emphasis mt-6">
        Prefer email?
        <a href="mailto:support@boardwise.co.za">
          support@boardwise.co.za
        </a>
      </p>
    </v-container>

    <v-dialog v-model="showForm" max-width="720">
      <v-card rounded="xl">
        <v-card-title class="d-flex align-center pa-6 pb-2">
          <v-icon
            :icon="currentRequest.icon"
            :color="currentRequest.color"
            class="mr-3"
          />

          <span>{{ currentRequest.title }}</span>

          <v-spacer />

          <v-btn
            icon="mdi-close"
            variant="text"
            aria-label="Close support form"
            @click="closeForm"
          />
        </v-card-title>

        <v-card-text class="pa-6">
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
              An confirmation email will be sent to you shortly.
            </v-alert>

            <div class="d-flex flex-wrap justify-end ga-3">
              <v-btn variant="text" @click="closeForm">
                Cancel
              </v-btn>

              <v-btn
                type="submit"
                color="primary"
                :loading="isSubmitting"
              >
                Submit request
              </v-btn>
            </div>
          </v-form>
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showSuccess" max-width="500">
      <v-card class="pa-7 text-center" rounded="xl">
        <v-icon
          icon="mdi-check-circle"
          color="success"
          size="64"
          class="mb-4"
        />

        <h2>Request received</h2>

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
          A real confirmation email will be added when the backend email
          service is connected.
        </p>

        <v-btn color="primary" block @click="showSuccess = false">
          Done
        </v-btn>
      </v-card>
    </v-dialog>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'

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
    ]
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
    ]
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
    ]
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

const emailRules = [
  value => Boolean(value?.trim()) || 'Email address is required',
  value =>
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) ||
    'Enter a valid email address'
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

  const randomNumber = Math.floor(1000 + Math.random() * 9000)

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