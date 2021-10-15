------ ***** 스프링 ***** -------

show user;
-- USER이(가) "MYMVC_USER"입니다.

create table spring_test
(no         number
,name       varchar2(100)
,writeday   date default sysdate
);

select *
from spring_test;

delete from spring_test;
commit;

-------------------------------------------------------------------------------------------------------------------------------------------------
show user;
-- USER이(가) "HR"입니다.

select employee_id, first_name || ' ' || last_name AS ename,
       nvl( (salary + salary*commission_pct)*12 ,  salary*12) AS yearpay,
       case when substr(jubun,7,1) in ('1','3') then '남' else '여' end AS gender,
       extract(year from sysdate) - ( case when substr(jubun,7,1) in('1','2') then 1900 else 2000 end + to_number(substr(jubun,1,2)) ) + 1 AS age        
from employees
order by 1;

-------------------------------------------------------------------------------------------------------------------------------------------------------------------------
show user;
-- USER이(가) "MYMVC_USER"입니다.

select * from tab;

select * 
from tbl_main_image;

select * from tbl_member;

select userid, name, email, mobile, postcode, address, detailaddress, extraaddress, gender
     , birthyyyy, birthmm, birthdd, coin, point, registerday, pwdchangegap
     ,nvl( lastlogingap, trunc( months_between(sysdate, registerday) )) as lastlogingap
from
(
select userid, name, email, mobile, postcode, address, detailaddress, extraaddress, gender
     , substr(birthday,1,4) as birthyyyy, substr(birthday,6,2) as birthmm, substr(birthday,9) as birthdd
     , coin, point, to_char(registerday,'yyyy-mm-dd') as registerday
     , trunc( months_between(sysdate, lastpwdchangedate) ) as pwdchangegap
from tbl_member
where status = 1 and userid = 'leess' and pwd = '9695b88a59a1610320897fa84cb7e144cc51f2984520efb77111d94b402a8382'
) M 
cross join 
(
select trunc( months_between(sysdate, max(logindate)) ) as lastlogingap 
from tbl_loginhistory 
where fk_userid = 'less' 
)  H


    ------- **** 게시판(답변글쓰기가 없고, 파일첨부도 없는) 글쓰기 **** -------
desc tbl_member;

create table tbl_board
(seq         number                not null    -- 글번호
,fk_userid   varchar2(20)          not null    -- 사용자ID
,name        varchar2(20)          not null    -- 글쓴이 
,subject     Nvarchar2(200)        not null    -- 글제목
,content     Nvarchar2(2000)       not null    -- 글내용   -- clob (최대 4GB까지 허용) 
,pw          varchar2(20)          not null    -- 글암호
,readCount   number default 0      not null    -- 글조회수
,regDate     date default sysdate  not null    -- 글쓴시간
,status      number(1) default 1   not null    -- 글삭제여부   1:사용가능한 글,  0:삭제된글
,constraint PK_tbl_board_seq primary key(seq)
,constraint FK_tbl_board_fk_userid foreign key(fk_userid) references tbl_member(userid)
,constraint CK_tbl_board_status check( status in(0,1) )
);

create sequence boardSeq
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;

select *
from tbl_board
order by seq desc;

select seq, fk_userid, name, subject  
            , readcount, to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate
       from tbl_board
       where status = 1
       order by seq desc


-- lag(컬럼명, 몇번째앞쪽인지) over(어떤컬럼을 기준으로 정렬할건지)
-- lead(컬럼명,몇번째뒤쪽인지)over(어떤컬럼을 기준으로 정렬할건지)
select previousseq, previoussubject
            ,seq, fk_userid, name, subject, content, readCount, regDate
            ,nextseq, nextsubject
from
(
select    lag(seq,1) over(order by seq desc) as previousseq
            , lag(subject,1) over(order by seq desc) as previoussubject
            , seq, fk_userid, name, subject, content, readCount, to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate
            , lead(seq,1) over(order by seq desc) as nextseq
            , lead(subject,1) over(order by seq desc) as nextsubject
from tbl_board
where status = 1
) V
where v.seq = 2


select previousseq, previoussubject
            ,seq, fk_userid, name, subject, content, readCount, regDate
            ,nextseq, nextsubject
		from
		(
		select    lag(seq,1) over(order by seq desc) as previousseq
		          , lag(subject,1) over(order by seq desc) as previoussubject
		          , seq, fk_userid, name, subject, content, readCount, to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate
		          , lead(seq,1) over(order by seq desc) as nextseq
		          , lead(subject,1) over(order by seq desc) as nextsubject
		from tbl_board
		where status = 1
		) V
		where V.seq = 1
        
        select * from tab;
        
        
