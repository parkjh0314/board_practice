package com.spring.employees.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.spring.employees.service.InterEmpService;

@Controller
public class EmpController {

	@Autowired
	private InterEmpService service;
	
	@RequestMapping(value = "/emp/empList.action")
	public ModelAndView empList(HttpServletRequest request, ModelAndView mav) {
		
		// employees 테이블에서 근무중인 사원들의 부서번호 가져오기
		List<String> deptIdList = service.deptIdList();
		
		String sDeptIdes = request.getParameter("sDeptIdes");
		// sDeptIdes로 올수있는값: null, 부서번호, 부서번호배열
		// null 검색버튼을 클릭하지 않고 처음으로 보여줄때
		/*
	    sDeptIdes ==> null  검색버튼을 클릭안하고 처음으로 보여줄때
	    sDeptIdes ==> "-9999,50,110"
	    sDeptIdes ==> ""
	    sDeptIdes ==> "10,30,50,80,110"
	*/
		String gender = request.getParameter("gender");
		// gender로 들어올수있는값 => null (검색전),"","남","여" 
		/*
	     gender ==> null  검색버튼을 클릭안하고 처음으로 보여줄때
	     gender ==> ""  
	     gender ==> "남"
	     gender ==> "여"  
	 */
		
		Map<String, Object> paraMap = new HashMap<>();
		
		if(sDeptIdes != null && !"".equals(sDeptIdes)) {
			String[] deptIdArr = sDeptIdes.split(",");
			paraMap.put("deptIdArr", deptIdArr);
			
			// 뷰단에서 체크된 값을 유지시키기 위한 것
			mav.addObject("sDeptIdes", sDeptIdes);
			
		}
		
		if(gender != null && !"".equals(gender)) {
			paraMap.put("gender", gender);
			
			// 뷰단에서 체크된 값을 유지시키기 위한 것
			mav.addObject("gender", gender);
		}
		
		List<Map<String,String>> empList = service.empList(paraMap);
		
		mav.addObject("deptIdList", deptIdList);
		mav.addObject("empList", empList);
		mav.setViewName("emp/empList.tiles2");
		return mav;
	}
	
