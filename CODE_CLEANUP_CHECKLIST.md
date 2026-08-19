# 코드 정리 체크리스트

실제 코드를 다 뒤져서 찾은 것만 적었습니다(추측 아님). 파일 경로 + 줄 번호 + 왜 문제인지 + 어떻게 고치면 되는지 순서로 정리했습니다. 초보자 기준으로 풀어 썼습니다.

---

## 1. 하드코드

### 1-1. 프론트 API 주소가 통째로 고정돼 있음 (제일 중요)

**파일**: `frontend/src/api/axiosInstance.js` 4번째 줄

```js
baseURL: 'http://localhost:9000',
```

**왜 문제냐면**: 이 값이 코드에 그대로 박혀 있으면, 나중에 Vercel 같은 곳에 프론트를 배포해도 그 배포된 사이트는 여전히 "내 컴퓨터의 localhost:9000"을 찾으러 갑니다. 즉 배포하는 순간 무조건 고장 납니다.

**어떻게 고치나**:
1. `frontend/.env` 파일을 만들고(없으면) 이렇게 씁니다:
   ```
   VITE_API_BASE_URL=http://localhost:9000
   ```
2. `axiosInstance.js`를 이렇게 바꿉니다:
   ```js
   baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:9000',
   ```
3. 나중에 Vercel에 배포할 때는 Vercel 프로젝트 설정의 "Environment Variables"에 `VITE_API_BASE_URL`을 실제 배포된 백엔드 주소로 등록하면 됩니다.

> 참고로 `backend/.../config/SecurityConfig.java`에 있는 CORS 허용 주소 목록(`localhost:5173`, `localhost:9000`, `https://sm-console.vercel.app`)은 하드코드처럼 보여도 **이건 고치지 마세요** — 허용할 주소를 코드에 명시적으로 박아두는 게 CORS 설정의 정석입니다.

### 1-2. 페이지 사이즈(`size`)가 숫자로 여기저기 흩어져 있음

**파일들**:
- `frontend/src/pages/AdminListPage.jsx` 19번째 줄 — `size: 10`
- `frontend/src/pages/AuditLogPage.jsx` 22번째 줄 — `size: 8`
- `frontend/src/pages/IncidentListPage.jsx` 23번째 줄 — `size: 10`
- `frontend/src/pages/IncidentMonitoringPage.jsx` 46번째 줄 — `size: 7`
- `frontend/src/pages/InquiryListPage.jsx` 24번째 줄 — `size: 10`
- `frontend/src/pages/UserListPage.jsx` 27, 44, 67, 259번째 줄 — `size: 10` (한 파일에 4번 반복!)

**왜 문제냐면**: 예를 들어 `UserListPage.jsx`의 페이지당 개수를 10에서 20으로 바꾸고 싶으면, 저 4곳을 전부 찾아서 고쳐야 합니다. 하나라도 빠뜨리면 화면마다 페이지당 개수가 서로 다르게 나오는 버그가 생깁니다.

**어떻게 고치나**: 각 파일 위쪽에 상수 하나만 선언하고 그걸 재사용하면 됩니다.
```js
const PAGE_SIZE = 10; // 파일 맨 위, import 아래에 선언

// 이후 size: 10 이라고 쓴 곳을 전부 size: PAGE_SIZE 로 교체
```

### 1-3. "SLA 임박" 기준 24시간이 그냥 숫자로 박혀 있음

**파일**: `frontend/src/components/Layout/Header.jsx` 43~44번째 줄
```js
const hoursLeft = (dueDate - today) / (1000 * 60 * 60);
return hoursLeft <= 24;
```

**왜 문제냐면**: "24"가 왜 24인지(정책 값인지, 그냥 예시로 넣은 건지) 코드만 보면 알 수 없고, 나중에 "12시간 전부터 임박 표시로 바꿔줘" 같은 요청이 오면 이 숫자를 찾아서 고쳐야 하는데 이름이 없어서 검색하기도 애매합니다.

**어떻게 고치나**:
```js
const SLA_URGENT_THRESHOLD_HOURS = 24; // 파일 위쪽에 선언

// ...
return hoursLeft <= SLA_URGENT_THRESHOLD_HOURS;
```

> 참고로 `backend/.../admin/service/AdminService.java` 27번째 줄의 `MIN_PASSWORD_LENGTH = 4`는 위와 반대로 **이미 잘 뽑아놓은 예시**입니다. 매직넘버를 상수로 뽑을 때 이런 식으로 하면 됩니다.

### 1-4. (추가로 찾은 것) Redis 캐시 TTL, 외부 API 주소도 숫자/문자열이 그대로 박혀 있음

