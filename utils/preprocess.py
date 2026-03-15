import cv2
import numpy as np


def preprocess_image(image_path):
    """
    영수증 이미지 전처리

    PaddleOCR v5는 자체 전처리(문서 방향 보정, unwarp 등)가 내장되어 있어
    과도한 전처리는 오히려 인식률을 떨어뜨림.

    → 파일 경로인 경우 그대로 반환 (PaddleOCR v5가 직접 로드)
    → numpy array인 경우 최소한의 전처리만 수행

    Args:
        image_path: 이미지 파일 경로 (str) 또는 numpy array

    Returns:
        str (파일 경로) 또는 numpy array (전처리된 이미지)
    """

    # 파일 경로인 경우 → PaddleOCR v5에 직접 전달 (내장 전처리 활용)
    if isinstance(image_path, str):
        return image_path

    # numpy array인 경우 (API에서 메모리 이미지 처리 시)
    img = image_path

    # 조건부 리사이즈 (너무 작은 이미지만 확대)
    h, w = img.shape[:2]

    if max(h, w) < 800:
        scale = 800 / max(h, w)
        img = cv2.resize(img, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)

    return img