import { defineStore } from 'pinia'
import { getBookList, createBook, updateBook, deleteBook } from '@/api/book'

export const useBookStore = defineStore('book', {
  state: () => ({
    bookList: [],
    currentBookId: null,
    loading: false
  }),
  
  getters: {
    currentBook: (state) => {
      return state.bookList.find(book => book.id === state.currentBookId)
    },
    bookCount: (state) => state.bookList.length
  },
  
  actions: {
    // 获取账本列表
    async fetchBookList() {
      this.loading = true
      try {
        const res = await getBookList()
        this.bookList = res.data || []
        // 如果没有当前账本，设置第一个为当前账本
        if (!this.currentBookId && this.bookList.length > 0) {
          this.currentBookId = this.bookList[0].id
        }
        return res
      } catch (error) {
        return Promise.reject(error)
      } finally {
        this.loading = false
      }
    },
    
    // 创建账本
    async createBook(bookData) {
      try {
        const res = await createBook(bookData)
        await this.fetchBookList()
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    
    // 更新账本
    async updateBook(bookId, bookData) {
      try {
        const res = await updateBook(bookId, bookData)
        await this.fetchBookList()
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    
    // 删除账本
    async deleteBook(bookId) {
      try {
        await deleteBook(bookId)
        if (this.currentBookId === bookId) {
          this.currentBookId = null
        }
        await this.fetchBookList()
      } catch (error) {
        return Promise.reject(error)
      }
    },
    
    // 切换当前账本
    switchBook(bookId) {
      this.currentBookId = bookId
      uni.setStorageSync('currentBookId', bookId)
    }
  }
})
