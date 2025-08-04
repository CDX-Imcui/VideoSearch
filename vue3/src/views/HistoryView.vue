<template>
  <div><!--对于输入框，需要有一个div包含全部，两个根级div并列会报错-->
    <!--工具栏-->
    <div style="margin-bottom: 15px">
      <el-input v-model="params.id" placeholder="任务id" clearable style="width: 200px"></el-input>
      <el-input v-model="params.uploader_id" placeholder="使用者id" clearable style="width: 200px"></el-input>
      <el-input v-model="params.video_name" placeholder="视频文件名" clearable
                style="width: 200px;margin-left: 5px"></el-input>
      <el-button type="primary" style="margin-left: 10px" @click="search()">查询</el-button>
      <el-button type="primary" style="margin-left: 10px" @click="reset()">清空</el-button>
      <el-button type="success" style="margin-left: 10px" @click="add()" v-if="user.role!=='role_student'">新增
      </el-button>
    </div>

    <!--展示部分-->
    <div>
      <el-table
          :data="tableData"
          height="510"
          style="width: 100%">
        <el-table-column prop="img" label="video" width="200">
          <template v-slot="scope">
            <video width="200" controls :src="'http://localhost:8080/api/files/'+ scope.row.img"
                   type="video/mp4"
                   preload="metadata">does not support the video tag
            </video>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="60"></el-table-column>
        <el-table-column prop="uploader_id" label="uploader_id" style="width: 60px"></el-table-column>
        <el-table-column prop="upload_time" label="上传时间" width="150"></el-table-column>
        <el-table-column prop="video_name" label="视频名" width="100"></el-table-column>
        <el-table-column prop="file_size" label="大小"></el-table-column>
        <el-table-column prop="description_info" label="详情" width="150"></el-table-column>
        <el-table-column prop="finish_time" label="完成" width="150"></el-table-column>
        <el-table-column prop="status" label="状态" width="150"></el-table-column>
        <el-table-column prop="remarks" label="备注" width="150"></el-table-column>
        <el-table-column label="操作" width="240">
          <template v-slot="scope"> <!--slot-scope="scope" 使得scope.row可以访问一行的数据-->
            <el-button type="primary" @click="edit(scope.row)" v-if="user.role==='role_admin'">编辑</el-button>
            <el-button type="primary" style="margin-left: 2px" @click="down(scope.row.img)">下载图片</el-button>
            <el-popconfirm title="确认删除吗" @confirm="deleteBook(scope.row)">
              <template v-slot:reference>
                <el-button type="danger" style="margin-left: 2px" v-if="user.role==='role_admin'">删除</el-button>
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

    <Dialog title="新增图书信息" :dialog-visible="addDialogFormVisible" @close="addDialogFormVisible = false"
            :submit="addBook">
      <el-form :model="form">
        <el-form-item label="图书名称" label-width="20%">
          <el-input v-model="form.name" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="图书作者" label-width="20%">
          <el-input v-model="form.author" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="图书价格" label-width="20%">
          <el-input v-model="form.price" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="图书出版社" label-width="20%">
          <el-input v-model="form.press" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="图书封面" label-width="20%">
          <el-upload action="http://localhost:8080/api/files/upload"
                     :before-upload="beforeUpload"
                     :on-success="successUpload" accept="video/*">
            <el-button size="small" type="primary">上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="图书分类" label-width="20%">
          <el-select v-model="form.typeid" placeholder="请选择">
            <el-option
                v-for="item in types"
                :key="item.id"
                :label="item.name"
                :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </Dialog>

    <Dialog title="编辑图书" :dialog-visible="editDialogVisible" @close="editDialogVisible = false" :submit="editBook">
      <el-form :model="form">
        <el-form-item label="图书名称" label-width="20%">
          <el-input v-model="form.name" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="图书作者" label-width="20%">
          <el-input v-model="form.author" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="图书价格" label-width="20%">
          <el-input v-model="form.price" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="图书出版社" label-width="20%">
          <el-input v-model="form.press" autocomplete="off" style="width: 90%"></el-input>
        </el-form-item>
        <el-form-item label="图书封面" label-width="20%">
          <el-upload action="http://localhost:8080/api/files/upload"
                     :before-upload="beforeUpload"
                     :on-success="successUpload">
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="图书分类" label-width="20%">
          <el-select v-model="form.typeid" placeholder="请选择">
            <el-option
                v-for="item in types"
                :key="item.id"
                :label="item.name"
                :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </Dialog>

  </div>
