import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
	state: () => ({
		userInfo: {
			id: 1,
			username: '简洛用户',
			avatar: '',
			description: '精简生活，逻辑理财'
		},
		settings: {
			theme: 'light',
			currency: 'CNY',
			firstDayOfWeek: 1
		}
	}),
	getters: {
		isLoggedIn: (state) => !!state.userInfo.id
	},
	actions: {
		updateUserInfo(info) {
			this.userInfo = { ...this.userInfo, ...info };
		},
		updateSettings(settings) {
			this.settings = { ...this.settings, ...settings };
		}
	}
});
