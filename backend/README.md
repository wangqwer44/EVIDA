# EVIDA Backend

상상기업 프로젝트(EVIDA) 백엔드 서버입니다.

## 기술 스택
- Java 21
- Spring Boot 3.4
- Spring Data JPA
- Spring Security (Basic Auth)
- WebFlux WebClient
- H2 (기본), PostgreSQL 확장 가능

## 구현 기능
- 영수증 업로드/분석/저장 API
- 국세청 룰베이스 필터(간이영수증 3만원 초과 반려)
- 불공제 키워드 자동 태깅
- 카드내역 매칭 점수 계산 및 수동검토 전환
- 부서별 커스텀 정책 관리 API
- 소명 요청/제출/재무팀 승인·반려 API
- 대시보드 요약 API

## 기본 계정
- employee / employee1234
- finance / finance1234
- admin / admin1234

## 실행
```
mvn spring-boot:run
혹은
./mvnw spring-boot:run
```

## 서버 종료
- 현재 터미널에서 실행 중이면 `Ctrl + C`
- 포트(8080)를 점유한 프로세스를 종료하려면:
- `kill -15`로 종료되지 않을 때만 강제 종료(`kill -9 <PID>`)를 사용

```bash
lsof -i :8080
kill -15 <PID>
```

## 테스트 웹
서버 실행 후 브라우저에서 아래 주소로 접속:

http://localhost:8080
