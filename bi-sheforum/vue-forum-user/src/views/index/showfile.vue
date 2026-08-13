<template>
	<div class="container">
		<!-- <button type="button" @click="settings.readthread()">读</button> -->
		<!-- <input type="file" id="file" /> -->
		<!-- <input type="button" @click="readText()" value="打开"> -->
		
		<div class="showtext">
			<pre id="tt">

			</pre>
		</div>
	</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter,useRoute } from 'vue-router'
import axios from "axios";
let route=useRoute();
let router = useRouter();
onMounted(() => {
	readthread(route.query)
})

async function readthread(thread) {
	console.log(thread);
	await axios.get('upload',{params: { file: thread.name }})
		.then((res) => {
            console.log(res.data);
			var tt = document.getElementById("tt")
			tt.innerHTML = res.data;
		})
}

</script>
<style lang="less" scoped>
@import url("./css/common.scss");

/* @import url("./css/style.css"); */
* {
	ul {
		list-style: none;
		padding: 0;
	}
}

.container {
	min-height: 700px;
	height: 100%;
	z-index: 1;
	background-color: #faf5eb;

	.show {
		display: flex;
		position: fixed;
		top: 45px;
		left: 170px;
		z-index: 10;

		ul {
			li {
				cursor: pointer;
				font-size: 18px;
				font-family: 宋体;
				color: #000;
				transition: all 0.2s;
				border: 1px solid #d8d8d8;
				background-color: #f6f1e7;

				.title {
					padding: 15px;
				}

				.checkedtitle {
					position: absolute;
					top: -1px;
					left: -1px;
					width: 100px;
					padding: 15px;
					background-color: #fbfaf4;
					box-shadow: 19px 10px 20px 0px #5c5c5c2b;
					border: 1px solid #d8d8d8;
				}
			}

			li:hover {
				color: #d80505;
			}
		}
	}

	.content {
		margin-left: 20px;

		.muli {
			background-color: #fbfaf4;
			padding: 20px 0 20px 20px;
			box-shadow: 19px 10px 20px 0px #5c5c5c2b;
			border: 1px solid #d8d8d8;
			border-left: 0;

			.sidebar {
				height: 500px;
				width: 900px;
				overflow-y: scroll;

				.leftNav {
					display: flex;
					justify-content: space-between;
					flex-wrap: wrap;
					// background-color: #ffffff00;

					li {
						width: 400px;
						padding: 10px 0;
						border: 0;
						border-bottom: 1px solid #d8d8d8;
						background-color: #fbfaf4;

						a {
							border: 0;
							color: inherit;
							text-decoration: none;
						}
					}

					li:hover {
						color: #d80505;
					}
				}
			}
		}

		.settings {
			box-shadow: 19px 10px 20px 0px #5c5c5c2b;
			background-color: #fbfaf4;
			padding: 20px 0 20px 20px;
			width: 400px;
			height: 300px;
			border: 1px solid #d8d8d8;
			border-left: 0;

			.fontsizenum {
				text-align: center;
				height: 45px;
				width: 100px;
				padding: 8px;
				margin-left: 10px;
				font-size: 18px;
				cursor: pointer;
				background-color: white;
				border-radius: 5px;
				border: 1.5px solid #d8d8d8;
				box-shadow: 0px 0px 20px -20px;
			}

			.guo-button {
				width: 50px;
				margin-left: 10px;
			}

			ul {
				display: flex;

				li {
					margin: 10px;
					width: 50px;
					height: 50px;
					border-radius: 50%;
				}
			}
		}
	}

	.showtext {
		// text-align: center;
		width: 1000px;
		margin: 0 auto;
		background-color: #f6f1e7;

		#tt {
			min-height: 800px;
			padding: 0 40px;
			border-left: 1px solid #d8d8d8;
			border-right: 1px solid #d8d8d8;
			font-size: 24px;
			line-height: 50px;
			word-wrap: break-word;
			white-space: pre-wrap;
			font-family: 微软雅黑;
		}
	}
}
</style>
	