- `backend/.../config/RedisCacheConfig.java` 32번째 줄: `.entryTtl(Duration.ofMinutes(10))` — 캐시 유지시간 10분이 이름 없이 그냥 숫자로 박혀 있습니다.
- `backend/.../externalapi/ExternalUserService.java` 23번째 줄: `String url = "https://randomuser.me/api/?results=5&nat=kr";` — 더미 회원 몇 명 생성할지(`results=5`)가 주소 문자열 안에 섞여 있습니다.
- 같은 파일 43~46번째 줄: 가짜 아이디/전화번호 만들 때 `90000`, `10000` 같은 숫자가 그냥 계산식에 박혀 있습니다.

이건 개발용 시드 데이터 생성 코드라 심각도는 낮지만, 시간 되시면 상수로 뽑아두시면 좋습니다. (`RedisCacheConfig`는 캐싱을 실제로 켜실 때 같이 정리하시면 됩니다.)

---

## 2. 유지보수를 해치는 코드

### 2-1. "복합 검색"이 사실 복합이 아니라 if-else 중 하나만 선택하는 구조

**파일들**:
- `backend/.../user/service/UserService.java` 의 `getSearch()` 메서드
- `backend/.../admin/service/AdminService.java` 의 `getAdmins()` 메서드
- `backend/.../inquiry/service/InquiryService.java` 의 `getInquiry()` 메서드

지금 이런 구조입니다:
```java
if (loginId != null && !loginId.isEmpty()) {
    users = userRepository.findByLoginIdContaining(loginId, pageable);
} else if (name != null && ...) {
    users = userRepository.findByName(name, pageable);
} else if (status != null) {
    users = userRepository.findByStatus(status, pageable);
} else {
    users = userRepository.findAll(pageable);
}
```

**왜 문제냐면**: 이름과 상태를 동시에 입력해서 검색해도, 실제로는 **이름만** 반영되고 상태는 무시됩니다(`else if`라서 둘 다 만족해도 하나만 탐). 검색 조건이 하나 늘어날 때마다 이 분기가 기하급수적으로 늘어나서 나중엔 손대기 무서워집니다.

**어떻게 고치나**: Spring Data JPA의 `Specification`을 쓰면 조건을 조합할 수 있습니다(관련해서 위쪽 대화에서 예시 코드 드렸었습니다 — 필요하시면 다시 요청 주세요).

### 2-2. 색상 매핑 객체가 파일마다 통째로 복붙돼 있음

**파일**: `frontend/src/pages/UserListPage.jsx`와 `frontend/src/components/UserDetailModal.jsx`

두 파일에 **완전히 똑같은** 객체가 각각 따로 선언돼 있습니다:
```js
const statusColors = {
  ACTIVE: { bg: '#d1e7dd', text: '#0f5132' },
  DORMANT: { bg: '#fff3cd', text: '#664d03' },
  WITHDRAWN: { bg: '#f8d7da', text: '#842029' }
};
```

**왜 문제냐면**: 나중에 "휴면 상태 색을 노란색에서 주황색으로 바꿔줘" 요청이 오면 두 파일 다 고쳐야 하는데, 하나 빠뜨리면 목록화면 색이랑 상세모달 색이 서로 달라지는 버그가 생깁니다. (`InquiryListPage.jsx`, `IncidentListPage.jsx`, `AuditLogPage.jsx`도 각자 자기 도메인용 색상 객체를 파일 안에 따로 갖고 있는 건 같은 패턴입니다.)

**어떻게 고치나**: `frontend/src/constants/statusColors.js` 같은 파일을 하나 만들어서 `export const USER_STATUS_COLORS = {...}`로 빼두고, 두 파일에서 import해서 씁니다.

### 2-3. 검색/목록 다시 불러오기 코드가 한 파일 안에서 4번 복붙됨

**파일**: `frontend/src/pages/UserListPage.jsx` 27, 44, 67, 259번째 줄

`getUser({ name, status, page, size: 10 })`(파라미터 조합만 살짝 다름)가 `useEffect`, `handleSearch`, `handleUpload`, 모달의 `onUpdated` 콜백 이렇게 4곳에 따로따로 써 있습니다.

**왜 문제냐면**: 같은 로직 4곳을 관리해야 하는 것도 문제고, 실제로 `AdminListPage.jsx`나 `InquiryListPage.jsx`, `IncidentListPage.jsx`는 전부 `fetchAdmins()`, `fetchInquiry()`, `fetchIncidents()`처럼 **함수 하나로 뽑아서** 재사용하고 있는데, `UserListPage.jsx`만 이 패턴을 안 따르고 있어서 프로젝트 안에서도 일관성이 깨져 있습니다.

