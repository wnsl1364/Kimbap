# InputTable 컬럼별 글자색 지정 기능

InputTable 컴포넌트에서 각 컬럼별로 유동적인 글자색을 지정할 수 있는 기능이 추가되었습니다.

## 사용 방법

컬럼 정의에 `textColor` 속성을 추가하여 글자색을 지정할 수 있습니다.

### 1. 고정 색상 지정 (CSS 클래스)

```javascript
const columns = [
  {
    field: 'productName',
    header: '상품명',
    type: 'readonly',
    textColor: 'text-blue-600' // Tailwind CSS 클래스
  },
  {
    field: 'status',
    header: '상태',
    type: 'readonly',
    textColor: 'text-green-500' // Tailwind CSS 클래스
  }
];
```

### 2. 고정 색상 지정 (직접 색상값)

```javascript
const columns = [
  {
    field: 'price',
    header: '가격',
    type: 'input',
    inputType: 'number',
    textColor: '#ff6b6b' // 직접 색상값
  },
  {
    field: 'description',
    header: '설명',
    type: 'input',
    textColor: 'rgb(34, 197, 94)' // RGB 색상값
  }
];
```

### 3. 동적 색상 지정 (조건부 색상)

```javascript
const columns = [
  {
    field: 'quantity',
    header: '수량',
    type: 'readonly',
    textColor: (rowData) => {
      // 수량이 10 이하면 빨간색, 그 외는 검은색
      return rowData.quantity <= 10 ? 'text-red-500' : 'text-gray-900';
    }
  },
  {
    field: 'status',
    header: '상태',
    type: 'readonly',
    textColor: (rowData) => {
      // 상태에 따른 색상 분기
      switch(rowData.status) {
        case '완료': return 'text-green-600';
        case '진행중': return 'text-blue-600';
        case '대기': return 'text-yellow-600';
        case '취소': return 'text-red-600';
        default: return 'text-gray-600';
      }
    }
  },
  {
    field: 'totalAmount',
    header: '총액',
    type: 'readonly',
    textColor: (rowData) => {
      // 직접 색상값으로 반환
      return rowData.totalAmount > 100000 ? '#dc2626' : '#059669';
    }
  }
];
```

### 4. 복합 조건 예제

```javascript
const columns = [
  {
    field: 'stockLevel',
    header: '재고수준',
    type: 'readonly',
    textColor: (rowData) => {
      const stock = rowData.stockLevel;
      const minStock = rowData.minStockLevel || 0;
      
      if (stock <= 0) {
        return 'text-red-700 font-bold'; // 재고 없음: 빨간색 + 굵게
      } else if (stock <= minStock) {
        return 'text-orange-600'; // 최소재고 이하: 주황색
      } else if (stock <= minStock * 2) {
        return 'text-yellow-600'; // 재고 부족: 노란색
      } else {
        return 'text-green-600'; // 충분한 재고: 초록색
      }
    }
  }
];
```

## 지원되는 컬럼 타입

다음 컬럼 타입들에서 `textColor` 속성을 사용할 수 있습니다:

- `readonly`: 읽기 전용 텍스트
- `input`: 입력 필드
- `inputsearch`: 검색 가능한 입력 필드
- `select`: 선택 드롭다운
- `clickable`: 클릭 가능한 텍스트

## 주의사항

1. **CSS 클래스 vs 직접 색상값**
   - CSS 클래스 (예: `text-red-500`): `class` 속성에 적용
   - 직접 색상값 (예: `#ff0000`, `rgb(255,0,0)`): `style` 속성에 적용

2. **함수형 textColor**
   - 함수는 `rowData` 매개변수를 받아 해당 행의 전체 데이터에 접근 가능
   - 반환값은 CSS 클래스 문자열 또는 직접 색상값

3. **성능 고려사항**
   - 함수형 textColor는 각 셀마다 호출되므로 복잡한 계산은 피하는 것이 좋습니다
   - 가능하면 computed 값이나 데이터 전처리를 통해 최적화하세요

## 전체 사용 예제

```vue
<template>
  <InputTable
    :data="tableData"
    :columns="columns"
    title="상품 목록"
    dataKey="productId"
  />
</template>

<script setup>
import { ref } from 'vue';

const tableData = ref([
  {
    productId: 1,
    productName: '김밥',
    quantity: 5,
    price: 3000,
    status: '재고부족',
    totalAmount: 15000
  },
  {
    productId: 2,
    productName: '라면',
    quantity: 50,
    price: 1500,
    status: '정상',
    totalAmount: 75000
  }
]);

const columns = [
  {
    field: 'productName',
    header: '상품명',
    type: 'readonly',
    textColor: 'text-blue-600'
  },
  {
    field: 'quantity',
    header: '수량',
    type: 'input',
    inputType: 'number',
    textColor: (rowData) => {
      return rowData.quantity <= 10 ? 'text-red-500' : 'text-gray-900';
    }
  },
  {
    field: 'price',
    header: '단가',
    type: 'input',
    inputType: 'number',
    textColor: '#059669'
  },
  {
    field: 'status',
    header: '상태',
    type: 'readonly',
    textColor: (rowData) => {
      switch(rowData.status) {
        case '정상': return 'text-green-600';
        case '재고부족': return 'text-red-600';
        case '품절': return 'text-gray-500';
        default: return 'text-gray-900';
      }
    }
  }
];
</script>
```
