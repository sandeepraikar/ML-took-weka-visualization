<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Weka Clustering</title>
<!-- Latest compiled and minified CSS -->

<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">

</head>
<body>


	<div>

		<fieldset>
			<span id="successMsg" class="alert-success"></span> <span id="errMsg"
				class="alert-danger"></span>
			<form id="idUploadDataSet" enctype="multipart/form-data" action="uploadImpl" method="post">
				<div class="form-group row">
					<label class="col-md-2" for="dataSet">Upload CSV file<em
						style="color: red;">*</em></label>
					<div class="col-md-6">
						<input id="dataSetFile" type="file" name="dataSet"
							class="form-control" />
					</div>

					<div class="col-md-2">
						<input type="submit" id="btnUploadDataSet" value="Upload"
							class="form-control" />
					</div>
				</div>
			</form>
		</fieldset>
	</div>

<script type="text/javascript">

	$("#errMsg").text(""); 		
	$("#errMsg").hide();  

	$("#btnUploadDataSet").click(function() {  
		var form = new FormData(document.getElementById('idUploadDataSet'));

		uploadedFileName = $("#dataSetFile").val().replace(/C:\\fakepath\\/i,'');
		

		if (!(uploadedFileName.substring(uploadedFileName.lastIndexOf(".") + 1) == "csv")) {
			$("#errMsg")
					.append(
							"<span>Please upload .csv file</span><br>");
			$("#errMsg").show();
			return false;
		}

		if(uploadedFileName.length>0){
			$("#idUploadDataSet").submit();
		}
	});

</script>
</body>
</html>