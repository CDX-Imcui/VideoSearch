<template>
  <div class="register-container">
    <div class="card">
      <div class="title">VideoSearch</div>
      <el-form :model="admin" label-position="top">
        <el-form-item>
          <el-input v-model="admin.username" placeholder="用户名">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="admin.password" placeholder="密码">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="admin.phone" placeholder="电话">
            <template #prefix><el-icon><Phone /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="admin.email" placeholder="邮箱">
            <template #prefix><el-icon><Message /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="register-button" @click="register">注 册</el-button>
        </el-form-item>
        <div class="footer-links">
          <el-button link type="primary" @click="toLogin">返回登录</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>


<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, Message } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const admin = reactive({
  username: '',
  password: '',
  phone: '',
  email: ''
})


const register = async () => {
  if (!admin.username) {
    ElMessage.error('用户名不能为空')
    return
  }
  if (!admin.password) {
    ElMessage.error('密码不能为空')
    return
  }
  try {
    const res = await request.post('/admin/register', admin)
    if (res.code === 200) {
      router.push('/login')
    } else {
      ElMessage.error(res.message)
    }
  } catch (err) {
    ElMessage.error('注册失败，请检查网络或联系管理员')
  }
}


function toLogin() {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f2f4f8;
  padding: 20px;
}

.card {
  background: white;
  border-radius: 8px;
  padding: 30px 30px;
  height: 400px;
  width: 450px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.title {
  font-size: 26px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
}

.register-button {
  width: 100%;
  background-color: #0066e6;
  color: white;
  border-radius: 18px;
}

.footer-links {
  margin-top: 20px;
  font-size: 13px;
  color: #888;
  display: flex;

  justify-content: space-between;/* 第一个子元素靠左对齐，最后一个子元素靠右对齐，中间的子元素之间有相等的间距 */
  align-items: center;
}
</style>

