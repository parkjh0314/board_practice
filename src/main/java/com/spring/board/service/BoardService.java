package com.spring.board.service;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.spring.board.common.AES256;
import com.spring.board.common.FileManager;
import com.spring.board.common.GoogleMail;
import com.spring.board.model.BoardVO;
import com.spring.board.model.CommentVO;
import com.spring.board.model.InterBoardDAO;
import com.spring.board.model.MemberVO;
import com.spring.board.model.TestVO;

//=== #31. Service 선언 === 
//트랜잭션 처리를 담당하는곳 , 업무를 처리하는 곳, 비지니스(Business)단
@Service
public class BoardService implements InterBoardService {

	/*
	   주문 
	   ==> 주문테이블 insert (DAO에 있는 주문테이블 insert 관련 method 호출)
	   ==> 제품테이블에 주문받은 제품의 개수는 주문량 만큼 감소해야 한다.(DAO에 있는 제품테이블update 관련 method 호출)
	   ==> 장바구니에서 주문을 한 경우: 장바구니 비우기를 해야함 (DAO에 있는 장바구니테이블 delete 관련 method 호출)
	   ==> 회원테이블에 포인트(마일리지)를 증가시켜주어야 한다. (DAO에 있는 회원테이블 update 관련 method 호출)
	   
	   위에서 호출된 4가지의 메소드가 모두 성공되었다면 commit해주고 1개라도 실패하면 rollback 해준다.
	   이러한 트랜잭션처리를 해주는 곳이 Service 단이다.
	 */
	
	// === #34. 의존객체 주입하기(DI: Dependency Injection) ===
	@Autowired
	private InterBoardDAO dao;
	// Type 에 따라 Spring 컨테이너가 알아서 bean 으로 등록된 com.spring.model.BoardDAO 의 bean 을  dao 에 주입시켜준다. 
	// 그러므로 dao 는 null 이 아니다.


	// === #45. 양방향 암호화 알고리즘인 AES256 를 사용하여 복호화 하기 위한 클래스 의존객체 주입하기(DI: Dependency Injection) === //
	@Autowired
	private AES256 aes;
	// Type 에 따라 Spring 컨테이너가 알아서 bean 으로 등록된  com.spring.board.common.AES256 의 bean 을  dao 에 주입시켜준다. 
	// 그러므로 dao 는 null 이 아니다.
	// com.spring.board.common.AES256 의 bean은 WEB-INF/spring/appServlet/servlet-context.xml 파일에서 bean으로 등록시켜주었음.
	
	@Autowired     // Type에 따라 알아서 Bean 을 주입해준다.
	private FileManager fileManager;
	
	// === #183. Spring Scheduler(스프링스케줄러)5. 
    // === Spring Scheduler(스프링 스케줄러)를 사용한 email 발송하기 === 
    @Autowired
    private GoogleMail mail; 
	
	// model단 (BoardDAO)에 존재하는 메소드를 호출해옴(여기서는 test_insert();) 
	@Override
	public int test_insert() {
		
		 int n = dao.test_insert();
		
		 return n;
	}

	// model단 (BoardDAO)에 존재하는 메소드를 호출해옴(여기서는 test_select();) 
	@Override
	public List<TestVO> test_select() {
		
      List<TestVO> testvoList = dao.test_select();
      return testvoList;

	}

	// model단 (BoardDAO)에 존재하는 메소드를 호출해옴(여기서는 test_insert(Map<String, String> paraMap);) 
	@Override
	public int test_insert(Map<String, String> paraMap) {
		int n = dao.test_insert(paraMap);
		return n;
	}

	// model단 (BoardDAO)에 존재하는 메소드를 호출해옴(여기서는 test_insert(TestVO vo);) 
	@Override
	public int test_insert(TestVO vo) {
		int n = dao.test_insert(vo);
		return n;
	}


	// model단 (BoardDAO)에 존재하는 메소드를 호출해옴(여기서는 test_employees()) 
	@Override
	public List<Map<String, String>> test_employees() {
		List<Map<String, String>> empList = dao.test_employees();
		return empList;
	}

	// ==== 37. 메인 페이지 요청 ==== // 
	@Override
	public List<String> getImgfilenameList() {
		
		List<String> imgfilenameList = dao.getImgfilenameList();
		
		return imgfilenameList;
	}

