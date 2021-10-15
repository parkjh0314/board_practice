package com.spring.board.model;

import java.util.List;
import java.util.Map;

public interface InterBoardDAO {

	int test_insert(); // spring_test 테이블에 insert하기

	List<TestVO> test_select(); // model단의 메소드를 호출해옴

	// view단의 form 태그에서 입력받은 값을 spring_tist 테이블에 insert하기
	int test_insert(Map<String, String> paraMap);

	// view단의 form 태그에서 입력받은 값을 spring_tist 테이블에 insert하기
	int test_insert(TestVO vo);

	// hr.employees 테이블의 정보를 select 해오기
	List<Map<String, String>> test_employees();

//////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	// 메인에 이미지 보여주기
	List<String> getImgfilenameList();

	// 로그인 처리하기
	MemberVO getLoginMember(Map<String, String> paraMap);
	int updateIdle(String userid);

	//글쓰기(파일첨부가 없는 글쓰기)
	int add(BoardVO boardvo);
	
	// === 페이징 처리를 안 한 검색어가 없는 전체 글목록 보여주기 === //
	List<BoardVO> boardListNoSearch();


	void setAddReadCount(String seq);  // 글 조회수 1 증가시키기
	BoardVO getView(String seq); // 글 1개 조회하기

	// 1개 글 수정하기
	int edit(BoardVO boardvo);

	// 1개 글 삭제하기
	int del(Map<String,String> paraMap);

	////////////////////////////////////////////////////////////////////////////////////////////////////
	int addComment(CommentVO commentvo); // 댓글쓰기(tbl_comment 테이블에 insert)
	int updateCommentCount(String parentSeq); // tbl_board테이블에 commentCount 컬럼의 값을 1 증가(update)
	int updateMemberPoint(Map<String, String> paraMap); //tbl_member 테이블에 point 컬럼의 값을 50증가(update)
	////////////////////////////////////////////////////////////////////////////////////////////////////

	// == 원게시글에 딸린 댓글들을 조회해오는 것 === // 
	List<CommentVO> getCommentList(String parentSeq);

	// BoardAOP 클래스에 사용하는 것으로 특정 회원에게 특정 점수만큼 포인트를 증가하기 위한 것
	void pointPlus(Map<String, String> paraMap);

	// 페이징 처리를 안 한 검색어가 있는 전체 글목록 보여주기
	List<BoardVO> boardListSearch(Map<String, String> paraMap);

	// 검색어 입력시 자동글 완성하기
	List<String> wordSearchShow(Map<String, String> paraMap);

	// ===  총 게시물건수(totalCount) 구하기 - 검색이 있을때와 검색이 없을때로 나뉜다. ===
	int getTotalCount(Map<String, String> paraMap);

	// 페이징 처리한 글목록 가져오기(검색이 있든지, 검색이 없든지 모두 다 포함한것)
	List<BoardVO> boardListSearchWithPaging(Map<String, String> paraMap);

	// 원게시물에 달린 댓글들을 페이징처리해서 조회해오기(Ajax 로 처리)
	List<CommentVO> getCommentListPaging(Map<String, String> paraMap);

	// 원게시물에 달린 댓글들의 totalPage 알아오기 (Ajax 로 처리)
	int getcommentTotalCount(Map<String, String> paraMap);
	
	//tbl_board 테이블에서 groupno 컬럼의 최대값 구하기 
	int getGroupnoMax();

	// 글쓰기(파일첨부가 있는 글쓰기)
	int add_withFile(BoardVO boardvo);

	// === #185. Spring Scheduler(스프링스케줄러)7. 
    // === Spring Scheduler(스프링 스케줄러)를 사용한 email 발송하기 === 
	List<Map<String, String>> getReservationList();
	void updateMailSendCheck(Map<String, String[]> paraMap);
	
	
	
}
