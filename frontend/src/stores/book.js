import { defineStore } from 'pinia'
import { getBookList, getBookDetail } from '@/api/book'

export const useBookStore = defineStore('book', {
  state: () => ({
    bookList: [],
    currentBook: null,
    currentBookId: localStorage.getItem('currentBookId') || null,
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
        // 如果没有当前账本，设置第一个为当前账本
        if (!this.currentBookId && this.bookList.length > 0) {
          this.currentBookId = this.bookList[0].id
          localStorage.setItem('currentBookId', this.currentBookId)
        }
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
    },
    setCurrentBook(book) {
      this.currentBook = book
      this.currentBookId = book.id
      localStorage.setItem('currentBookId', book.id)
    },
    switchBook(bookId) {
      this.currentBookId = bookId
      localStorage.setItem('currentBookId', bookId)
      // 从列表中查找账本信息
      const book = this.bookList.find(b => b.id === bookId)
      if (book) {
        this.currentBook = book
      }
    }
  }
})
