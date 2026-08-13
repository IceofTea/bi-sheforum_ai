<template>
  <div>
    <header class="header">
      <div class="logo" @click="goHome">校园论坛</div>
      <div class="user" @click="gotoUser">
        <img :src="myAvatar">
        <span>{{ userNickname }}</span>
      </div>
    </header>

    <div class="container">
      <div class="back">
        <button @click="goBack">返回</button>
        <button @click="init">刷新</button>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      
      <template v-else-if="threadData">
        <!-- 有封面 -->
        <div v-if="threadData.picture" class="thread-cover">
          <img :src="coverUrl" class="cover-img">
          <div class="cover-info">
            <span class="tag">{{ threadData.threadsSort?.name || '默认' }}</span>
            <h1>{{ threadData.name }}</h1>
              <div class="author">
<img :src="authorAvatar">
               <span>{{ threadData.writer }}</span>
               <span v-if="createTime" :title="formatDateTime(createTime)">{{ formatRelativeTime(createTime) }}</span>
            </div>
          </div>
        </div>
        
        <!-- 无封面 -->
        <div v-else class="thread-no-cover">
          <div class="author-row">
<img :src="authorAvatar">
             <div class="author-detail">
               <b>{{ threadData.writer }}</b>
               <span v-if="createTime" :title="formatDateTime(createTime)">{{ formatRelativeTime(createTime) }}</span>
             </div>
            <span class="tag">{{ threadData.threadsSort?.name || '默认' }}</span>
          </div>
          <h1>{{ threadData.name }}</h1>
        </div>

        <!-- 内容 -->
        <div class="thread-body">
          <div class="stats">
            <span>阅读: {{ threadData.views || 0 }}</span>
            <span>评论: {{ comments.length }}</span>
          </div>
          <div class="text">{{ threadData.introduction }}</div>
          <button :class="collected ? 'btn-collected' : ''" @click="toggleCollect">
            {{ collected ? '已收藏' : '收藏' }}
          </button>
        </div>

        <!-- 评论 -->
        <div class="comments">
          <h3>评论区 ({{ comments.length }})</h3>
          <div class="comment-form">
            <img :src="myAvatar">
            <textarea v-model="commentText" placeholder="评论..."></textarea>
            <button @click="submitComment" :disabled="submitting">发表</button>
          </div>
          <div class="list">
            <div v-for="(c,i) in comments" :key="c.id" class="item">
              <img :src="getAvatar(c.userInfo)">
              <div class="item-body">
                <div class="item-header">
                  <b>{{ c.userInfo?.nickname || '匿名' }}</b>
                  <span>#{{i+1}}</span>
                  <small>{{ c.time }}</small>
                  <button class="reply-btn" @click="showReplyForm(c.id)">回复</button>
                </div>
                <p>{{ c.comment }}</p>
                <!-- 楼中楼回复 -->
                <div v-if="c.replyContent?.length" class="replies">
                  <div v-for="r in c.replyContent" :key="r.id" class="reply-item">
                    <img :src="getAvatar(r.userInfo)">
                    <div class="reply-body">
                      <b>{{ r.userInfo?.nickname || '匿名' }}</b>
                      <span>回复</span>
                      <small>{{ r.time }}</small>
                      <p>{{ r.comment }}</p>
                    </div>
                  </div>
                </div>
                <!-- 回复表单 -->
                <div v-if="replyToCommentId === c.id" class="reply-form">
                  <img :src="myAvatar">
                  <input v-model="replyText" :placeholder="'回复 #' + (i+1)">
                  <button @click="submitReply(c.id)" :disabled="replySubmitting">{{ replySubmitting ? '发送中' : '发送' }}</button>
                  <button @click="replyToCommentId = null" class="cancel">取消</button>
                </div>
              </div>
            </div>
            <div v-if="!comments.length" class="empty">暂无评论</div>
          </div>
        </div>
      </template>
      
      <div v-else class="loading">帖子不存在</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { VERCODE_URL } from '@/plugins/config.js'
import { formatRelativeTime, formatDateTime } from '@/utils/timeFormat.js'

const route = useRoute()
const router = useRouter()

const threadData = ref(null)
const comments = ref([])
const commentText = ref('')
const collected = ref(false)
const loading = ref(true)
const submitting = ref(false)
const userNickname = ref('游客')
const myAvatar = ref('')
const authorAvatar = ref('')
const createTime = ref('')
const coverUrl = ref('')
const replyToCommentId = ref(null)
const replyText = ref('')
const replySubmitting = ref(false)

const colors = ['#667eea','#764ba2','#f093fb','#f5576c','#4facfe']

const getColor = (s) => {
  let h = 0
  for(let i=0; i<(s||'').length; i++) h = ((h<<5)-h) + (s||'').charCodeAt(i)
  return colors[Math.abs(h) % colors.length]
}

