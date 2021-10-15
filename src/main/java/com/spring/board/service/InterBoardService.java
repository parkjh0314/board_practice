package com.spring.board.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spring.board.model.BoardVO;
import com.spring.board.model.CommentVO;
import com.spring.board.model.MemberVO;
import com.spring.board.model.TestVO;

public interface InterBoardService {

	int test_insert(); // model단 (BoardDAO)에 존재하는 메소드를 호출해옴(여기서는 test_insert()) 
	
	List<TestVO> test_select(); // model단 (BoardDAO)에 존재하는 메소드를 호출해옴(여기서는 test_insert()) 
	
	int test_insert(Map<String, String> paraMap); // model단 (BoardDAO)에 존재하는 메소드를 호출해옴(여기서는 test_insert(Map<String, String> paraMap)) 

	int test_insert(TestVO vo); // model단 (BoardDAO)에 존재하는 메소드를 호출해옴(여기서는 test_insert(TestVO vo)) 

	List<Map<String, String>> test_employees(); // model단 (BoardDAO)에 존재하는 메소드를 호출해옴(여기서는 test_employees()) 

//////////////////////////////////////////////////////////////////////////////////////////////게시판	
	// 시작페이지에서 메인이미지를 보여주기
	List<String> getImgfilenameList();

	// 로그인 처리하기
	MemberVO getLoginMember(Map<String, String> paraMap);

	// 글쓰기(파일첨부가 없는 글쓰기)
	int add(BoardVO boardvo);

	// === 페이징 처리를 안 한 검색어가 없는 전체 글목록 보여주기 === //
	List<BoardVO> boardListNoSearch();

	// 글 한개를 보여주기
	BoardVO getView(String seq, String login_userid);

	// 글조회수 증가 없이 글 조회만을 해줌
	BoardVO getViewWithNoAddCount(String seq);

	// 1개글 수정하기
	int edit(BoardVO boardvo);

	// 글 삭제하기
	int del(Map<String,String> paraMap);

	// 댓글쓰기
	int addComment(CommentVO commentvo) throws Throwable ;

	// 원게시글에 딸린 댓글들을 조회해오는 것
	List<CommentVO> getCommentList(String parentSeq);

	// BoardAOP 클래스에 사용하는 것으로 특정 회원에게 특정 점수만큼 포인트를 증가하기 위한 것
	void pointPlus(Map<String, String> paraMap);

	// 페이징 처리를 안 한 검색어가 있는 전체 글목록 보여주기
	List<BoardVO> boardListSearch(Map<String, String> paraMap);

	 // 검색어 입력시 자동글 완성하기
	List<String> wordSearchShow(Map<String, String> paraMap);

	// 총 게시물건수(totalCount) 구하기 - 검색이 있을때와 검색이 없을때로 나뉜다.
	int getTotalCount(Map<String, String> paraMap);

	// 페이징 처리한 글목록 가져오기(검색이 있든지, 검색이 없든지 모두 다 포함한것)
	List<BoardVO> boardListSearchWithPaging(Map<String, String> paraMap);

	// 원게시물에 달린 댓글들을 페이징처리해서 조회해오기(Ajax 로 처리)
	List<CommentVO> getCommentListPaging(Map<String, String> paraMap);

	// 원게시물에 달린 댓글들의 totalPage 알아오기 (Ajax 로 처리)
	int getcommentTotalCount(Map<String, String> paraMap);

	// 글쓰기(파일첨부가 있는 글쓰기)
	int add_withFile(BoardVO boardvo);

	// === #182. Spring Scheduler(스프링스케줄러)4. 
    // === Spring Scheduler(스프링 스케줄러)를 사용한 email 발송하기 === 
    // <주의> 스케줄러로 사용되어지는 메소드는 반드시 파라미터가 없어야 한다.!!!!
    // 매일 새벽 4시 마다 고객이 예약한 2일전에 고객에게 예약이 있다는 e메일을 자동 발송 하도록 하는 예제를 만들어 본다. 
    // 고객들의 email 주소는 List<String(e메일주소)> 으로 만들면 된다.
    // 또는 e메일 자동 발송 대신에 휴대폰 문자를 자동 발송하는 것도 가능하다.     
    void reservationEmailSending() throws Exception; 
}
