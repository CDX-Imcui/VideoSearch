<template>
  <el-select
      v-model="selectedClasses"
      multiple
      filterable
      placeholder="查找"
      style="width: 300px">
    <el-option
        v-for="item in CLASS_NAMES"
        :key="item"
        :label="item"
        :value="item">
      <el-icon style="margin-right: 6px;">
        <i :class="selectedClasses.includes(item) ? 'el-icon-check' : 'el-icon-circle-check-outline'"/>
      </el-icon>
      {{ item }}
    </el-option>
  </el-select>


  <div class="video-upload-container">
    <div class="upload-section" v-show="!LocalVideoURL">
      <el-upload
          class="upload-demo"
          drag
          action="http://localhost:8080/api/files/upload"
          :before-upload="beforeUpload"
          :on-change="handleFileChange"
          :on-success="successUpload"
          accept="video/*"
      >
        <el-icon>
          <Upload/>
        </el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip">支持 mp4/webm/avi，文件不超过1GB</div>
      </el-upload>
    </div>

    <div style="display:flex; align-items: flex-start; gap: 40px;">
      <div v-show="LocalVideoURL">
        <h3>原视频</h3>
        <video :src="LocalVideoURL" controls style="height: 350px;display: block; "/>
      </div>
      <div v-show="processed">
        <h3>搜索后</h3>
        <video id="resultVideo" controls :src="'http://localhost:8080/api/files/'+ form.video_guid+ '_finished'"
               type="video/mp4" style="height: 350px;display: block; "
               preload="metadata">does not support the video tag
        </video>
      </div>
    </div>

  </div>
  <el-button v-show="form.video_guid"
             type="primary"
             style="margin-left: 20px; align-items: center"
             @click="submitVideo">
    <span>提交</span>
  </el-button>
</template>

<script setup lang="ts">
import {ref, reactive} from 'vue'
import {ElMessage} from "element-plus";
import {Upload} from "@element-plus/icons-vue";
import request from "@/utils/request.ts";

const CLASS_NAMES = ref<string[]>(["person", "bicycle", "car", "motorcycle", "airplane", "bus", "train",
  "truck", "boat", "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog",
  "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie",
  "suitcase", "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard",
  "surfboard", "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
  "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "couch", "potted plant",
  "bed", "dining table", "toilet", "tv", "laptop", "mouse", "remote", "keyboard", "cell phone", "microwave", "oven",
  "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"]);
// 当前已选项
const selectedClasses = ref<string[]>([]);

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
  status?: 'pending' | 'processing' | 'done' | 'failed';
  remarks?: string;

  [key: string]: any;
}

const form = reactive<Partial<Video>>({});
const user = ref(
    localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user") as string) : {}
);

const LocalVideoURL = ref<string | undefined>(undefined);
const processed = ref<boolean>(true);

function beforeUpload(file: File) {
  const maxSize = 1024 * 1024 * 1024; // 1GB
  if (file.size > maxSize) {
    ElMessage.error('上传文件不能超过 1GB');
    return false; // 阻止上传
  }
  return true; // 允许上传
}

// 选中文件后展示缩略图
function handleFileChange(fileObj: any) {
  const file = fileObj.raw
  if (!file) return
  const url = URL.createObjectURL(file)
  LocalVideoURL.value = url
}

interface Response<T> {
  code: number;
  message: string;
  data: T;
}

function successUpload(res: Response<string>) {
  form.video_guid = res.data;
  console.log(res);
  checkVideoReady(form.video_guid);
}

const submitVideo = async () => {
  if (!form.video_guid) {
    ElMessage.error("视频未上传或 GUID 不存在");
    return;
  }
  try {
    const res = await request.post('/files/commit/' + form.video_guid)
    if (res.code === 200) {
      // ElMessage.success("提交成功" + res.data);
      ElMessage.success("提交成功");
    } else {
      ElMessage.error('提交失败，res.code: ' + res.code + ', res.message: ' + res.message);
    }
  } catch (e: any) {
    ElMessage.error(`提交失败：${e || '未知错误'}`);
  }
}

let pollInterval: ReturnType<typeof setInterval> | null = null;

function checkVideoReady(videoGuid: string) {
  pollInterval = setInterval(() => {
    fetch(`http://localhost:8080/api/files/${videoGuid}_finished`, { method: 'HEAD' })
        .then(res => {
          if (res.ok) {
            if (pollInterval) clearInterval(pollInterval); // 停止轮询
            const video = document.getElementById('resultVideo') as HTMLVideoElement;
            // video.src = `http://localhost:8080/api/files/${videoGuid}_finished?ts=${Date.now()}`; // 加时间戳防止缓存
            video.src = `http://localhost:8080/api/files/${videoGuid}_finished`;
            video.load(); // 重新加载视频
            video.style.display = 'block';
          }
        })
        .catch(err => {
          console.log('Video not ready yet:'+err);
          // 文件不存在时会进这里，不处理即可
        });
  }, 2000); // 每2秒轮询一次
}


</script>

<style scoped>
.video-upload-container {
  padding: 20px;
  max-width: 800px;
}

.upload-section {
  margin-bottom: 20px;
}

.video-player {
  width: 100%;
  max-height: 400px;
  border-radius: 8px;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.1);
}

.empty-box {
  height: 200px;
  background: #f5f5f5;
  border: 2px dashed #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888;
  font-size: 16px;
}
</style>
