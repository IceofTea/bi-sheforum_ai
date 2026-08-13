import { ref, reactive } from 'vue';
import { defineStore } from 'pinia';
import axios from 'axios';

export const useThreadsStore = defineStore('threads', () => {
    // 话题数据
    const ReadSortData = reactive({});
    const CollectSortData = reactive({});

    // 总数据 - 改为使用ref以便于整体替换
    const readdata = ref([0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]);
    const collectdata = ref([0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]);

    // 每一个贴子
    const EveryThreadReadData = reactive({});
    const EveryThreadCollectData = reactive({});

    // 重置所有数据
    function resetData() {
        readdata.value = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
        collectdata.value = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];

        // 清空对象属性
        for (const key in EveryThreadReadData) {
            delete EveryThreadReadData[key];
        }
        for (const key in EveryThreadCollectData) {
            delete EveryThreadCollectData[key];
        }
        for (const key in ReadSortData) {
            delete ReadSortData[key];
        }
        for (const key in CollectSortData) {
            delete CollectSortData[key];
        }
    }

    async function threadList() {
        // 先重置数据
        resetData();

        try {
            // 获取所有贴子
            const threadsRes = await axios({
                method: 'get',
                url: '/api/threads/all',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            });

            // 初始化数据结构
            threadsRes.data.forEach(item => {
                EveryThreadReadData[item.name] = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
                EveryThreadCollectData[item.name] = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
                CollectSortData[item.threadsSort.name] = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
                ReadSortData[item.threadsSort.name] = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
            });

            // 获取用户阅读数据
            const readRes = await axios({
                method: 'get',
                url: '/api/userread',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            });

            readRes.data.forEach(item => {
                if (!item.time) return;
                const timeStr = item.time.toString();
                const parts = timeStr.split("年");
                if (parts.length < 2) return;
                const monthIndex = parseInt(parts[1].split("月")[0]) - 1;
                if (monthIndex >= 0 && monthIndex < 12) {
                    readdata.value[monthIndex]++;

                    if (EveryThreadReadData[item.threadInfo.name]) {
                        EveryThreadReadData[item.threadInfo.name][monthIndex]++;
                    }

                    if (ReadSortData[item.threadInfo.threadsSort.name]) {
                        ReadSortData[item.threadInfo.threadsSort.name][monthIndex]++;
                    }
                }
            });

            // 获取用户收藏数据
            const collectRes = await axios({
                method: 'get',
                url: '/api/usercollect',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            });

            collectRes.data.forEach(item => {
                if (!item.time) return;
                const timeStr = item.time.toString();
                const parts = timeStr.split("年");
                if (parts.length < 2) return;
                const monthIndex = parseInt(parts[1].split("月")[0]) - 1;
                if (monthIndex >= 0 && monthIndex < 12) {
                    collectdata.value[monthIndex]++;

                    if (EveryThreadCollectData[item.threadInfo.name]) {
                        EveryThreadCollectData[item.threadInfo.name][monthIndex]++;
                    }

                    if (CollectSortData[item.threadInfo.threadsSort.name]) {
                        CollectSortData[item.threadInfo.threadsSort.name][monthIndex]++;
                    }
                }
            });

        } catch (error) {
            console.error('Error fetching thread data:', error);
            // 可以在这里添加错误处理逻辑
        }
    }

    return {
        CollectSortData,
        ReadSortData,
        EveryThreadReadData,
        EveryThreadCollectData,
        readdata,
        collectdata,
        threadList,
        resetData // 暴露resetData方法以便外部调用
    };
});