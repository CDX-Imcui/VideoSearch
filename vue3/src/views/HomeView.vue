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
        <!--        上传-->
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
        <!--        取消src，避免一上来就去访问后端，等提交成功任务-->
        <!--        <video id="resultVideo" controls :src="'http://localhost:8080/api/files/'+ form.video_guid+ '_finished'"-->
        <video id="resultVideo" controls
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
import {reactive, ref} from 'vue'
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

const LocalVideoURL = ref<string | undefined>(undefined);//要求 LocalVideoURL 在无视频时为假值
// const LocalVideoURL = ref<string>('');//在布尔上下文中，undefined 和空字符串都会被视为 false，也可以
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

function successUpload(res: Response<string>) {
  form.video_guid = res.data;
  console.log(res);
}

const submitVideo = async () => {
  if (!form.video_guid) {
    ElMessage.error("视频未上传或 GUID 不存在");
    return;
  }
  try {
    ElMessage.success("正在提交…");//！即使反馈
    const res = await request.post('/files/commit/' + form.video_guid)
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
            clearInterval(pollInterval);//停止轮询
            pollInterval = null;
          }
          video.style.display = 'block';// 显示视频
        };

        video.onerror = () => {
          console.log('视频加载失败，继续轮询...');
        };

        video.load();
      }
    } catch (err) {
      // 请求失败说明文件还没准备好，不处理
      console.log('等待视频生成中…' + err);
    }
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
