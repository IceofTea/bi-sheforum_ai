<template>
  <div class="forum-container">
    <!-- 贴子头部区域 -->
    <div class="thread-header">
      <div class="thread-cover">
        <img :src="threadurl" class="cover-image" />
      </div>
      <div class="thread-meta">
        <h1 class="thread-title">{{ threadData.name }}</h1>
        <div class="meta-info">
          <span class="author">{{ threadData.writer }} 著</span>
          <span class="update-time">更新时间：{{ threadData.createTime ? threadData.createTime.substring(0, 16) : '未知' }}</span>
        </div>
        <div class="thread-tags">
          <span class="tag" v-for="item in threadsSort" :key="item">{{ item }}</span>
        </div>
        <div class="thread-stats">
          <span class="stat"><i class="el-icon-view"></i> {{ viewCount }} 浏览</span>
          <span class="stat"><i class="el-icon-star-off"></i> {{ collectCount }} 收藏</span>
        </div>
        <div class="thread-actions">
          <el-button type="primary" @click="gotoRead()" class="action-btn">
            <i class="el-icon-reading"></i> 进入详情页
          </el-button>
          <el-button
              :type="iscollected ? 'success' : 'default'"
              @click="iscollected ? removecollect() : addcollect()"
              class="action-btn"
          >
            <i class="el-icon-collection-tag"></i> {{ iscollected ? '已收藏' : '加入收藏' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 贴子内容区域 -->
    <div class="thread-content">
      <el-tabs v-model="activeTab" class="content-tabs">
        <el-tab-pane label="贴子简介" name="intro">
          <div class="intro-content">
            <div class="intro-text">{{ threadData.introduction }}</div>
            <div class="author-card">
              <div class="author-avatar">
                <img :src="VERCODE_URL+'/upload/' + writer.head" />
              </div>
              <div class="author-info">
                <h3>{{ writer.name }}</h3>
                <p>贴子作者</p>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="目录" name="toc" v-if="false">
          <!-- 目录内容 -->
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 评论区 -->
    <div class="comment-section">
      <h2 class="section-title"><i class="el-icon-chat-dot-round"></i> 评论区</h2>

      <!-- 评论表单 -->
      <div class="comment-form">
        <el-form :model="commentform" ref="formRef" :rules="rules">
          <el-form-item prop="comment">
            <el-input
                v-model="commentform.comment"
                type="textarea"
                :rows="4"
                placeholder="写下你的评论..."
                class="comment-textarea"
            />
          </el-form-item>
          <el-form-item class="form-actions">
            <el-button type="primary" @click="onSubmit(formRef)">发表评论</el-button>
            <el-button @click="resetForm(formRef)">清空内容</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 评论列表 -->
      <div class="comment-list-header">
        <span>全部评论</span>
        <el-radio-group v-model="commentSort" size="small" @change="sortComments">
          <el-radio-button label="time">最新</el-radio-button>
          <el-radio-button label="hot">最热</el-radio-button>
        </el-radio-group>
      </div>
      <div class="comment-list">
        <div class="comment-item" v-for="(item, index) in commentData" :key="item.id">
          <div class="comment-header">
            <div class="user-avatar">
              <img :src="VERCODE_URL+'/upload/' + item.userInfo.head" />
            </div>
            <div class="user-info">
              <span class="username">{{ item.userInfo.nickname }}</span>
              <span class="comment-meta">
                <span class="floor">{{index+1}}楼</span>
                <span class="time">{{ item.time }}</span>
              </span>
            </div>
          </div>
          <div class="comment-body">
            <p>{{ item.comment }}</p>
            <div class="comment-actions">
              <el-button size="small" :type="item.hasLiked ? 'primary' : 'default'" :icon="item.hasLiked ? 'el-icon-star-on' : 'el-icon-star-off'" circle @click="toggleCommentLike(item)"></el-button>
              <el-button
                  size="small"
                  @click="toggleReply(item.id)"
                  class="reply-btn"
              >
                回复({{ item.replyContent.length }})
              </el-button>
            </div>
          </div>

          <!-- 回复列表 -->
          <div class="reply-list" v-if="item.replyContent.length > 0">
            <div class="reply-item" v-for="i in item.replyContent" :key="i.id">
              <div class="reply-header">
                <div class="user-avatar small">
                  <img :src="VERCODE_URL+'/upload/' + i.userInfo.head" />
                </div>
                <div class="user-info">
                  <span class="username">{{ i.userInfo.nickname }}</span>
                  <span class="time">{{ formatRelativeTime(i.time) }}</span>
                </div>
              </div>
              <div class="reply-body">
                <p>{{ i.comment }}</p>
                <div class="reply-actions">
                  <el-button size="small" icon="el-icon-thumb" circle title="点赞"></el-button>
                  <el-button size="small" icon="el-icon-chat-line-round" circle title="回复"></el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 回复表单 -->
          <div class="reply-form" v-if="activeReply === item.id">
            <el-form :model="replycommentform" label-width="0">
              <el-form-item>
                <el-input
                    v-model="replycommentform.comment"
                    type="textarea"
                    :rows="2"
                    placeholder="写下你的回复..."
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="onreplySubmit(item.id)">提交回复</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { VERCODE_URL } from '@/plugins/config.js';
import { formatRelativeTime } from '@/utils/timeFormat.js';
import { ref, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter();
const route = useRoute();
const activeTab = ref('intro');
const activeReply = ref(null);
const threadData = ref({});
const commentData = ref([]);
const threadsSort = ref([]);
const threadurl = ref('');
const commentform = ref({});
const replycommentform = ref({});
const writer = ref({ head: 'liucixin' });
const iscollected = ref(false);
const collectlist = ref([]);
const formRef = ref();
const viewCount = ref(0);
const collectCount = ref(0);

const commentSort = ref('time')

function sortComments() {
  const sorted = [...commentData.value]
  if (commentSort.value === 'time') {
    sorted.sort((a, b) => new Date(b.time) - new Date(a.time))
  } else {
    sorted.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
  }
  commentData.value = sorted
}

const rules = reactive({
  comment: [
    { required: true, message: '评论不可为空', trigger: 'change' },
  ],
});

onMounted(async () => {
  getDetail();
  getComment();
});

function formatDate(date) {
  const year = date.getFullYear();
  const month = addLeadingZero(date.getMonth() + 1);
  const day = addLeadingZero(date.getDate());
  const hours = addLeadingZero(date.getHours());
  const minutes = addLeadingZero(date.getMinutes());
  const seconds = addLeadingZero(date.getSeconds());
  return `${year}年${month}月${day}号 ${hours}:${minutes}:${seconds}`;
}

function addLeadingZero(value) {
  return value < 10 ? `0${value}` : value;
}

function toggleReply(commentId) {
  activeReply.value = activeReply.value === commentId ? null : commentId;
}

async function toggleCommentLike(comment) {
  if (!sessionStorage.id) {
    ElMessage.warning('请先登录');
    return;
  }
  try {
    const res = await axios.post('/api/commentlike', { commentId: comment.id, userId: parseInt(sessionStorage.id) });
    if (res.status === 200) {
      comment.hasLiked = res.data.liked;
      comment.likeCount = res.data.count;
    }
  } catch (e) {
    ElMessage.error('操作失败');
  }
}

// 其余方法保持不变...
const onreplySubmit = async (id) => {
  var now = new Date();
  axios.post('api/reply', { ...replycommentform.value, userInfoId: sessionStorage.id, userCommentId: id, time: formatDate(now) }).then((res) => {
    getComment();
    replycommentform.value.comment = null;
    activeReply.value = null;
  })
}

const onSubmit = async (formEl) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    var now = new Date();
    if (valid) {
      axios.post('api/comments', { ...commentform.value, userInfoId: parseInt(sessionStorage.id), threadInfoId: parseInt(route.query.id), time: formatDate(now) }).then((res) => {
        getComment();
        formEl.resetFields();
        if (res.data && res.data.expInfo) {
          if (res.data.expInfo.gained) {
            ElMessage.success('评论成功，获得 ' + res.data.expInfo.exp + ' 经验值');
          } else if (res.data.expInfo.reason) {
            ElMessage.warning(res.data.expInfo.reason);
          }
          if (sessionStorage.id) {
            axios.get('/api/users/one', { params: { id: sessionStorage.id } }).then((userRes) => {
              if (userRes.data) {
                sessionStorage.setItem('userExp', userRes.data.experience || 0);
                sessionStorage.setItem('userLevel', userRes.data.level || 1);
              }
            });
          }
        }
      }).catch((err) => {
        ElMessage.error('评论失败');
      })
    }
  })
}