	// ==== #42. 로그인처리하기 === //
	@Override
	public MemberVO getLoginMember(Map<String, String> paraMap) {
		MemberVO loginuser = dao.getLoginMember(paraMap);
		
		// === #48. aes 의존객체를 사용하여 로그인 되어진 사용자(loginuser)의 이메일 값을 복호화 하도록 한다. === //
		// 			또한 암호변경 메시지와 휴면처리 유무 메시지를 띄우도록 업무처리를 한다.
		if(loginuser != null && loginuser.getPwdchangegap() >= 3) {
			// 마지막으로 암호를 변경한지 3개월이 지났으면
			loginuser.setRequirePwdChange(true); // 로그인시 암호를 변경하라는
		}
		
		if(loginuser != null && loginuser.getLastlogingap() >= 12 ) {
			//마지막으로 로그인한 날짜가 1년이 지났으면 휴면으로 지정
			loginuser.setIdle(1);
			
			// ==== tbl_member 테이블의 idle 컬럼의 값을 1로 변경하기 === // 
			int n = dao.updateIdle(paraMap.get("userid"));
		}
		
		if(loginuser != null) {
			String email = "";
			try {
				email = aes.decrypt(loginuser.getEmail());
			} catch (UnsupportedEncodingException | GeneralSecurityException e) {
				e.printStackTrace();
			}
			loginuser.setEmail(email);
		}
		
		return loginuser;
		
	}

	// === #55. 글쓰기(파일첨부가 없는 글쓰기) === //
	@Override
	public int add(BoardVO boardvo) {
		
		// === # 144. 글쓰기가 원글쓰기인지 아니면 답변글쓰기인지를 구분하여 tbl_board 테이블에 insert를 해주어야한다.
		//			  원글쓰기일 경우 tbl_board 테이블의 groupno 컬럼의 값은 groupno 컬럼의 최대값(max)+1로 해서 insert 해야하고,
		//			  답변글쓰기라면 넘겨받은 값(boardvo)을 그대로 insert 해줘야한다.
		
		// == 원글쓰기인지, 답변글쓰기인지 구분하기 ==
		if(boardvo.getFk_userid() == null || boardvo.getFk_seq().trim().isEmpty()) {
			// 원글쓰기라면 groupno 의 값은 groupno 컬럼의 컬럼의 최대값(max)+1 으로 해야한다.
			// groupno 컬럼의 최대값 (max)+1
			int groupno = dao.getGroupnoMax()+1;
			boardvo.setGroupno(String.valueOf(groupno));
		}
		
		int n = dao.add(boardvo);
		return n;
	}

	// === #59. 페이징 처리를 안 한 검색어가 없는 전체 글목록 보여주기 === //
	@Override
	public List<BoardVO> boardListNoSearch() {
		List<BoardVO> boardList = dao.boardListNoSearch();
		return boardList;
	}

	// === #63. 글 한개를 보여주는 페이지 요청 === //
	// (먼저, 로그인을 한 상태에서 다른사람의 글을 조회할 경우 글조회수 컬럼의 값을 1 증가시켜야함)
	@Override
	public BoardVO getView(String seq, String login_userid) {

		//login_userid : 로그인된상태-로그인한userid 로그인안된상태 - null
		BoardVO boardvo = dao.getView(seq); //글 1개 조회하기
		
		if(login_userid != null && boardvo != null && !login_userid.equals(boardvo.getFk_userid()) ) {
			// 글조회수는 로그인을 한 상태에서 다른 사람의 글을 읽을 때만 증가하도록 한다.
			// boardvo가 null이면 안됨
			
			 dao.setAddReadCount(seq); // 글조회수 1증가하기
			 boardvo = dao.getView(seq); // 조회수가 증가된 데이터를 다시 읽어와서 보여줌
			
		}
		return boardvo;
	}

	// === #70. 글조회수 증가 없이 글 조회만을 해줌 ===
	@Override
	public BoardVO getViewWithNoAddCount(String seq) {
		BoardVO boardvo = dao.getView(seq); // 글 1개 조회하기
		return boardvo;
	}

	// == #73. 1개글 수정하기 === //
	@Override
	public int edit(BoardVO boardvo) {
		int n = dao.edit(boardvo);
		return n;
	}

	// == #78. 1개글 삭제하기
	@Override
	public int del(Map<String,String> paraMap) {
		
		int n = dao.del(paraMap);
		
		// === #165. 파일첨부가 된 글이라면 글 삭제가 성공된 후 첨부파일을 삭제해주어야 한다. === //
	      String fileName = paraMap.get("fileName");
	      String path = paraMap.get("path");
	      
	      if( fileName != null && !"".equals(fileName) ) {
	         try {
	            fileManager.doFileDelete(fileName, path);
	         } catch (Exception e) {   }
	      }
	      ///////////////////////////////////////////////////////////////////
		
		return n;
	}

