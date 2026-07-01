# Sports-wms
KB IT's Your Life 7기 25회차 사이드 프로젝트 (WMS 창고 관리 시스템)
<hr>

## 개요
해당 WMS 시스템은 저가부터 고가까지의 다양한 스펙의 라켓, 소모성이 높고 온도에 민감한 셔틀콕, 다양한 사이즈와 옵션이 있는 신발 등 상품군의 특징이 명확한 배드민턴 용품의 물류 처리를 최적화하기 위해 기획되었습니다. 크기와 중량이 다양하고 복잡한 스포츠 용품의 특성을 반영하여, 입고부터 출고, 반품까지의 전 과정을 시스템화하고 창고 운영의 효율성을 극대화하는 것을 목표로 합니다.
<hr>

## 기술 스택
### Back
- Java 17
- Spring Boot 4.0.6
- Spring Data JPA
- Spring Security
- Lombok
- Springdoc OpenAPI 2.8.9

### Front
- Vue 3
- Vue Router 4
- Pinia
- Vue CLI 5

### DB
- MySQL 8.x

### Test
-

## 프로젝트 구조
```
src/main/java/com/example/sportswms/
│
├── domain/
│   ├── user/         # 회원, 인증
│   ├── product/      # 브랜드, 카테고리, 상품, SKU
│   ├── warehouse/    # 창고, 구역, 창고 관리자 배정
│   ├── inventory/    # 재고, 재고 변동 기록
│   ├── order/        # 발주, 지점
│   ├── inbound/      # 입고
│   └── outbound/     # 출고
│
└── global/
    ├── config/       # QueryLoggingInterceptor, WebConfig 등
    ├── exception/    # GlobalExceptionHandler, AccessDeniedException
    ├── init/         # 더미 데이터 초기화
    ├── security/     # SecurityConfig, AccessValidator
    └── util/         # MessageUtils

src/front/src/
├── api/              # HTTP 클라이언트
├── router/           # Vue Router
├── stores/           # Pinia (auth store)
└── views/            # 페이지 컴포넌트
```
<hr>

## 역할 및 권한

| 역할 | 설명 | 주요 기능 |
|---|---|---|
| `ROLE_GENERAL_MANAGER` | 본사 관리자 | 전체 조회, 상품/창고/지점 등록, 발주 창고 배정 |
| `ROLE_WAREHOUSE_MANAGER` | 창고 관리자 | 담당 창고 입고/출고/재고 관리 |
| `ROLE_USER` | 점주 | 발주 요청, 발주 취소, 수령 완료 처리 |
<hr>

## 주요 업무 흐름

### 입고 흐름
```
입고 등록 (창고관리자)
  → 상태 전이: PENDING → RECEIVED → DELIVERING → DELIVERED (본사관리자)
  → 검수 시작 (창고관리자)
  → 불량 수량 등록
  → 정상 구역 배정 / 불량 구역 배정
  → 입고 완료 → Inventory 반영
```

### 출고 흐름
```
발주 요청 (점주)
  → 창고 배정 (본사관리자) → StockOrder + Outbound 자동 생성
  → 구역 배정 / 출고 승인 (창고관리자) → Inventory allocate
  → 피킹 시작 → 피킹 완료 → Inventory 차감
  → 배송 출발 → 수령 완료 (점주) → StockOrder 완료 처리
```
<hr>

## 실행 방법

### 사전 요구사항
- Java 17
- MySQL 8.x
- Node.js 16+