</template>

<script setup lang="ts">
import {ref, reactive, onMounted} from "vue";
import {ElMessage} from "element-plus";
import request from "@/utils/request";
import Dialog from "@/components/Dialog.vue";


interface Video {
  id?: number;
  uploader_id?: string;
  upload_time?: string;
  video_name?: string;
  type?: string;
  video_guid?: string;
  file_size?: number;
  description_info?: string;
  finish_time?: string;
  status?:  'pending' | 'processing' | 'done' | 'failed';
  remarks?: string;
  [key: string]: any;
}

interface SearchParams {
  id: number | null;
  uploader_id: string;
  video_name: string;
  pageNum: number;
  pageSize: number;
}



const params = reactive<SearchParams>({
  id: null,
  uploader_id: '',
  video_name: '',
  pageNum: 1,
  pageSize: 10
});

const total = ref(20);
const tableData = ref<Video[]>([]);
const addDialogFormVisible = ref(false);
const editDialogVisible = ref(false);
const form = reactive<Partial<Video>>({});
const types = ref<Array<{ id: number; name: string; description: string }>>([]);
const user = ref(
    localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user") as string) : {}
);
form.uploader_id = user.value.id; // 设置上传者ID为当前用户ID

onMounted(() => {
  search();
  findTypes();
});

const findTypes = async () => {
  const res = await request.get("/type/findAll")
  try {
    if (res.code === 200) {
      types.value = res.data;
    } else {
      ElMessage.error(res.message);
    }
  } catch (error) {
    ElMessage.error("查询失败，请检查网络连接");
  }
}

const search = async () => {
  const res = await request.get("/book/search", {
    params: {...params}
  })
  try {
    if (res.code === 200) {
      tableData.value = res.data.list;
      total.value = res.data.total;
    } else {
      ElMessage.error(res.message);
    }
  } catch (error) {
    ElMessage.error("查询失败，请检查网络连接");
  }
}

function reset() {
  params.id = null;
  params.uploader_id = '';
  params.video_name = '';
  params.pageNum = 1;
  params.pageSize = 10;
  search();
}

function handleSizeChange(val: number) {
  params.pageSize = val;
  search();
}

function handleCurrentChange(val: number) {
  params.pageNum = val;
  search();
}

function add() {
  // Object.assign(form, {}); // 清空表单
  for (const key in form) {
    delete form[key];
  }
  addDialogFormVisible.value = true;
}

const addBook = async () => {
  const res = await request.post("/book/add", form)
  try {
    if (res.code === 200) {
      ElMessage.success("新增成功");
      addDialogFormVisible.value = false;
      search();
    } else {
      ElMessage.error(res.message);
    }
  } catch (error) {
    ElMessage.error("删除失败，请检查网络连接");
  }
}

function edit(book: Video) {
  Object.assign(form, book);
  editDialogVisible.value = true;
}

const editBook = async () => {
  const res = await request.post("/book/update", form)
  try {
    if (res.code === 200) {
      ElMessage.success("修改成功");
      editDialogVisible.value = false;
      search();
    } else {
      ElMessage.error(res.message);
    }
  } catch (error) {
    ElMessage.error("删除失败，请检查网络连接");
  }
}

const deleteBook = async (book: Video) => {
  const res = await request.delete(`/book/delete/${book.id}`)
  try {
    if (res.code === 200) {
      ElMessage.success("删除成功");
      search();
    } else {
      ElMessage.error(res.message);
    }
  } catch (error) {
    ElMessage.error("删除失败，请检查网络连接");
  }
}

function beforeUpload(file: File) {
  const maxSize = 1024 * 1024 * 1024; // 1GB
  if (file.size > maxSize) {
    ElMessage.error('上传文件不能超过 1GB');
    return false; // 阻止上传
  }
  return true; // 允许上传
}

interface Response<T> {
  code: number;
  message: string;
  data: T;
}
function successUpload(res: Response<string>) {
  form.video_guid = res.data;
  console.log(res);
}

function down(flag: string) {
  location.href = `http://localhost:8080/api/files/${flag}`;
}
</script>

