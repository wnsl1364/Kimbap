// stores/memberStore.js
import { defineStore } from 'pinia'

export const useMemberStore = defineStore('member', {
  state: () => ({
    user: null
  }),
  actions: {
    saveUser(userData) {
      this.user = userData
    },
    logout() {
      this.user = null;
      localStorage.removeItem('accessToken');
    }
  },
  getters: {
    isLogin: (state) => !!state.user,
    role: (state) => state.user?.memType || ''  // 권한용 getter
  }
});