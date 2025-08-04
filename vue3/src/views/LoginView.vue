<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="title">VideoSearch</h2>

      <el-form :model="admin" class="form">
        <el-form-item>
          <el-input v-model="admin.username" prefix-icon="User" placeholder="邮箱" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="admin.password" prefix-icon="Lock" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="login" round block>登录</el-button>
        </el-form-item>
      </el-form>

      <div class="footer-links">
        <span @click="toRegister" class="link">注册</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import request from "../utils/request"
import {reactive} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'

const router = useRouter()
const admin = reactive({
  username: '',
  password: ''
})

function login() {

  if (admin.username == null) {
    ElMessage.error('username不能为空');
    return;
  }

  if (admin.password == null || admin.password === '') {
    ElMessage.error('密码不能为空');
    return;
  }


  request.post('/admin/login', admin).then(res => {
    if (res.code == 200) {
      localStorage.setItem('user', JSON.stringify(res.data))//用户信息存储到localStorage，JSON.stringify()将对象转换为字符串
      router.push('/')
    } else {
      ElMessage.error(res.message)
    }
  })
}

function toRegister() {
  router.push('/register')
}
</script>
<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f2f4f8;
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 8px;
  padding: 30px 30px;
  height: 350px;
  width: 450px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.title {
  font-size: 26px;
  font-weight: bold;
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  background-color: #0066e6;
  color: white;
}

.footer-links {
  margin-top: 20px;
  font-size: 13px;
  color: #888;
  display: flex;

  justify-content: space-between;/* 第一个子元素靠左对齐，最后一个子元素靠右对齐，中间的子元素之间有相等的间距 */
  align-items: center;
}

.link {
  color: #409EFF;
  cursor: pointer;
}

</style>