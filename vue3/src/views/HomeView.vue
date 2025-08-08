<template>
  <!--上传区域-->
  <div class="upload-section">
    <el-upload
        drag
        class="custom-upload"
        action="http://localhost:8080/api/files/upload"
        :before-upload="beforeUpload"
        :on-change="handleFileChange"
        :on-success="successUpload"
        :on-progress="handleProgress"
        :show-file-list="fileList.length > 0"
        :file-list="fileList"
        accept="video/*">
      <el-icon>
        <Upload/>
      </el-icon>
      <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      <div class="el-upload__tip">支持 mp4/webm/avi，文件不超过1GB</div>
    </el-upload>

    <el-select
        v-model="selectedClasses"
        multiple
        filterable
        placeholder="查找"
        class="class-selector">
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
  </div>

  <!--视频播放区域-->
  <div class="video-section">
    <div v-show="LocalVideoURL" class="video-container">
      <h3>原视频</h3>
      <video :src="LocalVideoURL" controls class="video-player"/>
      <el-button
          :style="{ visibility: form.video_guid && selectedClasses.length > 0 ? 'visible' : 'hidden' }"
          type="primary"
          class="button"
          @click="submitVideo">
        <span>提交</span>
      </el-button>
    </div>

    <div v-show="processed" class="video-container">
      <h3>搜索后</h3>
      <video
          id="resultVideo"
          controls
          type="video/mp4"
          class="video-player"
          preload="metadata">
        does not support the video tag
      </video>
      <el-button class="button" type="success" @click="showStats">
        <el-icon>
          <DataAnalysis/>
        </el-icon>
        <span>分析统计</span>
      </el-button>
    </div>
  </div>

</template>

<script setup lang="ts">
import {reactive, ref} from 'vue'
import {Upload, DataAnalysis} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import request from "@/utils/request.ts";

const CLASS_NAMES = ref<string[]>(["person", "car", "bicycle", "motorcycle", "airplane", "bus", "train",
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

const LocalVideoURL = ref<string | undefined>(undefined);//要求 LocalVideoURL 在无视频时为假值
// const LocalVideoURL = ref<string>('');//在布尔上下文中，undefined 和空字符串都会被视为 false，也可以
const processed = ref<boolean>(false);

function beforeUpload(file: File) {
  const maxSize = 1024 * 1024 * 1024; // 1GB
  if (file.size > maxSize) {
    ElMessage.error('上传文件不能超过 1GB');
    return false; // 阻止上传
  }
  return true; // 允许上传
}

// 选中文件后展示缩略图
function handleFileChange(fileObj: any) {//函数会在用户选择文件后被调用，接收一个 fileObj 参数
  const file = fileObj.raw//从文件对象中获取原始文件
  if (!file) return
  //为文件创建一个临时 URL，指向本地文件的临时链接，可以用来预览文件内容
  LocalVideoURL.value = URL.createObjectURL(file)
}

interface Response<T> {
  code: number;
  message: string;
  data: T;
}

const fileList = ref<any[]>([]);

function handleProgress(_event: any, file: any) {
  fileList.value = [file];  // 在上传过程中显示文件
}

function successUpload(res: Response<string>) {
  form.video_guid = res.data;
  console.log(res);
  fileList.value = [];  // 上传完成后清空文件列表，进度条消失
}

const submitVideo = async () => {
  if (!form.video_guid) {
    ElMessage.error("视频未上传或 GUID 不存在");
    return;
  }
  try {
    ElMessage.success("正在提交…");//！即使反馈
    // const res = await request.post('/files/commit/' + form.video_guid)
    const res = await request.post('/files/commit', {flag: form.video_guid, selectedClasses: selectedClasses.value});
    if (res.code === 200) {
      ElMessage.success("传输完成，开始处理:" + res.data);
      checkVideoReady(form.video_guid + '_finished');
    } else {
      ElMessage.error('提交失败，res.code: ' + res.code + ', res.message: ' + res.message);
    }
  } catch (e: any) {
    ElMessage.error(`提交失败：${e || '未知错误'}`);
  }

}

let pollInterval: ReturnType<typeof setInterval> | null = null;

function checkVideoReady(videoGuid: string) {
  //让浏览器自己加载视频文件
  //pollInterval 变量存储定时器的 ID，以便之后可以通过 clearInterval() 停止轮询
  pollInterval = setInterval(async () => {
    try {
      const video = document.getElementById('resultVideo') as HTMLVideoElement | null;
      if (video) {
        video.src = `http://localhost:8080/api/files/${videoGuid}?ts=${Date.now()}`;

        // 检查视频是否真正可播放，监听 loadeddata 事件以确保视频确实已加载
        video.onloadeddata = () => {
          if (pollInterval) {
            processed.value = true;//记录下处理完毕
            clearInterval(pollInterval);//停止轮询
            pollInterval = null;
          }
          video.style.display = 'block';// 显示视频
        };

        video.onerror = () => {
          console.log('暂无视频，继续轮询...');
        };

        video.load();
      }
    } catch (err) {
      console.log('轮询错误：' + err);
    }
  }, 2000); // 每2秒轮询一次
}

// 显示信息
const showStats = () => {
  if (!form.video_guid) {
    ElMessage.warning('先上传视频');
    return;
  }

};

</script>

<style scoped>

.upload-section {
  display: flex;
  max-width: 800px;
  gap: 20px;
  margin-bottom: 30px;
  padding: 20px;
  border-radius: 8px;
  background-color: #f5f7fa;
}

.custom-upload :deep(.el-upload-dragger) {
  width: 260px;
  height: 100px;
  padding: 10px;
  border-radius: 10px;
}

.class-selector {
  width: 300px;
}

.video-section {
  display: flex;
  align-items: flex-start;
  gap: 40px;
  padding: 20px;
  border-radius: 8px;
  background-color: #f5f7fa;
}

.video-container {
  background: white;
  padding: 15px;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.video-player {
  height: 350px;
  display: block;
  border-radius: 4px;
}

.button {
  margin: 10px 0;
  width: 100%;
}
</style>

