<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    

<% String ctxPath = request.getContextPath(); %>
    
<style type="text/css">
   table, th, td {border: solid 1px gray;}

    #table {width: 970px; border-collapse: collapse;}
    #table th, #table td {padding: 5px;}
    #table th {background-color: #DDD;}
     
    .subjectStyle {font-weight: bold;
                   color: navy;
                   cursor: pointer;} 
</style>

<script type="text/javascript">

   $(document).ready(function(){
      
         $("span.subject").bind("mouseover", function() {
         var $target = $(event.target);
         $target.addClass("subjectStyle");
      });
         
         $("span.subject").bind("mouseout", function() {
            var $target = $(event.target);
         $target.removeClass("subjectStyle");
      });
      
         $("input#searchWord").keyup(function(event) {
         if (event.keyCode == 13) {
            // 엔터를 했을 경우
            goSearch();
         }
      });
         
         
         <%-- === #107. 검색어 입력시 자동글 완성하기 2  === --%>
         $("div#displayList").hide();
         
         $("input#searchWord").keyup(function() {
            
            var wordLength = $(this).val().length;
            // 검색어의 길이를 알아온다.
            
            if (wordLength == 0) {
               $("div#displayList").hide();
               //검색어 입력후 백스페이스키를 눌러서 검색어를 모두 지우면  검색된 내용이 나오지 않도록 해야한다.
         }else{
               $.ajax({
                  url:"<%= request.getContextPath()%>/wordSearchShow.action",
                  type:"GET",
                  data:{"searchType": $("select#searchType").val() 
                      ,"searchWord": $("input#searchWord").val() },
                  dataType:"JSON",
                  success: function(json){
                     
                     <%-- === #112. 검색어 입력시 자동글 완성하기 7  === --%>
                     if (json.length > 0) {
                     // 검색된 데이터가 있는 경우임. 
                     
                     var html = "";
                     
                     $.each(json, function(index, item) {
                        var word = item.word;
                        // word ==> 나는 이순신이오 
                        
                        var index = word.toLowerCase().indexOf( $("input#searchWord").val().toLowerCase() ); // 검색어(이순신)가 나오는 index 
                        
                        var len = $("input#searchWord").val().length;
                        //  len <= 3
                        
                      // console.log(word.substr(0, index)); // 검색어 앞까지의 글자 "나는 " 
                      // console.log(word.substr(index, len)); // 검색어 글자 "이순신"
                      // console.log(word.substr(index+len)); // 검색어 뒤부터 끝까지의 글자 "이오"
                        
                        result = "<span style='color:blue;'>" + word.substr(0,index) + "</span><span style='color:red;'>" + word.substr(index, len) + "</span><span style='color:blue;'>" + word.substr(index+len) + "</span>";
                        
                        html += "<span style='cursor:pointer;' class='result'>" + result + "</span><br/>";
                     });
                     
                     $("div#displayList").html(html);
                     $("div#displayList").show();
                     }
                     
                  },
                  error: function(request, status, error){
                      alert("code: "+request.status+"\n"+"message: "+request.responseText+"\n"+"error: "+error);
                  }
               });
         }
         });// end of $("input#searchWord").keyup(function() {});--------------------------------
         
       // === #113. 검색어 입력시 자동글 완성하기 8 === //
        $(document).on("click", ".result", function(){
           var word = $(this).text();
           $("input#searchWord").val(word);  // 텍스트 박스에 검색된 결과의 문자열을 입력해 준다는 뜻.
           $("div#displayList").hide();
           goSearch();
        });
         <%-- 
         === #113. 검색어 입력시 자동글 완성하기 8  ===
         $(document).on("click",".result",function(){
            var word = $(this).text();
            $("input#searchWord").val(word); // 텍스트 박스에 검색된 결과의 문자열을 입력해 준다.
            $("div#displayList").hide();
            goSearch();
         });
          --%>
          
         // 검색시 검색조건 및 검색어 값 유지시키기 
         if ( ${paraMap != null} ) {
            $("select#searchType").val("${paraMap.searchType}");
            $("input#searchWord").val("${paraMap.searchWord}");
      }
         
         
    });// end of $(document).ready(function(){})-------------------
         
    
    function goView(seq) {
      <%-- location.href = "<%= ctxPath%>/view.action?seq="+seq; --%>
      // ===  #123. 페이징 처리된후 특정 글제목을 클릭하여 상세내용을 본 이후 사용자가 목록보기 버튼을 클릭했을때 돌아갈 페이지를 알려주기 위해 현재 페이지 주소를 뷰단으로 넘겨준다.
   
      var frm = document.goViewFrm;
      frm.seq.value = seq;
      
      frm.method = "GET";
      frm.action = "<%= ctxPath%>/view.action";
      frm.submit();
   
   }// end of function goView() {seq}--------------------------
    
   
   function goSearch() {
      
      var frm = document.searchFrm;
      frm.action = "<%= request.getContextPath()%>/list.action";
      frm.method = "GET";
      frm.submit();
      
   }// end of function goSearch() {}-------------------------------
   
