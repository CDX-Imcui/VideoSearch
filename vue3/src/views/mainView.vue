<template>
  <div style="height: 100vh;">
    <el-container>
      <el-header style="background-color: #0066e6;height: 52px">
        <el-dropdown style="float: right;line-height: 60px;">
          <span class="user-dropdown-link" style="color: white;font-size: 18px;outline: none;">
            <el-icon><User/></el-icon>
            <span class="username">{{ user.username }}</span>
            <el-icon><arrow-down/></el-icon>
          </span>
          <template v-slot:dropdown>
            <el-dropdown-menu class="user-dropdown-menu">
              <el-dropdown-item>
                <span class="item-label">ID</span><span class="item-value">{{ user.id }}</span>
              </el-dropdown-item>
              <el-dropdown-item>
                <span class="item-label">用户名</span><span class="item-value">{{ user.username }}</span>
                <!--                username<span style="margin-left: 31px">{{ user.username }}</span>-->
              </el-dropdown-item>
              <el-dropdown-item>
                <span class="item-label">电话</span><span class="item-value">{{ user.phone }}</span>
                <!--                phone<span style="margin-left: 52px">{{ user.phone }}</span>-->
              </el-dropdown-item>
              <el-dropdown-item>
                <span class="item-label">邮箱</span><span class="item-value">{{ user.email }}</span>
                <!--                email<span style="margin-left: 60px">{{ user.email }}</span>-->
              </el-dropdown-item>
              <el-dropdown-item disabled>
                <span class="item-label">最近登录</span><span class="item-value">{{ user.lastLoginTime }}</span>
                <!--                lastLoginTime<span style="margin-left: 7px">{{ user.lastLoginTime }}</span>-->
              </el-dropdown-item>
              <el-dropdown-item>
                <span class="item-label">身份</span>
                <span class="item-value">
          {{ user.role?.startsWith('role_') ? user.role.slice(5) : user.role }}
        </span>
<!--                身份<span style="margin-left: 65px">-->
                <!--                  {{ user.role?.startsWith('role_') ? user.role.slice(5) : user.role }}-->
                <!--                </span>-->
              </el-dropdown-item>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
    </el-container>

    <el-container style="flex: 1;display: flex;">
      <el-aside style="overflow:hidden;display: flex;height:100%;width: 150px;">
        <el-menu
            :default-active="route.path"
            router
            text-color="#545c64"
            active-text-color="#ffd04b"
            style="border-right: none !important;"
        >
          <el-menu-item class="el_menu_item" index="/">
            <el-icon>
              <House/>
            </el-icon>
            <span>首页</span>
          </el-menu-item>

          <el-menu-item class="el_menu_item" index="/admin" v-if="user.role !== 'role_student'">
            <el-icon>
              <UserFilled/>
            </el-icon>
            <span>用户管理</span>
          </el-menu-item>


          <el-menu-item class="el_menu_item" index="/book">
            <el-icon>
              <List/>
            </el-icon>
            <span>历史任务</span>
          </el-menu-item>
          <!--          <el-sub-menu index="3">-->
          <!--            <template v-slot:title>-->
          <!--              <el-icon><List /></el-icon>-->
          <!--              <span>信息管理</span>-->
          <!--            </template>-->
          <!--            <el-menu-item-group>-->
          <!--              <el-menu-item index="/book">历史任务</el-menu-item>-->
          <!--            </el-menu-item-group>-->
          <!--          </el-sub-menu>-->
        </el-menu>
      </el-aside>

      <el-main>
        <router-view/>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import {ElIcon} from 'element-plus'
import {User, ArrowDown} from '@element-plus/icons-vue'
import {reactive} from 'vue'
import {useRouter, useRoute} from 'vue-router'

const router = useRouter()
const route = useRoute()

interface User {
  id: number;
  username: string;
  phone: string;
  email: string;
  lastLoginTime: string;
  role: string;
  major?: string;
  classname?: string;
  token?: string;
}
const user = reactive<User>(JSON.parse(localStorage.getItem("user") || '{}'))

function logout() {
  localStorage.removeItem("user")
  router.push("/login")
}
</script>

<style scoped>
.user-dropdown-link {
  color: white;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  transition: background-color 0.3s;
}

.user-dropdown-link:hover {
  background-color: rgba(255, 255, 255, 0.15);
  border-radius: 6px;
}

.user-dropdown-menu {
  min-width: 220px;
  padding: 4px 0;
}

.user-dropdown-menu .el-dropdown-item {
  display: flex;
  justify-content: space-between;
  padding: 0 16px;
  font-size: 14px;
}

.user-dropdown-menu .item-label {
  color: #303133;
  width: 35px;
  flex-shrink: 0;
}

.user-dropdown-menu .item-value {
  color: #606266;
  font-weight: 500;
  margin-left: 30px;
}


.el_menu_item {
  height: 36px !important;
  line-height: 36px !important;
  margin-bottom: 10px;
  padding-top: 20px !important;
  padding-bottom: 20px !important;
}

.el_menu_item:hover {
  background-color: #409EFF !important;
  color: white !important;
}

.el_menu_item .el-icon {
  font-size: 18px;
  vertical-align: middle;
}
</style>

