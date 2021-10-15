package com.spring.board.common;

import javax.servlet.http.HttpServletRequest;

public class MyUtil {

	// *** ? 다음의 데이터까지 포함한 현재 URLl 주소를 알려주는 메소드를 생성 *** //
	public static String getCurrentURL(HttpServletRequest request) {
		
		String currentURL = request.getRequestURL().toString();
		//http://localhost:9090/MyMVC/member/memberList.up
		
		String queryString = request.getQueryString();
		//currentShowPageNo=5&sizePerPage=10&searchType=name&searchWord=%ED%99%8D%EC%8A%B9%EC%9D%98
		// 검색을 하지 않았을 경우 queryString은 null이 됨
		
		if(queryString != null) { // ? 이하가 null이 아닐 경우에만 붙여주면 된다.
			currentURL += "?"+queryString;
			//http://localhost:9090/MyMVC/member/memberList.up?currentShowPageNo=5&sizePerPage=10&searchType=name&searchWord=%ED%99%8D%EC%8A%B9%EC%9D%98
		}
		String ctxPath = request.getContextPath();
		//     /MyMVC
		
		int beginIndex = currentURL.indexOf(ctxPath) +  ctxPath.length();
		//     27      = ctxPath가 시작되는인덱스값(21) + ctxPath의 길이(6)
		
		currentURL = currentURL.substring(beginIndex+1);
		// /member/memberList.up?currentShowPageNo=5&sizePerPage=10&searchType=name&searchWord=%ED%99%8D%EC%8A%B9%EC%9D%98 --> /가 포함되면 절대경로이므로 +1을 해주어 /를 빼준다. 
		return currentURL;
	}
	
	// **** 크로스 사이트 스크립트 공격에 대응하는 안전한 코드(시큐어 코드) **** //
	public static String secureCode(String str) {
		
		str = str.replaceAll("<", "&lt;");
		str = str.replaceAll(">", "&gt;");
		
		return str;
		
	}
	
}
