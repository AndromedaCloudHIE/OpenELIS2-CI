<%@ page language="java"
	contentType="text/html; charset=UTF-8"
	import="org.apache.commons.validator.GenericValidator, 
			org.openelisglobal.common.action.IActionConstants,
			org.openelisglobal.sample.util.AccessionNumberUtil,
			org.openelisglobal.common.util.IdValuePair,
			org.openelisglobal.internationalization.MessageUtil,
			org.openelisglobal.common.util.Versioning,
			org.openelisglobal.typeoftestresult.service.TypeOfTestResultServiceImpl.ResultType,
		    java.text.DecimalFormat,
			java.util.List,
			org.openelisglobal.resultvalidation.bean.AnalysisItem,
			org.openelisglobal.common.util.ConfigurationProperties,
			org.openelisglobal.common.util.ConfigurationProperties.Property,
			org.owasp.encoder.Encode" %>
			
<%@ page isELIgnored="false" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ taglib prefix="ajax" uri="/tags/ajaxtags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>


<c:set var="testSection"	value='${form.testSection}' />
<c:set var="results" value="${form.resultList}" />
<c:set var="pagingSearch" value='${form.paging.searchTermToPage}'/>
<c:set var="testSectionsByName" value='${form.testSectionsByName}' />
<c:set var="resultCount" value="${fn:length(results)}" />
<c:set var="rowColorIndex" value="${2}" />

<script type="text/javascript" src="scripts/OEPaging.js?"></script>

<%
	int rowColorIndex = 2;
	String searchTerm = request.getParameter("searchTerm");
	String url = request.getAttribute("javax.servlet.forward.servlet_path").toString();	
	//boolean showTestSectionSelect = !ConfigurationProperties.getInstance().isPropertyValueEqual(Property.configurationName, "CI RetroCI");
%>

<script>

function submitTestSectionSelect( element ) {
	
	var testSectionNameIdHash = [];

	<c:forEach items="${testSectionsByName}" var="testSection">
		testSectionNameIdHash["${testSection.id}"] = "${testSection.value}";
	</c:forEach>
		window.location.href = "ResultValidation.do?testSectionId=" + element.value + "&test=&type=" + testSectionNameIdHash[element.value];
	
}

function validateEntrySize( elementValue ){
	$("retrieveTestsID").disabled = (elementValue.length == 0);
}

function doShowTests(){
	var form = document.getElementById("mainForm");
	window.location.href = "AccessionValidationRange.do?accessionNumber="  + $("searchAccessionID").value;
}

function /*void*/ handleEnterEvent(  ){
	if( $("searchAccessionID").value.length > 0){
		doShowTests();
	}
	return false;
}

</script>

<% if( !(url.contains("RetroC"))){ %>
<div id="searchDiv" class="colorFill"  >
<div id="PatientPage" class="colorFill" style="display:inline" >
<h2><spring:message code="sample.entry.search"/></h2>
	<table width="50%">
		<tr >
		<td width="50%" align="right" >
			<%=MessageUtil.getContextualMessage("quick.entry.accession.range")%>
		</td>
		<td width="50%">
			<input name="accessionNumber"
			       size="20"
			       id="searchAccessionID"
			       maxlength="<%=Integer.toString(AccessionNumberUtil.getMaxAccessionLength())%>"
			       onkeyup="validateEntrySize( this.value );"
			       onblur="validateEntrySize( this.value );"
			       class="text"
			       type="text">
			<spring:message code="sample.search.scanner.instructions"/>
		</td>
	</tr>
		
	</table>
	<br/>
	
	<button type="button" name="retrieveTestsButton" id="retrieveTestsID"  onclick="doShowTests();" disabled="disabled" >
		<%= MessageUtil.getContextualMessage("validationentry.accession.range") %>
	</button>
	
	<h1>
		
	</h1>
</div>
</div>
<% }%>