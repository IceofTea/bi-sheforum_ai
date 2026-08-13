import axios from "axios";
import { VERCODE_URL,THREAD_URL } from '@/plugins/config.js';
async function readthread(name) {
    // console.log(11);
   await axios({
        method: 'get',
        url: THREAD_URL,
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        params: { file:'龙蛇演义.txt' }
    })
        .then((res) => {
            var tt = document.getElementById("tt")
             curFilename = '龙蛇演义';
            // console.log(tt);
            var titleRule = /\s*(第)([一二三四五六七八九十零百千万]{1,9})[章](\s*)(\S*)(\n|\r|\r\n)/g;	//  \s*(第)(.{1,9})[章节卷集部篇回](\s*)(\S*)(\n|\r|\r\n)
            var titleRuleS = /(第)(.{1,9})[章](\s*)(\S*)/;
            //进行章节标题匹配查找
            titles = res.data.data.match(titleRule);//全局
            //todo:处理章节标题
            // var fileTitle = []//全局
            var titleUL = "";
            titles.forEach(function (e) {
                var titlenow = e.match(/(第)(.{1,9})[章](\s*)(\S*)/)[0];
    
                fileTitle.push(titlenow);
                titleUL += '<li><a href="#" οnclick="loadData(\'' + titlenow + '\')"><i class="fas fa-stream"></i>' + titlenow + '</a></li> \
                ';
            })
            document.getElementById('leftNav').innerHTML = titleUL;
            //切割文档
            // var fileData = []//全局
            for (var i = 0; i < fileTitle.length - 1; i++) {
                fileData.push(res.data.data.substring(res.data.data.indexOf(fileTitle[i]), res.data.data.indexOf(fileTitle[i + 1])))
            }
            fileData.push(res.data.data.substring(res.data.data.indexOf(fileTitle[fileTitle.length - 1])))
            //加载文档
            // var currentPage = 0;//全局
            var cache = getCookie("localReadCache");
            if (isJSON(cache)) {
                var cacheJSON = JSON.parse(cache);
                if (cacheJSON.hasOwnProperty(curFilename)) {
                    currentPage = cacheJSON[curFilename];
                    paragraght.value = Number.parseInt(currentPage + 1);
                    // $('#leftNav').scrollTop($('.sidebar ul a')[currentPage - 1].offsetTop)
                }
            }
            tt.innerHTML = fileData[currentPage];
    
            var maxPage = fileTitle.length - 1;//全局
            var para = document.getElementById("paragraght");
            para.max = maxPage + 1;
        })
}


function setSize() {
    var fontsize = document.getElementById("fontsizt");
    if (Number.parseInt(fontsize.value) >= Number.parseInt(fontsize.min)) {
        var tt = document.getElementById('tt');
        tt.style.fontSize = fontsize.value + "px";
    }
}
function setParagraph(paragragh) {
    var para = document.getElementById("paragraght");
    if (Number.parseInt(paragragh) >= Number.parseInt(para.min) && Number.parseInt(para.value) <= Number.parseInt(para.max)) {
        console.log(paragragh);
        var index = Number.parseInt(paragragh) -1;
        var data = fileData[index];
        var tt = document.getElementById("tt");
        tt.innerHTML = fileData[index];
        currentPage = index;
        // $('#leftNav').scrollTop($('.sidebar ul a')[currentPage - 1].offsetTop)

        var cache = getCookie("localReadCache");
        if (isJSON(cache)) {
            var cacheJSON = JSON.parse(cache);
            cacheJSON[curFilename] = currentPage;
            setCookie("localReadCache", JSON.stringify(cacheJSON), 86400 * 60);
        } else {
            var newCache = {};
            newCache[curFilename] = currentPage;
            setCookie("localReadCache", JSON.stringify(newCache), 86400 * 60);
        }
    }
}
function loadData(title) {
    var index = fileTitle.indexOf(title);
    if (index > -1) {
        var data = fileData[index];
        var tt = document.getElementById("tt");
        tt.innerHTML = fileData[index];
        currentPage = index;

        document.getElementById('paragraght').value = Number.parseInt(currentPage + 1);
        var cache = getCookie("localReadCache");
        if (isJSON(cache)) {
            var cacheJSON = JSON.parse(cache);
            cacheJSON[curFilename] = currentPage;
            setCookie("localReadCache", JSON.stringify(cacheJSON), 86400 * 60);
        } else {
            var newCache = {};
            newCache[curFilename] = currentPage;
            setCookie("localReadCache", JSON.stringify(newCache), 86400 * 60);
        }
    }
}

function isJSON(str) {
    if (typeof str == 'string') {
        try {
            var obj = JSON.parse(str);
            if (typeof obj == 'object' && obj) {
                return true;
            } else {
                return false;
            }

        } catch (e) {
            console.log('error：' + str + '!!!' + e);
            return false;
        }
    }
    console.log('It is not a string!')
}


//liuming lium03@tom.com QQ395310500

//flow
// var styleObj=document.styleSheets[2]

//设置cookie
function setCookie(name, value, expires) {
    var exp = new Date();
    exp.setTime(exp.getTime() + (expires ? expires : 0) * 1000);
    var curCookie = name + "=" + escape(value) + ((expires) ? ";expires=" + exp.toGMTString() : "");
    document.cookie = curCookie;
}

//取出cookie
function getCookie(name) {
    var aCookie = document.cookie.split("; ");
    for (var i = 0; i < aCookie.length; i++) {
        var aCrumb = aCookie[i].split("=");
        if (name == aCrumb[0])
            return unescape(aCrumb[1]);
    }
    return null;
}

//删除cookie
function deleteCookie(name) {
    if (getCookie(name)) {
        document.cookie = name + "=; expires=Thu, 01-Jan-70 00:00:01 GMT";
    }
}

//flow

function setFont(fontSize) {
    if (document.getElementById('tt').getAttribute("style") == null) {
        document.getElementById('tt').setAttribute('style', `font-size:${fontSize}px;`)
    } else {
        document.getElementById('tt').setAttribute('style', document.getElementById('tt').getAttribute("style") + `font-size:${fontSize}px;`)
    }
}

function setColor(fcolor) {
    if (document.getElementById('tt').getAttribute("style") == null) {
        document.getElementById('tt').setAttribute('style', `color:${fcolor};`)
    } else {
        document.getElementById('tt').setAttribute('style', document.getElementById('tt').getAttribute("style") + `color:${fcolor};`)
    }
}
function setBack(bgcolor) {
    if (document.getElementById('tt').getAttribute("style") == null) {
        document.getElementById('tt').setAttribute('style', `background-color:${bgcolor};`)
    } else {
        document.getElementById('tt').setAttribute('style', document.getElementById('tt').getAttribute("style") + `background-color:${bgcolor};`)
    }
}
export default {
    readthread, setSize, setParagraph, loadData, isJSON, setCookie, getCookie, deleteCookie, setFont, setColor, setBack
}