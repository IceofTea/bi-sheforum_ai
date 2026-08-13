<template>
  <el-form ref="ruleFormRef" :model="ruleForm" :rules="rules" label-width="120px" class="demo-ruleForm" :size="formSize" status-icon>
    <el-card class="form-card" shadow="always">
      <div class="form-header">
        <h2 class="form-title">创建新分类</h2>
        <p class="form-description">填写表单以添加新的分类，确保提供有效的信息。</p>
      </div>

      <el-form-item label="名称" prop="name">
        <el-input v-model="ruleForm.name" placeholder="请输入分类名称" class="input-field" />
      </el-form-item>

      <el-form-item label="父分类">
        <el-radio-group v-model="parentSelectMode" size="small" style="margin-bottom: 10px;">
          <el-radio-button label="button">按钮模式</el-radio-button>
          <el-radio-button label="select">下拉模式</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- 按钮选择模式 -->
      <el-form-item v-if="parentSelectMode === 'button'" label="选择分类" prop="parent">
        <div class="category-buttons">
          <el-button
              v-for="item in allsort"
              :key="item.id"
              :type="ruleForm.parent === item.id ? 'primary' : 'default'"
              @click="ruleForm.parent = item.id"
              class="category-button"
          >
            {{ item.name }}
          </el-button>
        </div>
      </el-form-item>

      <!-- 下拉选择模式 -->
      <el-form-item v-else label="父分类" prop="parent">
        <el-select v-model="ruleForm.parent" placeholder="选择父分类" clearable class="input-field">
          <el-option v-for="item in allsort" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>

      <el-form-item class="form-buttons">
        <el-button type="primary" @click="submitForm(ruleFormRef)" :loading="loading" class="submit-btn">提交</el-button>
        <el-button @click="resetForm(ruleFormRef)" class="reset-btn">重置</el-button>
      </el-form-item>
    </el-card>
  </el-form>
</template>
<script setup>
import { reactive, ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

const router = useRouter();
const allsort = ref([]);
const loading = ref(false);
const formSize = ref('default');
const ruleFormRef = ref();
const parentSelectMode = ref('button'); // 默认按钮模式

const ruleForm = reactive({
  name: '',
  parent: '',
  status: 0,
});

onMounted(() => {
  parentlist();
});

const parentlist = () => {
  axios({
    method: 'get',
    url: '/api/sort',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
  }).then((res) => {
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
  parent: [
    {
      required: true,
      message: '请选择父分类',
      trigger: 'change',
    },
  ],
});

const submitForm = async (formEl) => {
  if (!formEl) return;
  loading.value = true;

  await formEl.validate((valid, fields) => {
    if (valid) {
      axios({
        method: 'post',
        url: '/api/sort',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        params: { ...ruleForm },
      })
          .then((res) => {
            ElMessage.success(res.msg || '提交成功');
            router.push('/sort/list');
          })
          .catch(() => {
            ElMessage.error('提交失败，请稍后重试');
          })
          .finally(() => {
            loading.value = false;
          });
    } else {
      console.log('表单验证失败!', fields);
      loading.value = false;
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
  max-width: 100%;
  padding: 25px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #f3f7fb, #ffffff);
  position: absolute;
  top: 20px;
  left: 20px;
  height: auto;
  width: auto;
}

.form-card {
  background-color: #fafafa;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.05);
  height: auto;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.form-header {
  margin-bottom: 20px;
}

.form-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
}

.form-description {
  font-size: 1rem;
  color: #606266;
}

.el-form-item {
  margin-bottom: 18px;
}

.input-field {
  border-radius: 8px;
  padding: 12px;
  background-color: #ffffff;
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
  width: 100%;
}

.input-field:focus {
  border-color: #409eff;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
}

.el-button {
  border-radius: 6px;
  transition: transform 0.3s ease, background-color 0.3s ease, box-shadow 0.3s ease;
}

.submit-btn {
  margin-right: 12px;
  background-color: #409eff;
  color: white;
  border: 1px solid #409eff;
}

.submit-btn:hover {
  background-color: #66b1ff;
  transform: scale(1.05);
}

.reset-btn {
  background-color: #f5f7fa;
  border-color: #dcdfe6;
  color: #606266;
}

.reset-btn:hover {
  background-color: #e9eff1;
}

.form-buttons {
  display: flex;
  justify-content: flex-start;
}

.category-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-button {
  min-width: 100px;
  background-color: #ffffff;
}

.category-button:hover {
  transform: scale(1.05);
}
</style>

