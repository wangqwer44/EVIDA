"""
적격증빙 검증 모듈 (국세청 규정 기반)

- 간이(수기)영수증 3만원 초과 → 가산세 대상
- 매입세액 불공제 키워드 자동 태깅
"""

import re

# 매입세액 불공제 대상 키워드 (2글자 이상만, 오탐 방지)
NON_DEDUCTIBLE_KEYWORDS = [
    # 여객운송
    "KTX", "SRT", "택시", "버스", "항공", "비행기", "기차",
    # 유흥/접대
    "노래방", "노래클럽", "나이트클럽", "유흥주점",
    "룸살롱", "단란주점",
    # 미용
    "미용실", "헤어샵", "네일샵", "피부과", "에스테틱",
    # 기타 불공제
    "골프장", "골프클럽", "사우나", "찜질방", "마사지",
]


def validate_receipt(parsed_data):
    """
    적격증빙 검증 수행

    Args:
        parsed_data: parse_receipt() 결과 dict

    Returns:
        dict: {
            status: "APPROVED" | "REVIEW_REQUIRED" | "REJECTED",
            is_deductible: bool,
            warnings: list of str,
            rules_applied: list of str
        }
    """

    warnings = []
    rules_applied = []
    status = "APPROVED"
    is_deductible = True

    total = parsed_data.get("total_amount", 0) or 0
    merchant = parsed_data.get("merchant", "") or ""
    items = parsed_data.get("items", [])
    raw_texts = parsed_data.get("raw_texts", [])

    # 전체 텍스트 합침 (키워드 검색용)
    all_text = " ".join(raw_texts)

    # ===== Rule 1: 간이영수증(수기영수증) 3만원 초과 검증 =====
    is_simple_receipt = False

    for text in raw_texts:
        if re.search(r'간이|수기|수입금액', text):
            is_simple_receipt = True
            break

    if is_simple_receipt and total > 30000:
        status = "REJECTED"
        warnings.append(
            f"간이영수증 금액 {total:,}원 > 30,000원 초과: 적격증빙 위반 (가산세 대상)"
        )
        rules_applied.append("간이영수증_3만원_초과")

    # ===== Rule 2: 매입세액 불공제 키워드 검사 =====
    # 매장명과 전체 텍스트에서 키워드 매칭 (단어 단위, 오탐 방지)
    matched_keywords = []

    for keyword in NON_DEDUCTIBLE_KEYWORDS:
        # 매장명에서 정확히 포함
        if keyword in merchant:
            matched_keywords.append(keyword)
            continue

        # 전체 텍스트에서 단어 경계 매칭 (부분 매칭 방지)
        # "바코드"의 "바"가 "바"(접대업소)로 매칭되지 않도록
        pattern = re.compile(rf'(?<![가-힣]){re.escape(keyword)}(?![가-힣])')
        if pattern.search(all_text):
            matched_keywords.append(keyword)

    if matched_keywords:
        is_deductible = False
        warnings.append(
            f"매입세액 불공제 대상 키워드 감지: {', '.join(matched_keywords)}"
        )
        rules_applied.append("매입세액_불공제_태깅")

    # ===== Rule 3: 금액 정합성 검증 =====
    items_total = parsed_data.get("items_total", 0)

    if total and items_total and total != items_total:
        diff = abs(total - items_total)
        if diff > 100:  # 100원 이상 차이
            if status != "REJECTED":
                status = "REVIEW_REQUIRED"
            warnings.append(
                f"금액 불일치: 합계 {total:,}원 vs 아이템 합산 {items_total:,}원 (차이: {diff:,}원)"
            )
            rules_applied.append("금액_정합성_불일치")

    # ===== Rule 4: 필수 정보 누락 검증 =====
    if not parsed_data.get("merchant"):
        warnings.append("상호명 인식 실패")
        rules_applied.append("상호명_누락")

    if not parsed_data.get("date"):
        warnings.append("날짜 인식 실패")
        rules_applied.append("날짜_누락")

    if not items:
        if status != "REJECTED":
            status = "REVIEW_REQUIRED"
        warnings.append("상품 항목을 인식하지 못했습니다")
        rules_applied.append("항목_누락")

    if total is None or total == 0:
        if status != "REJECTED":
            status = "REVIEW_REQUIRED"
        warnings.append("합계 금액을 인식하지 못했습니다")
        rules_applied.append("금액_누락")

    return {
        "status": status,
        "is_deductible": is_deductible,
        "warnings": warnings,
        "rules_applied": rules_applied
    }