<template>
  <el-form ref="ruleFormRef" :model="ruleForm" :rules="rules" label-width="120px" class="demo-ruleForm" :size="formSize" status-icon>
    <el-form-item label="名称" prop="name">
      <el-input v-model="ruleForm.name" />
    </el-form-item>

    <el-form-item label="父分类选择方式">
      <el-radio-group v-model="useButtonSelect" size="small">
        <el-radio-button :label="true">按钮选择</el-radio-button>
        <el-radio-button :label="false">下拉选择</el-radio-button>
      </el-radio-group>
    </el-form-item>

    <el-form-item label="父分类" prop="parent">
      <template v-if="useButtonSelect">
        <div class="category-buttons">
          <el-button
              v-for="item in allsort"
              :key="item.id"
              :type="ruleForm.parent === item.name ? 'primary' : 'default'"
              @click="ruleForm.parent = item.name"
              size="small"
              class="category-btn"
          >
            {{ item.name }}
          </el-button>
        </div>
      </template>
      <template v-else>
        <el-select v-model="ruleForm.parent" placeholder="一级分类">
          <el-option v-for="item in allsort" :key="item.id" :label="item.name" :value="item.name" />
        </el-select>
      </template>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="submitForm(ruleFormRef)">提交</el-button>
      <el-button @click="resetForm(ruleFormRef)">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import axios from 'axios';
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

let router = useRouter();
let route = useRoute();

let allsort = ref([]);
const formSize = ref('default');
const ruleFormRef = ref();
const useButtonSelect = ref(true); // 控制是否使用按钮选择方式

const ruleForm = ref({
  name: '',
  parent: '',
  status: 0,
});

onMounted(() => {
  parentlist();
  loadList();
});

const loadList = () => {
  axios({
    method: 'get',
    url: '/api/sort/one',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    params: route.query
  })
      .then((res) => {
        ruleForm.value = res.data;
        if (ruleForm.value.parent != null) {
          ruleForm.value.parent = res.data.parentcontent.name;
        }
      });
};

const parentlist = () => {
  axios({
    method: 'get',
    url: '/api/sort',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
  })
      .then((res) => {
        allsort.value = res.data;
      });
};

const rules = reactive({
  name: [
    {
      required: true,
      message: '请输入标题',
      trigger: 'change',
    },
  ],
});

const submitForm = async (formEl) => {
  if (!formEl) return;
  await formEl.validate((valid, fields) => {
    if (valid) {
      allsort.value.forEach((item) => {
        if (item.name == ruleForm.value.parent) {
          ruleForm.value.parent = item.id;
        }
      });
      axios.post('/api/sort/update', { ...ruleForm.value })
          .then((res) => {
            console.log(res);
            router.push('/sort/list');
          });
    } else {
      console.log('error submit!', fields);
    }
  });
};

const resetForm = (formEl) => {
  if (!formEl) return;
  formEl.resetFields();
};
</script>

<style scoped>
.demo-ruleForm {
  max-width: 600px;
  padding: 25px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #f3f7fb, #ffffff);
  margin: 30px auto;
}

.category-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-btn {
  min-width: 80px;
  text-align: center;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.category-btn:hover {
  transform: scale(1.05);
  background-color: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}
</style>
