# side-pair

프로젝트 팀원을 모집하고, 함께 프로젝트를 관리하고 완성할 수 있는 서비스입니다.<br>
Rest API형 서버로써 클라이언트는 프로토타입으로 제작하여 서버 공부에 좀 더 집중할 수 있도록 하고 있습니다.<br>
단순 기능 구현 뿐 아니라 성능, 코드의 재사용성 및 유지보수성을 고려하여 구현하는 것을 목표로 개발했습니다. <br>
기술적인 문제에 대한 해결 방법은 [여기](https://hoonblog.netlify.app/series/side-pair/)에서 확인할 수 있습니다.<br>

### ✅ 사용 기술 및 개발 환경

Java17, Spring Boot, JPA/Hibernate, AWS, MySql, Redis, JWT, QueryDSL, RestDoc, Jacoco, Git

### ✅ ERD

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2F2rISL%2FbtsLXQ5lLgL%2F6G9vykI7IdUoKQK2wRChv1%2Fimg.png)

### ✅ 주요 기능

1. 회원가입 / 탈퇴
2. 로그인 / 로그아웃
3. 회원정보 수정
4. 피드 작성 / 수정 / 삭제 / 조회 / 검색
5. 프로젝트 생성 / 수정 / 나가기 / 조회
6. 프로젝트 투투 생성 / 수정 / 삭제 / 조회 / 검색
7. 프로젝트 투두 체크 / 체크 해제
8. 프로젝트 신청서 생성 / 조회
9. 회고 작성 / 조회 (단일 및 전체)

### ✅ 프로젝트를 진행하며 고민한 Technical Issue

* [1:N fetchJoin과 limit을 함께 사용하여 발생한 OutOfMemory 문제 해결](https://hoonblog.netlify.app/jpa-out-of-memory/)
* [@SpringBootTest에서 지연 로딩 사용하기](https://hoonblog.netlify.app/using-delayed-loading-in-test/)
* [QueryDSL 사용 시 NPE가 발생했을 때 해결하기](https://hoonblog.netlify.app/npe-occurs-when-using-querydsl/)
* [Spring Argument Resolver를 활용한 커스텀 multipart/form-data 요청 처리](https://hoonblog.netlify.app/customized-multi-part-form-receive-data-request/)
* [Redis를 활용한 피드 조회 성능 최적화](https://hoonblog.netlify.app/performance-improvement-with-redis-cache-2/)
* [Spring Event를 활용한 비동기 로직 처리 및 결합도 감소](https://hoonblog.netlify.app/loose-combination-with-event/)
* [Spring Rest Docs 연동 및 API 문서 자동화 구현](https://hoonblog.netlify.app/rest-docs-connection/)
* [SHA-256과 Salting을 통한 보안 강화](https://hoonblog.netlify.app/sha-256/)
