import { defineStore } from 'pinia'

export const useMemberStore = defineStore('member', {
  state: () => ({
    user: null
  }),
  actions: {
    saveUser(member) {
      this.member = member;
    }
  }
});