# 이미지 업로드 → OCR → JSON 반환 API

import os
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import shutil

from services.receipt_ocr_pipeline import ReceiptOCRPipeline
from utils.parser import parse_receipt, save_json
from utils.validator import validate_receipt

# uploads 디렉토리 자동 생성
os.makedirs("uploads", exist_ok=True)

app = FastAPI(
    title="EVIDA Receipt OCR API",
    description="AI 기반 영수증 OCR 및 지출 증빙 자동화 API",
    version="1.0.0"
)

# CORS 설정 (Spring Boot 백엔드 연동용)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# OCR 파이프라인 초기화 (서버 시작 시 1회)
pipeline = ReceiptOCRPipeline()


@app.post("/receipt")
async def receipt_ocr(file: UploadFile = File(...)):
    """
    영수증 이미지를 업로드하면 OCR → 파싱 → 검증 결과를 JSON으로 반환

    Returns:
        - merchant: 상호명
        - date: 날짜
        - total_amount: 합계 금액
        - items: 상품 목록 [{name, quantity, unit_price, amount}]
        - validation: 적격증빙 검증 결과
    """

    # 파일 확장자 검증
    allowed_extensions = {".jpg", ".jpeg", ".png", ".bmp", ".tiff"}
    ext = os.path.splitext(file.filename)[1].lower()

    if ext not in allowed_extensions:
        raise HTTPException(
            status_code=400,
            detail=f"지원하지 않는 파일 형식입니다. ({', '.join(allowed_extensions)})"
        )

    # 파일 저장
    file_path = f"uploads/{file.filename}"

    try:
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        # 1. OCR 실행
        ocr_results = pipeline.run_ocr(file_path)

        if not ocr_results:
            raise HTTPException(
                status_code=422,
                detail="영수증에서 텍스트를 인식하지 못했습니다."
            )

        # 2. 파싱
        parsed_data = parse_receipt(ocr_results)

        # 3. 적격증빙 검증
        validation = validate_receipt(parsed_data)
        parsed_data["validation"] = validation

        # 4. JSON 저장
        save_json(parsed_data)

        return parsed_data

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"OCR 처리 중 오류 발생: {str(e)}"
        )
    finally:
        # 처리 후 업로드 파일 삭제 (선택)
        if os.path.exists(file_path):
            os.remove(file_path)


@app.get("/health")
async def health_check():
    return {"status": "ok", "service": "EVIDA Receipt OCR API"}