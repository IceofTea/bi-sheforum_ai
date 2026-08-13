<template>
    <div class="container">
        <div class="top">
            <div class="toplogo">
                <img src="./img/shoplogo.png" alt="">
                <p>毕设登录</p>
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
                        <h1>登录</h1>
                    </div>
                </template>

                <el-form ref="formRef" :model="form" :rules="rules" label-width="50px">
                    <el-form-item label="账号" prop="rootname">
                        <el-input v-model="form.rootname" />
                    </el-form-item>
                    <el-form-item label="密码" prop="password">
                        <el-input v-model="form.password" show-password />
                    </el-form-item>
                    
                    <el-form-item label="验证" prop="vercode">
                        <el-input style="display: inline; width: 120px; margin-right: 10px;" v-model="form.vercode"/>
                        <img class="vercode" :src="src" alt="" @click="updateVercode">
                    </el-form-item>
					<el-form-item>
                        <el-button @click="handleSubmit(formRef)" type="primary">登录</el-button>
                    </el-form-item>
                </el-form>
                <el-row justify="space-between">
                    <el-col :span="5">
                        <el-link href="/user/register" type="info">注册账号</el-link>
                    </el-col>
                    <el-col :span="5">
                        <el-link type="info">忘记密码?</el-link>
                    </el-col>
                </el-row>
            </el-card>
        </div>
        <div class="bottom">
            <div class="bottomnav">
                <a href="#">首页</a>
                <a href="#">BILIBILI</a>
                <a href="#">开发者信息</a>
                <a href="#">联系我们</a>
            </div>
            <div class="bottominfo">本站所收录的作品、社区话题、用户评论、用户上传内容或图片等均属用户个人行为。如前述内容侵害您的权益，欢迎举报投诉，一经核实，立即删除，本站不承担任何责任
            </div>
        </div>
    </div>
</template>
<script setup>
import { VERCODE_URL } from '@/plugins/config.js';
import axios from "axios"
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
let form = reactive({
    rootname: '',
    password: '',
    vercode:'',
    token:'',
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
    vercode: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
    ],
});
//获取router实例对象
let router = useRouter();
let route = useRoute();
updateVercode();
//验证码
let src=ref();
function updateVercode() {
    axios.get('/vercode',{}).then((res)=>{
        console.log(VERCODE_URL);
        src.value=VERCODE_URL+'/vercode/'+res.msg;
        form.token=res.msg;
    });
    
}
//登录
async function handleSubmit(formEl) {
    //校验整个表单
    formEl.validate(async (valid, fields) => {
        if (valid) {
            try {
                const res = await axios.get('/api/roots', { params: { ...form } })
                console.log("登录响应:", res)

                if (res && res.status === 200) {
                    // 登录成功
                    sessionStorage.rootname = form.rootname
                    sessionStorage.token = form.token
                    ElMessage.success('登录成功')

                    let { redirect } = route.query
                    console.log(redirect)
                    if (redirect) {
                        router.replace(redirect)
                    } else {
                        //跳转页面
                        router.push('/index')
                    }
                } else {
                    // 登录失败
                    ElMessage.error(res?.msg || '登录失败，请检查账号密码')
                }
            } catch (error) {
                console.error('登录请求失败:', error)
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
