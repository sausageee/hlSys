<%@ page language="java" contentType="text/html; charset=UTF-8; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<%@ page import="org.hpin.common.util.StrUtils"%>
<script type="text/javascript" language="javascript">


 function downexcel(fileName){
	window.location = "${path}/resource/downLoadExcel.action?filename="+fileName;
  }
 
	$(function(){
		//导出加载等待问题;
		//py_ready(1); //进度条显示初始化;
	});

	/* function showTheMb() {
		var param = navTab.getCurrentPanel();
		var flag = getSessionExportInVal();
		if(flag){
			py_show();
			var iCount = setInterval("getProcess()",100); //设置每隔100毫秒循环执行getProcess();
			getExportInProcess(param, iCount);
			
		} else {
			alert("请您耐心等待上次导入,完成后才能开始本次导入功能!");
			return false;
		}
		return true;
		
	} */
 </script> 
 		
<!--蒙版 start-->
<!--必须要的部分-->
<div class="py_theMb"><!--蒙版-->
    <div class="py_bakpic"><!--图片-->
</div>
</div>
<!--必须要的部分-->
<!--蒙版 end-->

<div class="pageFormContent" id="import">
	  <!-- <div class="tip"><span><b>客户信息导入</b></span></div> -->
	   <div class="tip"><span><b>模板下载</b>&nbsp;&nbsp;&nbsp;&nbsp; <a href="#" onclick="downexcel('impCustomerApplication.xlsx')">EXCEL模板</a></span></div>
	  <form style="overflow: hidden;" method="post" action="${path }/warehouse/application!impCustomerExcel.action" class="pageForm required-validate" onsubmit="return iframeCallback(this,returnOnclickAjaxDone);" enctype="multipart/form-data">
		<input name="navTabId" type="hidden" value="${ navTabId }"/>
		<input name="applicationNo" type="hidden" value="${applicationNo}"/>
		<div class="tip"><span><b>客户信息导入</b>&nbsp;&nbsp;</span></div>
		<p>
		  <label>附件：</label>
		  <input type="file" class="required valid" id="affix" name="affix">
		</p>
		<div></div>
		<div class="formBar">
			<ul>
				<li><div class="buttonActive"><div class="buttonContent"><button type="submit" id="sc">上传</button></div></div></li>
			</ul>
		</div>
	  </form>
</div>