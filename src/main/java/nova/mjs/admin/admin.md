# 학생회 로그인 플로우 
1. 로그인 창은 기본 서비스 로그인과 동일하다. <br>
   관리자 페이지로 redirect 할지, 메인 서비스로 redirect 할지는 Member의 Role 컬럼이 User인지, ADMIN 인지다. 

   1. 로그인: 어드민(학생회) 계정인 경우 mjs 측에서 가상으로 발급받은 가상id@mju.ac.kr 를 id 로 원칙으로 한다. 이때 member entity의 role이 ADMIN 으로 구성된다.
   2. 비밀번호 변경: 비밀번호 변경 시에는 contactEmail 로 이메일 인증을 진행한다. 이때 contactEmail은 실제하는 이메일로, mju.ac.kr 도메인이 아닌 gmail 등 다른 연락가능한 도메인이다.


# 관리자계정 회원가입의 경우
=== 전제사항 ===
사전 컨택을 통해서 아이디와 contact email을 받으면 그 정보를 통해서 백엔드에서
role, 학번, password(hellomjs1!로 초기 임시 비밀번호 통일) , gender, email을 초기화하여 회원과 학과 객체를 만들어 놓는다.

=== 해당 화면부터 ===
# 이메일 검증 API
회원가입 할때 이메일 작성하고 이메일 검증 누르면 이메일 발송 인증이 아니라 우리가 만든 아이디인지 확인,


# 학생회 계정 update API
비밀번호
emailId(검증용)
소속대학
소속학과
소속 프로필 URL
슬로건
인스타
공식홈페이지
학과소개
를 요청으로 받아서 저장함