/* 自定义trim */
function trim (str) {  //删除左右两端的空格,自定义的trim()方法
  return str == undefined ? "" : str.replace(/(^\s*)|(\s*$)/g, "")
}

//获取url地址上面的参数
function requestUrlParam(argname){
  var url = location.href//获取完整的请求路径
  var arrStr = url.substring(url.indexOf("?")+1).split("&")//从?号后开始进行分割,分割符为&,多个等式形成数组
  for(var i =0;i<arrStr.length;i++)
  {
      var loc = arrStr[i].indexOf(argname+"=")//查看id=是否存在,若非空,则将id=替换为空,最终就剩下了所想要的数据
      if(loc!=-1){
          return arrStr[i].replace(argname+"=","").replace("?","")
      }
  }
  return ""
}