const resetForm = (formEl) => {
  if (!formEl) return
  formEl.resetFields()
}

function getcollect() {
  axios.get('api/usercollect/one',{params:{id:sessionStorage.id}}).then((res)=>{
    collectlist.value = res.data
    iscollected.value = false;
    res.data.forEach((element) => {
      if (element.threadInfo.id == route.query.id) {
        iscollected.value = true;
        return
      }
    });
  })
}

function getDetail() {
  getcollect()
  axios.get('api/threads/one',{params: route.query}).then((res) => {
    threadData.value = res.data;
    threadsSort.value.push(res.data.threadsSort.name);
    threadsSort.value.push(res.data.threadsSort.parentcontent.name);
    threadsSort.value.push(res.data.threadsSort.parentcontent.parentcontent.name);
    threadurl.value = VERCODE_URL+'/upload/' + threadData.value.picture;
    writer.value = threadData.value.writerContant;
  })
}

function getComment() {
  axios.get('api/comments/bythread',{
    params: route.query
  }).then((res) => {
    commentData.value = res.data;
  })
}

function gotoRead() {
  var now = new Date();
  axios.post('api/userread',{userInfoId:sessionStorage.id,threadInfoId:route.query.id,time:formatDate(now)}).then((res)=>{
  })
  router.push(`/thread/read?name=${threadData.value.text}&id=${route.query.id}`)
}