	// === #85. 댓글쓰기(transaction 처리) === //
	// tbl_comment 테이블에 insert 된 다음에 
	// tbl_board 테이블에 commentCount 컬럼이 1증가(update) 하도록 요청한다.
	// 즉, 2개이상의 DML 처리를 해야하므로 Transaction 처리를 해야 한다.
	// >>>>> 트랜잭션처리를 해야할 메소드에 @Transactional 어노테이션을 설정하면 된다. 
	// rollbackFor={Throwable.class} 은 롤백을 해야할 범위를 말하는데 Throwable.class 은 error 및 exception 을 포함한 최상위 루트이다. 
	//즉, 해당 메소드 실행시 발생하는 모든 error 및 exception 에 대해서 롤백을 하겠다는 말이다.
	@Override
	@Transactional(propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED, rollbackFor= {Throwable.class}) //뭐든 오류나면 rollback하겠다
	public int addComment(CommentVO commentvo) throws Throwable {
		
		int result = 0, n = 0, m = 0;
	
		n = dao.addComment(commentvo); // 댓글쓰기(tbl_comment 테이블에 insert)
		//  n <== 1
		
		if(n == 1) { 
			m = dao.updateCommentCount(commentvo.getParentSeq()); // tbl_board테이블에 commentCount 컬럼의 값을 1 증가(update)
		//  m <== 1
		}
		
		if(m == 1) {
			Map<String, String> paraMap = new HashedMap<String, String>();
			paraMap.put("userid", commentvo.getFk_userid());
			paraMap.put("point", "50");
			result = dao.updateMemberPoint(paraMap); //tbl_member 테이블에 point 컬럼의 값을 50증가(update)
		}
	
		return result;
	}
	// == #91. 원게시글에 딸린 댓글들을 조회해오는 것 === // 
	@Override
	public List<CommentVO> getCommentList(String parentSeq) {
		List<CommentVO> commentList = dao.getCommentList(parentSeq);
		return commentList;
	}

	// #98. BoardAOP 클래스에 사용하는 것으로 특정 회원에게 특정 점수만큼 포인트를 증가하기 위한 것
	@Override
	public void pointPlus(Map<String, String> paraMap) {
		dao.pointPlus(paraMap);
	}

	
	// == #103. 페이징 처리를 안 한 검색어가 있는 전체 글목록 보여주기 == //
	@Override
	public List<BoardVO> boardListSearch(Map<String, String> paraMap) {
		List<BoardVO> boardList= dao.boardListSearch(paraMap);
		return boardList;
	}

	 // == #109. 검색어 입력시 자동글 완성하기 3 === //
	@Override
	public List<String> wordSearchShow(Map<String, String> paraMap) {
		List<String> wordList = dao.wordSearchShow(paraMap);
		return wordList;
	}

	// === #115. 총 게시물건수(totalCount) 구하기 - 검색이 있을때와 검색이 없을때로 나뉜다. ===
	@Override
	public int getTotalCount(Map<String, String> paraMap) {
		int n = dao.getTotalCount(paraMap);
		return n;
	}

	// == #118. 페이징 처리한 글목록 가져오기(검색이 있든지, 검색이 없든지 모두 다 포함한것)
	@Override
	public List<BoardVO> boardListSearchWithPaging(Map<String, String> paraMap) {
		List<BoardVO> boardList = dao.boardListSearchWithPaging(paraMap);
		return boardList;
	}

	// === #129. 원게시물에 달린 댓글들을 페이징처리해서 조회해오기(Ajax 로 처리) === //
	@Override
	public List<CommentVO> getCommentListPaging(Map<String, String> paraMap) {
		List<CommentVO> commentList = dao.getCommentListPaging(paraMap);
		return commentList;
	}

	// === #133. 원게시물에 달린 댓글들의 totalPage 알아오기 (Ajax 로 처리) === //
	@Override
	public int getcommentTotalCount(Map<String, String> paraMap) {
		int totalCount = dao.getcommentTotalCount(paraMap);
		return totalCount;
	}

	// === #157. 글쓰기(파일첨부가 있는 글쓰기) === 
	@Override
	public int add_withFile(BoardVO boardvo) {
		
		// 글쓰기가 원글쓰기인지 아니면 답변글쓰기인지를 구분하여 tbl_board 테이블에 insert를 해주어야한다.
		// 원글쓰기일 경우 tbl_board 테이블의 groupno 컬럼의 값은 groupno 컬럼의 최대값(max)+1로 해서 insert 해야하고,
		// 답변글쓰기라면 넘겨받은 값(boardvo)을 그대로 insert 해줘야한다.
		
		// == 원글쓰기인지, 답변글쓰기인지 구분하기 ==
		if(boardvo.getFk_userid() == null || boardvo.getFk_seq().trim().isEmpty()) {
			// 원글쓰기라면 groupno 의 값은 groupno 컬럼의 컬럼의 최대값(max)+1 으로 해야한다.
			// groupno 컬럼의 최대값 (max)+1
			int groupno = dao.getGroupnoMax()+1;
			boardvo.setGroupno(String.valueOf(groupno));
		}
		
		int n = dao.add_withFile(boardvo); // 첨부파일이 있는 경우
		
		return n;
	}