**어떻게 고치나**: 다른 페이지들처럼 `fetchUsers()` 함수 하나로 뽑아서 4곳에서 그 함수를 호출하도록 바꾸면 됩니다.

### 2-4. 검색창/버튼 인라인 스타일이 파일마다 4~5번씩 반복

**파일**: `AdminListPage.jsx`, `AuditLogPage.jsx`, `IncidentListPage.jsx`, `InquiryListPage.jsx`, `UserListPage.jsx` 전부

```jsx
style={{ maxWidth: '200px', height: '44px', borderRadius: '8px' }}
```
같은 스타일 객체가 검색 인풋, 셀렉트, 버튼마다 파일 하나에 4~5번씩 반복됩니다.

**왜 문제냐면**: 나중에 "검색창 높이를 44px에서 40px로 통일해줘" 하면 5개 파일 x 파일당 4~5곳, 도합 20곳 넘게 손으로 고쳐야 합니다.

**어떻게 고치나**: `App.css`에 `.search-input { height: 44px; border-radius: 8px; }` 같은 클래스를 만들어서 `className`으로 붙이는 쪽으로 바꾸면 한 곳만 고치면 됩니다.

### 2-5. 페이지네이션(이전/다음) 마크업이 5개 파일에 통째로 복붙됨

**파일**: `AdminListPage.jsx`, `AuditLogPage.jsx`, `IncidentListPage.jsx`, `InquiryListPage.jsx`, `UserListPage.jsx`

"이전"/"다음" 버튼 + 페이지 번호 표시하는 JSX 블록(10줄 정도)이 5개 파일에 똑같이 복붙돼 있습니다.

**어떻게 고치나**: `components/Pagination.jsx`를 하나 만들어서 `page`, `totalPages`, `onPageChange` props만 받게 하고, 5개 파일에서 그 컴포넌트를 불러다 쓰면 코드가 훨씬 짧아지고 한 곳만 관리하면 됩니다.

---

## 3. 실무자가 보면 "AI가 짠 코드"라고 바로 알아챌 만한 것들

### 3-1. 완전히 똑같은 주석 문구가 여러 파일에 토씨 하나 안 틀리고 반복됨

이게 제일 강력한 신호입니다. 사람이 각 화면을 따로따로 작업했다면 주석 표현이 파일마다 조금씩 다른 게 자연스러운데, 지금은 **글자 하나까지 똑같습니다**:

- `"실무형 플랫 화이트 디자인"` — `AdminListPage.jsx:67`, `IncidentListPage.jsx:53`, `InquiryListPage.jsx:54`, `UserListPage.jsx:80` (4곳, 토씨 하나 안 틀림)
- `"바깥쪽 여백 확보"` — `AdminListPage.jsx:104`, `AuditLogPage.jsx:163`, `AuditLogAnalyzePage.jsx:34`, `IncidentListPage.jsx:90`, `InquiryListPage.jsx:91`, `UserListPage.jsx:117` (5~6곳)

**조치**: 이런 주석을 지우거나, 파일마다 자기 화면에 맞는 말로 다시 쓰시길 추천합니다. (예: "상단 통계 카드" 정도로 짧게 줄이거나, 아예 지워도 무방한 수준의 주석입니다.)

### 3-2. 기초 문법 자체를 설명하는 주석 — 실무자는 절대 안 씁니다

- `backend/.../user/service/UserService.java` 138~141번째 줄:
  ```java
  //"*".repeat(id.length() - 3) --> 별표(*)만 여러 개 (예: "**")
  //id.charAt(id.length() - 1) --> 원본 글자 중 마지막 것 딱 하나 (예: "1")
  // substring은 "문자열의 일부분을 잘라내서 가져오는" 메서드
  ```
  `repeat()`, `charAt()`, `substring()`은 자바 표준 라이브러리 기본 메서드입니다. 이게 뭘 하는지 주석으로 설명해놓은 건 "나 자바 방금 배웠어요"로 읽힙니다.

- `backend/.../config/SecurityConfig.java` 23, 26번째 줄:
  ```java
  //@Configuration --> Spring에게 "이 클래스 안에는 @Bean이라고 표시된 메서드들이 있을 거고...
  //@RequiredArgsConstructor --> "final 필드들을 자동으로 채우는 생성자를 만들어달라
  ```
  스프링 기본 어노테이션이 뭘 하는지 설명하는 주석입니다. Spring Security 설정 파일까지 짤 수준이면 이건 이미 아는 게 당연하다고 보기 때문에, 실무자 눈엔 부자연스럽습니다.

- `backend/.../notification/SlackNotificationService.java` 29번째 줄:
  ```java
  //payload란 "실어 보낼 짐(데이터)"이라는 뜻, Map(key-value)
  ```
  영어 단어 뜻풀이 주석입니다.