------------------------------------------------------------------------
   ----- **** 댓글 게시판 **** -----

/* 
  댓글쓰기(tblComment 테이블)를 성공하면 원게시물(tblBoard 테이블)에
  댓글의 갯수(1씩 증가)를 알려주는 컬럼 commentCount 을 추가하겠다. 
*/
drop table tbl_board purge;

create table tbl_board
(seq         number                not null    -- 글번호
,fk_userid   varchar2(20)          not null    -- 사용자ID
,name        varchar2(20)          not null    -- 글쓴이 
,subject     Nvarchar2(200)        not null    -- 글제목
,content     Nvarchar2(2000)       not null    -- 글내용   -- clob (최대 4GB까지 허용) 
,pw          varchar2(20)          not null    -- 글암호
,readCount   number default 0      not null    -- 글조회수
,regDate     date default sysdate  not null    -- 글쓴시간
,status      number(1) default 1   not null    -- 글삭제여부   1:사용가능한 글,  0:삭제된글
,commentCount  number default 0 not null  --댓글 수
,constraint PK_tbl_board_seq primary key(seq)
,constraint FK_tbl_board_fk_userid foreign key(fk_userid) references tbl_member(userid)
,constraint CK_tbl_board_status check( status in(0,1) )
);

drop sequence boardSeq;

create sequence boardSeq
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;


----- **** 댓글 테이블 생성 **** -----
create table tbl_comment
(seq           number               not null   -- 댓글번호
,fk_userid     varchar2(20)         not null   -- 사용자ID
,name          varchar2(20)         not null   -- 성명
,content       varchar2(1000)       not null   -- 댓글내용
,regDate       date default sysdate not null   -- 작성일자
,parentSeq     number               not null   -- 원게시물 글번호
,status        number(1) default 1  not null   -- 글삭제여부
                                               -- 1 : 사용가능한 글,  0 : 삭제된 글
                                               -- 댓글은 원글이 삭제되면 자동적으로 삭제되어야 한다.
,constraint PK_tbl_comment_seq primary key(seq)
,constraint FK_tbl_comment_userid foreign key(fk_userid) references tbl_member(userid)
,constraint FK_tbl_comment_parentSeq foreign key(parentSeq) references tbl_board(seq) on delete cascade
,constraint CK_tbl_comment_status check( status in(1,0) ) 
);

create sequence commentSeq
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;

select *
from tbl_Comment
order by seq desc;

delete from tbl_comment where parentseq=1;

select * from tbl_board;

update tbl_member set point = 0;

select * from tbl_member;

commit;
---- transaction 처리를 위한 시나리오 만들기 ----
-- 회원들이 게시판에 글쓰기를 하면 글작성 1건당 point를 100점준다.
-- 회원들이 게시판에서 댓글쓰기를 하면 댓글작성 1건당 point를 50점을 준다.
-- 그런데 point는 300을 초과할수없다.

-- tbl_member 테이블의 point컬럼에 check 제약을 추가한다
alter table tbl_member
add constraint CK_tbl_member_point check(point between 0 and 300);

select * from tbl_member;

update tbl_member set point = 0
where userid ='irismaa96'

update tbl_member set point = 0
where userid ='leess'

