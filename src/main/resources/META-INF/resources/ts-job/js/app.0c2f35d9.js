(function(){"use strict";var t={694:function(t,e,s){var a=s(144),i=s(345),o=function(){var t=this,e=t._self._c;return e("div",[e("v-card",[e("v-hover",{scopedSlots:t._u([{key:"default",fn:function({hover:s}){return[e("v-parallax",{staticClass:"mx-auto transition-swing white lighten-5",class:"elevation-"+(s?24:6),attrs:{height:"500",src:t.backSrc}},[e("v-row",{attrs:{align:"center",justify:"center"}},[e("v-col",{staticClass:"text-center",attrs:{cols:"12"}},[e("v-hover",{scopedSlots:t._u([{key:"default",fn:function({hover:s}){return[e("v-card",{staticClass:"mx-auto transition-swing white lighten-5",class:"elevation-"+(s?24:6),staticStyle:{"text-align":"left"},attrs:{"max-width":"400"}},[e("v-img",{staticClass:"white--text align-end",attrs:{height:"200px",src:t.cardSrc}},[e("v-card-title",[t._v("TS - JOB")])],1),e("v-card-text",{staticClass:"text--primary"},[e("div",[t._v("QQ : 646425671")]),e("div",[t._v("EMAIL : wuyue930912@live.com")])])],1)]}}],null,!0)})],1)],1)],1)]}}])})],1),e("v-container",{attrs:{fluid:""}},[e("v-row",{attrs:{dense:""}},[e("v-col",[e("v-hover",{scopedSlots:t._u([{key:"default",fn:function({hover:t}){return[e("v-card",{staticStyle:{"margin-top":"30px"}},[e("div",{staticClass:"mx-auto transition-swing white lighten-5",class:"elevation-"+(t?24:6),staticStyle:{width:"100%",height:"600px"},attrs:{id:"job"}})])]}}])})],1),e("v-col",[e("v-hover",{scopedSlots:t._u([{key:"default",fn:function({hover:t}){return[e("v-card",{staticStyle:{"margin-top":"30px"}},[e("div",{staticClass:"mx-auto transition-swing white lighten-5",class:"elevation-"+(t?24:6),staticStyle:{width:"100%",height:"600px"},attrs:{id:"time"}})])]}}])})],1)],1),e("v-row",{attrs:{dense:""}},[e("v-col",[e("v-hover",{scopedSlots:t._u([{key:"default",fn:function({hover:t}){return[e("v-card",{staticClass:"mx-auto pa-6 transition-swing pt-4 white lighten-5",class:"elevation-"+(t?24:6),staticStyle:{"margin-top":"30px"}},[e("div",{staticStyle:{width:"100%",height:"600px"},attrs:{id:"cpu"}})])]}}])})],1),e("v-col",[e("v-hover",{scopedSlots:t._u([{key:"default",fn:function({hover:t}){return[e("v-card",{staticClass:"mx-auto pa-6 transition-swing pt-4 white lighten-5",class:"elevation-"+(t?24:6),staticStyle:{"margin-top":"30px"}},[e("div",{staticStyle:{width:"100%",height:"600px"},attrs:{id:"mem"}})])]}}])})],1)],1)],1)],1)},n=[],r=s(724),l=s(154),c={name:"IndexHome",data:()=>({backSrc:s(716),cardSrc:s(69),chartData:[],timeDataAxis:["懒","的","做","了","有","需","要","再","说","吧"],timeData:[220,182,191,234,290,330,310,123,442,150],timeDataMax:500,mem:25}),mounted(){this.drawChart(),this.drawTime(),this.drawCpu(),this.drawMem()},methods:{getContextPath(){const t=document.location.pathname,e=t.substr(1).indexOf("/"),s=t.substr(0,e+1);return"/ts-job"===s?"":s},drawMem(){let t=this.$echarts.init(document.getElementById("mem")),e={title:{text:"内存监控",left:"center"},legend:{top:"bottom"},series:[{type:"gauge",center:["50%","60%"],startAngle:200,endAngle:-20,min:0,max:100,splitNumber:10,itemStyle:{color:"#fff091"},progress:{show:!0,width:30},pointer:{show:!1},axisLine:{lineStyle:{width:30}},axisTick:{distance:-45,splitNumber:5,lineStyle:{width:2,color:"#fca400"}},splitLine:{distance:-52,length:14,lineStyle:{width:3,color:"#ff1b00"}},axisLabel:{distance:-20,color:"#d5d542",fontSize:20},anchor:{show:!1},title:{show:!1},detail:{valueAnimation:!0,width:"60%",lineHeight:40,borderRadius:8,offsetCenter:[0,"-15%"],fontSize:60,fontWeight:"bolder",formatter:"{value} %",color:"inherit"},data:[{value:20}]},{type:"gauge",center:["50%","60%"],startAngle:200,endAngle:-20,min:0,max:100,itemStyle:{color:"#ffba5f"},progress:{show:!0,width:8},pointer:{show:!1},axisLine:{show:!1},axisTick:{show:!1},splitLine:{show:!1},axisLabel:{show:!1},detail:{show:!1},data:[{value:20}]}]};function s(){const t=document.location.pathname,e=t.substr(1).indexOf("/"),s=t.substr(0,e+1);return"/ts-job"===s?"":s}t.setOption(e),setInterval((function(){l.Z.get(s()+"/ts-job/get-mem").then((e=>{const s=e.data.code;if(200===s){let s=e.data.data;t.setOption({series:[{data:[{value:s}]},{data:[{value:s}]}]})}}))}),5e3)},drawCpu(){let t=this.$echarts.init(document.getElementById("cpu")),e={title:{text:"CPU监控",left:"center"},legend:{top:"bottom"},series:[{type:"gauge",center:["50%","60%"],startAngle:200,endAngle:-20,min:0,max:100,splitNumber:5,itemStyle:{color:"#FFAB91"},progress:{show:!0,width:30},pointer:{show:!1},axisLine:{lineStyle:{width:30}},axisTick:{distance:-45,splitNumber:5,lineStyle:{width:2,color:"#59ec94"}},splitLine:{distance:-52,length:14,lineStyle:{width:3,color:"#b9ff69"}},axisLabel:{distance:-20,color:"#ff9c9c",fontSize:20},anchor:{show:!1},title:{show:!1},detail:{valueAnimation:!0,width:"60%",lineHeight:40,borderRadius:8,offsetCenter:[0,"-15%"],fontSize:60,fontWeight:"bolder",formatter:"{value} %",color:"inherit"},data:[{value:20}]},{type:"gauge",center:["50%","60%"],startAngle:200,endAngle:-20,min:0,max:100,itemStyle:{color:"#ff5f5f"},progress:{show:!0,width:8},pointer:{show:!1},axisLine:{show:!1},axisTick:{show:!1},splitLine:{show:!1},axisLabel:{show:!1},detail:{show:!1},data:[{value:20}]}]};function s(){const t=document.location.pathname,e=t.substr(1).indexOf("/"),s=t.substr(0,e+1);return"/ts-job"===s?"":s}t.setOption(e),setInterval((function(){l.Z.get(s()+"/ts-job/get-cpu").then((e=>{const s=e.data.code;if(200===s){let s=e.data.data;t.setOption({series:[{data:[{value:s}]},{data:[{value:s}]}]})}}))}),5e3)},drawChart(){let t=this.$echarts.init(document.getElementById("job"));l.Z.get(s()+"/ts-job/get-record-bi").then((e=>{const s=e.data.code;if(200===s){let s=e.data.data;t.setOption({series:[{data:s}]})}}));let e={title:{text:"执行状态分布",subtext:"全部任务执行状态",left:"center"},tooltip:{trigger:"item"},legend:{orient:"vertical",left:"left"},series:[{type:"pie",radius:"50%",data:this.chartData,emphasis:{itemStyle:{shadowBlur:10,shadowOffsetX:0,shadowColor:"rgba(0, 0, 0, 0.5)"}}}]};function s(){const t=document.location.pathname,e=t.substr(1).indexOf("/"),s=t.substr(0,e+1);return"/ts-job"===s?"":s}t.setOption(e)},drawTime(){let t=this.$echarts.init(document.getElementById("time")),e=this.timeDataAxis,s=this.timeData,a=this.timeDataMax,i=[];for(let r=0;r<s.length;r++)i.push(a);let o={title:{text:"调度任务时间分布",subtext:"最近12h任务执行情况",left:"center"},xAxis:{data:e,axisLabel:{inside:!0,color:"#fff"},axisTick:{show:!1},axisLine:{show:!1},z:10},yAxis:{axisLine:{show:!1},axisTick:{show:!1},axisLabel:{color:"#999"}},dataZoom:[{type:"inside"}],series:[{type:"bar",showBackground:!0,itemStyle:{color:new r.graphic.LinearGradient(0,0,0,1,[{offset:0,color:"#83bff6"},{offset:.5,color:"#188df0"},{offset:1,color:"#188df0"}])},emphasis:{itemStyle:{color:new r.graphic.LinearGradient(0,0,0,1,[{offset:0,color:"#2378f7"},{offset:.7,color:"#2378f7"},{offset:1,color:"#83bff6"}])}},data:s}]};const n=6;t.on("click",(function(a){console.log(e[Math.max(a.dataIndex-n/2,0)]),t.dispatchAction({type:"dataZoom",startValue:e[Math.max(a.dataIndex-n/2,0)],endValue:e[Math.min(a.dataIndex+n/2,s.length-1)]})})),t.setOption(o)}},computed:{color(){switch(this.value){case 0:return"blue-grey";case 1:return"teal";case 2:return"brown";case 3:return"indigo";default:return"blue-grey"}}}},d=c,h=s(1),u=(0,h.Z)(d,o,n,!1,null,"5a4a819b",null),v=u.exports,m=function(){var t=this,e=t._self._c;return e("v-app",[e("IndexBar"),e("v-container",{staticStyle:{"margin-top":"250px"}},[e("transition",{attrs:{name:"slide-fade",mode:"out-in"}},[e("router-view")],1)],1),e("IndexFooter")],1)},p=[],b=function(){var t=this,e=t._self._c;return e("div",[e("v-app-bar",{attrs:{absolute:"",color:"#6A76AB",dark:"","shrink-on-scroll":"",prominent:"",src:t.headerSrc,"scroll-target":"#scrolling-techniques","scroll-threshold":"500"},scopedSlots:t._u([{key:"img",fn:function({props:s}){return[e("v-img",t._b({attrs:{gradient:"to top right, rgba(100,115,201,.1), rgba(25,32,72,.3)"}},"v-img",s,!1))]}},{key:"extension",fn:function(){return[e("v-tabs",{attrs:{"align-with-title":""}},[e("v-tab",{attrs:{to:"/main/home"}},[t._v(" 首页 ")]),e("v-tab",{attrs:{to:"/main/list"}},[t._v(" 任务管理 ")])],1)]},proxy:!0}])},[e("v-app-bar-nav-icon",{on:{click:function(e){e.stopPropagation(),t.drawer=!t.drawer}}}),e("v-toolbar-title",{staticClass:"text-h6 mr-6 hidden-sm-and-down"},[t._v(" TECH SERVICE JOB 任务调度平台 ")])],1),e("v-navigation-drawer",{attrs:{absolute:"",temporary:""},model:{value:t.drawer,callback:function(e){t.drawer=e},expression:"drawer"}},[e("v-list-item",[e("v-list-item-content",[e("v-list-item-title",[t._v("TS任务调度平台")])],1)],1),e("v-divider"),e("v-list",{attrs:{dense:""}},t._l(t.drawers,(function(s){return e("v-list-item",{key:s.title,attrs:{link:""}},[e("v-list-item-icon",[e("v-icon",[t._v(t._s(s.icon))])],1),e("v-list-item-content",[e("v-list-item-title",[t._v(t._s(s.title))])],1)],1)})),1)],1)],1)},g=[],f={name:"IndexBar",watch:{group(){this.drawer=!1}},data:()=>({tooBarItems:[],value:1,drawer:!1,headerSrc:s(673),isLoading:!1,items:[{src:s(290)},{src:s(69)},{src:s(634)},{src:s(117)}],drawers:[{title:"QQ 646425671",icon:"mdi-view-dashboard"}]})},x=f,w=(0,h.Z)(x,b,g,!1,null,"65bdf67c",null),y=w.exports,k=function(){var t=this,e=t._self._c;return e("v-footer",{attrs:{dark:"",padless:""}},[e("v-card",{staticClass:"flex",attrs:{flat:"",tile:""}},[e("v-card-text",{staticClass:"py-2 white--text text-center"},[t._v(" "+t._s((new Date).getFullYear())+" — "),e("strong",[t._v("TECH SERVICE JOB")])])],1),e("v-bottom-sheet",{model:{value:t.sheet,callback:function(e){t.sheet=e},expression:"sheet"}},[e("v-sheet",{staticClass:"text-center",attrs:{height:"200px"}},[e("v-btn",{staticClass:"mt-6",attrs:{text:"",color:"red"},on:{click:function(e){t.sheet=!t.sheet}}},[t._v(" close ")]),e("div",{staticClass:"py-3"},[t._v(" This is a bottom sheet using the controlled by v-model instead of activator ")])],1)],1),e("v-navigation-drawer",{attrs:{absolute:"",temporary:""},model:{value:t.drawer,callback:function(e){t.drawer=e},expression:"drawer"}},[e("v-list-item",[e("v-list-item-avatar",[e("v-img",{attrs:{src:"https://randomuser.me/api/portraits/men/78.jpg"}})],1),e("v-list-item-content",[e("v-list-item-title",[t._v("John Leider")])],1)],1),e("v-divider"),e("v-list",{attrs:{dense:""}},t._l(t.drawers,(function(s){return e("v-list-item",{key:s.title,attrs:{link:""}},[e("v-list-item-icon",[e("v-icon",[t._v(t._s(s.icon))])],1),e("v-list-item-content",[e("v-list-item-title",[t._v(t._s(s.title))])],1)],1)})),1)],1)],1)},_=[],j={name:"IndexFooter",data:()=>({drawer:!1,sheet:!1,icons:["mdi-qq","mdi-twitter","mdi-linkedin","mdi-instagram"],drawers:[{title:"Home",icon:"mdi-view-dashboard"},{title:"About",icon:"mdi-forum"}]})},C=j,S=(0,h.Z)(C,k,_,!1,null,"8c047f8e",null),D=S.exports,J={name:"index",components:{IndexFooter:D,IndexHome:v,IndexBar:y},watch:{model(t){this.tab=null!=t?0:null}},data:()=>({model:null,search:null,tab:null,positionY1:0,Y1:0,ratio:.5,backSrc:s(716),collapseOnScroll:!0})},I=J,O=(0,h.Z)(I,m,p,!1,null,"be8a1014",null),K=O.exports,L=function(){var t=this,e=t._self._c;return e("v-app",[e("v-carousel",t._l(t.items,(function(t,s){return e("v-carousel-item",{key:s,attrs:{src:t.src,"reverse-transition":"fade-transition",transition:"fade-transition"}})})),1),e("v-card",{staticClass:"mx-auto",attrs:{"max-width":"344"}},[e("v-img",{attrs:{src:"https://cdn.vuetifyjs.com/images/cards/sunshine.jpg",height:"200px"}}),e("v-card-title",[t._v(" Top western road trips ")]),e("v-card-subtitle",[t._v(" 1,000 miles of wonder ")]),e("v-card-actions",[e("v-btn",{attrs:{color:"orange lighten-2",text:""}},[t._v(" Explore ")]),e("v-spacer"),e("v-btn",{attrs:{icon:""},on:{click:function(e){t.show=!t.show}}},[e("v-icon",[t._v(t._s(t.show?"mdi-chevron-up":"mdi-chevron-down"))])],1)],1),e("v-expand-transition",[e("div",{directives:[{name:"show",rawName:"v-show",value:t.show,expression:"show"}]},[e("v-divider"),e("v-card-text",[t._v(" I'm a thing. But, like most politicians, he promised more than he could deliver. You won't have time for sleeping, soldier, not with all the bed making you'll be doing. Then we'll go with that data file! Hey, you add a one and two zeros to that or we walk! You're going to do his laundry? I've got to find a way to escape. ")])],1)])],1),e("v-card",{staticClass:"mx-auto",attrs:{"max-width":"344"}},[e("v-img",{attrs:{src:"https://cdn.vuetifyjs.com/images/cards/sunshine.jpg",height:"200px"}}),e("v-card-title",[t._v(" Top western road trips ")]),e("v-card-subtitle",[t._v(" 1,000 miles of wonder ")]),e("v-card-actions",[e("v-btn",{attrs:{color:"orange lighten-2",text:""}},[t._v(" Explore ")]),e("v-spacer"),e("v-btn",{attrs:{icon:""},on:{click:function(e){t.show=!t.show}}},[e("v-icon",[t._v(t._s(t.show?"mdi-chevron-up":"mdi-chevron-down"))])],1)],1),e("v-expand-transition",[e("div",{directives:[{name:"show",rawName:"v-show",value:t.show,expression:"show"}]},[e("v-divider"),e("v-card-text",[t._v(" I'm a thing. But, like most politicians, he promised more than he could deliver. You won't have time for sleeping, soldier, not with all the bed making you'll be doing. Then we'll go with that data file! Hey, you add a one and two zeros to that or we walk! You're going to do his laundry? I've got to find a way to escape. ")])],1)])],1)],1)},$=[],N={data(){return{show:!1,items:[{src:"https://cdn.vuetifyjs.com/images/carousel/squirrel.jpg"},{src:"https://cdn.vuetifyjs.com/images/carousel/sky.jpg"},{src:"https://cdn.vuetifyjs.com/images/carousel/bird.jpg"},{src:"https://cdn.vuetifyjs.com/images/carousel/planet.jpg"}]}}},P=N,T=(0,h.Z)(P,L,$,!1,null,null,null),E=T.exports,A=function(){var t=this,e=t._self._c;return e("div",[[e("v-card",{staticClass:"mx-auto",attrs:{elevation:"16","max-width":"1700",height:"150"}},[e("v-hover",{scopedSlots:t._u([{key:"default",fn:function({hover:s}){return[e("div",{staticClass:"mx-auto pa-6 transition-swing pt-4 blue lighten-5",class:"elevation-"+(s?24:6)},[e("v-snackbar",{attrs:{timeout:1e3,value:!0,bottom:"",color:t.state,outlined:"",absolute:"",top:""},model:{value:t.snackbar,callback:function(e){t.snackbar=e},expression:"snackbar"}},[t._v(" "+t._s(t.text)+" ")]),e("v-card-title",[t._v(" 任务管理 "),e("v-spacer")],1),e("v-card-text",[e("v-col",{attrs:{cols:"12",sm:"10"}},[e("v-tooltip",{attrs:{bottom:""},scopedSlots:t._u([{key:"activator",fn:function({on:s,attrs:a}){return[e("v-btn",{attrs:{icon:""},on:{click:function(e){return t.searchJobs(1)}}},[e("v-icon",t._g(t._b({attrs:{dark:"",color:"green"}},"v-icon",a,!1),s),[t._v(" mdi-cached ")])],1)]}}],null,!0)},[e("span",[t._v("刷新")])]),e("v-divider",{staticClass:"mx-4",attrs:{vertical:""}}),e("v-dialog",{attrs:{persistent:"","max-width":"500px"},scopedSlots:t._u([{key:"activator",fn:function({on:s,attrs:a}){return[e("v-btn",t._g(t._b({attrs:{color:"primary",dark:""}},"v-btn",a,!1),s),[t._v(" 新建任务 ")])]}}],null,!0),model:{value:t.dialog,callback:function(e){t.dialog=e},expression:"dialog"}},[e("v-card",[e("v-snackbar",{attrs:{timeout:1e3,value:!0,bottom:"",color:t.state,outlined:"",absolute:"",top:""},model:{value:t.addSnackbar,callback:function(e){t.addSnackbar=e},expression:"addSnackbar"}},[t._v(" "+t._s(t.text)+" ")]),e("v-card-title",[e("span",{staticClass:"text-h5"},[t._v("新建任务")])]),e("v-card-text",[e("v-container",[e("v-form",{ref:"form",attrs:{"lazy-validation":""},model:{value:t.valid,callback:function(e){t.valid=e},expression:"valid"}},[e("v-text-field",{attrs:{counter:10,maxlength:"10",rules:t.jobNameRules,label:"任务名称*",required:""},model:{value:t.jobName,callback:function(e){t.jobName=e},expression:"jobName"}}),e("v-text-field",{attrs:{counter:20,maxlength:"20",rules:t.jobKeyRules,label:"任务KEY*",hint:"对应@TsJob注解中的参数",required:""},model:{value:t.jobKey,callback:function(e){t.jobKey=e},expression:"jobKey"}}),e("v-text-field",{attrs:{counter:20,rules:t.jobDescRules,label:"任务描述"},model:{value:t.jobDesc,callback:function(e){t.jobDesc=e},expression:"jobDesc"}}),e("v-text-field",{attrs:{label:"CRON*",hint:"0/59 * * * * ?",rules:t.jobCronRules,required:""},model:{value:t.jobCron,callback:function(e){t.jobCron=e},expression:"jobCron"}})],1)],1),e("small",[t._v("*号为必填项")])],1),e("v-card-actions",[e("v-spacer"),e("v-btn",{attrs:{color:"blue darken-1",text:""},on:{click:t.closeDialog}},[t._v(" 关闭 ")]),e("v-btn",{staticClass:"mr-4",attrs:{color:"blue darken-1",text:"",disabled:!t.valid},on:{click:t.validate}},[t._v(" 保存 ")])],1)],1)],1),e("v-divider",{staticClass:"mx-4",attrs:{vertical:""}})],1)],1)],1)]}}])})],1),e("v-card",{staticClass:"mx-auto",staticStyle:{"margin-top":"15px"},attrs:{elevation:"16","max-width":"1700",height:"660"}},[e("v-data-table",{staticClass:"elevation-1",attrs:{elevation:"55",height:"600",headers:t.headers,items:t.desserts,search:t.search,"sort-by":"calories","single-select":t.singleSelect,"items-per-page":t.itemsPerPage,page:t.page,"item-key":"id","show-select":"","hide-default-footer":"",loading:t.loading,"loading-text":"Loading... Please wait"},on:{"update:page":function(e){t.page=e}},scopedSlots:t._u([{key:"top",fn:function(){return[e("v-toolbar",{attrs:{flat:""}},[e("v-dialog",{attrs:{"max-width":"300px"},model:{value:t.dialogDelete,callback:function(e){t.dialogDelete=e},expression:"dialogDelete"}},[e("v-card",[e("v-card-title",{staticClass:"text-h5"},[t._v("确定要删除此数据?")]),e("v-card-actions",[e("v-spacer"),e("v-btn",{attrs:{color:"blue darken-1",text:""},on:{click:t.closeDelete}},[t._v("取消")]),e("v-btn",{attrs:{color:"blue darken-1",text:""},on:{click:t.deleteItemConfirm}},[t._v("确定")]),e("v-spacer")],1)],1)],1)],1)]},proxy:!0},{key:"item.actions",fn:function({item:s}){return[e("v-icon",{staticClass:"mr-2",attrs:{small:""},on:{click:function(e){return t.runJob(s)}}},[t._v(" mdi-play ")]),e("v-icon",{staticClass:"mr-2",attrs:{small:""},on:{click:function(e){return t.startJob(s)}}},[t._v(" mdi-timer ")]),e("v-icon",{staticClass:"mr-2",attrs:{small:""},on:{click:function(e){return t.stopJob(s)}}},[t._v(" mdi-lock ")]),e("v-icon",{staticClass:"mr-2",attrs:{small:""},on:{click:function(e){return t.editItem(s)}}},[t._v(" mdi-pencil ")]),e("v-icon",{attrs:{small:""},on:{click:function(e){return t.deleteItem(s)}}},[t._v(" mdi-delete ")])]}}]),model:{value:t.selected,callback:function(e){t.selected=e},expression:"selected"}}),e("v-divider"),e("div",{staticClass:"text-center pt-2",staticStyle:{"margin-top":"-65px"}},[e("v-pagination",{attrs:{length:t.pageCount,circle:""},on:{input:t.searchJobs},model:{value:t.page,callback:function(e){t.page=e},expression:"page"}})],1),e("v-dialog",{attrs:{persistent:"","max-width":"500px"},model:{value:t.editDialog,callback:function(e){t.editDialog=e},expression:"editDialog"}},[e("v-card",[e("v-snackbar",{attrs:{timeout:1e3,value:!0,bottom:"",color:t.state,outlined:"",absolute:"",top:""},model:{value:t.editSnackbar,callback:function(e){t.editSnackbar=e},expression:"editSnackbar"}},[t._v(" "+t._s(t.text)+" ")]),e("v-card-title",[e("span",{staticClass:"text-h5"},[t._v("修改调度")])]),e("v-card-text",[e("v-container",[e("v-form",{ref:"editForm",attrs:{"lazy-validation":""},model:{value:t.valid,callback:function(e){t.valid=e},expression:"valid"}},[e("v-text-field",{attrs:{counter:10,maxlength:"10",rules:t.jobNameRules,label:"任务名称*",required:""},model:{value:t.editJob.jobName,callback:function(e){t.$set(t.editJob,"jobName",e)},expression:"editJob.jobName"}}),e("v-text-field",{attrs:{rules:t.jobKeyRules,label:"任务KEY*",hint:"对应@TsJob注解中的参数",maxlength:"20",required:""},model:{value:t.editJob.jobKey,callback:function(e){t.$set(t.editJob,"jobKey",e)},expression:"editJob.jobKey"}}),e("v-text-field",{attrs:{counter:20,rules:t.jobDescRules,label:"任务描述"},model:{value:t.editJob.jobDesc,callback:function(e){t.$set(t.editJob,"jobDesc",e)},expression:"editJob.jobDesc"}}),e("v-text-field",{attrs:{label:"CRON*",hint:"0/59 * * * * ?",rules:t.jobCronRules,required:""},model:{value:t.editJob.jobCron,callback:function(e){t.$set(t.editJob,"jobCron",e)},expression:"editJob.jobCron"}})],1)],1),e("small",[t._v("*号为必填项")])],1),e("v-card-actions",[e("v-spacer"),e("v-btn",{attrs:{color:"blue darken-1",text:""},on:{click:t.closeEditDialog}},[t._v(" 关闭 ")]),e("v-btn",{staticClass:"mr-4",attrs:{color:"blue darken-1",text:"",disabled:!t.valid},on:{click:t.editValidate}},[t._v(" 保存 ")])],1)],1)],1)],1)]],2)},R=[],B={name:"Job",data(){return{dialogDelete:!1,loading:!1,showPwd:!1,showCheckPwd:!1,phone:"",jobCron:"",jobName:"",jobKey:"",jobDesc:"",valid:!0,jobNameRules:[t=>!!t||"任务名称不能为空",t=>t&&t.length<=10||"任务名称必须小于10个字符"],jobKeyRules:[t=>!!t||"任务KEY不能为空",t=>t&&t.length<=20||"任务KEY必须小于20个字符"],jobDescRules:[t=>!!t||"密码不能为空",t=>t&&t.length>=0&&t.length<=255||"密码长度必须为10~20字符之间"],jobCronRules:[t=>!!t||"CRON不能为空"],phoneRules:[t=>!!t||"手机号不能为空",t=>t&&11===t.length||"手机号必须为11位"],dialog:!1,editDialog:!1,addSnackbar:!1,editSnackbar:!1,snackbar:!1,state:"success",text:"查询失败",page:1,pageCount:0,itemsPerPage:10,selected:[],singleSelect:!0,search:"",headers:[{text:"ID",align:"start",sortable:!1,value:"id",hidden:!0},{text:"任务名称",value:"jobName"},{text:"任务KEY",value:"jobKey"},{text:"任务描述",value:"jobDes"},{text:"cron",value:"cron"},{text:"任务状态",value:"version"},{text:"操作",value:"actions",sortable:!1}],desserts:[],editedIndex:-1,editJob:{id:"",jobName:"",jobDes:"",jobKey:"",cron:""}}},watch:{dialog(t){t||this.close()},dialogDelete(t){t||this.closeDelete()}},methods:{getContextPath(){const t=document.location.pathname,e=t.substr(1).indexOf("/"),s=t.substr(0,e+1);return console.log(s),"/ts-job"===s?"":s},closeDialog(){this.dialog=!1,this.$refs.form.reset()},closeEditDialog(){this.editDialog=!1,this.$refs.editForm.reset()},editValidate(){const t=this.$refs.editForm.validate();t&&this.$axios.post(this.getContextPath()+"/ts-job/edit-job",{id:this.editJob.id,jobName:this.editJob.jobName,jobDes:this.editJob.jobDesc,jobKey:this.editJob.jobKey,cron:this.editJob.jobCron}).then((t=>{const e=t.data.code;200===e?(this.text="修改成功",this.state="success",this.editDialog=!1,this.state="success",this.searchJobs(this.page)):(this.text="修改失败",this.text=t.data.msg,this.state="error"),this.editSnackbar=!0})).catch((t=>{console.log(t),this.text="请求失败",this.editSnackbar=!0,this.state="error"}))},validate(){const t=this.$refs.form.validate();t&&this.$axios.post(this.getContextPath()+"/ts-job/add-job",{jobName:this.jobName,jobDes:this.jobDesc,jobKey:this.jobKey,cron:this.jobCron}).then((t=>{const e=t.data.code;console.log(e),200===e?(this.text="新增成功",this.dialog=!1,this.$refs.form.reset(),this.state="success",this.searchJobs(this.page)):(this.text="新增失败",this.text=t.data.msg,this.state="error"),this.addSnackbar=!0})).catch((t=>{console.log(t),this.text="请求失败",this.addSnackbar=!0,this.state="error"}))},searchJobs(t){this.loading=!0,this.page=t,this.$axios.post(this.getContextPath()+"/ts-job/search-job",{pageIndex:t,pageSize:this.itemsPerPage}).then((t=>{this.snackbar=!0,this.loading=!1,this.text="查询成功",this.state="success",this.desserts=t.data.data.result,this.pageCount=t.data.data.total})).catch((t=>{this.snackbar=!0,this.loading=!1,this.desserts=[],this.text="查询失败",this.state="error",alert(t.response.data.msg)}))},editItem(t){this.editedIndex=this.desserts.indexOf(t),this.editJob.id=t.id,this.editJob.jobName=t.jobName,this.editJob.jobKey=t.jobKey,this.editJob.jobDesc=t.jobDes,this.editJob.jobCron=t.cron,this.editDialog=!0},runJob(t){this.$axios.get(this.getContextPath()+"/ts-job/run-job?jobKey="+t.jobKey).then((t=>{const e=t.data.code;200===e?(this.snackbar=!0,this.loading=!1,this.text="执行成功",this.state="success"):(this.snackbar=!0,this.loading=!1,this.text=t.data.msg,this.state="error")})).catch((()=>{this.snackbar=!0,this.loading=!1,this.desserts=[],this.text="执行失败",this.state="error"}))},startJob(t){this.$axios.get(this.getContextPath()+"/ts-job/start-scheduler?jobKey="+t.jobKey).then((t=>{const e=t.data.code;200===e?(this.snackbar=!0,this.loading=!1,this.text="执行成功",this.state="success",this.searchJobs(this.page)):(this.snackbar=!0,this.loading=!1,this.text=t.data.msg,this.state="error")})).catch((()=>{this.snackbar=!0,this.loading=!1,this.desserts=[],this.text="执行失败",this.state="error"}))},stopJob(t){this.$axios.get(this.getContextPath()+"/ts-job/stop-scheduler?jobKey="+t.jobKey).then((t=>{const e=t.data.code;200===e?(this.snackbar=!0,this.loading=!1,this.text="执行成功",this.state="success",this.searchJobs(this.page)):(this.snackbar=!0,this.loading=!1,this.text=t.data.msg,this.state="error")})).catch((()=>{this.snackbar=!0,this.loading=!1,this.desserts=[],this.text="执行失败",this.state="error"}))},deleteItem(t){this.editedIndex=this.desserts.indexOf(t),this.dialogDelete=!0},deleteItemConfirm(){let t=this.desserts[this.editedIndex].id;this.$axios.get(this.getContextPath()+"/ts-job/del-job?id="+t).then((()=>{this.searchJobs(this.page)})),this.text="删除成功",this.snackbar=!0,this.closeDelete()},close(){this.dialog=!1,this.$nextTick((()=>{this.editedIndex=-1}))},closeDelete(){this.dialogDelete=!1}},mounted(){this.loading=!0,this.$axios.post(this.getContextPath()+"/ts-job/search-job",{pageIndex:1,pageSize:10}).then((t=>{this.snackbar=!0,this.loading=!1,this.text="查询成功",this.state="success",this.desserts=t.data.data.result,this.pageCount=t.data.data.total})).catch((t=>{this.snackbar=!0,this.loading=!1,this.desserts=[],this.text="查询失败",this.state="error",alert(t.response.data.msg)}))}},Z=B,z=(0,h.Z)(Z,A,R,!1,null,"03ae23b0",null),Y=z.exports,H=function(){var t=this,e=t._self._c;return e("div",[e("div",[e("v-responsive",{staticClass:"mx-auto mb-4",attrs:{"max-width":"700"}}),e("v-card",{staticClass:"mx-auto",attrs:{elevation:"16","max-width":"1700"}},[e("v-hover",{scopedSlots:t._u([{key:"default",fn:function({hover:s}){return[e("div",{staticClass:"mx-auto pa-6 transition-swing pt-4 orange lighten-4 white--text",class:"elevation-"+(s?24:6)},[e("v-card-title",[t._v(" tech service job log "),e("v-spacer"),e("v-tooltip",{attrs:{bottom:""},scopedSlots:t._u([{key:"activator",fn:function({on:s,attrs:a}){return[e("v-btn",{attrs:{icon:""},on:{click:t.reloadData}},[e("v-icon",t._g(t._b({attrs:{color:"green"}},"v-icon",a,!1),s),[t._v(" mdi-cached ")])],1)]}}],null,!0)},[e("span",[t._v("刷新")])]),e("v-tooltip",{attrs:{bottom:""},scopedSlots:t._u([{key:"activator",fn:function({on:s,attrs:a}){return[e("v-btn",{attrs:{icon:""},on:{click:t.clearData}},[e("v-icon",t._g(t._b({attrs:{color:"red"}},"v-icon",a,!1),s),[t._v(" mdi-delete ")])],1)]}}],null,!0)},[e("span",[t._v("清空")])])],1),e("v-card-text",[t._v(" TS-JOB执行日志 ")])],1)]}}])}),e("v-divider")],1),e("v-card",{staticClass:"mx-auto",staticStyle:{"margin-top":"15px"},attrs:{elevation:"16","max-width":"1700",height:"620"}},[e("v-virtual-scroll",{staticStyle:{"margin-top":"10px"},attrs:{items:t.items,height:"610","item-height":"64"},on:{scroll:function(e){return t.scrollGet(e)}},scopedSlots:t._u([{key:"default",fn:function({item:s}){return[e("v-list-item",[e("v-list-item-title",{attrs:{hidden:""}},[t._v(t._s(s.id))]),e("v-list-item-avatar",{attrs:{width:"120"}},[e("v-avatar",{attrs:{color:"blue lighten-4",rounded:"",size:"27"}},[t._v(" "+t._s(s.logLevel)+" ")])],1),e("v-list-item-content",[e("v-list-item-title",[t._v(t._s(s.method))])],1),e("v-list-item-content",[e("v-list-item-title",[t._v(t._s(s.desc))])],1),e("v-list-item-content",[e("v-list-item-title",[t._v(t._s(s.address))])],1),e("v-list-item-content",[e("v-list-item-title",[t._v(t._s(s.time))])],1),e("v-list-item-content",[e("v-list-item-title",[t._v(t._s(s.user))])],1),e("v-list-item-action",[e("v-btn",{staticClass:"ma-2",attrs:{color:"red",dark:""},on:{click:function(e){return t.deleteLog(s.id)}}},[t._v(" 删除 "),e("v-icon",{attrs:{dark:"",right:""}},[t._v(" mdi-cancel ")])],1)],1)],1),e("v-divider")]}}])})],1)],1)])},M=[],q={data(){return{items:[],page:1}},mounted(){this.initData(1)},methods:{reloadData(){this.initData(1)},clearData(){this.$axios.delete("maintain/api/system/clearLog/sys_log").then((()=>{this.initData(1)})).catch((t=>{this.desserts=[],alert(t.response.data.msg)}))},initData(t){this.$axios.post("maintain/api/system/getLogs",{pageIndex:t,pageSize:20}).then((t=>{this.items=t.data.data.result})).catch((t=>{this.desserts=[],alert(t.response.data.msg)}))},deleteLog(t){this.$axios.delete("maintain/api/system/delLog/"+t).then((()=>{this.initData(1)})).catch((t=>{this.desserts=[],alert(t.response.data.msg)}))},scrollGet(t){t.target.scrollTop+t.target.offsetHeight>t.target.scrollHeight-100&&(this.page=this.page+1,this.$axios.post("maintain/api/system/getLogs",{pageIndex:this.page,pageSize:20}).then((t=>{this.items=[...this.items,...t.data.data.result]})).catch((t=>{this.desserts=[],alert(t.response.data.msg)})))}}},F=q,V=(0,h.Z)(F,H,M,!1,null,"dae3f87e",null),G=V.exports;const Q=[{path:"/main",name:"main",redirect:"/main/home",component:K,props:!0,children:[{path:"home",component:v},{path:"about",component:E},{path:"list",component:Y},{path:"log",component:G}]},{path:"/",component:K,redirect:"/main/home"},{path:"/about",name:"about",component:E}];var W=Q,U=function(){var t=this,e=t._self._c;return e("transition",{attrs:{name:"slide-fade"}},[e("router-view")],1)},X=[],tt={},et=tt,st=(0,h.Z)(et,U,X,!1,null,"4477e461",null),at=st.exports,it=s(464),ot=s.n(it);s(556),s(939);a["default"].prototype.$echarts=r,a["default"].use(i.ZP),a["default"].use(ot()),a["default"].config.productionTip=!1,a["default"].prototype.$axios=l.Z;const nt=new i.ZP({mode:"hash",routes:W}),rt={};new(ot())(rt);new a["default"]({router:nt,vuetify:new(ot()),render:t=>t(at)}).$mount("#app")},716:function(t,e,s){t.exports=s.p+"img/back.78b6a6b0.jpeg"},290:function(t,e,s){t.exports=s.p+"img/p1.686fdba1.jpeg"},69:function(t,e,s){t.exports=s.p+"img/p2.1b4467e7.jpeg"},673:function(t,e,s){t.exports=s.p+"img/p26.ad2c06aa.jpg"},634:function(t,e,s){t.exports=s.p+"img/p3.044fe3bf.jpeg"},117:function(t,e,s){t.exports=s.p+"img/p4.dd074d15.jpeg"}},e={};function s(a){var i=e[a];if(void 0!==i)return i.exports;var o=e[a]={exports:{}};return t[a].call(o.exports,o,o.exports,s),o.exports}s.m=t,function(){var t=[];s.O=function(e,a,i,o){if(!a){var n=1/0;for(d=0;d<t.length;d++){a=t[d][0],i=t[d][1],o=t[d][2];for(var r=!0,l=0;l<a.length;l++)(!1&o||n>=o)&&Object.keys(s.O).every((function(t){return s.O[t](a[l])}))?a.splice(l--,1):(r=!1,o<n&&(n=o));if(r){t.splice(d--,1);var c=i();void 0!==c&&(e=c)}}return e}o=o||0;for(var d=t.length;d>0&&t[d-1][2]>o;d--)t[d]=t[d-1];t[d]=[a,i,o]}}(),function(){s.n=function(t){var e=t&&t.__esModule?function(){return t["default"]}:function(){return t};return s.d(e,{a:e}),e}}(),function(){s.d=function(t,e){for(var a in e)s.o(e,a)&&!s.o(t,a)&&Object.defineProperty(t,a,{enumerable:!0,get:e[a]})}}(),function(){s.g=function(){if("object"===typeof globalThis)return globalThis;try{return this||new Function("return this")()}catch(t){if("object"===typeof window)return window}}()}(),function(){s.o=function(t,e){return Object.prototype.hasOwnProperty.call(t,e)}}(),function(){s.r=function(t){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(t,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(t,"__esModule",{value:!0})}}(),function(){s.p=""}(),function(){var t={143:0};s.O.j=function(e){return 0===t[e]};var e=function(e,a){var i,o,n=a[0],r=a[1],l=a[2],c=0;if(n.some((function(e){return 0!==t[e]}))){for(i in r)s.o(r,i)&&(s.m[i]=r[i]);if(l)var d=l(s)}for(e&&e(a);c<n.length;c++)o=n[c],s.o(t,o)&&t[o]&&t[o][0](),t[o]=0;return s.O(d)},a=self["webpackChunkts_job"]=self["webpackChunkts_job"]||[];a.forEach(e.bind(null,0)),a.push=e.bind(null,a.push.bind(a))}();var a=s.O(void 0,[998],(function(){return s(694)}));a=s.O(a)})();
//# sourceMappingURL=app.0c2f35d9.js.map