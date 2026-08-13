<template>
  <div class="analytics-dashboard">
    <!-- 标题区域 -->
    <div class="dashboard-header card">
      <h1>舆情趋势分析面板</h1>
      <div class="header-controls">
        <el-select v-model="timeRange" placeholder="选择时间范围" @change="handleTimeRangeChange">
          <el-option label="最近7天" value="7"></el-option>
          <el-option label="最近30天" value="30"></el-option>
          <el-option label="最近3个月" value="90"></el-option>
          <el-option label="最近1年" value="365"></el-option>
        </el-select>
        <el-button type="primary" @click="exportData" :icon="Download">导出数据</el-button>
      </div>
    </div>

    <!-- 舆情预警 -->
    <div class="alert-section card" v-if="alertMessages.length > 0">
      <el-alert
          v-for="(alert, index) in alertMessages"
          :key="index"
          :title="alert.title"
          :type="alert.type"
          :description="alert.description"
          show-icon
          :closable="false"
      />
    </div>

    <!-- 收藏趋势 -->
    <div class="trend-section">
      <div class="section-header card">
        <h2>收藏趋势分析</h2>
        <div class="trend-controls">
          <el-radio-group v-model="collectChartType" @change="updateCollectCharts">
            <el-radio-button label="detail">详细数据</el-radio-button>
            <el-radio-button label="trend">趋势分析</el-radio-button>
          </el-radio-group>
          <div class="pagination-group">
            <span class="pagination-label">贴子分页:</span>
            <el-pagination
                v-model:current-page="threadCollectPage"
                :page-size="10"
                layout="prev, pager, next"
                :total="totalThreadCollectItems"
                small
                @current-change="updateCollectCharts"
            />
          </div>
        </div>
      </div>

      <div class="trend-content">
        <div class="trend-card main-card card">
          <div class="card-header">
            <h3>热门贴子收藏趋势</h3>
            <div class="card-actions">
              <el-tooltip content="显示收藏量最高的10个贴子" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
              <el-switch
                  v-model="showMA7"
                  active-text="7日均线"
                  @change="updateCollectCharts"
              />
            </div>
          </div>
          <div class="chart-container" ref="container" v-loading="collectLoading"></div>
        </div>

        <div class="trend-card side-card card">
          <div class="card-header">
            <h3>热门话题收藏分布</h3>
            <div class="pagination-group">
              <span class="pagination-label">话题分页:</span>
              <el-pagination
                  v-model:current-page="topicCollectPage"
                  :page-size="5"
                  layout="prev, pager, next"
                  :total="totalTopicCollectItems"
                  small
                  @current-change="updateCollectCharts"
              />
            </div>
          </div>
          <div class="month-selector">
            <el-select v-model="selectedMonth" placeholder="选择月份" @change="updateCollectCharts">
              <el-option
                  v-for="month in months"
                  :key="month.value"
                  :label="month.label"
                  :value="month.value"
              />
            </el-select>
          </div>
          <div class="chart-container" ref="kindcontainer" v-loading="kindCollectLoading"></div>
        </div>
      </div>
    </div>

    <!-- 浏览趋势 -->
    <div class="trend-section">
      <div class="section-header card">
        <h2>浏览趋势分析</h2>
        <div class="trend-controls">
          <el-radio-group v-model="readChartType" @change="updateReadCharts">
            <el-radio-button label="detail">详细数据</el-radio-button>
            <el-radio-button label="trend">趋势分析</el-radio-button>
          </el-radio-group>
          <div class="pagination-group">
            <span class="pagination-label">贴子分页:</span>
            <el-pagination
                v-model:current-page="threadReadPage"
                :page-size="10"
                layout="prev, pager, next"
                :total="totalThreadReadItems"
                small
                @current-change="updateReadCharts"
            />
          </div>
        </div>
      </div>

      <div class="trend-content">
        <div class="trend-card main-card card">
          <div class="card-header">
            <h3>热门贴子浏览趋势</h3>
            <div class="card-actions">
              <el-tooltip content="显示浏览量最高的10个贴子" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
              <el-switch
                  v-model="showYoY"
                  active-text="同比分析"
                  @change="updateReadCharts"
              />
            </div>
          </div>
          <div class="chart-container" ref="container2" v-loading="readLoading"></div>
        </div>

        <div class="trend-card side-card card">
          <div class="card-header">
            <h3>热门话题浏览分布</h3>
            <div class="pagination-group">
              <span class="pagination-label">话题分页:</span>
              <el-pagination
                  v-model:current-page="topicReadPage"
                  :page-size="5"
                  layout="prev, pager, next"
                  :total="totalTopicReadItems"
                  small
                  @current-change="updateReadCharts"
              />
            </div>
          </div>
          <div class="month-selector">
            <el-select v-model="selectedMonth" placeholder="选择月份" @change="updateReadCharts">
              <el-option
                  v-for="month in months"
                  :key="month.value"
                  :label="month.label"
                  :value="month.value"
              />
            </el-select>
          </div>
          <div class="chart-container" ref="kindcontainer2" v-loading="kindReadLoading"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import echarts from '@/plugins/echarts';