commit;

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regdate, status)
values(boardseq.nextval, 'leess', '서영학', '즐거운 하루되세요~~', '오늘도 늘 행복하게~~~', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regdate, status)
values(boardseq.nextval, 'leess', '온앤오프', '넌 날 사랑하게 될거야~', '난 너의 전부가되고 말거니까', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regdate, status)
values(boardseq.nextval, 'irismaa96', '박효신', '나의 기억어딘가에 동그마니 숨어있다', '울컥 쏟아져 내려오는 너~~', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regdate, status)
values(boardseq.nextval, 'irismaa96', '박지현', '박효신 컴백해', '앨범을 내란말이야 짤짤짤짤', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regdate, status)
values(boardseq.nextval, 'leess', '호세', 'ya se olvido casi todo', 'me parese que mi cerebro es no bein....oh... que tristesa...', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regdate, status)
values(boardseq.nextval, 'irismaa96', '위키미키', '위키미키 흥하자!!', '이뿌니들 이번곡도 좋았다아ㅏㅏ 열심히 하자~~~', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regdate, status)
values(boardseq.nextval, 'leess', '피카츄', '삐까?', '삐....삐까...삐까.....츄!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regdate, status)
values(boardseq.nextval, 'leess', '10cm', '내눈에만보여', '하루종일 너만 생각하다 보니 머리가 이상해 진것같아~~~', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regdate, status)
values(boardseq.nextval, 'leess', '10cm', '아메리카노', '조아 조아 조아 쏴쏴쏴쏴쏴쏴 아메리카노~~~진해 진해 진해~~~', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regdate, status)
values(boardseq.nextval, 'leess', '이순신', '배가너무아파요', '화장실가고싶은데 아직 20분 남았쥬 기다리다 죽겠쥬', '1234', default, default, default);

insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regdate, status)
values(boardseq.nextval, 'leess', '이순신', '아침을 먹으면 안됐다', '인간의 욕심은 끝이 없고 같은 실수를 반복하지', '1234', default, default, default);

commit;

select subject
from tbl_board
where status = 1
and lower(subject) like '%'|| lower ('jA') ||'%'
order by seq desc

select  seq, fk_userid, name, subject, readCount, regDate, commentCount
from
(
select row_number() over(order by seq desc) as rno,
        seq, fk_userid, name, subject, readCount,
        to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate,
        commentCount
from tbl_board
where status = 1
--and lower(subject) like '%' || lower('jA') || '%'
) V
where rno between 1 and 10;  -- 1페이지

select  seq, fk_userid, name, subject, readCount, regDate, commentCount
from
(
select row_number() over(order by seq desc) as rno,
        seq, fk_userid, name, subject, readCount,
        to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate,
        commentCount
from tbl_board
where status = 1
--and lower(subject) like '%' || lower('jA') || '%'
) V
where rno between 11 and 20; -- 2페이지


desc tbl_comment;

SELECT
    *
FROM tbl_board

insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '아메리카노~~~~~ 진해~~~진해~~~진해~~~~~~',22);

insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '어떻게하노~~~~~시럽~~시럽~~~시럽~~~~',22);
insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '빼고주세요~~~~~~~빼고주세요~~~~~~',22);
insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '사글세내고 돈 없을 때 ~~~밥대신에~~~~~',22);
insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '짜장면먹고 후~~~~~~~~~~~~~식으로~~~~~~~~~~~~~~~~~~',22);
insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '아메아메아메아메아메아메아메아메아메아메아메아메아메아메아메',22);
insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'leess', '이순신', '친구들아 수고했고 웃으면서 보자',22);
insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '아메아메아메아메아메아메아메아메아메아메아메아메아메아메아메',22);
insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '아메리카노~~~~~~~~~~~~좋아좋아좋아',22);
insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '쏴쏴쏴쏴쏴쏴쏴',22);

insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '어떻게해야',22);
insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '할지몰라',22);
insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '웃질않아도',22);
insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '아름다워',22);
insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '바라만봐도',22);
insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '너무좋아',22);
insert into tbl_comment(seq, fk_userid, name, content, parentseq)
values(commentSeq.nextval, 'irismaa96', '박지현', '죽겠네~~~',22);
commit;

update tbl_board set commentcount = 17 where seq = 22

select name, content, regDate
from
(
select row_number() over (order by seq desc) as rno, name, content, to_char(regdate, 'yyyy-mm-dd hh24:mi:ss') as regDate
from tbl_comment
where status = 1 and parentseq = 22
)V
where rno between 1 and 5


-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
                                   ---- **** 댓글 및 답변글 및 파일첨부가 있는 게시판 **** ---- 

drop table tbl_comment purge;

drop sequence commentSeq; 

drop table tbl_board purge;

drop sequence boardSeq;

commit;

