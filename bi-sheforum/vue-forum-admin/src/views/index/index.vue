<template>
  <div class="top">
    <div class="read card">
      <div class="container" ref="container">
        浏览量
      </div>
    </div>

    <div class="read_left">
      <!-- 欢迎区 -->
      <div class="toweb card welcome-card">
        <div class="towebtext">
          <div class="greeting">{{ greeting }}, <span class="role">管理员同志</span> 👋</div>
          <div class="subtext">欢迎登录后台系统</div>
        </div>
        <div class="towebbutton" @click="gotoUserWeb()">
          🚀 前往论坛主页
        </div>
      </div>

      <!-- 底部区域 -->
      <div class="read_left_bottom">
        <!-- 日历 -->
        <div class="calendar card">
          <el-calendar>
            <template #date-cell="{ data }">
              <p :class="data.isSelected ? 'is-selected' : ''">
                {{ data.day.split('-').slice(2).join('-') }}
              </p>
            </template>
          </el-calendar>
        </div>

        <!-- 时间与功能 -->
        <div class="calendar_left">
          <!-- 时间展示卡片 -->
          <div class="timecard card">
            <div class="time">
              <img src="./img/Logo.png" alt="系统Logo">
              <div class="contant">
                <div class="countdown-footer timevalue">{{ timevalue }}</div>
                <div class="countdown-footer">{{ datevalue }}</div>
                <div class="countdown-footer">{{ date }}</div>
              </div>
            </div>
          </div>

          <!-- 功能按钮区 -->
          <div class="function card">
            <div class="card-header">
              <span>🚧 常用功能</span>
            </div>
            <div class="content">
              <el-button class="text item" @click="gotolist('/reader/list')">
                <el-icon><User /></el-icon><p>用户列表</p>
              </el-button>
              <el-button class="text item" @click="gotolist('/thread/list')">
                <el-icon><Reading /></el-icon><p>贴子列表</p>
              </el-button>
              <el-button class="text item" @click="gotolist('/writer/list')">
                <el-icon><EditPen /></el-icon><p>版主列表</p>
              </el-button>
              <el-button class="text item" @click="gotolist('/sort/list')">
                <el-icon><Discount /></el-icon><p>分类列表</p>
              </el-button>
              <el-button class="text item" @click="gotolist('/comment/list')">
                <el-icon><ChatLineSquare /></el-icon><p>评论列表</p>
              </el-button>
              <el-button class="text item" @click="gotolist()">
                <el-icon><TurnOff /></el-icon><p>一键换肤</p>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
  <div style="display: flex">
    <div class="thread card">
      <div class="threadcontainer" ref="threadcontainer">
      </div>
    </div>
    <div class="statistic card">
      <el-row>
        <el-col :span="12">
          <el-statistic title="当前在线用户" :value="2047" />
        </el-col>
        <el-col :span="12">
          <el-statistic title="男女比例" :value="17">
            <template #suffix>/11</template>
          </el-statistic>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-statistic title="邀约用户" :value="writers.length" />
        </el-col>
        <el-col :span="12">
          <el-statistic title="贴子数量" :value="threads.length" />
        </el-col>
      </el-row>
    </div>
  </div>
</template>
<script setup>
import { VERCODE_URL } from '@/plugins/config.js';
import axios from 'axios';
import { ref, onMounted, reactive, computed } from 'vue'
import { User, Reading, EditPen, Discount, ChatLineSquare, TurnOff } from '@element-plus/icons-vue'
import time from './js/time'
import echarts from '@/plugins/echarts';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useThreadsStore } from '@/stores/threads';

const now = new Date();
const hour = now.getHours();

const greeting = computed(() => {
  if (hour >= 5 && hour < 12) {
    return '早上好';
  } else if (hour >= 12 && hour < 14) {
    return '中午好';
  } else if (hour >= 14 && hour < 18) {
    return '下午好';
  } else {
    return '晚上好';
  }
});

