<script setup lang="ts">
defineProps<{
  title: string,
  dialogVisible: boolean,
  submit: () => void
}>()

const emit = defineEmits(['close'])
</script>

<template>
  <el-dialog :title="title" :model-value="dialogVisible" @close="emit('close')">

    <slot /> <!-- 表单内容插槽 -->
    <template #footer>
      <el-button @click="emit('close')">取消</el-button>
      <el-button type="primary" @click="submit">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">

</style>