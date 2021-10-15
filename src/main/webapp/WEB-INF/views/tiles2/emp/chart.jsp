<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<script src="https://code.highcharts.com/highcharts.js"></script>
<script src="https://code.highcharts.com/modules/data.js"></script>
<script src="https://code.highcharts.com/modules/drilldown.js"></script>
<script src="https://code.highcharts.com/modules/exporting.js"></script>
<script src="https://code.highcharts.com/modules/export-data.js"></script>
<script src="https://code.highcharts.com/modules/accessibility.js"></script>

<style>
.highcharts-figure, .highcharts-data-table table {
    min-width: 320px; 
    max-width: 800px;
    margin: 1em auto;
}

.highcharts-data-table table {
   font-family: Verdana, sans-serif;
   border-collapse: collapse;
   border: 1px solid #EBEBEB;
   margin: 10px auto;
   text-align: center;
   width: 100%;
   max-width: 500px;
}
.highcharts-data-table caption {
    padding: 1em 0;
    font-size: 1.2em;
    color: #555;
}
.highcharts-data-table th {
   font-weight: 600;
    padding: 0.5em;
}
.highcharts-data-table td, .highcharts-data-table th, .highcharts-data-table caption {
    padding: 0.5em;
}
.highcharts-data-table thead tr, .highcharts-data-table tr:nth-child(even) {
    background: #f8f8f8;
}
.highcharts-data-table tr:hover {
    background: #f1f7ff;
}

input[type="number"] {
   min-width: 50px;
}

table {
      width: 300px;
   }
   
table, th, td {
   border: solid 1px gray;
}

th {
   text-align: center;   
}
</style>

<div align="center">
   <h2>우리회사 사원 통계정보(차트)</h2>
   
   <form name="searchFrm" style="margin: 20px 0 50px 0; ">
      <select name="searchType" id="searchType" style="height: 25px;">
         <option value="">통계선택</option>
         <option value="deptname">부서별 인원통계</option>
         <option value="gender">성별 인원통계</option>
         <option value="deptnameGender">부서별 성별 인원통계</option>
      </select>
   </form>
   
   <div id="chart_container" style="width: 80%;"></div>
   <div id="table_container" style="width: 90%; margin: 20px 0;"></div>
</div>

