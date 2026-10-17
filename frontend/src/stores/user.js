import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

    const isLoggedIn = computed(() => !!token.value)

    const setLogin = (t, u) => {
        token.value = t
        user.value = u
        localStorage.setItem('token', t)
        localStorage.setItem('user', JSON.stringify(u))
    }

    const setUser = (u) => {
        user.value = u
        localStorage.setItem('user', JSON.stringify(u))
    }

    const logout = () => {
        token.value = ''
        user.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('user')
    }

    return { token, user, isLoggedIn, setLogin, setUser, logout }
})