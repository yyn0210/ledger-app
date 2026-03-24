import { defineStore } from 'pinia'
import { getBookList, getBookDetail } from '@/api/book'

export const useBookStore = defineStore('book', {
  state: () => ({
    bookList: [],
    currentBook: null,
    loading: false
  }),
  getters: {
    bookCount: state => state.bookList.length
  },
  actions: {
    async fetchBookList(params) {
      this.loading = true
      try {
        const data = await getBookList(params)
        this.bookList = data.list || data
      } catch (error) {
        console.error('获取账本列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    async fetchCurrentBook(id) {
      this.loading = true
      try {
        const data = await getBookDetail(id)
        this.currentBook = data
      } catch (error) {
        console.error('获取账本详情失败:', error)
      } finally {
        this.loading = false
      }
    }
  }
})
