import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import LogOutButton from '~/components/features/auth/LogOutButton.vue'

const push = vi.fn()
const logOutMock = vi.fn().mockResolvedValue(true)

vi.mock('vue-router', () => ({
    useRouter: () => ({ push })
}))

vi.stubGlobal('useAuth', () => ({
    logout: logOutMock
}))

describe('LogOutButton.vue', () => {
    it('triggers logout composable and redirects home on click', async () => {
        const wrapper = mount(LogOutButton)

        await wrapper.find('[data-test="logout-button"]').trigger('click')

        expect(logOutMock).toHaveBeenCalled()
        expect(push).toHaveBeenCalledWith('/')
    })
})