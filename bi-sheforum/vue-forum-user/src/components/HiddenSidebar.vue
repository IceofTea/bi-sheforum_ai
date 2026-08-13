<template>
  <div
    class="hidden-sidebar"
    :class="{ expanded: isExpanded, fixed: isFixed }"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <div class="sidebar-trigger" @click="toggleFixed">
      <i :class="isFixed ? 'el-icon-lock' : 'el-icon-more'"></i>
      <span class="trigger-tooltip">{{ isFixed ? '点击折叠' : '点击固定' }}</span>
    </div>

    <div class="sidebar-content">
      <div class="sidebar-header">
        <div class="logo-area">
          <i class="el-icon-location-outline"></i>
          <span>导航</span>
        </div>
      </div>

      <div class="nav-section quick-nav">
        <div class="nav-item" @click="goHome">
          <i class="el-icon-house"></i>
          <span>首页</span>
        </div>
        <div class="nav-item" @click="goUser">
          <i class="el-icon-user"></i>
          <span>个人中心</span>
        </div>
        <div class="nav-item" @click="gotoupload">
          <i class="el-icon-edit"></i>
          <span>发布帖子</span>
        </div>
      </div>

      <div class="nav-section" v-if="userId">
        <div class="section-title">
          <span>我的关注</span>
          <span class="count">{{ followedSorts.length }}</span>
        </div>
        <div class="my-sort-list" v-if="followedSorts.length > 0">
          <div
            v-for="item in followedSorts"
            :key="item.id"
            class="sort-tag"
            :style="{ backgroundColor: getSortColor(item.id) }"
            @click="goSort(item.id)"
          >
            <span>{{ item.name }}</span>
            <i class="el-icon-close" @click.stop="handleFollow(item.id, true)"></i>
          </div>
        </div>
        <div class="empty-tip" v-else>
          <span>关注话题更快找到</span>
        </div>
      </div>

      <div class="nav-section">
        <div class="section-title">
          <span>全部话题</span>
        </div>
        <div class="all-sort-grid">
          <div
            v-for="item in allSorts.slice(0, 12)"
            :key="item.id"
            class="sort-card"
            :style="{ borderLeftColor: getSortColor(item.id) }"
            @click="goSort(item.id)"
          >
            <div class="sort-card-name">{{ item.name }}</div>
            <div class="sort-card-meta">{{ item.threadCount || 0 }} 篇</div>
          </div>
        </div>
<div class="more-sort" v-if="allSorts.length > 12" @click="handleShowMore">
          <i class="el-icon-more"></i>
          <span>查看更多</span>
        </div>
      </div>

      <div class="nav-section" v-if="userId">
        <div class="section-title">
          <span>我创建的</span>
          <button class="create-btn" @click="showCreateDialog">+ 创建</button>
        </div>
        <div class="my-sort-list" v-if="createdSorts.length > 0">
          <div
            v-for="item in createdSorts"
            :key="item.id"
            class="sort-tag"
            :style="{ backgroundColor: getSortColor(item.id) }"
            @click="goSort(item.id)"
          >
            <span>{{ item.name }}</span>
          </div>
        </div>
        <div class="empty-tip" v-else>
          <span>创建你的专属话题</span>
        </div>
      </div>
    </div>

