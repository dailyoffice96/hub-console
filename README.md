# SM Console

회원, 문의, 장애 및 시스템 설정을 관리하기 위한 관리자용 운영 콘솔입니다.

## 주요 기능

- 로그인 및 세션 기반 인증
- 관리자 회원 목록 조회 및 권한 필터
- 문의 도메인 CRUD
- APM 장애 감지 및 장애 심각도 시각화
- Webhook 및 Slack 알림 연동
- 조회 인덱스 및 캐싱을 통한 성능 개선
- 낙관적 락을 이용한 동시 수정 방지

## 기술 스택

### Frontend

- React
- JavaScript
- Vite
- Vercel

### Backend

- [실제 백엔드 기술 입력]
- REST API
- [실제 데이터베이스 입력]

## 실행 방법

### Frontend

```bash
cd frontend
npm install
npm run dev
```

### Backend

```bash
cd backend
[실제 백엔드 실행 명령어]
```

## 프로젝트 구조

```text
sm-console/
├── frontend/
├── backend/
└── README.md
```

## 주요 작업 내역

- `feat:` 로그인 및 세션 기반 인증 처리
- `feat:` 관리자 회원 목록 조회 및 권한 필터 구현
- `feat:` 문의 도메인 CRUD 및 관리자 권한 검증
- `refactor:` 도메인 패키지 계층 구조 정리
- `test:` 회원 및 문의 서비스 핵심 로직 테스트 추가
- `perf:` 조회 인덱스 및 캐싱 적용
- `feat:` 낙관적 락 기반 동시 수정 방지
- `feat:` APM 장애 감지 및 Webhook 알림 연동
- `fix:` Vercel 배포 주소 CORS 허용
- `chore:` 환경변수 템플릿 및 비밀정보 관리 정리