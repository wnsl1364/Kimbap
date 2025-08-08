// src/utils/pdf-font.js
import fontBase64 from '@/assets/fonts/NotoSansKR-Regular.base64.txt?raw'
import { jsPDF } from 'jspdf'

export const registerKoreanFont = (doc) => {
  doc.addFileToVFS('NotoSansKR-Regular.ttf', fontBase64)
  doc.addFont('NotoSansKR-Regular.ttf', 'NotoSansKR', 'normal')
  doc.setFont('NotoSansKR')
  doc.setFontSize(16)
}