<el-dialog title="全部话题" v-model="showAllSorts" width="600px" :append-to-body="true">
      <div class="all-sort-dialog-grid">
        <div
          v-for="item in allSorts"
          :key="item.id"
          class="sort-card"
          :style="{ borderLeftColor: getSortColor(item.id) }"
          @click="goSort(item.id); showAllSorts = false"
        >
          <div class="sort-card-name">{{ item.name }}</div>
          <div class="sort-card-meta">{{ item.threadCount || 0 }} 篇</div>
        </div>
      </div>
    </el-dialog>

    <transition name="fade">
      <div v-if="showBackTop" class="back-top" @click="scrollToTop">
        <i class="el-icon-top"></i>
      </div>
    </transition>

    <div v-if="dialogVisible" class="custom-modal-overlay" @click.self="dialogVisible = false">
      <div class="custom-modal">
        <div class="modal-header">
          <h3>创建板块</h3>
          <i class="el-icon-close" @click="dialogVisible = false"></i>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>板块名称</label>
            <input v-model="createForm.name" placeholder="请输入板块名称" />
          </div>
          <div class="form-group">
            <label>板块描述</label>
            <textarea v-model="createForm.introduction" placeholder="请输入板块描述"></textarea>
          </div>
          <div class="form-group">
            <label>所属分类</label>
            <select v-model="createForm.parent">
              <option :value="null">作为一级分类</option>
              <option v-for="item in topLevelSorts" :key="item.id" :value="item.id">{{ item.name }}</option>
            </select>
          </div>
          <div class="form-actions">
            <button class="btn-cancel" @click="dialogVisible = false">取消</button>
            <button class="btn-submit" @click="submitCreate">创建</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const props = defineProps({
  userId: {
    type: Number,
    default: null
  }
})

const isExpanded = ref(false)
const isFixed = ref(false)
const dialogVisible = ref(false)
const createForm = ref({
  name: '',
  introduction: '',
  parent: null
})

const allSorts = ref([])
const followedSorts = ref([])
const mostVisitedSorts = ref([])
const createdSorts = ref([])

const topLevelSorts = computed(() => {
  return allSorts.value.filter(s => s.parent === null || s.parent === 1000)
})

const getChildren = (parentId) => {
  return allSorts.value.filter(s => s.parent === parentId)
}

const isFollowed = (sortId) => {
  return followedSorts.value.some(s => s.id === sortId)
}

const showAllSorts = ref(false)

const getSortColor = (id) => {
  const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9'];
  return colors[id % colors.length];
}

const gotoupload = () => {
  router.push('/uploadthread');
}

const handleShowMore = () => {
  console.log('handleShowMore called, showAllSorts:', showAllSorts.value)
  showAllSorts.value = true
}

const handleMouseEnter = () => {
  if (!isFixed.value) {
    isExpanded.value = true
  }
}

const handleMouseLeave = () => {
  if (!isFixed.value) {
    isExpanded.value = false
  }
}

const toggleFixed = () => {
  isFixed.value = !isFixed.value
  localStorage.setItem('sidebarFixed', isFixed.value)
}

const goHome = () => {
  router.push('/')
}

const goUser = () => {
  if (props.userId) {
    router.push('/user/index')
  } else {
    router.push('/login')
  }
}

const goSort = (sortId) => {
  if (props.userId) {
    axios.get('/api/usersort/visit', {
      params: { userId: props.userId, sortId, visitType: 1 }
    })
  }
  router.push(`/sortthread?id=${sortId}`)
}