create table tbl_board
(seq         number                not null    -- 글번호
,fk_userid   varchar2(20)          not null    -- 사용자ID
,name        varchar2(20)          not null    -- 글쓴이 
,subject     Nvarchar2(200)        not null    -- 글제목
,content     Nvarchar2(2000)       not null    -- 글내용   -- clob (최대 4GB까지 허용) 
,pw          varchar2(20)          not null    -- 글암호
,readCount   number default 0      not null    -- 글조회수
,regDate     date default sysdate  not null    -- 글쓴시간
,status      number(1) default 1   not null    -- 글삭제여부   1:사용가능한 글,  0:삭제된글
,commentCount  number default 0 not null  --댓글 수
,groupno        number                not null   -- 답변글쓰기에 있어서 그룹번호 
                                                 -- 원글(부모글)과 답변글은 동일한 groupno 를 가진다.
                                                 -- 답변글이 아닌 원글(부모글)인 경우 groupno 의 값은 groupno 컬럼의 최대값(max)+1 로 한다.

,fk_seq         number default 0      not null   -- fk_seq 컬럼은 절대로 foreign key가 아니다.!!!!!!
                                                 -- fk_seq 컬럼은 자신의 글(답변글)에 있어서 
                                                 -- 원글(부모글)이 누구인지에 대한 정보값이다.
                                                 -- 답변글쓰기에 있어서 답변글이라면 fk_seq 컬럼의 값은 
                                                 -- 원글(부모글)의 seq 컬럼의 값을 가지게 되며,
                                                 -- 답변글이 아닌 원글일 경우 0 을 가지도록 한다.

,depthno        number default 0       not null  -- 답변글쓰기에 있어서 답변글 이라면
                                                 -- 원글(부모글)의 depthno + 1 을 가지게 되며,
                                                 -- 답변글이 아닌 원글일 경우 0 을 가지도록 한다.

,fileName       varchar2(255)                    -- WAS(톰캣)에 저장될 파일명(20200725092715353243254235235234.png)                                       
,orgFilename    varchar2(255)                    -- 진짜 파일명(강아지.png)  // 사용자가 파일을 업로드 하거나 파일을 다운로드 할때 사용되어지는 파일명 
,fileSize       number                           -- 파일크기  
,constraint PK_tbl_board_seq primary key(seq)
,constraint FK_tbl_board_fk_userid foreign key(fk_userid) references tbl_member(userid)
,constraint CK_tbl_board_status check( status in(0,1) )
);

create sequence boardSeq
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;

create table tbl_comment
(seq           number               not null   -- 댓글번호
,fk_userid     varchar2(20)         not null   -- 사용자ID
,name          varchar2(20)         not null   -- 성명
,content       varchar2(1000)       not null   -- 댓글내용
,regDate       date default sysdate not null   -- 작성일자
,parentSeq     number               not null   -- 원게시물 글번호
,status        number(1) default 1  not null   -- 글삭제여부
                                               -- 1 : 사용가능한 글,  0 : 삭제된 글
                                               -- 댓글은 원글이 삭제되면 자동적으로 삭제되어야 한다.
,constraint PK_tbl_comment_seq primary key(seq)
,constraint FK_tbl_comment_userid foreign key(fk_userid) references tbl_member(userid)
,constraint FK_tbl_comment_parentSeq foreign key(parentSeq) references tbl_board(seq) on delete cascade
,constraint CK_tbl_comment_status check( status in(1,0) ) 
);

create sequence commentSeq
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;

begin
    for i in 1..100 loop
        insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regDate, status, groupno)
        values(boardSeq.nextval, 'leess', '이순신', '이순신 입니다'||i, '안녕하세요? 이순신'|| i ||' 입니다.', '1234', default, default, default, i);
    end loop;
end;

begin
    for i in 101..200 loop
        insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regDate, status, groupno)
        values(boardSeq.nextval, 'irismaa96', '박지현', '박효신 컴백해'||i, '박효신컴백해'|| i ||' !!!!!', '1234', default, default, default, i);
    end loop;
end;

select * from tbl_board order by seq desc;

commit;

----- **** 답변글쓰기는 일반회원은 불가하고 직원(관리자)들만 답변글쓰기가 가능하도록 한다. **** ----
select *
from tbl_member

-- ***** tbl_member 테이블 gradelevel 이라는 컬럼을 추가하겠다. ***** ---
alter table tbl_member
add gradelevel number default 1 ;

--- *** 직원(관리자)들에게는 gradelevel 컬럼의 값을 10으로 부여하겠다. gradelevel 컬럼의 값이 10인 직원들만 답변글쓰기가 가능하다 **** --
update tbl_member set gradelevel = 10
where userid = 'admin';