- `backend/.../externalapi/ExternalUserInitializer.java` 23번째 줄:
  ```java
  //서버가 켜질 때 자동으로 실행되는 메서드
  ```
  `CommandLineRunner`가 뭔지 설명하는 초심자용 주석입니다.

**조치**: 이 주석들은 지우시는 걸 추천합니다. "왜 이렇게 했는지"(비즈니스 이유) 설명은 남기고, "이 문법/메서드가 뭘 하는지" 설명만 골라서 지우면 됩니다. 구분 기준: 그 줄을 지워도 자바/스프링을 아는 사람이 코드를 이해하는 데 지장이 없으면 지워도 되는 주석입니다.

### 3-3. (추가로 찾은 것) 완전히 똑같은 주석이 서로 다른 두 파일에 또 있음 — 이번엔 여러 줄짜리

3-1보다 더 확실한 증거입니다. **여러 줄짜리 주석 블록이 토씨 하나 안 틀리고** 두 파일에 그대로 복사돼 있습니다:

- `backend/.../excel/UserExcelService.java` 63~64번째 줄
- `backend/.../excel/AuditLogExcelService.java` 63~64번째 줄

```java
//ByteArrayOutputStream으로 변환
//자바 메모리 안에 있는 객체일 뿐, 아직 "파일"이 아니에요. 이걸 실제로 브라우저에 전송하려면 바이트(byte) 형태로 바꿔야 함
```

회원 엑셀 내보내기랑 감사로그 엑셀 내보내기는 완전히 다른 기능인데, 이 설명 문구가 토씨 하나 안 틀리고 똑같습니다. 사람이 각각 짰다면 있을 수 없는 일입니다. 게다가 `ByteArrayOutputStream`이 뭔지 설명하는 것 자체도 3-2와 같은 종류(기초 API 설명)입니다.

같은 파일(`UserExcelService.java`) 31~32번째 줄에도 비슷한 게 있습니다:
```java
// Workbook, Sheet 만들기
// 파일(Workbook) → 그 안에 시트(Sheet)들 → 시트 안에 행(Row)들 → 행 안에 셀(Cell)들
```
Apache POI 라이브러리의 Workbook/Sheet/Row/Cell 구조를 설명하는 주석입니다 — 역시 기초 설명.

그리고 `frontend/src/api/authApi.js` 4~7번째 줄에도 같은 종류가 있습니다:
```js
//실제로 보낼 데이터 (아이디/비번을 폼 형식으로 변환)
axiosInstance.post('/login', new URLSearchParams ({ loginId, password }),
//"이 데이터는 form 형식(x-www-form-urlencoded)이다"라고 서버에 알려줌
{headers: { 'Content-Type': 'application/x-www-form-urlencoded' }}
```
HTTP 요청 헤더가 뭘 하는지 설명하는 주석입니다.

**조치**: 위 3곳 다 지우시면 됩니다. 지워도 코드 이해에 전혀 지장 없습니다.

### 3-4. 프로젝트 전체에 걸쳐 주석 밀도/톤이 기계처럼 균일함

파일 하나하나를 보면 다 괜찮은데, **전체를 쭉 훑어보면** 어느 파일을 열어도 "왜 이렇게 짰는지"를 설명하는 주석이 거의 같은 밀도, 같은 문체로 붙어 있습니다. 사람이 몇 주에 걸쳐 짠 코드는 보통 파일마다 주석이 많다 적다 들쭉날쭉한데, 이 프로젝트는 그게 아주 고르게 유지되고 있다는 것 자체가 패턴으로 읽힐 수 있습니다.

**조치**: 특별히 손볼 "한 줄"이 있는 문제는 아니고, 위 3-1/3-2 정리하시면서 자연스럽게 파일마다 주석 양이 들쭉날쭉해지게 두시면 됩니다. 일부러 다 지울 필요는 없고, 정말 중요한 이유(트레이드오프, 왜 이 방식을 선택했는지)만 남기고 나머지는 쳐내시면 자연스러워집니다.

---

## 요약: 손대실 때 우선순위 추천

1. `axiosInstance.js` baseURL (1-1) — 배포 막는 진짜 버그라 제일 먼저
2. 3-1, 3-2 주석 정리 — 한 번 훑으면서 지우는 거라 시간 얼마 안 걸림
3. 2-2, 2-3, 2-5 중복 코드 — 시간 되는 만큼
4. 1-2, 1-3 매직넘버 상수화 — 여유 있으면
5. 2-1 검색 구조 개선 — 제일 손이 많이 가니 맨 마지막