const loadData = async () => {
  if (!props.userId) return

  try {
    const res = await axios.get('/api/usersort/all', {
      params: { userId: props.userId }
    }).catch(() => ({ data: null }))

    if (res.data && res.data.sorts) {
      const sorts = res.data.sorts
      const userData = res.data.userData || {}

      followedSorts.value = sorts.filter(s => userData[s.id]?.followed)

      const created = sorts.filter(s => userData[s.id]?.created)
      created.forEach(s => {
        s.threadCount = userData[s.id]?.threadCount || 0
      })
      createdSorts.value = created
    }

    const visitedRes = await axios.get('/api/usersort/mostVisited', {
      params: { userId: props.userId, limit: 5 }
    }).catch(() => ({ data: [] }))
    mostVisitedSorts.value = visitedRes.data || []
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const allThreads = ref([])
const loadAllSorts = async () => {
  console.log('loadAllSorts called, userId:', props.userId)
  try {
    const params = {}
    if (props.userId) params.userId = props.userId
    const [sortsRes, threadsRes] = await Promise.all([
      axios.get('/api/usersort/all', { params }),
      axios.get('/api/threads/all')
    ])

    const raw = sortsRes.data
    const sorts = Array.isArray(raw) ? raw : (raw?.sorts || [])
    allThreads.value = threadsRes.data || []

    const threadCountMap = {}
    allThreads.value.forEach(t => {
      const sortId = t.threadsSortId || t.threadsSort?.id
      if (sortId) {
        threadCountMap[sortId] = (threadCountMap[sortId] || 0) + 1
      }
    })

    sorts.forEach(sort => {
      sort.threadCount = threadCountMap[sort.id] || 0
    })

    allSorts.value = sorts
    console.log('allSorts updated with counts:', allSorts.value.slice(0, 5).map(s => ({id: s.id, name: s.name, count: s.threadCount})))
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const showCreateDialog = () => {
  console.log('showCreateDialog called, userId:', props.userId, 'dialogVisible will be:', !dialogVisible.value)
  createForm.value = { name: '', introduction: '', parent: null }
  dialogVisible.value = true
  console.log('dialogVisible now:', dialogVisible.value)
}

const submitCreate = () => {
  handleCreate()
}

const handleCreate = async () => {
  console.log('Creating sort with userId:', props.userId, 'form:', createForm.value)
  if (!createForm.value.name) {
    ElMessage.warning('请输入板块名称')
    return
  }
  if (!props.userId) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await axios({
      method: 'post',
      url: '/api/usersort/create',
      params: {
        userId: props.userId,
        name: createForm.value.name,
        introduction: createForm.value.introduction,
        parent: createForm.value.parent
      }
    })
    console.log('Create response:', res)
    if (res.status === 200) {
      dialogVisible.value = false
      ElMessage.success('创建成功')
      loadData()
    }
  } catch (error) {
    console.error('创建失败:', error)
    ElMessage.error('创建失败: ' + (error.response?.data?.msg || error.message))
  }
}

const handleFollow = async (sortId, isFollowed) => {
  console.log('handleFollow:', sortId, isFollowed, 'userId:', props.userId)
  if (!props.userId) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (isFollowed) {
      await axios.get('/api/usersort/unfollow', {
        params: { userId: props.userId, sortId }
      })
      ElMessage.success('取消关注成功')
    } else {
      await axios.get('/api/usersort/follow', {
        params: { userId: props.userId, sortId }
      })
      ElMessage.success('关注成功')
    }
    loadData()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  isFixed.value = localStorage.getItem('sidebarFixed') === 'true'
  if (props.userId != null && props.userId !== 0) {
    loadData()
  }
})

watch(() => props.userId, (newVal) => {
  console.log('userId changed:', newVal)
  loadAllSorts()
  if (newVal != null && newVal !== 0) {
    loadData()
  }
}, { immediate: true })

const handleStorageChange = (e) => {
  if (e.key === 'sortFollowChanged' || e.key === 'userLoggedIn') {
    loadData()
    loadAllSorts()
  }
}
window.addEventListener('storage', handleStorageChange)

const intervalId = setInterval(() => {
  loadData()
  loadAllSorts()
}, 30000)

const showBackTop = ref(false)
const handleScroll = () => {
  showBackTop.value = window.scrollY > 300
}
window.addEventListener('scroll', handleScroll, { passive: true })

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onBeforeUnmount(() => {
  clearInterval(intervalId)
  window.removeEventListener('storage', handleStorageChange)
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style lang="less" scoped>
.hidden-sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 0;
  z-index: 1000;
  
  &.expanded, &.fixed {
    width: 260px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
  }
  
  &:not(.expanded):not(.fixed):hover {
    width: 260px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
  }
  
  .sidebar-trigger {
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 20px;
    height: 40px;
    background: linear-gradient(135deg, #667eea, #764ba2);
    border-radius: 0 10px 10px 0;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #fff;
    font-size: 14px;
    transition: all 0.3s;
    overflow: visible;
    
    &::before {
      content: '';
      position: absolute;
      width: 8px;
      height: 8px;
      background: rgba(255,255,255,0.4);
      border: 1px solid rgba(255,255,255,0.6);
      border-radius: 2px;
    }
    
    &:hover {
      width: 28px;
      
      .trigger-tooltip {
        opacity: 1;
        transform: translateX(0);
      }
    }
    
    .trigger-tooltip {
      position: absolute;
      left: 30px;
      background: rgba(0,0,0,0.85);
      color: #fff;
      padding: 6px 12px;
      border-radius: 6px;
      font-size: 12px;
      white-space: nowrap;
      opacity: 0;
      transform: translateX(-10px);
      transition: all 0.3s;
      pointer-events: none;
      box-shadow: 0 2px 10px rgba(0,0,0,0.2);
    }
  }
  
  .sidebar-content {
    height: 100%;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 20px 0;
    
    &::-webkit-scrollbar {
      width: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.3);
      border-radius: 2px;
    }
    
    .sidebar-header {
      padding: 0 15px 15px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.2);
      margin-bottom: 15px;
      
      .logo-area {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #fff;
        font-size: 16px;
        font-weight: 600;
        
        i {
          font-size: 20px;
        }
      }
    }
    
    .quick-nav {
      display: flex;
      gap: 8px;
      padding: 0 10px;
      margin-bottom: 15px;

      .nav-item {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
        padding: 12px 8px;
        background: rgba(255, 255, 255, 0.1);
        border-radius: 8px;
        color: rgba(255, 255, 255, 0.9);
        cursor: pointer;
        transition: all 0.2s;
        font-size: 12px;

        &:hover {
          background: rgba(255, 255, 255, 0.2);
          transform: translateY(-2px);
        }

        i {
          font-size: 18px;
        }
      }
    }

    .nav-section {
      margin-bottom: 18px;

      .section-title {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 15px;
        margin-bottom: 10px;
        color: rgba(255, 255, 255, 0.7);
        font-size: 12px;
        font-weight: 500;
        text-transform: uppercase;
        letter-spacing: 0.5px;

        .count {
          background: rgba(255, 255, 255, 0.15);
          padding: 2px 8px;
          border-radius: 10px;
          font-size: 11px;
        }

        .create-btn {
          background: rgba(255, 255, 255, 0.2);
          border: none;
          color: #fff;
          padding: 4px 12px;
          border-radius: 12px;
          font-size: 12px;
          cursor: pointer;

          &:hover {
            background: rgba(255, 255, 255, 0.3);
          }
        }
      }

      .my-sort-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        padding: 0 10px;

        .sort-tag {
          display: flex;
          align-items: center;
          gap: 4px;
          padding: 6px 10px;
          border-radius: 16px;
          font-size: 12px;
          color: #fff;
          cursor: pointer;
          transition: all 0.2s;

          &:hover {
            transform: scale(1.05);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
          }

          i {
            font-size: 10px;
            opacity: 0.7;

            &:hover {
              opacity: 1;
            }
          }
        }
      }

      .all-sort-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 8px;
        padding: 0 10px;

        .sort-card {
          padding: 10px;
          background: rgba(255, 255, 255, 0.08);
          border-radius: 8px;
          border-left: 3px solid;
          cursor: pointer;
          transition: all 0.2s;

          &:hover {
            background: rgba(255, 255, 255, 0.15);
            transform: translateX(2px);
          }

          .sort-card-name {
            font-size: 12px;
            color: #fff;
            font-weight: 500;
            margin-bottom: 4px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .sort-card-meta {
            font-size: 10px;
            color: rgba(255, 255, 255, 0.5);
          }
        }
      }

      .more-sort {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
        padding: 10px;
        margin: 8px 10px 0;
        background: rgba(255, 255, 255, 0.05);
        border-radius: 8px;
        color: rgba(255, 255, 255, 0.6);
        font-size: 12px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          background: rgba(255, 255, 255, 0.1);
          color: #fff;
        }
      }
      
      .nav-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px 15px;
        color: rgba(255, 255, 255, 0.85);
        cursor: pointer;
        transition: all 0.2s;
        font-size: 14px;
        
        &:hover {
          background: rgba(255, 255, 255, 0.15);
          color: #fff;
        }
        
        i {
          font-size: 16px;
        }
      }
      
      .sort-list {
        .sort-item {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px 15px;
          color: rgba(255, 255, 255, 0.8);
          cursor: pointer;
          transition: all 0.2s;
          font-size: 13px;
          
          &:hover {
            background: rgba(255, 255, 255, 0.15);
            color: #fff;
          }
          
          i {
            font-size: 14px;
            color: rgba(255, 255, 255, 0.7);
          }
          
          .followed-icon {
            margin-left: auto;
            color: #67c23a;
          }
          
          .thread-count {
            margin-left: auto;
            font-size: 11px;
            color: rgba(255, 255, 255, 0.6);
          }
        }
      }
      
      .empty-tip {
        padding: 8px 15px;
        color: rgba(255, 255, 255, 0.5);
        font-size: 12px;
        font-style: italic;
      }
    }
  }
}

