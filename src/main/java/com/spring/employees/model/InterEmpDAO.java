package com.spring.employees.model;

import java.util.List;
import java.util.Map;

public interface InterEmpDAO {

	// employees 테이블에서 근무중인 사원들의 부서번호 가져오기
	List<String> deptIdList();

	// employees 테이블에서 조건에 만족하는 사원들을 가져오기
	List<Map<String, String>> empList(Map<String, Object> paraMap);

	// employees 테이블에서 부서별 인원수 및 퍼센티지 가져오기
	List<Map<String, String>> employeeCntByDeptname();
	
	// employees 테이블에서 성별 인원 및 퍼센티지 가져오기
	List<Map<String, String>> employeeCntByGender();

	// 특정 부서명에 근무하는 직원들의 성별 인원수 및 퍼센티지 가져오기
	List<Map<String, String>> genderCntSpecialDeptname(Map<String,String> paraMap);

}
