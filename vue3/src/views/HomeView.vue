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

    <!-- 元数据信息 -->
    <div v-if="form.video_guid">
      <span>时长: {{ formatTime(duration) }}</span><br>
      <span>大小: {{ form.file_size ? (form.file_size / (1024 * 1024)).toFixed(2) : '0.00' }} MB</span><br>
      <span>文件名: {{ form.video_name }}</span><br>
      <span>分辨率: {{ form.width }} × {{ form.height }}</span>
    </div>
  </div>

  <!--视频播放区域-->
  <div class="video-section" v-show="LocalVideoURL">
    <div class="video-container">
      <h3>{{ form.video_guid }}</h3>
      <video :src="LocalVideoURL"
             ref="videoRef"
             @loadedmetadata="onLoadedMetadata"
             controls/>
      <!--      动态生成一个高亮覆盖层，用于标记视频播放进度条上的特定时间段。为该容器设置一个引用highlightOverlay。-->
      <div v-if="duration > 0" ref="highlightOverlay" class="highlight-overlay">
        <div v-for="(range, index) in analysisRanges"
             :key="index"
             class="highlight-segment"
             :title="`${formatTime(range.start)} - ${formatTime(range.end)}`">
          <!--          每个高亮段的样式（如宽度和位置）会在 positionHighlightOverlay 方法中动态计算，并根据视频的总时长和时间段范围调整-->
          <!--        title 属性 鼠标悬停时显示时间段的起止时间，格式化为 mm:ss-->
        </div>
      </div>
      <div style="display: flex;width: 622px;">
        <el-select
            v-model="selectedClasses"
            multiple
            filterable
            placeholder="只看"
            style="width: 300px;margin: 10px 0;">
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
        <el-button type="primary" style="margin: 10px 0 10px 10px;flex: 1;" @click="submitVideo">
          <span>确定</span>
        </el-button>
      </div>
    </div>

    <div class="video-container">
      <h3>只看{{ selectedClasses.join('、')}}</h3>
      <video
          id="resultVideo"
          controls
          type="video/mp4"
          class="video-player"
          preload="metadata">
        does not support the video tag
      </video>
      <el-button style="margin: 10px 0;width: 622px;" type="success" @click="showAnalysis">
        <el-icon>
          <DataAnalysis/>
        </el-icon>
        <span>分析统计</span>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {nextTick, onBeforeUnmount, onMounted, reactive, ref} from 'vue'
import {DataAnalysis, Upload} from "@element-plus/icons-vue";
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
  width?: number;
  height?: number;

  [key: string]: any;
}

const form = reactive<Partial<Video>>({});

const LocalVideoURL = ref<string | undefined>(undefined);//要求 LocalVideoURL 在无视频时为假值
// const LocalVideoURL = ref<string>('');//在布尔上下文中，undefined 和空字符串都会被视为 false，也可以
const processed = ref<boolean>(false);

const videoRef = ref<HTMLVideoElement | null>(null);
const duration = ref(0);
const highlightOverlay = ref<HTMLElement | null>(null);

interface TimeRange {
  start: number;
  end: number;
}

let analysisRanges = ref<TimeRange[]>([]);

function beforeUpload(file: File) {
  const maxSize = 1024 * 1024 * 1024; // 1GB
  if (file.size > maxSize) {
    ElMessage.error('上传文件不能超过 1GB');
    return false; // 阻止上传
  }
  form.file_size = file.size; // 保存文件大小
  return true; // 允许上传
}

// 选中文件后展示缩略图
function handleFileChange(fileObj: any) {//选择文件后接收一个fileObj参数
  const file = fileObj.raw//从文件对象中获取原始文件
  if (!file) return
  form.video_name = file.name;
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
    processed.value = false; // 重置处理状态
    const res = await request.post('/files/commit', {flag: form.video_guid, selectedClasses: selectedClasses.value});
    if (res.code === 200) {
      ElMessage.success("传输完成，开始处理:" + res.data);
      checkVideoReady(form.video_guid);
    } else {
      ElMessage.error('提交失败，res.code: ' + res.code + ', res.message: ' + res.message);
    }
  } catch (e: any) {
    ElMessage.error(`提交失败：${e || '未知错误'}`);
  }

}

let pollInterval: ReturnType<typeof setInterval> | null = null;
let listenerBound = false;

function checkVideoReady(videoGuid: string) {
  const video = document.getElementById('resultVideo') as HTMLVideoElement | null;
  if (!video) return;

  if (!listenerBound) {// 只绑定一次监听器
    video.addEventListener('loadeddata', () => {
      if (pollInterval) {
        clearInterval(pollInterval);//  停止轮询
        pollInterval = null;
      }
      processed.value = true;
      video.style.display = 'block';
      console.log('视频加载成功，停止轮询。');
      getAnalysis(); // 视频加载成功后获取分析
    });

    video.addEventListener('error', () => {
      console.log('视频还没就绪，继续轮询...');
    });
    listenerBound = true;
  }

  if (!pollInterval) {
    pollInterval = setInterval(async () => {
      video.src = `http://localhost:8080/api/files/${videoGuid + '_finished'}?ts=${Date.now()}`;
      video.load();   // 触发加载流程，浏览器会触发对应事件
    }, 2000); // 每2秒执行
  }
}


