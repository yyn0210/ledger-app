<<<<<<< HEAD
<template>
  <n-config-provider :locale="zhCN" :date-locale="dateZhCN">
    <n-message-provider>
      <router-view />
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { zhCN, dateZhCN } from 'naive-ui'
</script>

<style>
body {
  margin: 0;
  padding: 0;
}
</style>
=======
<script setup>
import HelloWorld from './components/HelloWorld.vue'
</script>

<template>
  <HelloWorld />
</template>
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
