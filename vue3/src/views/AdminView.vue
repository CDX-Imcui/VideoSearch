<template>
  <div><!--对于输入框，需要有一个div包含全部，两个根级div并列会报错-->
    <div style="margin-bottom: 15px">
      <el-input v-model="params.id" placeholder="输入id" clearable style="width: 200px"
                @change="searchAdmin"></el-input>
      <el-input v-model="params.name" placeholder="输入姓名" clearable style="width: 200px"
                @change="searchAdmin"></el-input>
      <!--      @foucs当点击时就开始调用函数，@change是当输入数据后，状态改变调用函数-->
      <el-select v-model="params.major" placeholder="专业" style="width: 100px" @focus="fetchMajors"
                 @change="searchAdmin">
        <el-option
            v-for="major in majors"
            :key="major"
            :label="major"
            :value="major"
        ></el-option>
      </el-select>
      <el-select v-model="params.classname" placeholder="班级" style="width: 100px" @focus="fetchClassnameList"
                 @change="searchAdmin">
        <el-option
            v-for="classname in classnameList"
            :key="classname"
            :label="classname"
            :value="classname"
        ></el-option>
      </el-select>
      <el-button type="primary" style="margin-left: 10px" @click="searchAdmin()">查询</el-button>
      <el-button type="primary" style="margin-left: 10px" @click="reset()">清空</el-button>
      <el-button type="success" style="margin-left: 10px" @click="add()" v-if="user.role==='role_admin'">新增
      </el-button>

    </div>
    <!--展示表格-->
    <div>
      <el-table
          :data="tableData"
          height="510"
          style="width: 100%">
        <el-table-column prop="id" label="ID" width="60px"></el-table-column>
        <el-table-column prop="username" label="姓名" width="90px"></el-table-column>
        <el-table-column prop="password" label="密码" v-if="user.role==='role_admin'"></el-table-column>
        <el-table-column prop="major" label="专业"></el-table-column>
        <el-table-column prop="classname" label="班级"></el-table-column>
        <el-table-column prop="phone" label="电话" width="100px"></el-table-column>
        <el-table-column prop="email" label="邮箱" width="200px"></el-table-column>
        <!--        formatter 属性允许在表格列中自定义数据的显示格式-->
        <el-table-column prop="lastLoginTime" label="上次登录" :formatter="formatDate" width="140"></el-table-column>
        <el-table-column prop="role" label="角色" :formatter="formatRole"></el-table-column>

        <el-table-column label="操作" v-if="user.role==='role_admin'" width="160">
          <template v-slot="scope"> <!--slot-scope="scope" 使得scope.row可以访问一行的数据-->
            <el-button type="primary" @click="edit(scope.row)">修改</el-button>
            <el-popconfirm title="确认删除吗" @confirm="deleteAdmin(scope.row)">
              <template #reference>
                <el-button type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!--分页布局-->
    <div style="margin-top: 10px">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="params.pageNum"
          :page-sizes="[10, 20, 30, 40]"
          :page-size="params.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
      </el-pagination>
    </div>


    <!--对话框-->
    <Dialog title="新增用户信息" :dialog-visible="addDialogFormVisible" @close="addDialogFormVisible = false"
            :submit="addAdmin">
      <el-form :model="form">
        <!--        <el-form-item label="Id" label-width="10%">-->
        <!--          <el-input v-model="form.id" autocomplete="off" style="width: 50%"></el-input>-->
        <!--        </el-form-item>-->
        <el-form-item label="姓名" label-width="10%">
          <el-input v-model="form.username" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="密码" label-width="10%">
          <el-input v-model="form.password" autocomplete="off" style="width:90%"></el-input>
        </el-form-item>

        <el-form-item label="专业" label-width="10%">
          <el-input v-model="form.major" autocomplete="off" style="width:90%"></el-input>
        </el-form-item>
        <el-form-item label="班级" label-width="10%">
          <el-input v-model="form.classname" autocomplete="off" style="width:90%"></el-input>
        </el-form-item>
        <el-form-item label="电话" label-width="10%">
          <el-input v-model="form.phone" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="邮件" label-width="10%">
          <el-input v-model="form.email" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="身份" label-width="10%">
          <el-select v-model="form.role" placeholder="请选择">
            <el-option label="教师" value="role_teacher"></el-option>
            <el-option label="学生" value="role_student"></el-option>
            <el-option label="管理员" value="role_admin"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </Dialog>


    <Dialog title="编辑用户信息" :dialog-visible="editDialogVisible" @close="editDialogVisible = false"
            :submit="editAdmin">
      <el-form :model="form">
        <!--        <el-form-item label="Id" label-width="15%">-->
        <!--          <el-input v-model="form.id" autocomplete="off" style="width: 90%"></el-input>-->
        <!--        </el-form-item>-->
        <el-form-item label="姓名" label-width="15%">
          <el-input v-model="form.username" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="密码" label-width="15%">
          <el-input v-model="form.password" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="专业" label-width="15%">
          <el-select v-model="form.major" placeholder="专业" @focus="fetchMajors">
            <el-option
                v-for="major in majors"
                :key="major"
                :label="major"
                :value="major"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="班级" label-width="15%">
          <el-select v-model="form.classname" placeholder="班级" @focus="fetchClassnameList">
            <el-option
                v-for="classname in classnameList"
                :key="classname"
                :label="classname"
                :value="classname"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="电话" label-width="15%">
          <el-input v-model="form.phone" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="邮件" label-width="15%">
          <el-input v-model="form.email" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="身份" label-width="15%">
          <el-select v-model="form.role" placeholder="请选择">
            <el-option label="教师" value="role_teacher"></el-option>
            <el-option label="学生" value="role_student"></el-option>
            <el-option label="管理员" value="role_admin"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </Dialog>

  </div>
