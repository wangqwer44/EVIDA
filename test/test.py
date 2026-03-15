"""
EVIDA OCR 테스트 스크립트
"""

import sys
import os

# 프로젝트 루트를 path에 추가
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))

from services.receipt_ocr_pipeline import ReceiptOCRPipeline
from utils.parser import parse_receipt, save_json
from utils.validator import validate_receipt


def test_ocr():

    # 테스트 이미지 경로
    image_path = os.path.join(
        os.path.dirname(os.path.dirname(__file__)),
        "uploads",
        "receipt.jpg"
    )

    if not os.path.exists(image_path):
        print(f"[ERROR] 테스트 이미지가 없습니다: {image_path}")
        print("uploads/ 폴더에 receipt.jpg를 넣어주세요.")
        return

    print(f"[INFO] 이미지 경로: {image_path}")

    # 1. OCR 실행
    print("\n===== 1. OCR 실행 =====")
    pipeline = ReceiptOCRPipeline()
    results = pipeline.run_ocr(image_path)

    print(f"인식된 텍스트 수: {len(results)}개\n")

    for bbox, text, score in results:
        print(f"  [{score:.3f}] {text}")

    # 2. 파싱
    print("\n===== 2. 파싱 결과 =====")
    data = parse_receipt(results)

    print(f"  매장명:  {data.get('merchant')}")
    print(f"  날짜:    {data.get('date')}")
    print(f"  시간:    {data.get('time')}")
    print(f"  합계:    {data.get('total_amount'):,}원" if data.get('total_amount') else "  합계:    인식 실패")

    if data.get("items"):
        print(f"\n  상품 목록 ({len(data['items'])}건):")
        for item in data["items"]:
            name = item.get("name", "?")
            qty = item.get("quantity", "?")
            amount = item.get("amount", 0)
            print(f"    - {name}  x{qty}  = {amount:,}원")

    # 3. 적격증빙 검증
    print("\n===== 3. 적격증빙 검증 =====")
    validation = validate_receipt(data)
    data["validation"] = validation

    print(f"  상태: {validation['status']}")
    print(f"  공제 가능: {validation['is_deductible']}")

    if validation["warnings"]:
        print("  경고:")
        for w in validation["warnings"]:
            print(f"    ⚠️  {w}")

    # 4. JSON 저장
    output_path = os.path.join(
        os.path.dirname(os.path.dirname(__file__)),
        "receipt_result.json"
    )
    save_json(data, output_path)
    print(f"\n[INFO] 결과 저장: {output_path}")


if __name__ == "__main__":
    test_ocr()