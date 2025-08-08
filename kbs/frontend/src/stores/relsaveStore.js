// stores/relsaveStore.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { format } from 'date-fns'
import { getRelOrdListWaiting, getRelDetails, getLotsByPcode } from '@/api/distribution'
// ↑ LOT별 재고 조회 API(getLotsByPcode)는 따로 구현 필요
//    GET /api/materials/lots?pcode=... → [{ lotNo, pcode, qty, expDt, ... }]

export const useRelsaveStore = defineStore('relsave', () => {
  // ====== 상태 ======
  const formData = ref({})        // 출고 지시서 마스터 데이터
  const releaseList = ref([])     // "출고지시서 불러오기" 모달 목록
  const products = ref([])        // 위쪽 테이블(제품별 지시수량)
  const allocations = ref([])     // 아래쪽 테이블(LOT별 할당 내역)
  const lotStockMap = ref(new Map()) // LOT 재고 캐시 (lotNo → { lotNo, pcode, qty, expDt, ... })

  // ====== 마스터 입력 초기화 ======
  const resetForm = () => { formData.value = {} }

  // ====== 마스터 목록 조회 (출고 지시서 목록) ======
  const fetchRelsaves = async () => {
    const { data } = await getRelOrdListWaiting()
    const rows = Array.isArray(data) ? data : []
    // 날짜 포맷 처리
    releaseList.value = rows.map(it => ({
      ...it,
      relDtStr: it.relDt ? format(new Date(it.relDt), 'yyyy-MM-dd') : ''
    }))
  }

  // ====== 상세 조회 (마스터 선택 시 제품 목록 채움) ======
  const fetchRelDetails = async (relMasCd) => {
    if (!relMasCd) return
    const { data } = await getRelDetails(relMasCd)
    const rows = Array.isArray(data) ? data : []

    // products 테이블 세팅
    products.value = rows.map(r => ({
      pcode:     r.pcode,         // 제품코드
      prodName:  r.prodName,      // 제품명
      ordQty:    r.ordQty ?? 0,   // 요청수량
      relOrdQty: r.relOrdQty ?? 0 // 지시수량
    }))

    // 마스터 코드 저장
    formData.value.relMasCd = relMasCd

    // 기존 배분 내역/LOT캐시 초기화
    allocations.value = []
    lotStockMap.value = new Map()
  }

  // ====== 헬퍼 함수: 특정 제품 배분합 구하기 ======
  const getAllocatedQtyByPcode = (pcode) =>
    allocations.value
      .filter(a => a.pcode === pcode)
      .reduce((sum, a) => sum + Number(a.allocQty || 0), 0)

  // ====== 헬퍼 함수: 특정 LOT 배분합 구하기 ======
  const getAllocatedQtyByLot = (lotNo) =>
    allocations.value
      .filter(a => a.lotNo === lotNo)
      .reduce((sum, a) => sum + Number(a.allocQty || 0), 0)

  // ====== LOT 재고 조회 (한번 로드된 pcode는 캐시 사용) ======
  const ensureLotsLoaded = async (pcode) => {
    // 이미 해당 제품의 LOT 재고가 로드되어 있다면 재조회 안함
    const has = Array.from(lotStockMap.value.values()).some(l => l.pcode === pcode)
    if (has) return

    // API 호출 후 캐시에 저장
    const { data } = await getLotsByPcode(pcode) // [{ lotNo, pcode, qty, expDt, ... }]
    ;(data ?? []).forEach(l => {
      lotStockMap.value.set(l.lotNo, l)
    })
  }

  // ====== 전체 자동배분(FEFO: 유통기한 오름차순) ======
  const autoDistributeAll = async () => {
    for (const row of products.value) {
      await autoDistributeOne(row.pcode)
    }
  }

  // ====== 특정 제품 자동배분(FEFO) ======
  const autoDistributeOne = async (pcode) => {
    const row = products.value.find(p => p.pcode === pcode)
    if (!row) return

    // LOT 재고 보장 (없으면 조회)
    await ensureLotsLoaded(pcode)

    // 해당 제품의 LOT만 추출 → 유통기한 오름차순 정렬(FEFO)
    const lots = Array.from(lotStockMap.value.values())
      .filter(l => l.pcode === pcode)
      .sort((a, b) => new Date(a.expDt) - new Date(b.expDt))

    // 배분 필요 수량 계산(지시수량 - 이미 배분된 수량)
    let need = Math.max(0, Number(row.relOrdQty) - getAllocatedQtyByPcode(pcode))
    if (need <= 0) return

    // LOT별 재고에서 필요 수량만큼 순서대로 배분
    for (const lot of lots) {
      const stock = lotStockMap.value.get(lot.lotNo)
      const lotRemain = Math.max(0, Number(stock.qty) - getAllocatedQtyByLot(lot.lotNo))
      if (lotRemain <= 0) continue

      const take = Math.min(need, lotRemain)
      if (take > 0) {
        allocations.value.push({
          pcode,
          prodName: row.prodName,
          lotNo: lot.lotNo,
          allocQty: take
        })
        need -= take
      }
      if (need === 0) break // 필요 수량 모두 채우면 종료
    }
  }

  // ====== 아래 테이블 데이터(LOT별 수량 + 잔여재고) ======
  const allocationRows = computed(() => {
    return allocations.value.map(a => {
      const stock = lotStockMap.value.get(a.lotNo)
      const totalForLot = getAllocatedQtyByLot(a.lotNo)
      const remain = stock ? Math.max(0, Number(stock.qty) - totalForLot) : ''
      return { ...a, remainQty: remain }
    })
  })

  return {
    // 상태
    formData,
    releaseList,
    products,
    allocations,
    allocationRows,
    // 액션
    fetchRelsaves,
    fetchRelDetails,
    autoDistributeAll,
    autoDistributeOne
  }
})
