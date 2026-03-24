<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('App Launch')
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
/*每个页面公共 css */
@import '@/uni_modules/uview-ui/index.scss';

page {
  background-color: #f5f6f7;
  font-size: 14px;
  line-height: 1.5;
}

/* 全局字体 */
view, text, button {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>
