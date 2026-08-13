<template>
    <div class="container">
        <div class="top">
            <div class="toplogo">
                <img src="./img/shoplogo.png" alt="">
                <p>伍商管理</p>
            </div>
            <div class="topnav">
                <a href="#">基本版</a>
                <a href="#">English</a>
                <a href="#">手机版</a>
                <a href="#">用户指南</a>
            </div>
        </div>
        <div class="middle">
            <div class="picture">
                <img src="./img/购物2.png" alt="">
            </div>
            <el-card class="form-card">
                <template #header>
                    <div class="card-header">
                        <h1>注册</h1>
                    </div>
                </template>

                <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
                    <el-form-item label="账号" prop="rootname">
                        <el-input v-model="form.rootname" />
                    </el-form-item>
                    <el-form-item label="密码" prop="password">
                        <el-input v-model="form.password" show-password />
                    </el-form-item>
                    <el-form-item label="真实姓名" prop="realname">
                        <el-input v-model="form.realname" />
                    </el-form-item>
					<el-form-item>
                        <el-button @click="handleSubmit(formRef)" type="primary">注册</el-button>
                    </el-form-item>
                </el-form>
            </el-card>
        </div>
        <div class="bottom">
            <div class="bottomnav">
                <a href="#">首页</a>
                <a href="#">BILIBILI</a>
                <a href="#">开发者信息</a>
                <a href="#">联系我们</a>
            </div>
            <div class="bottominfo">ICP备案 粤B2-20090191-18 公安 粤公网安备 44010602006299增值电信业务许可证 粤B2-20090191 B2-20090058
            </div>
        </div>
    </div>
</template>
<script setup>
import axios from "axios"
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
let form = ref({
    status:0
});
//获取form组件实例
let formRef = ref();
//表单验证规则
const rules = reactive({
    rootname: [
        // 为空执行上行，输入执行下行
        { required: true, message: '请输入账号', trigger: 'blur' },
        { min: 3, max: 20, message: '账户长度3到20', trigger: 'blur' },
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { pattern: /\d{3,}/, message: '账户长度3到20', trigger: 'blur' },
    ],

});
//获取router实例对象
let router = useRouter();
let route = useRoute();

//注册
async function handleSubmit(formEl) {
    //校验整个表单
    formEl.validate(async (valid, fields) => {
        if (valid) {
            try {
                const res = await axios.post('/api/roots', { ...form.value })
                console.log("注册响应:", res.data)

                if (res.data && res.data.status === 200) {
                    ElMessage.success('注册成功，请登录')
                    router.push('/user/login')
                } else {
                    ElMessage.error(res.data?.msg || '注册失败')
                }
            } catch (error) {
                console.error('注册请求失败:', error)
                ElMessage.error('网络错误，请稍后重试')
            }

        } else {
            //没通过校验
            console.log('校验失败字段', fields)
            ElMessage.error('请检查表单内容')
        }
    })
}
</script>
<style lang="less" scoped>
@import "./css/login.scss";
@import "./css/reset.css";
</style>