function removecollect(params) {
  let id = '';
  for (let index = 0; index < collectlist.value.length; index++) {
    if(collectlist.value[index].threadInfo.id == route.query.id && collectlist.value[index].userInfo.id == sessionStorage.id){
      id = collectlist.value[index].id
    }
  }
  axios.delete('api/usercollect',{params:{id:id}}).then((res)=>{
    getcollect();
  })
}

function addcollect() {
  var now = new Date();
  axios.post('api/usercollect',{userInfoId:sessionStorage.id,threadInfoId:route.query.id,time:formatDate(now)}).then((res)=>{
    getcollect();
  })
}
</script>

<style scoped>
.forum-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  box-shadow: 0 0 10px rgba(0,0,0,0.1);
}

/* 贴子头部样式 */
.thread-header {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.thread-cover {
  width: 220px;
  height: 300px;
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.cover-image:hover {
  transform: scale(1.05);
}

.thread-meta {
  flex: 1;
}

.thread-title {
  font-size: 28px;
  margin: 0 0 10px;
  color: #333;
}

.meta-info {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
  color: #666;
  font-size: 14px;
}

.thread-tags {
  display: flex;
  gap: 10px;
  margin: 20px 0;
}

.tag {
  background: #f0f2f5;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.thread-stats {
  display: flex;
  gap: 20px;
  margin: 20px 0;
  color: #666;
}

.stat {
  display: flex;
  align-items: center;
  gap: 5px;
}

.thread-actions {
  margin-top: 30px;
  display: flex;
  gap: 15px;
}

.action-btn {
  padding: 10px 20px;
}

/* 贴子内容样式 */
.thread-content {
  margin-bottom: 40px;
}

.content-tabs {
  margin-top: 20px;
}

.intro-content {
  display: flex;
  gap: 30px;
  padding: 20px 0;
}

.intro-text {
  flex: 1;
  line-height: 1.8;
  color: #333;
  font-size: 15px;
}

.author-card {
  width: 250px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  text-align: center;
}

.author-avatar {
  width: 100px;
  height: 100px;
  margin: 0 auto 15px;
  border-radius: 50%;
  overflow: hidden;
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-info h3 {
  margin: 0 0 5px;
  font-size: 18px;
}

.author-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.comment-list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  font-size: 14px;
  color: #909399;
}

/* 评论区样式 */
.comment-section {
  padding-top: 30px;
  border-top: 1px solid #eee;
}

.section-title {
  font-size: 22px;
  margin: 0 0 20px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-form {
  margin-bottom: 30px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.comment-textarea {
  font-size: 14px;
}

.form-actions {
  margin-top: 15px;
  text-align: right;
}

/* 评论列表样式 */
.comment-list {
  margin-top: 20px;
}

.comment-item {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
}

.user-avatar.small {
  width: 40px;
  height: 40px;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  flex: 1;
}

.username {
  font-weight: bold;
  color: #333;
}

.comment-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #999;
}

.comment-body {
  padding-left: 65px;
}

.comment-body p {
  margin: 0 0 10px;
  line-height: 1.6;
  color: #333;
}

.comment-actions {
  display: flex;
  gap: 10px;
}

.reply-btn {
  margin-left: auto;
}

/* 回复列表样式 */
.reply-list {
  margin-top: 20px;
  padding-left: 50px;
  border-left: 2px solid #eee;
}

.reply-item {
  padding: 15px 0;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.reply-body {
  padding-left: 50px;
}

.reply-body p {
  margin: 0 0 5px;
  font-size: 14px;
  color: #555;
}

.reply-actions {
  display: flex;
  gap: 5px;
}

/* 回复表单样式 */
.reply-form {
  margin-top: 15px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
}
</style>