--- *** 글번호 197 에 대한 답변글쓰기를 한다라면 아래와 같이 insert를 해야한다. *** -- 
select * from tbl_board where seq = 197;

 insert into tbl_board(seq, fk_userid, name, subject, content, pw, readCount, regDate, status, groupno, fk_seq, depthno)
 values (boardSeq.nextval, 'admin', '관리자', '글번호 197에 대한 답변글입니다', '답변내용 입니다. 박효신 컴백해 2222222222222', '1234', default, default, default, 197, 197, 1);
 
 commit;
 
 ---- **** 답변글이 있을시 글목록 보여주기 **** ---
 select * from tbl_board order by seq desc;
 
 --  *** 계층형 쿼리(답변형 게시판) **** --
 select seq, fk_userid, name, subject, readCount, regDate
            ,groupno, fk_seq, depthno
 from
 (
  select rownum as rno
            ,  seq, fk_userid, name, subject, readCount, regDate
            ,groupno, fk_seq, depthno
      from 
      ( 
      select seq, fk_userid, name, subject, readCount,
              to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate,
              commentCount
              groupno, fk_seq, depthno
      from tbl_board
      where status = 1
      start with fk_seq = 0
      connect by prior seq = fk_seq   -- connect by prior 다음에 나오는 컬럼 seq는 starat with 되어지는 행의 컬럼이다.  fk_seq는 start with 되는 행이 아닌 다른 행에 존재하는 컬럼이다.
      
      order siblings by groupno desc, seq asc
       -- order siblings by를 사용하는 이유는 그냥 정렬(order by)하면 계층 구조가 개지기때문. 계층구조를 그대로 유지하면서 동일한 groupno를 가진 행끼리 정렬을 하려면 siblings를 써야한다.
      ) V
)T
where rno between 1 and 10;

--- **** tbl_member 테이블에 존재하는 제약조건 조회하기 ****----
select *
from user_constraints
where table_name ='TBL_MEMBER';

alter table tbl_member drop constraint CK_TBL_MEMBER_POINT;

commit;


----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
------ ==== Spring Scheduler(스프링 스케줄러)를 사용한 email 자동 발송하기 ==== ------
desc tbl_member;

create table tbl_reservation
(reservationSeq    number        not null
,fk_userid         varchar2(40)  not null
,reservationDate   date          not null
,mailSendCheck     number default 0 not null  -- 메일발송 했으면 1, 메일발송을 안했으면 0 으로 한다.
,constraint PK_tbl_reservation primary key(reservationSeq)
,constraint FK_tbl_reservation foreign key(fk_userid) references tbl_member(userid)
,constraint CK_tbl_reservation check(mailSendCheck in(0,1))
);


create sequence seq_reservation
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;

select *
from tbl_member;

select to_char(sysdate, 'yyyy-mm-dd') AS 오늘날짜
from dual; -- 2020-12-21

delete from  tbl_reservation;

insert into tbl_reservation(reservationSeq, fk_userid, reservationDate)
values(seq_reservation.nextval, 'Lea', to_date('2020-12-24 13:00','yyyy-mm-dd hh24:mi') );

insert into tbl_reservation(reservationSeq, fk_userid, reservationDate)
values(seq_reservation.nextval, 'irismaa96', to_date('2020-12-24 14:00','yyyy-mm-dd hh24:mi') );

insert into tbl_reservation(reservationSeq, fk_userid, reservationDate)
values(seq_reservation.nextval, 'Lea', to_date('2020-12-25 11:00','yyyy-mm-dd hh24:mi') );

insert into tbl_reservation(reservationSeq, fk_userid, reservationDate)
values(seq_reservation.nextval, 'irismaa96', to_date('2020-12-25 15:00','yyyy-mm-dd hh24:mi') );

commit;

select reservationSeq, fk_userid, 
       to_char(reservationDate, 'yyyy-mm-dd hh24:mi:ss') as reservationDate, 
       mailSendCheck
from tbl_reservation
order by reservationSeq desc;

-- *** 오늘로 부터 2일(이틀) 뒤에 예약되어진 회원들을 조회하기 *** --
select R.reservationSeq, M.userid, M.name, M.email, 
       to_char(R.reservationDate,'yyyy-mm-dd hh24:mi') as reservationDate
from tbl_member M join tbl_reservation R
on M.userid = R.fk_userid
where R.mailSendCheck = 0
and to_char(reservationDate, 'yyyy-mm-dd') = to_char(sysdate+2, 'yyyy-mm-dd');

/*
  update tbl_reservation set mailSendCheck = 1
  where reservationSeq IN ('1','2');
*/