import { storeToRefs } from 'pinia';
import { useThreadsStore } from '@/stores/threads';
import { QuestionFilled, Download } from '@element-plus/icons-vue';
import { ElLoading } from 'element-plus';

const threadStore = useThreadsStore();
const { ReadSortData, CollectSortData, EveryThreadReadData, EveryThreadCollectData } = storeToRefs(threadStore);

// Refs
const container = ref(null);
const container2 = ref(null);
const kindcontainer = ref(null);
const kindcontainer2 = ref(null);

// 分页控制
const threadCollectPage = ref(1);
const topicCollectPage = ref(1);
const threadReadPage = ref(1);
const topicReadPage = ref(1);

// 图表类型控制
const collectChartType = ref('detail');
const readChartType = ref('detail');

// 其他控制
const timeRange = ref('30');
const selectedMonth = ref('1月');
const showMA7 = ref(false);
const showYoY = ref(false);

// 加载状态
const collectLoading = ref(false);
const kindCollectLoading = ref(false);
const readLoading = ref(false);
const kindReadLoading = ref(false);

// 预警信息
const alertMessages = ref([]);

// 月份数据
const months = ref([
  { label: '1月', value: '1月' },
  { label: '2月', value: '2月' },
  { label: '3月', value: '3月' },
  { label: '4月', value: '4月' },
  { label: '5月', value: '5月' },
  { label: '6月', value: '6月' },
  { label: '7月', value: '7月' },
  { label: '8月', value: '8月' },
  { label: '9月', value: '9月' },
  { label: '10月', value: '10月' },
  { label: '11月', value: '11月' },
  { label: '12月', value: '12月' }
]);

// 计算属性
const totalThreadCollectItems = computed(() => {
  return Object.keys(EveryThreadCollectData.value).length;
});

const totalTopicCollectItems = computed(() => {
  return Object.keys(CollectSortData.value).length;
});

const totalThreadReadItems = computed(() => {
  return Object.keys(EveryThreadReadData.value).length;
});

const totalTopicReadItems = computed(() => {
  return Object.keys(ReadSortData.value).length;
});

// 热门数据筛选
const hotThreadsCollect = computed(() => {
  return getTopItems(EveryThreadCollectData.value, threadCollectPage.value, 10);
});

const hotTopicsCollect = computed(() => {
  return getTopItems(CollectSortData.value, topicCollectPage.value, 5);
});

const hotThreadsRead = computed(() => {
  return getTopItems(EveryThreadReadData.value, threadReadPage.value, 10);
});

const hotTopicsRead = computed(() => {
  return getTopItems(ReadSortData.value, topicReadPage.value, 5);
});