</template>

<script setup lang="ts">
import {ref, reactive, onMounted} from 'vue'
import request from '@/utils/request'
import Dialog from '@/components/Dialog.vue'
import {ElMessage} from "element-plus";

const params: {
  id: number | null
  name: string
  phone: string
  pageNum: number
  pageSize: number
  major?: string
  classname?: string
} = reactive({
  id: null,
  name: '',
  phone: '',
  pageNum: 1,
  pageSize: 10
})

const total = ref<number>(0)
const tableData = ref<Array<Record<string, any>>>([])

const addDialogFormVisible = ref(false)
const editDialogVisible = ref(false)

interface Form {
  id?: number | null
  username?: string
  password?: string
  major?: string
  classname?: string
  phone?: string
  email?: string
  role?: string

  [key: string]: any
}

const form = reactive<Form>({})

interface User {
  id?: number
  role?: string

  [key: string]: any
}

const user = ref<User>(JSON.parse(localStorage.getItem('user') ?? '{}'))

const majors = ref<string[]>([])
const classnameList = ref<string[]>([])

onMounted(() => {
  searchAdmin()
})

const fetchMajors = async () => {
  try {
    const res = await request.get('/admin/majors')
    if (res.code === 200) {
      majors.value = res.data
    } else {
      ElMessage.error('无法获取专业列表')
    }
  } catch (e) {
    ElMessage.error('无法获取专业列表')
  }
}

const fetchClassnameList = async () => {
  try {
    const res = await request.get('/admin/classnameList')
    if (res.code === 200) {
      classnameList.value = res.data
    } else {
      ElMessage.error('无法获取班级列表')
    }
  } catch (e) {
    ElMessage.error('无法获取班级列表')
  }
}

const formatDate = (_row: any, _column: any, cellValue: string) => {
  return cellValue ? cellValue.replace('T', ' ') : ''
}

const formatRole = (_row: any, _column: any, cellValue: string) => {
  if (cellValue === 'role_admin') return '管理员'
  if (cellValue === 'role_teacher') return '教师'
  return '学生'
}

const searchAdmin = async () => {
  try {
    const res = await request.get('/admin/searchAdmin', {params})
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('查询失败')
  }
}
const reset = () => {
  params.id = null
  params.name = ''
  params.phone = ''
  params.pageNum = 1
  params.pageSize = 10
  searchAdmin()
}
const handleSizeChange = (val: number) => {
  params.pageSize = val
  searchAdmin()
}
const handleCurrentChange = (val: number) => {
  params.pageNum = val
  searchAdmin()
}

const add = () => {
  Object.assign(form, {}) // 清空表单
  addDialogFormVisible.value = true
}
const addAdmin = async () => {
  try {
    const {id, ...rest} = form // !去掉id字段,id字段在新增时不需要传递
    const res = await request.post('/admin/addAdmin', rest)
    // alert(JSON.stringify(form, null, 2))
    if (res.code === 200) {
      ElMessage.success('新增成功')
      addDialogFormVisible.value = false
      searchAdmin()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('新增失败')
  }
}

const edit = (scope_row: Form) => {
  Object.assign(form, scope_row)
  editDialogVisible.value = true
}

const editAdmin = async () => {
  try {
    const res = await request.post('/admin/updateAdmin', form)
    if (res.code === 200) {
      ElMessage.success('修改成功')
      editDialogVisible.value = false
      searchAdmin()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('修改失败')
  }
}

const deleteAdmin = async (admin: Form) => {
  try {
    const res = await request.delete(`/admin/deleteAdmin/${admin.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      searchAdmin()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('删除失败，请检查网络连接')
  }
}


</script>