	// >>> Excel 파일로 다운받기 //
	@RequestMapping(value = "/excel/downloadExcelFile.action", method= {RequestMethod.POST})
	public String downloadExcelFile(HttpServletRequest request, Model model) {

		String sDeptIdes = request.getParameter("sDeptIdes");
		// sDeptIdes로 올수있는값: null, 부서번호, 부서번호배열
		// null 검색버튼을 클릭하지 않고 처음으로 보여줄때
		/*
	    sDeptIdes ==> null  검색버튼을 클릭안하고 처음으로 보여줄때
	    sDeptIdes ==> "-9999,50,110"
	    sDeptIdes ==> ""
	    sDeptIdes ==> "10,30,50,80,110"
	*/
		String gender = request.getParameter("gender");
		// gender로 들어올수있는값 => null (검색전),"","남","여" 
		/*
	     gender ==> null  검색버튼을 클릭안하고 처음으로 보여줄때
	     gender ==> ""  
	     gender ==> "남"
	     gender ==> "여"  
	 */
		
		Map<String, Object> paraMap = new HashMap<>();
		
		
		
		if(sDeptIdes != null && !"".equals(sDeptIdes)) {
			String[] deptIdArr = sDeptIdes.split(",");
			paraMap.put("deptIdArr", deptIdArr);
			
		}
		
		if(gender != null && !"".equals(gender)) {
			paraMap.put("gender", gender);
		}
		
		List<Map<String,String>> empList = service.empList(paraMap);
		
		// === 조회결과물인 empList를 가지고 엘셀시트 생성하기 ===
		// 시트를 생성하고, 행을 생성하고, 셀을 생성하고, 셀안에 내용을 넣어주면 된다.
		
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		
		//시트생성
		SXSSFSheet sheet = workbook.createSheet("HR사원정보"); //HR사원정보라는 시트 생성
		
		// 시트 열 너비 설정
		sheet.setColumnWidth(0, 2000);
		sheet.setColumnWidth(1, 4000);
		sheet.setColumnWidth(2, 2000);
		sheet.setColumnWidth(3, 4000);
		sheet.setColumnWidth(4, 3000);
		sheet.setColumnWidth(5, 2000);
		sheet.setColumnWidth(6, 1500);
		sheet.setColumnWidth(7, 1500);
		
		// 행의 위치를 나타내는 변수
		 int rowLocation = 0;

		
		////////////////////////////////////////////////////////////////////////////////////////
		// CellStyle 정렬하기(Alignment)
		// CellStyle 객체를 생성하여 Alignment 세팅하는 메소드를 호출해서 인자값을 넣어준다.
		// 아래는 HorizontalAlignment(가로)와 VerticalAlignment(세로)를 모두 가운데 정렬 시켰다.
		CellStyle mergeRowStyle = workbook.createCellStyle();
		mergeRowStyle.setAlignment(HorizontalAlignment.CENTER);
		mergeRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		// import org.apache.poi.ss.usermodel.VerticalAlignment 으로 해야함.
		
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		
		// CellStyle 배경색(ForegroundColor)만들기
        // setFillForegroundColor 메소드에 IndexedColors Enum인자를 사용한다.
        // setFillPattern은 해당 색을 어떤 패턴으로 입힐지를 정한다.
        mergeRowStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex()); // IndexedColors.DARK_BLUE.getIndex() 는 색상(남색)의 인덱스값을 리턴시켜준다.  
        mergeRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex()); // IndexedColors.LIGHT_YELLOW.getIndex() 는 연한노랑의 인덱스값을 리턴시켜준다.  
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
     // Cell 폰트(Font) 설정하기
        // 폰트 적용을 위해 POI 라이브러리의 Font 객체를 생성해준다.
        // 해당 객체의 세터를 사용해 폰트를 설정해준다. 대표적으로 글씨체, 크기, 색상, 굵기만 설정한다.
        // 이후 CellStyle의 setFont 메소드를 사용해 인자로 폰트를 넣어준다.
        Font mergeRowFont = workbook.createFont(); // import org.apache.poi.ss.usermodel.Font; 으로 한다.
        mergeRowFont.setFontName("나눔고딕");
        mergeRowFont.setFontHeight((short)500);
        mergeRowFont.setColor(IndexedColors.WHITE.getIndex());
        mergeRowFont.setBold(true);
                
        mergeRowStyle.setFont(mergeRowFont); 
        
     // CellStyle 테두리 Border
        // 테두리는 각 셀마다 상하좌우 모두 설정해준다.
        // setBorderTop, Bottom, Left, Right 메소드와 인자로 POI라이브러리의 BorderStyle 인자를 넣어서 적용한다.
        headerStyle.setBorderTop(BorderStyle.THICK);
        headerStyle.setBorderBottom(BorderStyle.THICK);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        
     // Cell Merge 셀 병합시키기
        /* 셀병합은 시트의 addMergeRegion 메소드에 CellRangeAddress 객체를 인자로 하여 병합시킨다.
           CellRangeAddress 생성자의 인자로(시작 행, 끝 행, 시작 열, 끝 열) 순서대로 넣어서 병합시킬 범위를 정한다. 배열처럼 시작은 0부터이다.  
        */ 
        // 병합할 행 만들기
        Row mergeRow =  sheet.createRow(rowLocation); // 엑셀에서 행은 0부터 시작함
        
        // 병합할 행에 "우리회사 사원정보"로 셀을 만들어 셀에 스타일주기
        for (int i = 0; i<8; i++) {
        	Cell cell = mergeRow.createCell(i);
        	cell.setCellStyle(mergeRowStyle);
        	cell.setCellValue("우리회사 사원정보");
        }
        
        // 셀 병합하기
        sheet.addMergedRegion(new CellRangeAddress(rowLocation, rowLocation, 0, 7)); //시작 행, 끝행, 시작 열, 끝 열
		
     // CellStyle 천단위 쉼표, 금액
        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        //헤더 행 생성
        Row headerRow =  sheet.createRow(++rowLocation); // 엑셀에서 행은 0부터 시작함 
        												//++rowLocation은 전위연산자임
        
     // 해당 행의 첫번째 열 셀 생성
        Cell headerCell = headerRow.createCell(0); // 엑셀에서 열의 시작은 0 부터 시작한다.
        headerCell.setCellValue("부서번호");
        headerCell.setCellStyle(headerStyle);
        
        // 해당 행의 두번째 열 셀 생성
        headerCell = headerRow.createCell(1);
        headerCell.setCellValue("부서명");
        headerCell.setCellStyle(headerStyle);
        
        // 해당 행의 세번째 열 셀 생성
        headerCell = headerRow.createCell(2);
        headerCell.setCellValue("사원번호");
        headerCell.setCellStyle(headerStyle);
        
        // 해당 행의 네번째 열 셀 생성
        headerCell = headerRow.createCell(3);
        headerCell.setCellValue("사원명");
        headerCell.setCellStyle(headerStyle);
        
        // 해당 행의 다섯번째 열 셀 생성
        headerCell = headerRow.createCell(4);
        headerCell.setCellValue("입사일자");
        headerCell.setCellStyle(headerStyle);
        
        // 해당 행의 여섯번째 열 셀 생성
        headerCell = headerRow.createCell(5);
        headerCell.setCellValue("월급");
        headerCell.setCellStyle(headerStyle);
        
        // 해당 행의 일곱번째 열 셀 생성
        headerCell = headerRow.createCell(6);
        headerCell.setCellValue("성별");
        headerCell.setCellStyle(headerStyle);
        
        // 해당 행의 여덟번째 열 셀 생성
        headerCell = headerRow.createCell(7);
        headerCell.setCellValue("나이");
        headerCell.setCellStyle(headerStyle);
        
        // HR사원정보에 해당하는 행 및 셀 생성하기
        Row bodyRow = null;
        Cell bodyCell = null;
        
        for(int i=0; i<empList.size(); i++) {
        	Map<String,String> empMap = empList.get(i);
        	
        	//행생성
        	sheet.createRow(i+(rowLocation+1));
        	
        	//데이터 부서번호 표시
        	bodyCell = bodyRow.createCell(0);
        	bodyCell.setCellValue(empMap.get("department_id"));

        	//데이터 부서명 표시
        	bodyCell = bodyRow.createCell(1);
        	bodyCell.setCellValue(empMap.get("department_name"));
        	
        	//데이터 사원번호 표시
        	bodyCell = bodyRow.createCell(2);
        	bodyCell.setCellValue(empMap.get("employee_id"));
        	
        	//데이터 사원명 표시
        	bodyCell = bodyRow.createCell(3);
        	bodyCell.setCellValue(empMap.get("fullname"));
        	
        	//데이터 입사일자 표시
        	bodyCell = bodyRow.createCell(4);
        	bodyCell.setCellValue(empMap.get("hire_date"));
        	
        	//데이터 월급 표시
        	bodyCell = bodyRow.createCell(5);
        	bodyCell.setCellValue(Integer.parseInt(empMap.get("monthsal")));
        	
        	//데이터 성별 표시
        	bodyCell = bodyRow.createCell(6);
        	bodyCell.setCellValue(empMap.get("gender"));
        	
        	//데이터 나이 표시
        	bodyCell = bodyRow.createCell(7);
        	bodyCell.setCellValue(Integer.parseInt(empMap.get("deparagetment_name")));
        }// end of for ---
        
        model.addAttribute("Locale", Locale.KOREA);
        model.addAttribute("workbook", workbook);
        model.addAttribute("workbookName", "HR사원정보");
        
        return "excelDownloadView";
	}
	
	// >>> 차트를 보여주는 view단 <<< //
	@RequestMapping(value = "/emp/chart.action")
	public ModelAndView chart(ModelAndView mav) {
		mav.setViewName("emp/chart.tiles2");
		return mav;
	}
	
	// >>> 차트그리기(Ajax) 부서명별 인원수 및 퍼센티지 가져오기 <<< //
	@ResponseBody
	@RequestMapping(value="/chart/employeeCntByDeptname.action", produces="text/plain;charset=UTF-8")
	public String employeeCntByDeptname() {
		
		List<Map<String, String>> deptnamePercentageList = service.employeeCntByDeptname();
		
		Gson gson = new Gson();
		JsonArray jsonArr = new JsonArray();
		
		for(Map<String,String> map : deptnamePercentageList) {
			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("department_name", map.get("department_name"));
			jsonObj.addProperty("cnt", map.get("cnt"));
			jsonObj.addProperty("percentage", map.get("percentage"));
			
			jsonArr.add(jsonObj);
		}
		return gson.toJson(jsonArr);
	}

	@ResponseBody
	@RequestMapping(value="/chart/employeeCntByGenger.action", produces="text/plain;charset=UTF-8")
	public String employeeCntByGender() {
	List<Map<String, String>> genderPercentageList = service.employeeCntByGender();
		
		Gson gson = new Gson();
		JsonArray jsonArr = new JsonArray();
		
		for(Map<String,String> map : genderPercentageList) {
			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("gender", map.get("gender"));
			jsonObj.addProperty("cnt", map.get("cnt"));
			jsonObj.addProperty("percentage", map.get("percentage"));
			
			jsonArr.add(jsonObj);
		}
		return gson.toJson(jsonArr);
	}
	
	// >>> 차트그리기(Ajax) 특정 부서명에 근무하는 직원들의 성별 인원수 및 퍼센티지 가져오기 <<< //
	   @ResponseBody
	   @RequestMapping(value="/chart/genderCntSpecialDeptname.action", produces="text/plain;charset=UTF-8")
	   public String genderCntSpecialDeptname(HttpServletRequest request) {
	      
	      String deptname = request.getParameter("deptname");
	      Map<String,String> paraMap = new HashMap<String,String>();
	      paraMap.put("deptname", deptname);
	      
	      List<Map<String,String>> genderPercentageList = service.genderCntSpecialDeptname(paraMap);
	      
	      Gson gson = new Gson();
	      JsonArray jsonArr = new JsonArray();
	      
	      for(Map<String,String> map : genderPercentageList) {
	         JsonObject jsonObj = new JsonObject();
	         jsonObj.addProperty("gender", map.get("gender"));
	         jsonObj.addProperty("cnt", map.get("cnt"));
	         jsonObj.addProperty("percentage", map.get("percentage"));
	         
	         jsonArr.add(jsonObj);
	      }
	      
	      return gson.toJson(jsonArr);
	   }
	   
	// >>> 기상청 공공데이터(오픈데이터)를 가져와서 날씨정보 보여주기 <<< //
   @RequestMapping(value="/opendata/weatherXML.action", method= {RequestMethod.GET}) 
   public String weatherXML() {
      return "opendata/weatherXML";
      //  /Board/src/main/webapp/WEB-INF/views/opendata/weatherXML.jsp 파일을 생성한다.
   }
   
   @ResponseBody
   @RequestMapping(value="/opendata/weatherXMLtoJSON.action", method= {RequestMethod.POST}, produces="text/plain;charset=UTF-8") 
   public String weatherXMLtoJSON(HttpServletRequest request) { 
      String str_jsonObjArr = request.getParameter("str_jsonObjArr");
      //System.out.println(str_jsonObjArr);
      // [{"locationName":"속초","ta":"1.2"},{"locationName":"북춘천","ta":"-7.3"},{"locationName":"철원","ta":"-5.8"},{"locationName":"동두천","ta":"-4.7"},{"locationName":"파주","ta":"-5.4"},{"locationName":"대관령","ta":"-6.5"},{"locationName":"춘천","ta":"-7.0"},{"locationName":"백령도","ta":"1.3"},{"locationName":"북강릉","ta":"3.1"},{"locationName":"강릉","ta":"3.2"},{"locationName":"동해","ta":"2.5"},{"locationName":"서울","ta":"-3.3"},{"locationName":"인천","ta":"-3.6"},{"locationName":"원주","ta":"-6.5"},{"locationName":"울릉도","ta":"2.1"},{"locationName":"수원","ta":"-3.0"},{"locationName":"영월","ta":"-6.2"},{"locationName":"충주","ta":"-6.0"},{"locationName":"서산","ta":"-0.3"},{"locationName":"울진","ta":"3.4"},{"locationName":"청주","ta":"-2.7"},{"locationName":"대전","ta":"-1.0"},{"locationName":"추풍령","ta":"-2.7"},{"locationName":"안동","ta":"-1.8"},{"locationName":"상주","ta":"-1.2"},{"locationName":"포항","ta":"1.5"},{"locationName":"군산","ta":"0.3"},{"locationName":"대구","ta":"0.4"},{"locationName":"전주","ta":"0.9"},{"locationName":"울산","ta":"1.9"},{"locationName":"창원","ta":"1.6"},{"locationName":"광주","ta":"2.4"},{"locationName":"부산","ta":"1.2"},{"locationName":"통영","ta":"2.0"},{"locationName":"목포","ta":"1.4"},{"locationName":"여수","ta":"3.2"},{"locationName":"흑산도","ta":"4.6"},{"locationName":"완도","ta":"3.4"},{"locationName":"고창","ta":"0.9"},{"locationName":"순천","ta":"1.4"},{"locationName":"홍성","ta":"-1.3"},{"locationName":"제주","ta":"6.5"},{"locationName":"고산","ta":"6.8"},{"locationName":"성산","ta":"5.8"},{"locationName":"서귀포","ta":"8.3"},{"locationName":"진주","ta":"2.4"},{"locationName":"강화","ta":"-3.1"},{"locationName":"양평","ta":"-5.7"},{"locationName":"이천","ta":"-5.3"},{"locationName":"인제","ta":"-5.2"},{"locationName":"홍천","ta":"-7.7"},{"locationName":"태백","ta":"-2.2"},{"locationName":"정선군","ta":"-4.5"},{"locationName":"제천","ta":"-7.0"},{"locationName":"보은","ta":"-1.4"},{"locationName":"천안","ta":"-2.6"},{"locationName":"보령","ta":"0.9"},{"locationName":"부여","ta":"0.0"},{"locationName":"금산","ta":"-0.7"},{"locationName":"세종","ta":"-1.7"},{"locationName":"부안","ta":"1.1"},{"locationName":"임실","ta":"0.2"},{"locationName":"정읍","ta":"0.7"},{"locationName":"남원","ta":"0.7"},{"locationName":"장수","ta":"-1.7"},{"locationName":"고창군","ta":"0.8"},{"locationName":"영광군","ta":"0.0"},{"locationName":"김해시","ta":"1.1"},{"locationName":"순창군","ta":"1.0"},{"locationName":"북창원","ta":"2.3"},{"locationName":"양산시","ta":"2.1"},{"locationName":"보성군","ta":"4.2"},{"locationName":"강진군","ta":"3.7"},{"locationName":"장흥","ta":"3.4"},{"locationName":"해남","ta":"3.6"},{"locationName":"고흥","ta":"2.8"},{"locationName":"의령군","ta":"3.1"},{"locationName":"함양군","ta":"1.8"},{"locationName":"광양시","ta":"4.4"},{"locationName":"진도군","ta":"3.7"},{"locationName":"봉화","ta":"-3.0"},{"locationName":"영주","ta":"-2.6"},{"locationName":"문경","ta":"-0.9"},{"locationName":"청송군","ta":"-2.6"},{"locationName":"영덕","ta":"-0.2"},{"locationName":"의성","ta":"-0.4"},{"locationName":"구미","ta":"0.1"},{"locationName":"영천","ta":"0.9"},{"locationName":"경주시","ta":"0.3"},{"locationName":"거창","ta":"0.6"},{"locationName":"합천","ta":"1.9"},{"locationName":"밀양","ta":"2.2"},{"locationName":"산청","ta":"1.5"},{"locationName":"거제","ta":"1.8"},{"locationName":"남해","ta":"2.0"}] 
     // return str_jsonObjArr;
      
      String[] arr_str_jsonObjArr = str_jsonObjArr.split("\\},");
      
      for(int i=0; i<arr_str_jsonObjArr.length; i++) {
         arr_str_jsonObjArr[i] += "}";
      }
      
      String[] locationArr = {"서울","인천","수원","춘천","강릉","청주","홍성","대전","안동","포항","대구","전주","울산","부산","창원","여수","광주","목포","제주","울릉도","백령도"};
      String result = "[";
      for(String jsonObj : arr_str_jsonObjArr) {
         for(int i=0; i<locationArr.length; i++) {
         //   if(jsonObj.indexOf(locationArr[i]) >= 0) { // 북춘천,춘천,북강릉,강릉,북창원,창원이 있으므로  if(jsonObj.indexOf(locationArr[i]) >= 0) { 을 사용하지 않음 
            if( jsonObj.substring(jsonObj.indexOf(":")+2, jsonObj.indexOf(",")-1).equals(locationArr[i]) ) { 
               result += jsonObj+","; 
               break;
            }
         }
      }// end of for------------------------------
      
      result = result.substring(0, result.length()-1);
      result = result + "]";
      
      System.out.println(result);
   //   [{"locationName":"춘천","ta":"-2.1"},{"locationName":"백령도","ta":"1.3"},{"locationName":"강릉","ta":"5.0"},{"locationName":"서울","ta":"-0.7"},{"locationName":"인천","ta":"-1.0"},{"locationName":"울릉도","ta":"2.0"},{"locationName":"수원","ta":"0.2"},{"locationName":"청주","ta":"0.9"},{"locationName":"대전","ta":"2.4"},{"locationName":"안동","ta":"1.3"},{"locationName":"포항","ta":"4.6"},{"locationName":"대구","ta":"3.7"},{"locationName":"전주","ta":"2.3"},{"locationName":"울산","ta":"4.6"},{"locationName":"창원","ta":"3.0"},{"locationName":"광주","ta":"5.1"},{"locationName":"부산","ta":"5.9"},{"locationName":"목포","ta":"3.1"},{"locationName":"여수","ta":"6.0"},{"locationName":"홍성","ta":"1.7"},{"locationName":"제주","ta":"6.6"}] 
      return result;
   }

    
   
}
