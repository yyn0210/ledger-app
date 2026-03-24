import { defineStore } from 'pinia';

export const useBookStore = defineStore('book', {
	state: () => ({
		currentBookId: 1,
		books: [
			{ id: 1, name: '日常账本', description: '记录日常生活开支', color: '#4F46E5', icon: 'book' }
		]
	}),
	getters: {
		currentBook: (state) => {
			return state.books.find(book => book.id === state.currentBookId);
		}
	},
	actions: {
		setCurrentBook(bookId) {
			this.currentBookId = bookId;
		},
		addBook(book) {
			this.books.push({
				id: Date.now(),
				...book
			});
		},
		removeBook(bookId) {
			this.books = this.books.filter(book => book.id !== bookId);
		}
	}
});