.custom-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.custom-modal {
  background: #fff;
  border-radius: 8px;
  width: 480px;
  max-width: 90%;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-bottom: 1px solid #ebeef5;

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }

    i {
      cursor: pointer;
      font-size: 20px;
      color: #909399;
      transition: color 0.2s;
      &:hover { color: #606266; }
    }
  }

  .modal-body {
    padding: 24px;

    .form-group {
      margin-bottom: 20px;

      label {
        display: block;
        margin-bottom: 8px;
        font-size: 14px;
        color: #606266;
      }

      input, textarea, select {
        width: 100%;
        padding: 10px 14px;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        font-size: 14px;
        color: #606266;
        box-sizing: border-box;
        transition: border-color 0.2s;

        &::placeholder {
          color: #c0c4cc;
        }

        &:focus {
          outline: none;
          border-color: #409eff;
        }
      }

      textarea {
        min-height: 100px;
        resize: vertical;
        font-family: inherit;
      }

      select {
        cursor: pointer;
        background-color: #fff;
      }
    }
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;

    button {
      padding: 9px 23px;
      border-radius: 4px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      border: none;
      transition: all 0.2s;
    }

    .btn-cancel {
      background: #fff;
      color: #606266;
      border: 1px solid #dcdfe6;
      &:hover {
        background: #f5f7fa;
        border-color: #c0c4cc;
      }
    }

    .btn-submit {
      background: #409eff;
      color: #fff;
      border: 1px solid #409eff;
      &:hover {
        background: #66b1ff;
        border-color: #66b1ff;
      }
    }
  }

  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 16px 20px;
    border-top: 1px solid #eee;

    button {
      padding: 8px 20px;
      border-radius: 6px;
      font-size: 14px;
      cursor: pointer;
      border: none;
    }

    .btn-cancel {
      background: #f5f7fa;
      color: #666;
      &:hover { background: #eef1f6; }
    }

    .btn-confirm {
      background: #409eff;
      color: #fff;
      &:hover { background: #66b1ff; }
    }
  }
}

.back-top {
  position: fixed;
  bottom: 40px;
  right: 24px;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  z-index: 999;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: all 0.3s;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
  }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.all-sort-dialog-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;

  .sort-card {
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;
    border-left: 3px solid;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #eef1f6;
      transform: translateY(-2px);
    }

    .sort-card-name {
      font-size: 13px;
      color: #333;
      font-weight: 500;
      margin-bottom: 4px;
    }

    .sort-card-meta {
      font-size: 11px;
      color: #999;
    }
  }
}
</style>