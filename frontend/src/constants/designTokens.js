// 여러 목록 페이지(회원/관리자/문의/장애 등)에서 반복해서 쓰던 색상·그림자 값을 한 곳에 모았습니다.
// 디자인이 바뀌면 페이지마다 돌아다니며 고치지 말고 이 파일 값만 수정하면 됩니다.

// 검색 버튼, 표 헤더 글자, 번호 배지 등에 공통으로 쓰이는 남색
export const PRIMARY_NAVY = '#1E2A4A';

// RowTable 안 검색 영역의 "검색" 버튼 배경색
export const SEARCH_BTN_COLOR = PRIMARY_NAVY;

// RowTable 헤더(번호/이름/상태 등 컬럼명) 글자 색상
export const TABLE_HEADER_TEXT = PRIMARY_NAVY;

// 표 안의 일반 데이터 글자 색상. 필드마다 진하기가 들쭉날쭉하던 걸 이 색 하나로 통일했다.
// (상태 배지처럼 의미가 있는 색은 그대로 두고, "그냥 텍스트"만 이 색을 쓴다)
export const TABLE_TEXT_COLOR = '#2E2E2E';

// 통계 카드, 표 카드 등 흰 배경 카드에 공통으로 쓰는 은은한 그림자
export const SOFT_SHADOW = '0 8px 20px -10px rgba(30, 42, 74, 0.18)';