let threadStore = useThreadsStore();
//从store中提取属性同时保持响应式
let {
  EveryThreadReadData,
  EveryThreadCollectData,
  readdata,
  collectdata,
} = storeToRefs(threadStore);

const container = ref(null);
const threadcontainer = ref(null);
const datevalue = ref(time.clock("date"))
const timevalue = ref(time.clock("time"))
const amvalue = ref(time.clock("am"))
const date = ref(time.clock("xq"))
let threads = ref([]);
let writers = ref([]);
let sorts = ref([]);
let sortdata = ref([
  {
    value: 0,
    itemStyle: {
      color: '#6D9DC5'
    }
  },
  {
    value: 0,
    itemStyle: {
      color: '#F4A259'
    }
  },
  {
    value: 0,
    itemStyle: {
      color: '#90BE6D'
    }
  },
  {
    value: 0,
    itemStyle: {
      color: '#574AE2'
    }
  },
  {
    value: 0,
    itemStyle: {
      color: '#F25C54'
    }
  },
  {
    value: 0,
    itemStyle: {
      color: '#5D2E8C'
    }
  },
  {
    value: 0,
    itemStyle: {
      color: '#468FAF'
    }
  },
]);

function showtime() {
  setInterval(() => {
    timevalue.value = time.clock("time")
  }, 1000);
}

onMounted(async () => {
  await threadStore.threadList();
  showtime();
  readInit()
  await getnum();
  await threadsortnum();
});
const dataInitialized = ref(false);

function resetChartData() {
  dataInitialized.value = false;
  collectdata.value = Array(12).fill(0);
  readdata.value = Array(12).fill(0);
  var myChart = echarts.init(container.value);
  myChart.clear();
}

function handlePageChange() {
  resetChartData();
  readInit();
}

function readInit() {
  if (dataInitialized.value) return;

  axios.get('/api/userread/count').then(({ data }) => {
    const collectArray = Array(12).fill(0);
    const readArray = Array(12).fill(0);

    data.forEach(item => {
      const index = item.month - 1;
      collectArray[index] = item.collectCount;
      readArray[index] = item.readCount;
    });

    collectdata.value = collectArray;
    readdata.value = readArray;
    dataInitialized.value = true;
  });

  const myChart = echarts.init(container.value);
  var option = {
    color: ['#4CAF50', '#2196F3'],  // 收藏改为绿色，浏览改为蓝色
    title: {
      text: '年度访问',
      left: 'center',  // 标题居中
      textStyle: {
        fontWeight: 'bold',
        fontSize: 20,
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6A79A8'
        }
      },
      backgroundColor: '#F5F5F5',  // 提示框背景色
      borderColor: '#5D6D7E',
      borderWidth: 1,
      textStyle: {
        color: '#333'
      }
    },
    legend: {
      data: ['收藏', '浏览'],
      textStyle: {
        color: '#666',
        fontSize: 14
      },
      right: '5%'  // 右对齐
    },
    toolbox: {
      feature: {
        saveAsImage: {}
      }
    },
    grid: {
      left: '5%',
      right: '5%',
      bottom: '10%',
      top: '15%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'],
        axisLabel: {
          fontSize: 12,
          color: '#999'
        },
        axisLine: {
          lineStyle: {
            color: '#E6E6E6'
          }
        }
      }
    ],
    yAxis: [
      {
        type: 'value',
        axisLabel: {
          fontSize: 12,
          color: '#999'
        },
        axisLine: {
          lineStyle: {
            color: '#E6E6E6'
          }
        },
        splitLine: {
          lineStyle: {
            color: '#E6E6E6'
          }
        }
      }
    ],
    series: [
      {
        name: '收藏',
        type: 'line',
        smooth: true,
        showSymbol: false,  // 初始隐藏圆圈
        symbol: 'circle',
        symbolSize: 10,
        itemStyle: {
          color: '#4CAF50',
          borderColor: '#388E3C',
          borderWidth: 2
        },
        lineStyle: {
          color: '#4CAF50',
          width: 3
        },
        areaStyle: {
          opacity: 0.6,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#4CAF50' },
            { offset: 1, color: '#A5D6A7' }
          ])
        },
        data: collectdata.value,
        emphasis: {
          focus: 'series',
          itemStyle: {
            color: '#4CAF50',
            borderColor: '#388E3C',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'top'
          },
          symbolSize: 12  // 鼠标悬停时增大圆圈
        }
      },
      {
        name: '浏览',
        type: 'line',
        smooth: true,
        showSymbol: false,  // 初始隐藏圆圈
        symbol: 'circle',
        symbolSize: 10,
        itemStyle: {
          color: '#2196F3',
          borderColor: '#1976D2',
          borderWidth: 2
        },
        lineStyle: {
          color: '#2196F3',
          width: 3
        },
        areaStyle: {
          opacity: 0.6,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2196F3' },
            { offset: 1, color: '#90CAF9' }
          ])
        },
        data: readdata.value,
        emphasis: {
          focus: 'series',
          itemStyle: {
            color: '#2196F3',
            borderColor: '#1976D2',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'top'
          },
          symbolSize: 12  // 鼠标悬停时增大圆圈
        }
      }
    ]
  };

  myChart.setOption(option);
}

