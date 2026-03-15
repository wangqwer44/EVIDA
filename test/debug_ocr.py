"""
PaddleOCR v5 결과 접근 방식 디버그
"""

import sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))

from paddleocr import PaddleOCR

image_path = os.path.join(
    os.path.dirname(os.path.dirname(__file__)),
    "uploads", "receipt.jpg"
)

ocr = PaddleOCR(lang="korean", use_textline_orientation=True)
result = ocr.ocr(image_path)

obj = result[0]

print(f"type: {type(obj)}")
print(f"hasattr rec_texts: {hasattr(obj, 'rec_texts')}")
print(f"'rec_texts' in dir: {'rec_texts' in dir(obj)}")

# dict-like 접근 테스트
try:
    texts = obj['rec_texts']
    print(f"obj['rec_texts'] 성공: {len(texts)}개")
    print(f"처음 5개: {texts[:5]}")
except Exception as e:
    print(f"obj['rec_texts'] 실패: {e}")

# attribute 접근 테스트
try:
    texts = obj.rec_texts
    print(f"obj.rec_texts 성공: {len(texts)}개")
except Exception as e:
    print(f"obj.rec_texts 실패: {e}")

# keys 확인
try:
    print(f"keys: {list(obj.keys())[:10]}")
except:
    pass

# isinstance 확인
print(f"isinstance list: {isinstance(obj, list)}")
print(f"isinstance dict: {isinstance(obj, dict)}")