const makeAvatar = (name) => {
  const n = String(name||'U').charAt(0).toUpperCase()
  const c = getColor(name)
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100"><rect fill="${c}" width="100" height="100" rx="50"/><text x="50" y="60" text-anchor="middle" fill="white" font-size="40" font-weight="bold">${n}</text></svg>`
  return 'data:image/svg+xml;base64,' + btoa(unescape(encodeURIComponent(svg)))
}

const getAvatar = (u) => {
  if(u?.head) return VERCODE_URL + '/upload/' + u.head
  return makeAvatar(u?.nickname)
}

const getData = (res) => {
  if(res && res.data !== undefined) return res.data
  return res
}

const init = async () => {
  loading.value = true
  try {
    const userId = sessionStorage.id
    
    if(userId) {
      const ur = await axios.get('/api/users/one', {params:{id:userId}})
      const ud = getData(ur)
      if(ud && ud.nickname) {
        userNickname.value = ud.nickname || '用户'
        if(ud.head) myAvatar.value = VERCODE_URL + '/upload/' + ud.head
      }
    }
    
    const tr = await axios.get('/api/threads/one', {params:{id:route.query.id}})
    const td = getData(tr)
    if(td) {
      threadData.value = td
      if(td.picture) coverUrl.value = VERCODE_URL + '/upload/' + td.picture
      if(td.createTime) createTime.value = td.createTime.substring(0,10)
      if(td.writerId) {
        try {
          const ar = await axios.get('/api/users/one', {params:{id:td.writerId}})
          const ad = getData(ar)
          if(ad?.head) authorAvatar.value = VERCODE_URL + '/upload/' + ad.head
        } catch(e) {}
      }
      authorAvatar.value = authorAvatar.value || makeAvatar(td.writer)
    }
    
    if(userId) {
      axios.post('/api/userread', {threadInfoId:route.query.id, userInfoId:userId}).catch(()=>{})
    }
    
    if(userId) {
      const cr = await axios.get('/api/usercollect/one', {params:{id:userId}})
      const cd = getData(cr)
      collected.value = (cd||[]).some(x => x.threadInfo?.id == route.query.id)
    }
    
    const comr = await axios.get('/api/comments/bythread', {params:{id:route.query.id}})
    const cmt = getData(comr)
    comments.value = cmt || []
  } catch(e) {
    console.error('init error:', e)
  }
  loading.value = false
}

onMounted(() => {
  myAvatar.value = makeAvatar('我')
  init()
})

const submitComment = async () => {
  if(!commentText.value.trim()) return
  if(!sessionStorage.id) { ElMessage.warning('请先登录'); return }
  submitting.value = true
  try {
    const res = await axios.post('/api/comments', {comment:commentText.value, userInfoId:parseInt(sessionStorage.id), threadInfoId:parseInt(route.query.id)})
    commentText.value = ''
    init()
    if (res.data && res.data.expInfo) {
      if (res.data.expInfo.gained) {
        ElMessage.success('评论成功，获得 ' + res.data.expInfo.exp + ' 经验值');
      } else if (res.data.expInfo.reason) {
        ElMessage.warning(res.data.expInfo.reason);
      }
    }
  } catch(e) {
    ElMessage.error('评论失败');
  }
  submitting.value = false
}

const toggleCollect = async () => {
  if(!sessionStorage.id) { ElMessage.warning('请先登录'); return }
  try {
    if(collected.value) {
      const cr = await axios.get('/api/usercollect/one', {params:{id:sessionStorage.id}})
      const cd = getData(cr)
      const item = (cd||[]).find(x => x.threadInfo?.id == route.query.id)
      if(item) await axios.delete('/api/usercollect', {params:{id:item.id}})
    } else {
      await axios.post('/api/usercollect', {userInfoId:sessionStorage.id, threadInfoId:route.query.id})
    }
    collected.value = !collected.value
  } catch(e) {}
}

const showReplyForm = (commentId) => {
  if(!sessionStorage.id) { ElMessage.warning('请先登录'); return }
  console.log('show reply form for comment:', commentId)
  replyToCommentId.value = commentId
  replyText.value = ''
}

const submitReply = async (commentId) => {
  console.log('submit reply:', commentId, replyText.value)
  if(!replyText.value.trim()) { console.log('empty text'); return }
  if(!sessionStorage.id) { ElMessage.warning('请先登录'); return }
  replySubmitting.value = true
  try {
    console.log('posting to /api/reply...')
    const res = await axios.post('/api/reply', {userCommentId:commentId, userInfoId:sessionStorage.id, comment:replyText.value})
    console.log('reply response:', res)
    replyText.value = ''
    replyToCommentId.value = null
    init()
  } catch(e) {
    console.error('submit reply error:', e)
  }
  replySubmitting.value = false
}

