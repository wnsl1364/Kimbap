# 냉동김밥
[KDT] 프로젝트 중심(MES, SCM, 구독형 ERP) Java 풀스택 웹 개발자 양성과정 (3회차) - 기업요구 SCM 프로젝트

## 프로젝트 개요
본 프로젝트는 라면 공장 MES(Manufacturing Execution System)를 구현하는 시스템입니다.  
생산관리, 품질관리, 설비관리, 자재관리, 주문관리 등 제조현장의 실행 프로세스를 지원합니다.

## 팀원별 역할 분담

| 팀원    | 담당 기능 |
|---------|-----------|
| 권민준 | 기준정보 |
| 김다산 | 자재 관리 |
| 이준영 | 자재 관리 |
| 이민호 | 생산 관리 |
| 심재진 | 로그인 |
| 한주연 | 주문 관리 |
| ?? | 물류관리 |

## 프로젝트 구조
/client  → 프론트(Vue + Vite)
/server  → 백엔드(Spring Boot)

## 사용 기술
- **Backend**: Spring Boot, JPA, Oracle
- **Frontend**: Vue3, Vuex, Axios
- **Database**: Oracle
- **Tool**: STS (Spring Tool Suite 4), VSCode

## 브랜치 전략
- main : 운영 브랜치
- feature/기능명 : 기능 개발용 브랜치 (예: feature/order-page, feature/login)

## Git 사용법
- git checkout -b feature/기능명
- git add .
- git commit -m "feat: 기능 설명"
- git push origin feature/기능명

## 개발 규칙
[네트워크 통신규약]
- 데이터를 주고 받는 통신은 REST API를 사용한다.
- URI는 명사형을 사용한다.
- 모든 데이터는 JSON으로 전달
- 성공시 응답법
```json
{
  "result_code": "SUCCESS",
  "message": "성공",
  "data": "반환결과"
}
```
- 실패시 응답법
```json
{
  "result_code": "FAIL",
  "message": "실패",
  "data": "반환결과"
}
```
<br><br>

[코딩 스타일 표준]
- 모든 폴더와 파일의 이름은 소문자로 하고, 띄어쓰기는 ‘-’로 표시한다. <br>
  ex) noodle, noodle-src.js
- 모든 vue 컴포넌트 파일은 파스칼 표기법에 따른다. <br>
  ex) SideBar.vue, MainMenu.vue
- 모든 변수와 함수의 이름은 카멜 표기법에 따르고 의미 있는 이름을 쓴다. <br>
  ex) empInfo, loadFile()
- 상수는 스네이크 표기법에 따른다. <br>
  ex) MAX_COUNT, MIN_COUNT

## DB 참고
- 잘말아조 erdCloud 참고

