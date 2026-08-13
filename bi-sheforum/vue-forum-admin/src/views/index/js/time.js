function Day(day) {
    switch (day) {
        case 1:
            return '星期一';
        case 2:
            return '星期二';
        case 3:
            return '星期三';
        case 4:
            return '星期四';
        case 5:
            return '星期五';
        case 6:
            return '星期六';
        case 0:
            return '星期日';
    }
}
//加零
function Hours(hours) {
    if (hours < 10) return '0' + hours;
    else return hours;
}

function twelve(hours) {
    if (hours <= 12) return hours;
    else {
        hours -= 12;
        return hours;
    }
}

function isAM(hours) {
    if (hours <= 12) return 'AM';
    else {
        return 'PM';
    }
}

function clock(item) {
    var now = new Date();
    var year = now.getFullYear();
    var month = now.getMonth() + 1;
    var date = now.getDate();
    var hours = now.getHours();
    var min = now.getMinutes();
    var sec = now.getSeconds();
    var day = now.getDay();
    var oDate = document.querySelector('.date');
    var oTime = document.querySelector('.time');
    if (item == "date") {
        return `${year}-${month}-${date}`
    }
    else if (item == "time") {
        return `${Hours(twelve(hours))}:${Hours(min)}:${Hours(sec)} ${isAM(hours)}`;
    } else if (item == "xq") {
        console.log(now);
        return `${Day(day)}`
    }else if (item == "am") {
        return `${isAM(hours)}`
    }
    // oDate.innerHTML = ;
    // oTime.innerHTML = 
}
setInterval(clock, 10);

export default {
    Day, Hours, twelve, isAM, clock
}