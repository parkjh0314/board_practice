package com.spring.employees.model;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EmpDAO implements InterEmpDAO {

	@Resource
	private SqlSessionTemplate sqlsession3;

	// employees 테이블에서 근무중인 사원들의 부서번호 가져오기
	@Override
	public List<String> deptIdList() {
		List<String> deptIdList = sqlsession3.selectList("emp.deptIdList");
		return deptIdList;
	}

	// employees 테이블에서 조건에 만족하는 사원들을 가져오기
	@Override
	public List<Map<String, String>> empList(Map<String, Object> paraMap) {
		List<Map<String, String>> empList = sqlsession3.selectList("emp.empList", paraMap);
		return empList;
	}
	
	// employees 테이블에서 부서별 인원수 및 퍼센티지 가져오기
	@Override
	public List<Map<String, String>> employeeCntByDeptname() {
		List<Map<String, String>> deptnamePercentageList = sqlsession3.selectList("emp.employeeCntByDeptname");
		return deptnamePercentageList;
	}
	
	// employees 테이블에서 성별 인원 및 퍼센티지 가져오기
	@Override
	public List<Map<String, String>> employeeCntByGender() {
		List<Map<String, String>> genderPercentageList = sqlsession3.selectList("emp.employeeCntByGender");
		return genderPercentageList;
	}
	
	// 특정 부서명에 근무하는 직원들의 성별 인원수 및 퍼센티지 가져오기
	   @Override
	   public List<Map<String, String>> genderCntSpecialDeptname(Map<String,String> paraMap) {
	      List<Map<String, String>> genderPercentageList = sqlsession3.selectList("emp.genderCntSpecialDeptname", paraMap);
	      return genderPercentageList;
	   }
	
}
