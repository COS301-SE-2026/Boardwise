import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import CommunityEvents from '~/components/features/community/CommunityEvents.vue'

describe('CommunityEvents', () => {
  const community = {
    id: 1,
    isMember: true
  }

  const mountComponent = () => {
    return mount(CommunityEvents, {
      props: {
        community,
        modelValue: true
      },
      global: {
        stubs: {
          EventReminder: {
            props: ['event'],
            template: `
              <div class="event">
                {{ event.name }}
              </div>
            `
          },
          CreateEventModal: {
            template: '<div></div>'
          },
          BaseEmptyState: {
            props: ['title'],
            template: '<div>{{ title }}</div>'
          },
          BaseButton: {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })
  }

  it('renders the community events area', () => {
    const wrapper = mountComponent()

    expect(wrapper.exists()).toBe(true)
  })

  it('shows Create Event button for community members', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Create Event')
  })

  it('shows empty state when there are no events', () => {
    const wrapper = mount(CommunityEvents, {
      props: {
        community: {
          id: 'community-with-no-events',
          isMember: true
        },
        modelValue: true
      },
      global: {
        stubs: {
          BaseEmptyState: {
            props: ['title'],
            template: '<div>{{ title }}</div>'
          },
          CreateEventModal: {
            template: '<div />'
          },
          BaseButton: {
            template: '<button><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('No upcoming events')
  })
})