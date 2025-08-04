# 창고 구역 용량 관리 개선 방안

## 현재 문제점
- DB에서 `vol` 값이 100으로 고정됨
- 실제 구역별 차등 용량 관리 불가
- 자재별 적재 기준(부피/중량/수량) 미분화

## 해결 방안

### 1. DB 스키마 개선 (백엔드)

#### A. 창고구역 테이블 (warehouse_area)
```sql
ALTER TABLE warehouse_area ADD COLUMN max_volume DECIMAL(10,2) COMMENT '최대 부피(㎥)';
ALTER TABLE warehouse_area ADD COLUMN max_weight DECIMAL(10,2) COMMENT '최대 중량(kg)';
ALTER TABLE warehouse_area ADD COLUMN max_count INT COMMENT '최대 수량(개)';
ALTER TABLE warehouse_area ADD COLUMN capacity_unit VARCHAR(10) DEFAULT 'volume' COMMENT '관리 기준 단위';
ALTER TABLE warehouse_area ADD COLUMN area_width DECIMAL(5,2) COMMENT '구역 가로(m)';
ALTER TABLE warehouse_area ADD COLUMN area_depth DECIMAL(5,2) COMMENT '구역 세로(m)';
ALTER TABLE warehouse_area ADD COLUMN area_height DECIMAL(5,2) COMMENT '구역 높이(m)';
```

#### B. 자재 테이블 (material)
```sql
ALTER TABLE material ADD COLUMN unit_volume DECIMAL(8,4) COMMENT '단위당 부피(㎥)';
ALTER TABLE material ADD COLUMN unit_weight DECIMAL(8,4) COMMENT '단위당 중량(kg)';
ALTER TABLE material ADD COLUMN density DECIMAL(6,2) COMMENT '밀도(kg/㎥)';
ALTER TABLE material ADD COLUMN packaging_type VARCHAR(20) COMMENT '포장 형태';
```

### 2. 용량 계산 로직

#### A. 부피 기준 계산
```javascript
const volumeCapacity = area_width * area_depth * area_height * 0.8; // 80% 활용률
```

#### B. 중량 기준 계산
```javascript
const weightCapacity = floor_max_load_per_sqm * (area_width * area_depth);
```

#### C. 수량 기준 계산
```javascript
const countCapacity = Math.floor(volumeCapacity / material.unit_volume);
```

### 3. 적재 검증 로직

```javascript
// 다중 조건 검증
function canPlace(area, material, quantity) {
    const neededVolume = material.unit_volume * quantity;
    const neededWeight = material.unit_weight * quantity;
    
    const volumeOK = (area.current_volume + neededVolume) <= area.max_volume;
    const weightOK = (area.current_weight + neededWeight) <= area.max_weight;
    const countOK = (area.current_count + quantity) <= area.max_count;
    
    return volumeOK && weightOK && countOK;
}
```

### 4. 단계별 구현 계획

#### Phase 1: 임시 해결책 (현재)
- 프론트엔드에서 구역 코드 기반 용량 추정
- 기본 단위별 용량 차등 적용

#### Phase 2: 기본 용량 설정
- 창고구역별 실제 물리적 크기 측정
- DB에 기본 용량 데이터 입력

#### Phase 3: 자재별 속성 추가
- 자재별 부피/중량 정보 수집
- 포장 형태별 적재 효율 고려

#### Phase 4: 고도화
- AI 기반 최적 적재 배치
- 실시간 용량 모니터링
- 예측 분석 기반 공간 효율화

## 추천 우선순위

1. **즉시**: 현재 임시 해결책 적용
2. **1주 내**: DB 스키마 확장 + 기본 용량 데이터 입력
3. **2주 내**: 자재별 속성 데이터 수집
4. **1개월 내**: 고도화된 적재 검증 로직 구현
