import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'
import { useUserStore } from './user'

export const useCartStore = defineStore('cart', () => {
    const count = ref(0)

    const fetchCount = async () => {
        const userStore = useUserStore()
        if (!userStore.isLoggedIn) {
            count.value = 0
            return
        }
        try {
            const res = await request.get('/cart/count')
            count.value = res.data || 0
        } catch (e) {
            count.value = 0
        }
    }

    const reset = () => {
        count.value = 0
    }

    return { count, fetchCount, reset }
})