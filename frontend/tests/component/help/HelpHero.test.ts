import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createVuetify } from 'vuetify'

import HelpHero from '~/components/features/help/HelpHero.vue'

const vuetify = createVuetify()

describe('HelpHero', () => {

  it('renders the page title', () => {

    const wrapper = mount(HelpHero, {
      global: {
        plugins: [ vuetify]
      }
    })

    expect(wrapper.text()).toContain('Help Centre')

  })

  it('renders the page description', () => {

    const wrapper = mount(HelpHero, {
      global: {
        plugins: [ vuetify]
      }
    })

    expect(wrapper.text()).toContain(
      'Find answers to common questions about Boardwise.'
    )

  })

})