</script>
   
<div style="padding-left: 3%;">
   <h2 style="margin-bottom: 30px;">글목록</h2>
   
   <table id="table">
      <tr>
         <th style="width: 70px;  text-align: center;">글번호</th>
         <th style="width: 360px; text-align: center;">제목</th>
         <th style="width: 70px;  text-align: center;">성명</th>
         <th style="width: 150px; text-align: center;">날짜</th>
         <th style="width: 70px;  text-align: center;">조회수</th>
      </tr>   
      <c:forEach var="boardvo" items="${boardList}" varStatus="status">
         <tr>
            <td align="center">${boardvo.seq}</td>
            <td align="left">
               <%--  === 댓글쓰기가 없는 게시판 ===
               <span class="subject" onclick="goView('${boardvo.seq}')">${boardvo.subject}</span>
               --%>
               
               <%--  === 댓글쓰기가 있는 게시판 === --%>
               <%-- 
               <c:if test="${boardvo.commentCount > 0}">
               <span class="subject" onclick="goView('${boardvo.seq}')">${boardvo.subject} <span style="vertical-align: super;">[<span style="color: red; font-size: 9pt; font-style: italic; font-weight: bold;">${boardvo.commentCount}</span>]</span> </span> 
               </c:if>
               
               <c:if test="${boardvo.commentCount == 0}">
               <span class="subject" onclick="goView('${boardvo.seq}')">${boardvo.subject}</span>  
               </c:if>
               --%>
               
               <%--  === 댓글쓰기 및 답변형 게시판 시작 === --%>
               <%-- 답변글이 아닌 원글인 경우 --%>
               <%-- 
               <c:if test="${boardvo.depthno == 0}">
                  <c:if test="${boardvo.commentCount > 0}">
                  <span class="subject" onclick="goView('${boardvo.seq}')">${boardvo.subject} <span style="vertical-align: super;">[<span style="color: red; font-size: 9pt; font-style: italic; font-weight: bold;">${boardvo.commentCount}</span>]</span> </span> 
                  </c:if>
                  
                  <c:if test="${boardvo.commentCount == 0}">
                  <span class="subject" onclick="goView('${boardvo.seq}')">${boardvo.subject}</span>  
                  </c:if>
               </c:if>
               --%>
               
               <%-- 답변글인 경우 --%>
               <%-- 
               <c:if test="${boardvo.depthno > 0}">
                  <c:if test="${boardvo.commentCount > 0}">
                  <span class="subject" onclick="goView('${boardvo.seq}')"><span style="color: red; font-style: italic; padding-left: ${boardvo.depthno * 20}px;">└Re&nbsp;</span>${boardvo.subject} <span style="vertical-align: super;">[<span style="color: red; font-size: 9pt; font-style: italic; font-weight: bold;">${boardvo.commentCount}</span>]</span> </span> 
                  </c:if>
                  
                  <c:if test="${boardvo.commentCount == 0}">
                  <span class="subject" onclick="goView('${boardvo.seq}')"><span style="color: red; font-style: italic; padding-left: ${boardvo.depthno * 20}px;">└Re&nbsp;</span>${boardvo.subject}</span>  
                  </c:if>
               </c:if> 
               --%>
               <%--  === 댓글쓰기 및 답변형 게시판 끝 === --%>   
               
          <%-- === 댓글쓰기 및 답변형 및 파일첨부가 있는 게시판 시작 === --%>
          <%-- 첨부파일이 없는 경우 --%>
          <c:if test="${empty boardvo.fileName}">
               <%-- 답변글이 아닌 원글인 경우 --%>
               <c:if test="${boardvo.depthno == 0}">
                  <c:if test="${boardvo.commentCount > 0}">
                  <span class="subject" onclick="goView('${boardvo.seq}')">${boardvo.subject} <span style="vertical-align: super;">[<span style="color: red; font-size: 9pt; font-style: italic; font-weight: bold;">${boardvo.commentCount}</span>]</span> </span> 
                  </c:if>
                  
                  <c:if test="${boardvo.commentCount == 0}">
                  <span class="subject" onclick="goView('${boardvo.seq}')">${boardvo.subject}</span>  
                  </c:if>
               </c:if>
               
               <%-- 답변글인 경우 --%>
               <c:if test="${boardvo.depthno > 0}">
                  <c:if test="${boardvo.commentCount > 0}">
                  <span class="subject" onclick="goView('${boardvo.seq}')"><span style="color: red; font-style: italic; padding-left: ${boardvo.depthno * 20}px;">└Re&nbsp;</span>${boardvo.subject} <span style="vertical-align: super;">[<span style="color: red; font-size: 9pt; font-style: italic; font-weight: bold;">${boardvo.commentCount}</span>]</span> </span> 
                  </c:if>
                  
                  <c:if test="${boardvo.commentCount == 0}">
                  <span class="subject" onclick="goView('${boardvo.seq}')"><span style="color: red; font-style: italic; padding-left: ${boardvo.depthno * 20}px;">└Re&nbsp;</span>${boardvo.subject}</span>  
                  </c:if>
               </c:if> 
          </c:if>
               
          <%-- 첨부파일이 있는 경우 --%>
          <c:if test="${not empty boardvo.fileName}">
               <%-- 답변글이 아닌 원글인 경우 --%>
               <c:if test="${boardvo.depthno == 0}">
                  <c:if test="${boardvo.commentCount > 0}">
                  <span class="subject" onclick="goView('${boardvo.seq}')">${boardvo.subject} <span style="vertical-align: super;">[<span style="color: red; font-size: 9pt; font-style: italic; font-weight: bold;">${boardvo.commentCount}</span>]</span> </span> &nbsp;<img src="<%= request.getContextPath()%>/resources/images/disk.gif" /> 
                  </c:if>
                  
                  <c:if test="${boardvo.commentCount == 0}">
                  <span class="subject" onclick="goView('${boardvo.seq}')">${boardvo.subject}</span> &nbsp;<img src="<%= request.getContextPath()%>/resources/images/disk.gif" />  
                  </c:if>
               </c:if>
               
               <%-- 답변글인 경우 --%>
               <c:if test="${boardvo.depthno > 0}">
                  <c:if test="${boardvo.commentCount > 0}">
                  <span class="subject" onclick="goView('${boardvo.seq}')"><span style="color: red; font-style: italic; padding-left: ${boardvo.depthno * 20}px;">└Re&nbsp;</span>${boardvo.subject} <span style="vertical-align: super;">[<span style="color: red; font-size: 9pt; font-style: italic; font-weight: bold;">${boardvo.commentCount}</span>]</span> </span> &nbsp;<img src="<%= request.getContextPath()%>/resources/images/disk.gif" />   
                  </c:if>
                  
                  <c:if test="${boardvo.commentCount == 0}">
                  <span class="subject" onclick="goView('${boardvo.seq}')"><span style="color: red; font-style: italic; padding-left: ${boardvo.depthno * 20}px;">└Re&nbsp;</span>${boardvo.subject}</span> &nbsp;<img src="<%= request.getContextPath()%>/resources/images/disk.gif" /> 
                  </c:if>
               </c:if> 
          </c:if>      
          <%-- === 댓글쓰기 및 답변형 및 파일첨부가 있는 게시판 끝 === --%>
                 
            </td>
            <td align="center">${boardvo.name}</td>
            <td align="center">${boardvo.regDate}</td>
            <td align="center">${boardvo.readCount}</td>
                        </tr>
      </c:forEach>
   </table>
   
   <%-- #112. 페이지바 보여주기 --%>
   <div align="center" style="width: 70%; border: solid 0px gray; margin:20px auto;">
		${pageBar}   
   </div>
   
   <%-- === #101. 글검색 폼 추가하기 : 글제목, 글쓴이로 검색을 하도록 한다. === --%>
   <form name="searchFrm" style="margin-top: 20px;">
      <select name="searchType" id="searchType" style="height: 26px;">
         <option value="subject">글제목</option>
         <option value="name">글쓴이</option>
      </select>
      <input type="text" name="searchWord" id="searchWord" size="40" style="width:328px;" autocomplete="off" /> 
      <button type="button" onclick="goSearch()">검색</button>
   </form>
   
   
   <%-- === #106. 검색어 입력시 자동글 완성하기 1  === --%>
   <div id="displayList" style="border:solid 1px gray; border-top:0px; width:328px; height:100px; margin-left:69px; margin-top:-1px; overflow:auto;">
   </div>
   
   <%--  페이징 처리된후 특정 글제목을 클릭하여 상세내용을 본 이후 사용자가 목록보기 버튼을 클릭했을때 돌아갈 페이지를 알려주기 위해 현재 페이지 주소를 뷰단으로 넘겨준다. --%>
     <form name="goViewFrm">
         <input type="hidden" name="seq">
         <input type="hidden" name="gobackURL" style="width: 800px" value="${gobackURL}">
   	</form>
</div>