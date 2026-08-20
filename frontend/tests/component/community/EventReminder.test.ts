import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import EventReminder from '~/components/features/community/EventReminder.vue'

describe('EventReminder', () => {
  const event = {
    id: '1',
    name: 'Catan Night',
    date: '2026-08-25',
    time: '18:00',
    location: 'Pretoria',
    rsvped: false
  }

  const mountComponent = () => {
    return mount(EventReminder, {
      props: {
        event
      },
      global: {
        stubs: {
          BaseCard: {
            template: '<div><slot /></div>'
          },
          BaseButton: {
            template: `
              <button>
                <slot />
              </button>
            `
          }
        }
      }
    })
  }

  it('renders the event name', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Catan Night')
  })

  it('renders the date and time', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('2026-08-25')
    expect(wrapper.text()).toContain('18:00')
  })

  it('renders the location', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Pretoria')
  })

  it('shows RSVP when the user has not RSVP’d', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('RSVP')
  })

  it('shows Going when the user has RSVP’d', () => {
    const wrapper = mount(EventReminder, {
      props: {
        event: {
          ...event,
          rsvped: true
        }
      },
      global: {
        stubs: {
          BaseCard: {
            template: '<div><slot /></div>'
          },
          BaseButton: {
            template: '<button><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('Going')
  })

  it('emits toggle-rsvp with the event id', async () => {
    const wrapper = mountComponent()

    await wrapper.find('button').trigger('click')

    expect(wrapper.emitted('toggle-rsvp')).toEqual([
      ['1']
    ])
  })
})