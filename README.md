# Gomdolbook Back-end (곰돌북 백엔드)

Gomdolbook은 사용자들이 자신의 독서 기록을 효율적으로 관리하고, 책 컬렉션을 만들며, 알라딘 API를 통해 다양한 도서 정보를 탐색할 수 있도록 돕는 웹 애플리케이션의 백엔드 시스템입니다. Spring Boot를 기반으로 구축되었으며, RESTful API를 통해 프론트엔드와 통신합니다.

## 주요 기능

Gomdolbook 백엔드는 다음과 같은 핵심 기능을 제공합니다:

### 1. 독서 기록 관리

* **독서 상태 관리:** 책의 독서 상태(읽기 전, 읽는 중, 읽음, 신규)를 `READING`, `FINISHED`, `TO_READ`, `NEW` 등 다양한 상태로 관리할 수 있습니다.
* **독서 노트 작성:** 각 책에 대한 개인적인 노트(note1, note2, note3)를 기록할 수 있습니다.
* **별점 부여:** 책에 대한 별점(`rating`)을 기록할 수 있습니다.
* **독서 시작/완료일 기록:** 책의 독서 시작일(`startedAt`)과 완료일(`finishedAt`)을 기록하여 독서 습관을 추적할 수 있습니다.
* **월별 독서 현황:** 완료된 책들의 월별 목록을 캘린더 형식으로 조회하여 독서 현황을 한눈에 파악할 수 있습니다.

### 2. 책 컬렉션 기능

* **나만의 컬렉션 생성:** 사용자가 원하는 이름으로 자신만의 책 컬렉션(예: "인생 책", "2024년 독서 목록")을 만들 수 있습니다.
* **컬렉션에 책 추가/제거:** 생성한 컬렉션에 책을 추가하거나 제거할 수 있습니다.
* **컬렉션 목록 조회:** 자신이 만든 모든 컬렉션과 각 컬렉션에 포함된 책의 커버 이미지를 한눈에 확인할 수 있습니다.

### 3. 알라딘 API 연동

* **도서 정보 검색:** 알라딘 API를 통해 ISBN, 제목 등을 기반으로 책 정보를 검색하고 상세 정보를 확인할 수 있습니다.
* **도서 정보 캐싱:** 알라딘 API로부터 가져온 도서 정보는 캐싱되어 불필요한 API 호출을 줄이고 응답 속도를 향상시킵니다.

### 4. 사용자 인증 및 인가

* **OAuth2 Resource Server (JWT):** Keycloak과 같은 OAuth2 서버를 통해 JWT(JSON Web Token)를 사용하여 사용자 인증 및 인가를 처리합니다. `email` 클레임을 기반으로 사용자 정보를 식별합니다.
* **사용자 자동 등록:** 새로운 사용자가 서비스를 이용할 경우, 자동으로 사용자 정보를 시스템에 등록합니다.

### 5. 시스템 아키텍처

* **Spring Boot 기반:** Java Spring Boot 프레임워크를 사용하여 견고하고 확장 가능한 백엔드를 구축했습니다.
* **JPA & QueryDSL:** 데이터베이스 영속성 계층은 Spring Data JPA와 QueryDSL을 활용하여 효율적인 데이터 접근 및 복잡한 쿼리를 구현합니다.
* **Caffeine Cache:** 빠른 응답을 위해 Caffeine 기반의 로컬 캐시를 적용하여 반복적인 데이터 조회 성능을 최적화합니다.
* **AOP (Aspect-Oriented Programming):** 로깅, 사용자 유효성 검증 등의 횡단 관심사(cross-cutting concerns)를 AOP로 분리하여 코드의 가독성과 유지보수성을 높였습니다.
* **예외 처리:** 전역 예외 처리(`CustomExceptionHandler`)를 통해 일관된 오류 응답을 제공하여 클라이언트가 예측 가능한 방식으로 오류를 처리할 수 있도록 돕습니다.

## 사용 방법 (API 엔드포인트)

Gomdolbook 백엔드는 RESTful API를 통해 기능을 제공합니다. 다음은 주요 API 엔드포인트 및 사용 예시입니다.

### 📚 도서 및 독서 기록 관련 API

* **알라딘 도서 정보 조회**
    * `GET /v1/book/{isbn}`: ISBN을 통해 알라딘에서 단일 도서 정보를 가져옵니다.
    * `GET /v1/book/search?q={query}`: 검색어를 통해 알라딘에서 도서를 검색합니다.
* **내 서재 및 독서 기록 조회/수정**
    * `POST /v1/book/save`: 도서 정보를 저장하고 독서 기록을 시작합니다. `status` 필드를 포함할 수 있습니다.
    * `GET /v1/readingLog?isbn={isbn}`: 특정 ISBN의 책에 대한 내 독서 기록 상세 정보(노트, 별점, 상태 등)를 조회합니다.
    * `POST /v1/readingLog/update`: 독서 기록의 노트(`note1`, `note2`, `note3`) 내용을 수정합니다.
    * `POST /v1/readingLog/rating/update?isbn={isbn}&star={star}`: 특정 책의 별점을 업데이트합니다.
    * `GET /v1/status/{isbn}`: 특정 ISBN의 책에 대한 현재 독서 상태를 조회합니다.
    * `POST /v1/status/{isbn}/update?status={status}`: 특정 책의 독서 상태를 업데이트합니다.
    * `GET /v1/book/Library?status={status}`: 특정 독서 상태(`READING`, `FINISHED` 등)에 해당하는 내 서재 목록을 조회합니다.
    * `GET /v1/book/calendar/finished`: 완료된 책들의 월별 캘린더 데이터를 조회합니다.

### 📦 컬렉션 관리 API

* `GET /v1/collection/list`: 내가 만든 모든 컬렉션 목록과 각 컬렉션에 포함된 책들의 커버 이미지를 조회합니다.
* `GET /v1/collection/{name}`: 특정 컬렉션에 포함된 모든 책의 상세 정보를 조회합니다.
* `POST /v1/collection/create?name={name}`: 새로운 책 컬렉션을 생성합니다.
* `POST /v1/collection/{name}/book/add`: 특정 컬렉션에 책을 추가합니다. 요청 본문에는 `BookSaveCommand` 형태의 책 정보가 필요합니다.
* `DELETE /v1/collection/{name}/book/remove?isbn={isbn}`: 특정 컬렉션에서 ISBN에 해당하는 책을 제거합니다.
* `DELETE /v1/collection/delete?name={name}`: 특정 컬렉션을 삭제합니다.

---