// 方法
function getTopItems(data, page, pageSize = 10) {
  if (!data) return [];

  const items = Object.entries(data)
      .sort((a, b) => {
        const sumA = a[1].reduce((acc, val) => acc + val, 0);
        const sumB = b[1].reduce((acc, val) => acc + val, 0);
        return sumB - sumA;
      });

  const start = (page - 1) * pageSize;
  return items.slice(start, start + pageSize);
}

// 计算7日移动平均
function calculateMA7(data) {
  return data.map((_, idx) => {
    const start = Math.max(0, idx - 3);
    const end = Math.min(data.length - 1, idx + 3);
    const sum = data.slice(start, end + 1).reduce((a, b) => a + b, 0);
    return parseFloat((sum / (end - start + 1)).toFixed(2));
  });
}

// 计算同比数据
function calculateYoY(data) {
  // 这里简化处理，实际应该对比去年同月数据
  return data.map((val, idx) => {
    if (idx < 12) return null;
    const lastYearVal = data[idx - 12];
    return lastYearVal ? parseFloat(((val - lastYearVal) / lastYearVal * 100).toFixed(2)) : null;
  });
}

// 检测异常数据
function detectAnomalies() {
  const alerts = [];

  // 检测收藏量激增
  hotThreadsCollect.value.forEach(([name, data]) => {
    const lastMonth = data[data.length - 1];
    const prevMonth = data[data.length - 2];
    if (lastMonth > prevMonth * 1.5) {
      alerts.push({
        type: 'warning',
        title: '收藏量激增预警',
        description: `贴子"${name}"本月收藏量激增 ${Math.round((lastMonth/prevMonth - 1)*100)}%，请关注`
      });
    }
  });

  // 检测浏览量骤降
  hotThreadsRead.value.forEach(([name, data]) => {
    const lastMonth = data[data.length - 1];
    const prevMonth = data[data.length - 2];
    if (lastMonth < prevMonth * 0.7) {
      alerts.push({
        type: 'error',
        title: '浏览量骤降预警',
        description: `贴子"${name}"本月浏览量下降 ${Math.round((1 - lastMonth/prevMonth)*100)}%，请检查`
      });
    }
  });

  alertMessages.value = alerts;
}

async function getthreaddata() {
  if (!Object.keys(ReadSortData.value)[0]) {
    await threadStore.threadList();
  }
}

function handleTimeRangeChange() {
  updateAllCharts();
}

function updateAllCharts() {
  updateCollectCharts();
  updateReadCharts();
  detectAnomalies();
}

function exportData() {
  // 这里实现数据导出逻辑
  console.log('导出数据');
}

async function updateCollectCharts() {
  collectLoading.value = true;
  kindCollectLoading.value = true;

  try {
    await getthreaddata();
    collectInit();
    kindcollectInit();
  } finally {
    collectLoading.value = false;
    kindCollectLoading.value = false;
  }
}

async function updateReadCharts() {
  readLoading.value = true;
  kindReadLoading.value = true;

  try {
    await getthreaddata();
    readInit();
    kindreadInit();
  } finally {
    readLoading.value = false;
    kindReadLoading.value = false;
  }
}