function threadsortnum() {
  const myChart = echarts.init(threadcontainer.value);
  const option = {
    title: {
      text: '主题活跃度趋势',
      left: 'center',
      top: '2%',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: '{b} : {c}'
    },
    grid: {
      left: '5%',
      right: '5%',
      bottom: '15%', // 给x轴文字留出空间
      top: '18%',    // 给标题留空间
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: sorts.value,
      axisLabel: {
        rotate: 0,
        color: '#666',
        fontSize: 12
      },
      axisTick: {
        alignWithLabel: true
      },
      axisLine: {
        lineStyle: {
          color: '#ccc'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisLabel: {
        color: '#888'
      },
      splitLine: {
        lineStyle: {
          color: '#eee'
        }
      }
    },
    series: [
      {
        type: 'bar',
        data: sortdata.value,
        barWidth: '50%',
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#6EC6FF' },
            { offset: 1, color: '#2196F3' }
          ]),
          shadowColor: 'rgba(0, 0, 0, 0.15)',
          shadowBlur: 8
        },
        label: {
          show: true,
          position: 'top',
          fontWeight: 'bold',
          color: '#333',
          formatter: '{c}'
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#42A5F5' },
              { offset: 1, color: '#1E88E5' }
            ])
          }
        },
        animationDelay: function (idx) {
          return idx * 100;
        },
        animationDurationUpdate: 800,
        animationEasingUpdate: 'cubicOut'
      }
    ],
    animationEasing: 'elasticOut'
  };

  myChart.setOption(option);
}


async function getnum() {
  await axios({
    method: 'get',
    url: '/api/writer',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
  }).then((res) => {
    writers.value = res.data;
  })

  await axios({
    method: 'get',
    url: '/api/sort',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
  }).then((res) => {
    res.data.forEach(element => {
      if (element.parent == 3) {
        sorts.value.push(element.name);
      }
    });
  })

  await axios({
    method: 'get',
    url: '/api/threads/all',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
  }).then((res) => {
    threads.value = res.data;

    res.data.forEach(item => {
      switch (item.threadsSort.name) {
        case sorts.value[0]:
          sortdata.value[0].value++;
          break;
        case sorts.value[1]:
          sortdata.value[1].value++;
          break;
        case sorts.value[2]:
          sortdata.value[2].value++;
          break;
        case sorts.value[3]:
          sortdata.value[3].value++;
          break;
        case sorts.value[4]:
          sortdata.value[4].value++;
          break;
        case sorts.value[5]:
          sortdata.value[5].value++;
          break;
        default:
          break;
      }
    })
  })
}

function gotoUserWeb() {
  location.assign('/');
}
let router = useRouter();
function gotolist(params) {
  router.push(params);
}
</script>

<style lang="less" scoped>
html {
  color: #3E3E3E !important;
}
@import url("./css/index.scss");
</style>