<script type="text/javascript">
   $(document).ready(function(){
      
      $("select#searchType").bind("change", function(){
         func_choice($(this).val()); 
         // $(this).val() 은 "" 또는 "deptname" 또는 "gender" 또는 "deptnameGender" 이다. 
      });
      
   });// end of $(document).ready()-------------------------
   
   
   function func_choice(searchTypeVal) {
      
      switch (searchTypeVal) {
         case "": // 통계선택을 선택한 경우
            $("div#chart_container").empty();
            $("div#table_container").empty();
            $("div.highcharts-data-table").empty();
            break;
            
         case "deptname": // 부서별 인원통계를 선택한 경우 
         
            $.ajax({
               url:"<%= request.getContextPath()%>/chart/employeeCntByDeptname.action",
               dataType:"JSON",
               success:function(json){
               
                  $("div#chart_container").empty();
                  $("div#table_container").empty();
                  $("div.highcharts-data-table").empty();
                  
                  var resultArr = [];
                  
                  for(var i=0; i<json.length; i++) {
                     var obj;
                     
                     if(json[i].department_name == "Shipping") {
                        obj = {name: json[i].department_name, 
                              y: Number(json[i].percentage),
                              sliced: true,
                                selected: true};
                     }
                     else {
                        obj = {name: json[i].department_name, 
                              y: Number(json[i].percentage)};
                     }
                     
                     resultArr.push(obj); // 배열속에 객체를 넣기 
                  }// end of for--------------------------
                  
                  ////////////////////////////////////////////////////////
                  Highcharts.chart('chart_container', {
                      chart: {
                          plotBackgroundColor: null,
                          plotBorderWidth: null,
                          plotShadow: false,
                          type: 'pie'
                      },
                      title: {
                          text: '우리회사 부서별 인원통계'
                      },
                      tooltip: {
                          pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
                      },
                      accessibility: {
                          point: {
                              valueSuffix: '%'
                          }
                      },
                      plotOptions: {
                          pie: {
                              allowPointSelect: true,
                              cursor: 'pointer',
                              dataLabels: {
                                  enabled: true,
                                  format: '<b>{point.name}</b>: {point.percentage:.1f} %'
                              }
                          }
                      },
                      series: [{
                          name: '인원비율',
                          colorByPoint: true,
                          data: resultArr
                      }]
                  });
                  ////////////////////////////////////////////////////////   
                  
                  var html = "<table>";
                  
                  html += "<tr>" +
                             "<th>부서명</th>" +
                             "<th>인원수</th>" +
                             "<th>퍼센티지</th>" +
                          "</tr>";
                  
                  $.each(json, function(index, item){
                	  html += "<tr>" +
                	  			 "<td style='text-align: center;'>"+item.department_name+"</td>" +
                	  			 "<td style='text-align: center;'>"+item.cnt+"</td>" +
                	  			 "<td style='text-align: center;'>"+item.percentage+"</td>" +
                	  		  "</tr>";
                  });        
                          
                  html +="</table>";
                  
                  $("div#table_container").html(html);
               },
               error: function(request, status, error){
                  alert("code: "+request.status+"\n"+"message: "+request.responseText+"\n"+"error: "+error);
               }
            });
         
            break;   
   
         case "gender": // 성별 인원통계를 선택한 경우 
            
        	  $.ajax({ //부서별 통계만 받아오기
                  url:"<%= request.getContextPath()%>/chart/employeeCntByGenger.action",
                  dataType:"JSON",
                  success:function(json){
                  
                     $("div#chart_container").empty();
                     $("div#table_container").empty();
                     $("div.highcharts-data-table").empty();
                     
                     var resultArr = []; 
                     
                     for(var i=0; i<json.length; i++) {
                        var obj;
                        
                        if(json[i].gender == "남") {
                           obj = {name: json[i].department_name, 
                                 y: Number(json[i].percentage),
                                 sliced: true,
                                   selected: true};
                        }
                        else {
                           obj = {name: json[i].department_name, 
                                 y: Number(json[i].percentage)};
                        }
                        
                        resultArr.push(obj); // 배열속에 객체를 넣기 
                     }// end of for--------------------------
                     
                     ////////////////////////////////////////////////////////
                     Highcharts.chart('chart_container', {
                         chart: {
                             plotBackgroundColor: null,
                             plotBorderWidth: null,
                             plotShadow: false,
                             type: 'pie'
                         },
                         title: {
                             text: '우리회사 성별 인원통계'
                         },
                         tooltip: {
                             pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
                         },
                         accessibility: {
                             point: {
                                 valueSuffix: '%'
                             }
                         },
                         plotOptions: {
                             pie: {
                                 allowPointSelect: true,
                                 cursor: 'pointer',
                                 dataLabels: {
                                     enabled: true,
                                     format: '<b>{point.name}</b>: {point.percentage:.1f} %'
                                 }
                             }
                         },
                         series: [{
                             name: '인원비율',
                             colorByPoint: true,
                             data: resultArr
                         }]
                     });
                     ////////////////////////////////////////////////////////   
                     
                     var html = "<table>";
                     
                     html += "<tr>" +
                                "<th>성별</th>" +
                                "<th>인원수</th>" +
                                "<th>퍼센티지</th>" +
                             "</tr>";
                     
                     $.each(json, function(index, item){
                   	  html += "<tr>" +
                   	  			 "<td style='text-align: center;'>"+item.gender+"</td>" +
                   	  			 "<td style='text-align: center;'>"+item.cnt+"</td>" +
                   	  			 "<td style='text-align: center;'>"+item.percentage+"</td>" +
                   	  		  "</tr>";
                     });        
                             
                     html +="</table>";
                     
                     $("div#table_container").html(html);
                  },
                  error: function(request, status, error){
                     alert("code: "+request.status+"\n"+"message: "+request.responseText+"\n"+"error: "+error);
                  }
               });
         
            break;
            
         case "deptnameGender": // 부서별 성별 인원통계를 선택한 경우 
             $.ajax({
                url:"<%= request.getContextPath()%>/chart/employeeCntByDeptname.action",
                dataType:"JSON",
                success:function(json1) {
                   
                   $("div#chart_container").empty();
                   $("div#table_container").empty();
                   $("div.highcharts-data-table").empty();
                   
                   var deptnameArr = []; // 부서명별 인원수 퍼센티지 객체 배열 
                   
                   $.each(json1, function(index, item){
                      deptnameArr.push({"name": item.department_name,
                                            "y": Number(item.percentage),
                                            "drilldown": item.department_name,
                                            "cnt": item.cnt});
                   });// end of $.each(json1, function(index, item){}-----------
                         
                   var genderArr = []; // 특정 부서명에 근무하는 직원들의 성별 인원수 퍼센티지 객체 배열 
                   
                   $.each(json1, function(index1, item1){
                      
                      $.ajax({
                         url:"<%= request.getContextPath()%>/chart/genderCntSpecialDeptname.action",
                         type:"GET",
                         data:{"deptname":item1.department_name},
                         dataType:"JSON",
                         success:function(json2) {
                            
                            var subArr = [];
                            
                            $.each(json2, function(index2, item2){
                               
                               subArr.push([item2.gender, 
                                           Number(item2.percentage)]);
                               
                            });// end of $.each(json2, function(index2, item2){})-----------
                            
                            genderArr.push({"name": item1.department_name,
                                             "id": item1.department_name,
                                             "data": subArr});
                            
                         },
                         error: function(request, status, error){
                            alert("code: "+request.status+"\n"+"message: "+request.responseText+"\n"+"error: "+error);
                         }
                      });
                      
                   });// end of $.each(json1, function(index1, item1){})-----------
                   
                   ////////////////////////////////////////////////////////
                   Highcharts.chart('chart_container', {
                       chart: {
                           type: 'column'
                       },
                       title: {
                           text: '부서별 남녀 비율'
                       },
                       subtitle: {
                        <%--  text: 'Click the columns to view versions. Source: <a href="http://statcounter.com" target="_blank">statcounter.com</a>' --%>
                       },
                       accessibility: {
                           announceNewData: {
                               enabled: true
                           }
                       },
                       xAxis: {
                           type: 'category'
                       },
                       yAxis: {
                           title: {
                               text: '구성비율(%)'
                           }
                   
                       },
                       legend: {
                           enabled: false
                       },
                       plotOptions: {
                           series: {
                               borderWidth: 0,
                               dataLabels: {
                                   enabled: true,
                                   format: '{point.y:.1f}%'
                               }
                           }
                       },
                   
                       tooltip: {
                           headerFormat: '<span style="font-size:11px">{series.name}</span><br>',
                           pointFormat: '<span style="color:{point.color}">{point.name}</span>: <b>{point.y:.2f}%</b> of total, {point.cnt}명<br/>'
                       },
                   
                       series: [{
                                 name: "부서명",
                                 colorByPoint: true,
                                 data: deptnameArr    // **** 위에서 구한 값을 대입시킴. 부서명별 인원수 퍼센티지 객체 배열  ****
                                }],
                       drilldown: {
                                    series: genderArr // **** 위에서 구한 값을 대입시킴. 특정 부서명에 근무하는 직원들의 성별 인원수 퍼센티지 객체 배열  ****
                                  }
                    });  
                   ////////////////////////////////////////////////////////
                },
                error: function(request, status, error){
                   alert("code: "+request.status+"\n"+"message: "+request.responseText+"\n"+"error: "+error);
                }
             });
             
             break;
         
      }
         
   }// end of function func_choice(searchTypeVal) {}----------------

</script>

