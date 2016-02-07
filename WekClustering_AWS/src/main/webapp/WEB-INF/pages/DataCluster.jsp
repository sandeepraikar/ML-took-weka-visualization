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
<title>Clustering</title>
<!-- Latest compiled and minified CSS -->

<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
<style>
body {
  font: 11px sans-serif;
}

.axis path,
.axis line {
  fill: none;
  stroke: #000;
  shape-rendering: crispEdges;
}

.dot {
  stroke: #000;
}

.tooltip {
  position: absolute;
  width: 200px;
  height: 28px;
  pointer-events: none;
}

</style>
</head>
<body>
	<div>

		<fieldset>
			<span id="successMsg" class="alert-success"></span> <span id="errMsg"
				class="alert-danger"></span>
			
			
			<div class="form-group row">

				<label class=" col-md-2" for="description">Please enter the no. of Clusters<em
					style="color: red;">*</em></label>
				<div class="col-md-5">
					<input id="txtClusterSize" name="clusterSize" class="form-control" maxlength="2"
						type="text" />
				</div>
				
				
				<div class="col-md-2">
					<input type="submit" id="btnCreateCluster" value="Create Cluster"
						class="form-control" />
				</div>
			</div>	
			

				<div id="selectAttribute">
				<label class=" col-md-2" for="itemName">Please select 2 attributes</label>
				<div class="readonly-text col-md-2">
					<select name="attributes" multiple="multiple"
						id="clusterAttributes" size="6">
						<c:forEach items="${attributeList}" var="attribute">
							<option value="${attribute}">${attribute}</option>
						</c:forEach>
					</select>
				</div>
				<div class="col-md-2">
					<input type="submit" id="btnVisualizeCluster" value="Visualize Cluster"
						class="form-control" />
				</div>
				
				</div>
		</fieldset>
</div>
<script src="http://d3js.org/d3.v3.min.js"></script>
<script type="text/javascript">

$("#errMsg").text(""); 		
$("#errMsg").hide();  

var arffFileName ='${fileName}';
$('#btnVisualizeCluster').attr("disabled", "disabled");
var dataSetFile;

$("#txtClusterSize").keypress(function (e) {
 //if the letter is not digit then display error and don't type anything
 if (e.which != 8 && e.which != 0 && e.which != 46 && (e.which < 48 || e.which > 57)) {
    //display error message
    $("#errmsg").html("Digits Only").show().fadeOut("slow");
    return false;
}
});


$("#btnCreateCluster").click(function() {
	
	$.ajax({
  		type: "post",
  		url: "createClusterImpl",
  		data: { numOfClusters : $("#txtClusterSize").val(), arffFileName :arffFileName }
  	}).done(function( data ) {
  		alert("Cluster created with KMeans Algorithm succussfully!!");
  		dataSetFile=data;
  		
		//$('#btnCreateCluster').attr("disabled", "disabled");
		alert("Please select the attributes to plot the graph!");
		$('#btnVisualizeCluster').removeAttr('disabled');
  	});
	
});

$("#btnVisualizeCluster").click(function() {
	
	var clusterList = $("#clusterAttributes").val();
	if(clusterList.length>=1 && clusterList.length<3){
		visualizeCluster(dataSetFile,clusterList[0],clusterList[1]);	
	}else{
		alert("Please select 2 attributes!");	
	}
});
function visualizeCluster(dataSetFile,xPlot,yPlot){
	var margin = {top: 175, right: 20, bottom: 30, left: 70},
    width = 960 - margin.left - margin.right,
    height = 400 - margin.top - margin.bottom;

/* 
 * value accessor - returns the value to encode for a given data object.
 * scale - maps value to a visual display encoding, such as a pixel position.
 * map function - maps from data value to display value
 * axis - sets up axis
 */ 

var xPlot_formatted;
var yPlot_formatted;
// setup x 
if (/\s/.test(xPlot)) {
	xPlot_formatted = "'"+xPlot+"'";    
}else{
	xPlot_formatted = xPlot;
}
if (/\s/.test(yPlot)) {
	
	yPlot_formatted = "'"+yPlot+"'";
}else{
	yPlot_formatted = yPlot;
}

var xValue = function(d) {return d[xPlot_formatted];}, // data -> value
    xScale = d3.scale.linear().range([0, width]), // value -> display
    xMap = function(d) { return xScale(xValue(d));}, // data -> display
    xAxis = d3.svg.axis().scale(xScale).orient("bottom");

    
// setup y
var yValue = function(d) {return d[yPlot];}, // data -> value
    yScale = d3.scale.linear().range([height, 0]), // value -> display
    yMap = function(d) { return yScale(yValue(d));}, // data -> display
    yAxis = d3.svg.axis().scale(yScale).orient("left");

// setup fill color
var cValue = function(d) { return d.Cluster;},
    color = d3.scale.category10();

// add the graph canvas to the body of the webpage
var svg = d3.select("body").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

// add the tooltip area to the webpage
var tooltip = d3.select("body").append("div")
    .attr("class", "tooltip")
    .style("opacity", 0);

//build URL

//var url ="http://localhost:8000/"+dataSetFile;

// load data
d3.csv(dataSetFile, function(error, data) {

  // change string (from CSV) into number format
  data.forEach(function(d) {
    d[xPlot_formatted] = +d[xPlot_formatted];
    d[yPlot_formatted] = +d[yPlot_formatted];
//    console.log(d);
  });

  // don't want dots overlapping axis, so add in buffer to data domain
  xScale.domain([d3.min(data, xValue)-1, d3.max(data, xValue)+1]);
  yScale.domain([d3.min(data, yValue)-1, d3.max(data, yValue)+1]);

  // x-axis
  svg.append("g")
      .attr("class", "x axis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis)
    .append("text")
      .attr("class", "label")
      .attr("x", width)
      .attr("y", -6)
      .style("text-anchor", "end")
      .text(xPlot_formatted);

  // y-axis
  svg.append("g")
      .attr("class", "y axis")
      .call(yAxis)
    .append("text")
      .attr("class", "label")
      .attr("transform", "rotate(-90)")
      .attr("y", 6)
      .attr("dy", ".71em")
      .style("text-anchor", "end")
      .text(yPlot_formatted);

  // draw dots
  svg.selectAll(".dot")
      .data(data)
    .enter().append("circle")
      .attr("class", "dot")
      .attr("r", 3.5)
      .attr("cx", xMap)
      .attr("cy", yMap)
      .style("fill", function(d) { return color(cValue(d));}) 
      .on("mouseover", function(d) {
          tooltip.transition()
               .duration(200)
               .style("opacity", .9);
          tooltip.html(d.Cluster + "<br/> (" + xValue(d) 
	        + ", " + yValue(d) + ")")
               .style("left", (d3.event.pageX + 5) + "px")
               .style("top", (d3.event.pageY - 28) + "px");
      })
      .on("mouseout", function(d) {
          tooltip.transition()
               .duration(500)
               .style("opacity", 0);
      });

  // draw legend
  var legend = svg.selectAll(".legend")
      .data(color.domain())
    .enter().append("g")
      .attr("class", "legend")
      .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

  // draw legend colored rectangles
  legend.append("rect")
      .attr("x", width - 18)
      .attr("width", 18)
      .attr("height", 18)
      .style("fill", color);

  // draw legend text
  legend.append("text")
      .attr("x", width - 24)
      .attr("y", 9)
      .attr("dy", ".35em")
      .style("text-anchor", "end")
      .text(function(d) { return d;})
});

}
	


</script>
</body>
</html>