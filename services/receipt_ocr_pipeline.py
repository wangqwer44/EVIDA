from paddleocr import PaddleOCR
from utils.preprocess import preprocess_image


class ReceiptOCRPipeline:

    def __init__(self):

        self.ocr = PaddleOCR(
            lang="korean",
            use_textline_orientation=True
        )

    def run_ocr(self, image_path):
        """
        이미지 경로를 받아 OCR 실행 후 결과 반환

        PaddleOCR v5 결과 구조:
            result[0] = OCRResult 객체
            - rec_texts: ['STARBUCKS', '4,500', ...]
            - rec_scores: [0.96, 0.99, ...]
            - rec_polys: [array([[x1,y1],[x2,y2],[x3,y3],[x4,y4]]), ...]

        Args:
            image_path: 영수증 이미지 파일 경로

        Returns:
            list of (bbox, text, score) tuples
            bbox = [[x1,y1],[x2,y2],[x3,y3],[x4,y4]]
        """

        # 1. 전처리 (경로 문자열 → 경로 그대로 전달)
        img = preprocess_image(image_path)

        # 2. OCR 실행 (v5: predict 권장이지만 ocr도 호환)
        result = self.ocr.ocr(img)

        texts = []

        if not result or len(result) == 0:
            return texts

        ocr_result = result[0]

        # === PaddleOCR v5: OCRResult 객체 ===
        if isinstance(ocr_result, dict) and 'rec_texts' in ocr_result:
            rec_texts = ocr_result['rec_texts']
            rec_scores = ocr_result['rec_scores']
            rec_polys = ocr_result['rec_polys']

            for i in range(len(rec_texts)):
                text = rec_texts[i].strip()
                score = float(rec_scores[i])
                poly = rec_polys[i].tolist()  # numpy array → list

                if text:
                    texts.append((poly, text, score))

        # === PaddleOCR v4 이하: list of [bbox, (text, score)] ===
        elif isinstance(ocr_result, list):
            for line in ocr_result:
                try:
                    bbox = line[0]
                    rec = line[1]

                    if isinstance(rec, (list, tuple)) and len(rec) >= 2:
                        text = str(rec[0]).strip()
                        score = float(rec[1])
                    else:
                        text = str(rec).strip()
                        score = 0.0

                    if text:
                        texts.append((bbox, text, score))
                except Exception as e:
                    print(f"[WARN] OCR 라인 파싱 실패: {e}")
                    continue

        return texts