### 1. DB 설정
```sql
CREATE DATABASE dummyDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. application.properties 설정
```properties
spring.application.name=SportsWms

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/dummyDB?useSSL=false&useUnicode=true&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true
spring.datasource.username={DB_USERNAME}
spring.datasource.password={DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

# 더미 데이터 초기화 여부
wms.init.enabled=true
```

### 3. 실행
```bash
# 백엔드 + 프론트엔드 통합 빌드 후 실행
./gradlew bootRun

# 프론트엔드 개발 서버 별도 실행 (http://localhost:3000)
cd src/front
npm install
npm run serve
```
<hr>

## 테스트 계정

더미 데이터 초기화 시 아래 계정이 자동 생성됩니다. (비밀번호 공통: `Test1234!`)

| 계정 | 역할 | 배정 |
|---|---|---|
| `admin` | 본사 관리자 | 전체 |
| `wm1` ~ `wm5` | 창고 관리자 | 창고 1 ~ 5 |
| `owner1` ~ `owner10` | 점주 | 지점 1 ~ 10 |

### 로그인 API
```http
POST /login
Content-Type: application/x-www-form-urlencoded

loginId=admin&password=Test1234!
```
<hr>

## ERD
추후 기술 예정
<hr>

## API 명세서

### 인증
| Method | URL | 설명 | 권한 |
|---|---|---|---|
| POST | `/login` | 로그인 (form-data: loginId, password) | 전체 |
| POST | `/logout` | 로그아웃 | 인증 |
| POST | `/api/users/signup` | 회원가입 | 전체 |
| GET | `/api/users/me` | 내 정보 조회 | 인증 |
| GET | `/api/users?role=` | 역할별 회원 목록 | 본사관리자 |

### 상품
| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/products` | 상품 목록 조회 | 본사관리자 |
| POST | `/api/products` | 상품 등록 | 본사관리자 |
| GET | `/api/products/skus` | SKU 목록 조회 | 본사관리자 |
| POST | `/api/products/skus` | SKU 등록 | 본사관리자 |
| GET | `/api/products/brands` | 브랜드 목록 조회 | 본사관리자 |
| POST | `/api/products/brands` | 브랜드 등록 | 본사관리자 |
| GET | `/api/products/categories` | 카테고리 목록 조회 | 본사관리자 |

### 창고
| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/warehouses` | 창고 목록 조회 | 본사관리자, 창고관리자 |
| POST | `/api/warehouses` | 창고 등록 | 본사관리자 |
| GET | `/api/warehouses/my` | 내 담당 창고 목록 | 창고관리자 |
| GET | `/api/warehouses/sections` | 전체 구역 목록 | 본사관리자, 창고관리자 |
| POST | `/api/warehouses/sections` | 구역 등록 | 본사관리자 |
| GET | `/api/warehouses/managers` | 창고 관리자 배정 목록 | 본사관리자 |
| POST | `/api/warehouses/managers` | 창고 관리자 배정 | 본사관리자 |
| GET | `/api/warehouses/section-types` | 구역 타입 목록 | 본사관리자, 창고관리자 |
| GET | `/api/warehouses/management-types` | 창고 관리 타입 목록 | 본사관리자 |

### 재고
| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/inventory?warehouseId=&sectionId=&skuId=` | 재고 목록 조회 | 본사관리자, 창고관리자 |
| GET | `/api/inventory/transactions?warehouseId=&sectionId=&skuId=` | 재고 변동 기록 조회 | 본사관리자, 창고관리자 |

### 발주 / 지점
| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/orders/details` | 발주 목록 조회 (관리자: 전체, 점주: 본인 지점) | 본사관리자, 점주 |
| POST | `/api/orders` | 발주 요청 | 점주 |
| DELETE | `/api/orders/{orderGroupId}` | 발주 취소 | 점주 |
| POST | `/api/orders/assign` | 발주 창고 배정 | 본사관리자 |
| GET | `/api/orders/warehouse` | 담당 창고 발주 목록 | 창고관리자 |
| GET | `/api/orders/warehouse/{stockOrderId}/details` | 발주 상세 조회 | 창고관리자 |
| GET | `/api/orders/stores` | 지점 목록 조회 | 본사관리자, 점주 |
| POST | `/api/orders/stores` | 지점 등록 | 본사관리자 |
| GET | `/api/orders/stores/my` | 내 담당 지점 목록 | 점주 |
| GET | `/api/orders/stores/managers` | 지점 관리자 배정 목록 | 본사관리자 |
| POST | `/api/orders/stores/assign` | 지점 관리자 배정 | 본사관리자 |

### 입고
| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/inbounds` | 입고 목록 조회 | 본사관리자, 창고관리자 |
| POST | `/api/inbounds` | 입고 등록 | 창고관리자 |
| GET | `/api/inbounds/{inboundId}/details` | 입고 상세 목록 | 본사관리자, 창고관리자 |
| PATCH | `/api/inbounds/{inboundId}/status?nextStatus=` | 입고 상태 전이 (PENDING→RECEIVED→DELIVERING→DELIVERED) | 본사관리자 |
| PATCH | `/api/inbounds/{inboundId}/inspect` | 검수 시작 (DELIVERED→INSPECTING) | 창고관리자 |
| PATCH | `/api/inbounds/{inboundId}/complete` | 입고 완료 (INSPECTING→COMPLETED) | 창고관리자 |
| GET | `/api/inbounds/{inboundId}/assignable-sections` | 정상품 구역 드롭다운 | 창고관리자 |
| GET | `/api/inbounds/{inboundId}/defect-sections` | 불량품 구역 드롭다운 | 창고관리자 |
| PATCH | `/api/inbounds/{inboundId}/details/{detailId}/defect?defectQuantity=` | 불량 수량 등록 | 창고관리자 |
| PATCH | `/api/inbounds/{inboundId}/details/{detailId}/defect/reset` | 불량 수량 초기화 | 창고관리자 |
| PATCH | `/api/inbounds/{inboundId}/details/{detailId}/section?sectionId=` | 정상 구역 배정 | 창고관리자 |
| PATCH | `/api/inbounds/{inboundId}/details/{detailId}/section/clear` | 정상 구역 초기화 | 창고관리자 |
| PATCH | `/api/inbounds/{inboundId}/details/{detailId}/defect-section?sectionId=` | 불량 구역 배정 | 창고관리자 |
| PATCH | `/api/inbounds/{inboundId}/details/{detailId}/defect-section/clear` | 불량 구역 초기화 | 창고관리자 |

### 출고
| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/outbounds` | 출고 목록 조회 (역할별 필터링) | 본사관리자, 창고관리자, 점주 |
| GET | `/api/outbounds/{outboundId}/details` | 출고 상세 목록 | 본사관리자, 창고관리자, 점주 |
| GET | `/api/outbounds/{outboundId}/details/{detailId}/assignable-sections` | 피킹 구역 드롭다운 | 창고관리자 |
| PATCH | `/api/outbounds/{outboundId}/details/{detailId}/section?sectionId=` | 피킹 구역 배정 | 창고관리자 |
| PATCH | `/api/outbounds/{outboundId}/details/{detailId}/section/clear` | 피킹 구역 초기화 | 창고관리자 |
| PATCH | `/api/outbounds/{outboundId}/approve` | 출고 승인 (ASSIGNED→APPROVED) | 창고관리자 |
| PATCH | `/api/outbounds/{outboundId}/picking/start` | 피킹 시작 (APPROVED→PICKING) | 창고관리자 |
| PATCH | `/api/outbounds/{outboundId}/picking/complete` | 피킹 완료 (PICKING→PACKING) | 창고관리자 |
| PATCH | `/api/outbounds/{outboundId}/ship` | 배송 출발 (PACKING→SHIPPED) | 창고관리자 |
| PATCH | `/api/outbounds/{outboundId}/deliver` | 수령 완료 (SHIPPED→DELIVERED) | 점주 |
<hr>

## 주요 구현 사항

추후 기술 예정

<hr>

## Commit Convention
- feat : 기능 추가
- fix : 버그 수정
- docs : 문서 수정
- style : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
- refactor : 코드 리팩토링
- test : 테스트 코드, 리팩토링 테스트 코드 추가
- chore : 빌드 업무 수정, 패키지 매니저 수정
<hr>