// 图表初始化
function collectInit() {
  if (!container.value) return;

  const myChart = echarts.init(container.value);
  const series = [];

  // 添加主要数据系列
  hotThreadsCollect.value.forEach(([name, data]) => {
    series.push({
      name,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: {
        color: getRandomColor()
      },
      emphasis: {
        itemStyle: {
          color: '#ff4d4f',
          borderColor: '#fff',
          borderWidth: 2
        }
      },
      data,
      markPoint: {
        data: [
          { type: 'max', name: '最大值' },
          { type: 'min', name: '最小值' }
        ]
      }
    });

    // 添加7日均线
    if (showMA7.value && collectChartType.value === 'trend') {
      series.push({
        name: `${name}-7日均线`,
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: {
          type: 'dashed',
          width: 1
        },
        itemStyle: {
          color: getRandomColor()
        },
        data: calculateMA7(data),
        zlevel: -1
      });
    }
  });

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      },
      formatter: params => {
        let result = `<div style="font-weight:bold;margin-bottom:5px">${params[0].axisValue}</div>`;
        params.forEach(item => {
          const isMA = item.seriesName.includes('-7日均线');
          result += `
            <div style="display:flex;align-items:center;margin:5px 0">
              <span style="display:inline-block;width:10px;height:10px;background:${
              isMA ? 'transparent' : item.color
          };border:1px solid ${item.color};border-radius:50%;margin-right:5px"></span>
              ${item.seriesName}: <span style="font-weight:bold;margin-left:5px">${item.value}</span>
              ${isMA ? '<span style="color:#888;margin-left:5px">(7日均线)</span>' : ''}
            </div>
          `;
        });
        return result;
      }
    },
    legend: {
      data: hotThreadsCollect.value.map(([name]) => name)
          .concat(showMA7.value ? hotThreadsCollect.value.map(([name]) => `${name}-7日均线`) : []),
      type: 'scroll',
      orient: 'horizontal',
      bottom: 0,
      textStyle: {
        overflow: 'truncate',
        width: 100
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      axisLine: {
        lineStyle: {
          color: '#ddd'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#ddd'
        }
      },
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    series,
    animationEasing: 'elasticOut',
    animationDelay: idx => idx * 100
  };

  myChart.setOption(option);

  window.addEventListener('resize', () => {
    myChart.resize();
  });
}

function kindcollectInit() {
  if (!kindcontainer.value) return;

  const myChart = echarts.init(kindcontainer.value);
  const monthIndex = months.value.findIndex(m => m.value === selectedMonth.value);

  const seriesData = hotTopicsCollect.value.map(([name, data]) => ({
    name,
    value: data[monthIndex]
  })).sort((a, b) => b.value - a.value);

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      data: seriesData.map(item => item.name)
    },
    series: [
      {
        name: '收藏量',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: true
        },
        data: seriesData
      }
    ]
  };

  myChart.setOption(option);

  window.addEventListener('resize', () => {
    myChart.resize();
  });
}

// readInit 和 kindreadInit 与上面类似，只是数据源不同
function readInit() {
  if (!container2.value) return;

  const myChart = echarts.init(container2.value);
  const series = [];

  hotThreadsRead.value.forEach(([name, data]) => {
    series.push({
      name,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: {
        color: getRandomColor()
      },
      emphasis: {
        itemStyle: {
          color: '#1890ff',
          borderColor: '#fff',
          borderWidth: 2
        }
      },
      data,
      markPoint: {
        data: [
          { type: 'max', name: '最大值' },
          { type: 'min', name: '最小值' }
        ]
      }
    });

    // 添加同比分析
    if (showYoY.value && readChartType.value === 'trend') {
      const yoyData = calculateYoY(data);
      series.push({
        name: `${name}-同比`,
        type: 'bar',
        barGap: '-100%',
        barWidth: '60%',
        itemStyle: {
          color: 'rgba(150, 150, 150, 0.3)'
        },
        data: yoyData,
        tooltip: {
          formatter: params => {
            return `${name} 同比: ${params.value || 0}%`;
          }
        },
        label: {
          show: true,
          position: 'top',
          formatter: params => {
            return params.value ? `${params.value}%` : '';
          },
          color: '#666'
        }
      });
    }
  });

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: hotThreadsRead.value.map(([name]) => name)
          .concat(showYoY.value ? hotThreadsRead.value.map(([name]) => `${name}-同比`) : []),
      type: 'scroll',
      orient: 'horizontal',
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      axisLine: {
        lineStyle: {
          color: '#ddd'
        }
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '浏览量',
        axisLine: {
          lineStyle: {
            color: '#ddd'
          }
        },
        splitLine: {
          lineStyle: {
            type: 'dashed'
          }
        }
      },
      showYoY.value ? {
        type: 'value',
        name: '同比(%)',
        min: -100,
        max: 100,
        axisLine: {
          lineStyle: {
            color: '#ddd'
          }
        },
        splitLine: {
          show: false
        }
      } : null
    ].filter(Boolean),
    series,
    animationEasing: 'elasticOut',
    animationDelay: idx => idx * 100
  };

  myChart.setOption(option);

  window.addEventListener('resize', () => {
    myChart.resize();
  });
}

