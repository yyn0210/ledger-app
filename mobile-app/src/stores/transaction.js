import { defineStore } from 'pinia';

export const useTransactionStore = defineStore('transaction', {
	state: () => ({
		transactions: [],
		filter: {
			type: 'all',
			category: '',
			dateRange: null
		}
	}),
	getters: {
		filteredTransactions: (state) => {
			let result = state.transactions;
			if (state.filter.type !== 'all') {
				result = result.filter(t => t.type === state.filter.type);
			}
			if (state.filter.category) {
				result = result.filter(t => t.category === state.filter.category);
			}
			return result;
		},
		totalIncome: (state) => {
			return state.transactions
				.filter(t => t.type === 'income')
				.reduce((sum, t) => sum + t.amount, 0);
		},
		totalExpense: (state) => {
			return state.transactions
				.filter(t => t.type === 'expense')
				.reduce((sum, t) => sum + t.amount, 0);
		}
	},
	actions: {
		addTransaction(transaction) {
			this.transactions.unshift({
				id: Date.now(),
				...transaction,
				createdAt: new Date().toISOString()
			});
		},
		removeTransaction(id) {
			this.transactions = this.transactions.filter(t => t.id !== id);
		},
		setFilter(filter) {
			this.filter = { ...this.filter, ...filter };
		},
		clearFilter() {
			this.filter = { type: 'all', category: '', dateRange: null };
		}
	}
});