// getAnalysis
const getAnalysis = async () => {
  try {
    const res = await request.get('/files/analysis/' + form.video_guid);
    if (res.code === 200) {
      analysisRanges.value = res.data.map((item: string) => {//map 会遍历数组中的每个元素。  将回调函数的返回值收集到一个新的数组中，最终返回这个新数组
        const [start, end] = item.split('-').map(Number);//使用解构赋值； "12.5-15.0" -> {start: 12.5, end: 15.0}
        return {start, end};
      });
      console.log('分析结果:', analysisRanges.value);
      await nextTick(() => {
        positionHighlightOverlay();
      });
    } else {
      ElMessage.error('分析统计失败，res.code: ' + res.code + ', res.message: ' + res.message);
    }
  } catch (e) {
    ElMessage.error('分析失败：' + (e as Error).message);
  }
};


function formatTime(seconds: number) {
  const m = Math.floor(seconds / 60).toString().padStart(2, '0');
  const s = Math.floor(seconds % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
}

function showAnalysis() {
  const formattedStr = analysisRanges.value
      .map(range => `${formatTime(range.start)} - ${formatTime(range.end)}`)
      .join(', ');
  ElMessage.success(formattedStr);
}


function onLoadedMetadata() {//当视频的元数据（如时长、尺寸等）加载完成时
  if (!videoRef.value) return;//检查 videoRef 是否存在，确保引用的 <video> 元素有效
  duration.value = videoRef.value.duration;//获取视频的总时长
  form.width = videoRef.value.videoWidth;
  form.height = videoRef.value.videoHeight;
  nextTick(() => {
    positionHighlightOverlay();
  });
}

// 自动找到原生 <video> 控件的进度条位置
function positionHighlightOverlay() {
  if (!videoRef.value || !highlightOverlay.value) return;
  const videoRect = videoRef.value.getBoundingClientRect();//获取原视频元素的尺寸和相对于视口的位置

  // 设置overlay宽度等于实际进度条 !!! 视频宽度减去估计值
  highlightOverlay.value.style.width = `${videoRect.width - 35}px`;
  //计算高亮覆盖层的垂直位置
  const bottomOffsetPx = 30;//手动计算进度条DOM 视频控件高度350px，进度条在视频底部上30px左右
  highlightOverlay.value.style.top = `${videoRect.bottom - bottomOffsetPx}px`;
  // 设置 overlay 左侧对齐进度条左侧 !!! 视频左侧往右偏移20px
  highlightOverlay.value.style.left = `${videoRect.left + 17}px`;

  // 更新每个区间位置与宽度
  const dur = duration.value || 1;
  analysisRanges.value.forEach((range, idx) => {
    const seg = highlightOverlay.value!.children[idx] as HTMLElement;//感叹号是TS非空断言操作符，告诉编译器value在运行时非null
    seg.style.left = `${(range.start / dur) * 100}%`;
    seg.style.width = `${((range.end - range.start) / dur) * 100}%`;
  });
}


onMounted(() => {
  window.addEventListener('resize', positionHighlightOverlay);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', positionHighlightOverlay);
});


</script>


<style scoped>
.upload-section {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  padding: 10px;
  border-radius: 8px;
  background-color: #f5f7fa;
}

.custom-upload :deep(.el-upload-dragger) {
  width: 260px;
  height: 100px;
  padding: 10px;
  border-radius: 10px;
}

.video-section {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 20px 20px;
  border-radius: 8px;
  background-color: #f5f7fa;
  justify-content: space-between; /* 确保均匀分布 */
  width: 100%; /* 确保容器占满宽度 */
}

.video-container {
  background: white;
  padding: 10px 0;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  flex: 1; /* 让每个子元素均分宽度 */
  display: flex;
  flex-direction: column;
  align-items: center; /* 横向居中 */
}

video {
  width: 622px;
  height: 350px; /* 保持高度 */
  display: block;
  margin: 0 auto; /* 水平居中 */
  border-radius: 4px;
  background: #000; /* 没资源时显示黑底，不会很窄 */
}

.highlight-overlay {
  position: absolute;
  left: 0;
  height: 10px; /* 高亮条厚度 */
  pointer-events: none; /* 不影响拖动进度条 */

  /* 灰色背景*/
  background: rgba(0, 0, 0, 0.2); /* 半透明灰色底、rgba(255, 0, 0, 0.5) */
  border-radius: 2px;
  overflow: hidden; /* 防止内部高亮段溢出圆角 */
  z-index: 20;
}

.highlight-segment {
  position: absolute;
  top: 0;
  height: 100%;
  background-color: rgba(102, 165, 232, 0.8);
  backdrop-filter: blur(2px); /* 背景模糊 */
  border-radius: 2px;
}

</style>