const goHome = () => router.push('/')
const goBack = () => router.back()
const gotoUser = () => router.push('/user/index')
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { background: #f5f7fa; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
.header { height: 56px; background: linear-gradient(135deg, #667eea, #764ba2); display: flex; align-items: center; justify-content: space-between; padding: 0 20px; position: sticky; top: 0; z-index: 100; }
.logo { color: #fff; font-size: 20px; font-weight: bold; cursor: pointer; }
.user { display: flex; align-items: center; gap: 8px; color: #fff; cursor: pointer; }
.user img { width: 32px; height: 32px; border-radius: 50%; }
.container { max-width: 800px; margin: 0 auto; padding: 16px; }
.back { display: flex; gap: 10px; margin-bottom: 16px; }
.back button { padding: 8px 16px; background: #fff; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; }
.loading { padding: 40px; text-align: center; color: #999; background: #fff; border-radius: 12px; }

.thread-cover { position: relative; height: 240px; background: #000; border-radius: 12px 12px 0 0; overflow: hidden; }
.cover-img { width: 100%; height: 100%; object-fit: cover; }
.cover-info { position: absolute; bottom: 0; left: 0; right: 0; padding: 16px; background: linear-gradient(transparent, rgba(0,0,0,0.7)); color: #fff; }
.tag { display: inline-block; padding: 4px 10px; background: rgba(255,255,255,0.9); color: #333; border-radius: 4px; font-size: 12px; margin-bottom: 8px; }
.cover-info h1 { font-size: 22px; margin: 8px 0; line-height: 1.4; }
.author { display: flex; align-items: center; gap: 8px; font-size: 13px; opacity: 0.9; margin-top: 10px; }
.author img { width: 24px; height: 24px; border-radius: 50%; border: 2px solid #fff; }

.thread-no-cover { background: #fff; padding: 20px; border-radius: 12px 12px 0 0; }
.author-row { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.author-row img { width: 42px; height: 42px; border-radius: 50%; }
.author-detail b { display: block; color: #333; font-size: 15px; }
.author-detail span { font-size: 12px; color: #999; }
.thread-no-cover h1 { font-size: 22px; color: #1a1a1a; margin: 0; }

.thread-body { background: #fff; padding: 20px; border-radius: 0 0 12px 12px; margin-bottom: 16px; }
.stats { display: flex; gap: 20px; color: #666; padding-bottom: 14px; margin-bottom: 14px; border-bottom: 1px solid #f0f0f0; }
.text { font-size: 15px; line-height: 1.8; color: #333; white-space: pre-wrap; margin-bottom: 16px; }
.thread-body button { padding: 8px 20px; background: #fff; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; }
.btn-collected { background: #67c23a !important; color: #fff !important; border-color: #67c23a !important; }

.comments { background: #fff; padding: 20px; border-radius: 12px; }
.comments h3 { font-size: 16px; color: #333; margin-bottom: 16px; }
.comment-form { display: flex; gap: 12px; margin-bottom: 20px; }
.comment-form img { width: 40px; height: 40px; border-radius: 50%; flex-shrink: 0; }
.comment-form textarea { flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 8px; resize: none; font-size: 14px; }
.comment-form button { padding: 8px 16px; background: #667eea; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.list .item { display: flex; gap: 12px; padding: 14px 0; border-bottom: 1px solid #f5f5f5; }
.list .item img { width: 36px; height: 36px; border-radius: 50%; flex-shrink: 0; }
.list .item b { color: #333; font-size: 14px; }
.list .item span { color: #667eea; font-size: 12px; margin-left: 8px; }
.list .item small { color: #aaa; font-size: 12px; margin-left: 8px; }
.list .item p { color: #333; font-size: 14px; margin-top: 6px; line-height: 1.6; }
.list .item .item-header { display: flex; align-items: center; gap: 8px; }
.list .item .reply-btn { padding: 2px 8px; background: #f0f0f0; border: none; border-radius: 4px; font-size: 12px; color: #666; cursor: pointer; margin-left: auto; }
.list .item .replies { margin-top: 10px; padding-left: 12px; border-left: 2px solid #667eea; background: #f8f9fc; padding: 10px; border-radius: 0 8px 8px 0; }
.list .item .reply-item { display: flex; gap: 8px; padding: 8px 0; }
.list .item .reply-item img { width: 24px; height: 24px; }
.list .item .reply-body b { font-size: 12px; }
.list .item .reply-body span { font-size: 11px; color: #999; }
.list .item .reply-body p { margin-top: 4px; font-size: 13px; }
.list .item .reply-form { display: flex; gap: 8px; margin-top: 10px; align-items: center; }
.list .item .reply-form img { width: 24px; height: 24px; }
.list .item .reply-form input { flex: 1; padding: 6px 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 13px; }
.list .item .reply-form button { padding: 6px 12px; background: #667eea; color: #fff; border: none; border-radius: 6px; cursor: pointer; font-size: 12px; }
.list .item .reply-form button.cancel { background: #999; }
.empty { text-align: center; padding: 40px; color: #999; }
</style>