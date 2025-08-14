// stores/relsaveStore.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { format } from 'date-fns'
import {
  getRelOrdListWaiting, // 대기(m1) 지시서 목록
  getRelDetails,        // 마스터 → 상세(행) 목록
  getLotsByPcode,       // 제품별 LOT 재고(FIFO: inboDt)
  saveReleaseProcess
} from '@/api/distribution'

export const useRelsaveStore = defineStore('relsave', () => {
  // ===== 상태 =====
  const formData = ref({
    relDt: format(new Date(), 'yyyy-MM-dd')
  }) // 지시서 마스터

  const releaseList = ref([])        // 불러오기 모달 목록
  const products = ref([])           // 상단 테이블: 지시 상세(행)
  const allocations = ref([])        // 하단 테이블: LOT 배분 [{ _key, relOrdCd, pcode, prodName, lotNo, allocQty }]
  const lotStockMap = ref(new Map()) // LOT 캐시: lotNo -> { lotNo, pcode, qty, inboDt, wcode?, wareAreaCd? }

  // ===== 마스터 초기화 =====
  const resetForm = () => {
    formData.value = { relDt: format(new Date(), 'yyyy-MM-dd') }
    products.value = []
    allocations.value = []
    lotStockMap.value = new Map()
    releaseList.value = []
  }

  // ===== 불러오기 모달: 대기(m1) 목록 =====
  const fetchRelsaves = async () => {
    const { data } = await getRelOrdListWaiting()
    const rows = Array.isArray(data) ? data : []
    releaseList.value = rows.map(it => ({
      ...it,
      relDtStr: it.relDt ? format(new Date(it.relDt), 'yyyy-MM-dd') : ''
    }))
  }

  // ===== 마스터 선택 → 상세(행) 조회 =====
  const fetchRelDetails = async (relMasCd) => {
    if (!relMasCd) return
    const { data } = await getRelDetails(relMasCd)
    const rows = Array.isArray(data) ? data : []

    // 서버에서 relOrdCd / ordDCd를 꼭 내려줘야 함
    products.value = rows.map(r => ({
      relOrdCd:  r.relOrdCd,         // ✅ 행 식별자 & dataKey
      ordDCd:    r.ordDCd,           // (선택) 단가 조회/추가 로직에 사용
      pcode:     r.pcode,
      prodName:  r.prodName,
      ordQty:    r.ordQty ?? 0,      // 요청수량(있으면)
      relOrdQty: r.relOrdQty ?? 0    // 지시수량
    }))

    formData.value.relMasCd = relMasCd
    formData.value.cpCd = rows[0]?.cpCd || ''
    allocations.value = []           // 새 지시서 선택 시 초기화
    lotStockMap.value = new Map()
  }

  // ===== 합계 헬퍼 =====
  const getAllocatedQtyByRelOrd = (relOrdCd) =>
    allocations.value
      .filter(a => a.relOrdCd === relOrdCd)
      .reduce((s, a) => s + Number(a.allocQty || 0), 0)

  const getAllocatedQtyByLot = (lotNo) =>
    allocations.value
      .filter(a => a.lotNo === lotNo)
      .reduce((s, a) => s + Number(a.allocQty || 0), 0)

  // ===== LOT 재고 로딩(캐시) =====
  const ensureLotsLoaded = async (pcode) => {
    // 이미 같은 pcode LOT이 있으면 재조회 생략
    const has = Array.from(lotStockMap.value.values()).some(l => l.pcode === pcode)
    if (has) return
    const { data } = await getLotsByPcode(pcode) // [{ lotNo, pcode, qty, inboDt, ... }]
    ;(data ?? []).forEach(l => lotStockMap.value.set(l.lotNo, l))
  }

  // ===== 자동배분(FIFO: inboDt 오름차순) =====
  const autoDistributeAll = async () => {
    for (const row of products.value) {
      // eslint-disable-next-line no-await-in-loop
      await autoDistributeOne(row)
    }
  }

  const autoDistributeOne = async (row) => {
    if (!row) return
    const { pcode, relOrdCd } = row

    await ensureLotsLoaded(pcode)

    const lots = Array.from(lotStockMap.value.values())
      .filter(l => l.pcode === pcode)
      .sort((a, b) => new Date(a.inboDt) - new Date(b.inboDt)) // FIFO

    // 행 기준 필요수량 = 지시수량 - (해당 행에 이미 배분된 합)
    let need = Math.max(0, Number(row.relOrdQty) - getAllocatedQtyByRelOrd(relOrdCd))
    if (need <= 0) return

    for (const lot of lots) {
      const stock = lotStockMap.value.get(lot.lotNo)
      const lotRemain = Math.max(0, Number(stock?.qty ?? 0) - getAllocatedQtyByLot(lot.lotNo))
      if (lotRemain <= 0) continue

      const take = Math.min(need, lotRemain)
      if (take > 0) {
        allocations.value.push({
          _key: `${relOrdCd}-${lot.lotNo}-${Date.now()}-${Math.random()}`, // dataKey 충돌 방지
          relOrdCd,                         // ✅ 통일
          pcode,
          prodName: row.prodName,
          lotNo: lot.lotNo,
          allocQty: take
        })
        need -= take
      }
      if (need === 0) break
    }
  }

// ===== LOT별 수량(잔여 포함) 화면용 =====
const allocationRows = computed(() =>
  allocations.value.map(a => {
    const stock = lotStockMap.value.get(a.lotNo);
    const totalForLot = getAllocatedQtyByLot(a.lotNo);
    const remainQtyRaw = stock ? Math.max(0, Number(stock.qty) - totalForLot) : '';
    
    // remainQty를 10으로 나눈 뒤 반내림
    const remainQty = remainQtyRaw !== '' ? Math.floor(remainQtyRaw / 40) : '';

    return { ...a, remainQty };
  })
);

  // ===== 초기화/검증/저장 보조 =====
  const clearAllocations = () => { allocations.value = [] }

  const validateBeforeSave = () => {
    // 각 행의 배분합 == 지시수량 확인 (필요 시 <= 로 완화 가능)
    for (const row of products.value) {
      const sum = getAllocatedQtyByRelOrd(row.relOrdCd)
      if (sum !== Number(row.relOrdQty)) {
        throw new Error(`"${row.prodName}" 배분합(${sum})이 지시수량(${row.relOrdQty})과 다릅니다.`)
      }
    }
    // LOT별 잔여 재고 음수 방지
    for (const [lotNo, stock] of lotStockMap.value.entries()) {
      const used = getAllocatedQtyByLot(lotNo)
      if (used > Number(stock.qty)) {
        throw new Error(`LOT ${lotNo} 배분(${used})이 재고(${stock.qty})를 초과합니다.`)
      }
    }
  }

  // ===== 저장 payload 생성 =====
  const buildSavePayload = () => {
    const byRelOrd = new Map()
    allocations.value.forEach(a => {
      if (!byRelOrd.has(a.relOrdCd)) byRelOrd.set(a.relOrdCd, [])
      byRelOrd.get(a.relOrdCd).push(a)
    })

    const items = products.value.map(row => {
      const lotLines = (byRelOrd.get(row.relOrdCd) || []).map(a => ({
        lotNo: a.lotNo,
        wareAreaCd: lotStockMap.value.get(a.lotNo)?.wareAreaCd, // LOT 캐시에서 구역코드
        allocQty: a.allocQty
      }))
      return {
        relOrdCd: row.relOrdCd,     // ✅ 서버 핵심키
        ord_d_cd: row.ordDCd,       // (선택) 서비스에서 사용 시 포함
        pcode: row.pcode,
        qty: lotLines.reduce((s, l) => s + Number(l.allocQty), 0),
        lots: lotLines
      }
    })

    return {
      relMasCd: formData.value.relMasCd,
      cpCd: formData.value.cpCd || '',
      memo: formData.value.note || '',
      items
    }
  }

  // ===== 실제 저장 호출 =====
  const saveRelease = async () => {
    validateBeforeSave()
    const payload = buildSavePayload()
    console.log('payload.items[0]=', JSON.stringify(payload.items?.[0], null, 2));
    const { data } = await saveReleaseProcess(payload)
    return data
  }

  return {
    // state
    formData,
    releaseList,
    products,
    allocations,
    allocationRows,

    // actions
    resetForm,
    fetchRelsaves,
    fetchRelDetails,
    ensureLotsLoaded,
    autoDistributeAll,
    autoDistributeOne,
    clearAllocations,
    validateBeforeSave,
    buildSavePayload,
    saveRelease
  }
})