function kindreadInit() {
  if (!kindcontainer2.value) return;

  const myChart = echarts.init(kindcontainer2.value);
  const monthIndex = months.value.findIndex(m => m.value === selectedMonth.value);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#ddd'
        }
      },
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    yAxis: {
      type: 'category',
      data: hotTopicsRead.value.map(([name]) => name),
      axisLine: {
        lineStyle: {
          color: '#ddd'
        }
      }
    },
    series: [
      {
        name: '浏览量',
        type: 'bar',
        barWidth: '60%',
        data: hotTopicsRead.value.map(([_, data]) => data[monthIndex]),
        itemStyle: {
          color: params => {
            // 根据值大小设置不同颜色
            const value = params.value;
            const maxValue = Math.max(...hotTopicsRead.value.map(([_, data]) => data[monthIndex]));
            const ratio = value / maxValue;
            if (ratio > 0.8) return '#ff4d4f';
            if (ratio > 0.5) return '#faad14';
            return '#1890ff';
          }
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{c}'
        }
      }
    ]
  };

  myChart.setOption(option);

  window.addEventListener('resize', () => {
    myChart.resize();
  });
}

// 辅助函数 - 生成随机颜色
function getRandomColor() {
  const colors = [
    '#1890ff', '#2fc25b', '#facc14', '#223273', '#8543e0',
    '#13c2c2', '#3436c7', '#f04864', '#d3adf7', '#b37feb'
  ];
  return colors[Math.floor(Math.random() * colors.length)];
}

onMounted(() => {
  updateAllCharts();
});
</script>

<style lang="less" scoped>
.analytics-dashboard {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;

  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    margin-bottom: 20px;
    background: linear-gradient(to right, #f8f9fa, #fff);

    h1 {
      margin: 0;
      font-size: 28px;
      color: #333;
      font-weight: 600;
    }

    .header-controls {
      display: flex;
      gap: 15px;
      align-items: center;

      .el-select {
        width: 150px;
      }
    }
  }

  .alert-section {
    margin-bottom: 20px;

    .el-alert {
      margin-bottom: 10px;
    }
  }

  .trend-section {
    margin-bottom: 30px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px 20px;
      margin-bottom: 15px;

      h2 {
        margin: 0;
        font-size: 22px;
        color: #444;
        font-weight: 500;
      }

      .trend-controls {
        display: flex;
        align-items: center;
        gap: 20px;

        .pagination-group {
          display: flex;
          align-items: center;
          gap: 10px;

          .pagination-label {
            font-size: 14px;
            color: #666;
          }
        }
      }
    }

    .trend-content {
      display: flex;
      gap: 15px;

      .trend-card {
        flex: 1;
        transition: all 0.3s ease;

        &:hover {
          box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
          transform: translateY(-2px);
        }

        &.main-card {
          flex: 0 0 65%;
        }

        &.side-card {
          flex: 0 0 34%;
        }

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 15px;
          border-bottom: 1px solid #f0f0f0;

          h3 {
            margin: 0;
            font-size: 18px;
            color: #555;
            font-weight: 500;
          }

          .card-actions {
            display: flex;
            align-items: center;
            gap: 15px;

            .el-icon {
              color: #999;
              cursor: pointer;

              &:hover {
                color: #1890ff;
              }
            }
          }
        }

        .month-selector {
          padding: 10px 15px;

          .el-select {
            width: 120px;
          }
        }

        .chart-container {
          width: 100%;
          height: 400px;
          padding: 10px;
        }
      }
    }
  }

  .card {
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
    background-color: #fff;
    overflow: hidden;
    transition: box-shadow 0.3s;
  }
}
</style>