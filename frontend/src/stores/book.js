import { defineStore } from 'pinia'
import { getBookList, getBookDetail } from '@/api/book'

export const useBookStore = defineStore('book', {
  state: () => ({
    bookList: [],
    currentBook: null,
<<<<<<< HEAD
=======
    currentBookId: localStorage.getItem('currentBookId') || null,
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
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
<<<<<<< HEAD
=======
        // 如果没有当前账本，设置第一个为当前账本
        if (!this.currentBookId && this.bookList.length > 0) {
          this.currentBookId = this.bookList[0].id
          localStorage.setItem('currentBookId', this.currentBookId)
        }
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
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
<<<<<<< HEAD
=======
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
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
    }
  }
})