	 //  === #184. Spring Scheduler(스프링스케줄러)6. 
    //  === Spring Scheduler(스프링 스케줄러)를 사용한 email 발송하기 === 
    // <주의> 스케줄러로 사용되어지는 메소드는 반드시 파라미터가 없어야 한다.!!!!
    // 매일 새벽 4시 마다 고객이 예약한 2일전에 고객에게 예약이 있다는 e메일을 자동 발송 하도록 하는 예제를 만들어 본다. 
    // 고객들의 email 주소는 List<String(e메일주소)> 으로 만들면 된다.
    // 또는 e메일 자동 발송 대신에 휴대폰 문자를 자동 발송하는 것도 가능하다. 
/*
	스케줄은 3가지 종류  cron, fixedDelay, fixedRate 가 있다. 
	
	
	@Scheduled(cron="0 0 0 * * ?")
	cron 스케줄에 따라서 일정기간에 시작한다. 매일 자정마다 (00:00:00)에 실행한다.
	
	>>> cron 표기법 <<<
	
	문자열의 좌측부터 우측까지 아래처럼 의미가 부여되고 각 항목은 공백 문자로 구분한다.
	
	순서는 초 분 시 일 월 요일명 이다.
	----------------------------------------------------------------------------------------------------------------    
	의미             초               분              시             일                         월             요일명                                                                   년도
	----------------------------------------------------------------------------------------------------------------
	사용가능한	0~59     0~59     0~23      1~31           1~12      1~7 (1=>일요일, 2=>월요일, ... 7=>토요일)     1970 ~ 2099 
	값           - * /    - * /    - * /     - * ? / L W    - * /     - * ? / L #
	
	* 는 모든 수를 의미.
	
	? 는 해당 항목을 사용하지 않음.  
	일에서 ?를 사용하면 월중 날짜를 지정하지 않음. 요일명에서 ?를 사용하면 주중 요일을 지정하지 않음.
	
	- 는 기간을 설정. 시에서 10-12이면 10시, 11시, 12시에 동작함.
	분에서 57-59이면 57분, 58분, 59분에 동작함.
	
	, 는 특정 시간을 지정함. 요일명에서 2,4,6 은 월,수,금에만 동작함.
	
	/ 는 시작시간과 반복 간격 설정함. 초위치에 0/15로 설정하면 0초에 시작해서 15초 간격으로 동작함. 
	분위치에 5/10으로 설정하면 5분에 시작해서 10분 간격으로 동작함.
	
	L 는 마지막 기간에 동작하는 것으로 일과 요일명에서만 사용함. 일위치에 사용하면 해당월의 마지막 날에 동작함.
	요일명에 사용하면 토요일에 동작함.
	
	W 는 가장 가까운 평일에 동작하는 것으로 일에서만 사용함.  일위치에 15W로 설정하면 15일이 토요일이면 가장 가까운 14일 금요일에 동작함.
	일위치에 15W로 설정하고 15일이 일요일이면 16일에 동작함.
	일위치에 15W로 설정하고 15일이 평일이면 15일에 동작함.
	
	LW 는 L과 W의 조합.                             그달의 마지막 평일에 동작함.
	
	# 는 몇 번째 주와 요일 설정함. 요일명에서만 사용함.    요일명위치에 6#3이면 3번째 주 금요일에 동작함.
	요일명위치에 4#2이면 2번째 주 수요일에 동작함.
	
	
	※ cron 스케줄 사용 예
	0 * * * * *             ==> 매 0초마다 실행(즉, 1분마다 실행함)
	
	* 0 * * * *             ==> 매 0분마다 실행(즉, 1시간마다 실행함)
	
	0 * 14 * * *            ==> 14시에 0분~59분까지 1분 마다 실행
	
	* 10,50 * * * *         ==> 매 10분, 50분 마다 실행
	, : 여러 값 지정 구분에 사용 
	
	0 0/10 14 * * *         ==> 14시 0분 부터 시작하여 10분 간격으로 실행(즉, 6번 실행함)
	/ : 초기값과 증가치 설정에 사용
	* 
	0 0/10 14,18 * * *      ==> 14시 0분 부터 시작하여 10분 간격으로 실행(6번 실행함) 그리고 
	==> 18시 0분 부터 시작하여 10분 간격으로 실행(6번 실행함)
	/ : 초기값과 증가치 설정에 사용 
	, : 여러 값 지정 구분에 사용 
	
	0 0 12 * * *            ==> 매일 12(정오)시에 실행
	0 15 10 * * *           ==> 매일 오전 10시 15분에 실행
	0 0 14 * * *            ==> 매일 14시에 실행
	
	0 0 0/6 * * *        ==> 매일 0시 6시 12시 18시 마다 실행
	- : 범위 지정에 사용  / : 초기값과 증가치 설정에 사용
	
	0 0/5 14-18 * * *    ==> 매일 14시 부터 18시에 시작해서 0분 부터 매 5분간격으로 실행
	/ : 증가치 설정에 사용
	
	0 0-5 14 * * *          ==> 매일 14시 0분 부터 시작해서 14시 5분까지 1분마다 실행   
	- : 범위 지정에 사용
	
	0 0 8 * * 2-6           ==> 평일 08:00 실행 (월,화,수,목,금)  
	
	0 0 10 * * 1,7          ==> 토,일 10:00 실행 (토,일) 
	
	0 0/5 14 * * ?          ==> 아무요일, 매월, 매일 14:00부터 14:05분까지 매분 0초 실행 (6번 실행됨)
	
	0 15 10 ? * 6L          ==> 매월 마지막 금요일 아무날의 10:15:00에 실행
	
	0 15 10 15 * ?          ==> 아무요일, 매월 15일 10:15:00에 실행 
	
	* /1 * * * *            ==> 매 1분마다 실행
	
	* /10 * * * *           ==> 매 10분마다 실행 
	
	
	>>> fixedDelay <<<
	이전에 실행된 task의 종료시간으로부터 정의된 시간만큼 지난 후 다음에 task를 실행함. 단위는 밀리초임.
	@Scheduled(fixedDelay=1000)
	
	>>> fixedRate <<<
	이전에 실행된 task의 시작 시간으로부터 정의된 시간만큼 지난 후 다음 task를 실행함. 단위는 밀리초임.
	@Scheduled(fixedRate=1000)

*/	
	@Override
//	@Scheduled(cron="0 * * * * *")
	@Scheduled(cron="30 15 1 * * *")
	public void reservationEmailSending() throws Exception {
		// <주의> 스케줄러로 사용되어지는 메소드는 반드시 파라미터가 없어야 한다.!!!!
		
		// === 현재시각 나타내기 === //
		Calendar currentDate = Calendar.getInstance(); // 현재날짜와 시간을 얻어온다. 
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		System.out.println("현재시각 => " + df.format(currentDate.getTime()));
		// 현재시각 => 2020-12-22 11:44:00
		// 현재시각 => 2020-12-22 11:45:00
		// 현재시각 => 2020-12-22 11:46:00
		
		// === e메일을 발송할 회원 대상 알아오기 === // 
		List<Map<String, String>> reservationList = dao.getReservationList();
		
		// *** e메일 발송하기 *** // 
		if(reservationList != null && reservationList.size() > 0) {
			
			String[] reservationSeqArr = new String[reservationList.size()]; 
		   	// String[] reservationSeqArr 을 생성하는 이유는 
		   	// e메일 발송 후 tbl_reservation 테이블의 mailSendCheck 컬럼의 값을 0 에서 1로 update 하기 위한 용도로 
		   	// update 되어질 예약번호를 기억하기 위한 것임.
			
			for(int i=0; i<reservationList.size(); i++) {
				String emailContents = "사용자 ID: " + reservationList.get(i).get("USERID") + "<br/> 예약자명: " + reservationList.get(i).get("NAME") +"님의 방문 예약일은 <span style='color:red;'>" + reservationList.get(i).get("RESERVATIONDATE") + "</span> 입니다."; 
				mail.sendmail_Reservation( aes.decrypt(reservationList.get(i).get("EMAIL")) , emailContents);
				
				reservationSeqArr[i] = reservationList.get(i).get("RESERVATIONSEQ");
			}// end of for-------------------------------------------
			
			// e메일을 발송한 행은 발송했다라는 표시해주기 //
			Map<String, String[]> paraMap = new HashMap<>();
			paraMap.putIfAbsent("reservationSeqArr", reservationSeqArr);
			
			dao.updateMailSendCheck(paraMap);
		}
		
	
	
	
	    	  
	      
	}


	
	
	
	
	
	
}
