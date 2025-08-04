<template>
  <h1>{{ msg }}</h1>

  <div class="card">
    <button type="button" @click="add">pinia中的数据 count is {{ helloWorldStore.count }}</button>
  </div>

</template>

<script setup lang="ts">
import {onMounted} from "vue";

defineProps<{ msg: string }>()

import {useHelloWorldStore} from '../store/HelloWorld'

const helloWorldStore = useHelloWorldStore()

function add() {
  helloWorldStore.count++
}

onMounted(() => {


})


</script>

<style scoped>
.read-the-docs {
  color: #888;
}
</style>
