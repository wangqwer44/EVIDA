import re
import json
from utils.line_grouping import group_lines


def parse_receipt(results):
    """
    OCR 결과를 파싱하여 구조화된 영수증 데이터 반환

    지원하는 영수증 형식:
    - 카페 (스타벅스 등): 주문번호 → 상품 → 합계
    - 마트/편의점: 번호+상품명+수량+가격 패턴
    - 일반 영수증: 가격 패턴이 포함된 줄 자동 감지

    Args:
        results: list of (bbox, text, score) tuples from OCR

    Returns:
        dict: {merchant, date, total_amount, items, raw_texts}
    """

    # 1. 라인 그룹핑 (같은 줄 텍스트 합치기)
    lines = group_lines(results)

    merchant = None
    date = None
    time = None
    items = []
    total_amount = None
    payment_method = None

    # 2. 전체 텍스트 (줄 단위)
    full_texts = [line["full_text"] for line in lines]

    # 3. 매장명 추출 (첫 번째 줄 우선, 영문/한글 브랜드명)
    for line in lines[:5]:
        text = line["full_text"].strip()

        # 특수문자 정리 (OCR 노이즈 제거)
        cleaned = re.sub(r'["\'"]+$', '', text).strip()

        # 영문 브랜드명 (STARBUCKS, EDIYA 등)
        if re.match(r'^[A-Za-z\s]{3,}', cleaned):
            merchant = cleaned
            break

        # 한글 브랜드명 (순수 한글, 숫자/특수문자 없음)
        if re.match(r'^[가-힣\s]{2,}$', cleaned):
            # 소득공제, 현금 등 키워드 제외
            if not re.search(r'소득공제|현금|합계|주문|결제|부가세|과세|면세', cleaned):
                merchant = cleaned
                break

    # 4. 날짜/시간 추출
    for text in full_texts:

        # 날짜+시간 붙어있는 패턴: 2021-08-1113:02:33
        dt_match = re.search(r'(\d{4})[-./](\d{2})[-./](\d{2})\s*(\d{1,2}):(\d{2}):(\d{2})', text)
        if dt_match and not date:
            date = f"{dt_match.group(1)}-{dt_match.group(2)}-{dt_match.group(3)}"
            time = f"{dt_match.group(4)}:{dt_match.group(5)}:{dt_match.group(6)}"
            continue

        # 날짜만: 2021-08-11
        date_match = re.search(r'(\d{4})[-./](\d{1,2})[-./](\d{1,2})', text)
        if date_match and not date:
            date = f"{date_match.group(1)}-{date_match.group(2).zfill(2)}-{date_match.group(3).zfill(2)}"

        # 시간만: 13:02:33
        time_match = re.search(r'(\d{1,2}):(\d{2}):(\d{2})', text)
        if time_match and not time:
            time = time_match.group(0)

    # 5. 결제수단 추출
    for text in full_texts:
        if re.search(r'카카오페이|카카오', text, re.IGNORECASE):
            payment_method = "카카오페이"
            break
        elif re.search(r'신용카드|체크카드|삼성카드|현대카드|KB|롯데카드', text):
            payment_method = "카드"
            break
        elif re.search(r'현금', text):
            payment_method = "현금"
            break

    # 6. 영수증 형식 감지 + 상품 파싱
    receipt_type = _detect_receipt_type(lines)

    if receipt_type == "cafe":
        items, total_amount = _parse_cafe_receipt(lines)
    elif receipt_type == "mart":
        items, total_amount = _parse_mart_receipt(lines)
    else:
        items, total_amount = _parse_generic_receipt(lines)

    # 7. 합계가 아직 없으면 결제금액 줄에서 추출
    if total_amount is None:
        for line in lines:
            text = line["full_text"]
            parts = line["texts"]
            if re.search(r'카카오페이|결제.*액|합\s*계|총\s*액|합산', text):
                amount = _extract_price_from_parts(parts)
                if amount and amount >= 100:
                    total_amount = amount
                    break

    # 8. items에서 계산한 합계
    items_total = sum(item.get("amount", 0) for item in items)

    return {
        "merchant": merchant,
        "date": date,
        "time": time,
        "payment_method": payment_method,
        "total_amount": total_amount,
        "items_total": items_total,
        "items": items,
        "raw_texts": full_texts
    }


# ============================================================
# 영수증 형식 감지
# ============================================================

def _detect_receipt_type(lines):
    """
    영수증 형식을 자동 감지

    Returns:
        "cafe": 카페 (주문번호 키워드 있음)
        "mart": 마트/편의점 (001 상품명 수량 가격 패턴)
        "generic": 일반
    """
    for line in lines:
        text = line["full_text"]

        # 카페: 주문번호 키워드
        if re.search(r'주문\s*번호|ORDER', text, re.IGNORECASE):
            return "cafe"

    # 마트: 숫자번호 + 상품명 + 가격원 패턴
    mart_pattern_count = 0
    for line in lines:
        text = line["full_text"]
        # "001 상품명 1 2,190원" 또는 "001 상품명 2,190원"
        if re.search(r'^\d{1,3}\s*[\*]?\s*[가-힣A-Za-z]', text):
            mart_pattern_count += 1

    if mart_pattern_count >= 2:
        return "mart"

    return "generic"


# ============================================================
# 카페 영수증 파싱 (스타벅스 등)
# ============================================================

def _parse_cafe_receipt(lines):
    """
    카페 영수증 파싱: 주문번호 → 상품 → 합계
    """
    items = []
    total_amount = None
    item_section = False

    for line in lines:
        text = line["full_text"]
        parts = line["texts"]

        if re.search(r'주문\s*번호|ORDER', text, re.IGNORECASE):
            item_section = True
            continue

        if re.search(r'^합\s*계|^소\s*계|^TOTAL', text, re.IGNORECASE):
            item_section = False
            amount = _extract_price_from_parts(parts)
            if amount and amount > 0:
                total_amount = amount
            continue

        if re.search(r'결제.*액|결제.*금', text):
            amount = _extract_price_from_parts(parts)
            if amount and amount > 0 and total_amount is None:
                total_amount = amount
            continue

        if not item_section:
            continue

        item = _parse_item_line_cafe(parts)
        if item:
            items.append(item)

    return items, total_amount


# ============================================================
# 마트/편의점 영수증 파싱
# ============================================================

def _parse_mart_receipt(lines):
    """
    마트/편의점 영수증 파싱

    패턴: [번호] [*] [상품명] [수량] [가격원]
    예: "001 더건강한샌드위치햄 1 2,190원"
    예: "002 * 재사용봉투20L 1 850원"

    바코드 줄(순수 숫자 13자리+)은 스킵
    """
    items = []
    total_amount = None

    for line in lines:
        text = line["full_text"]
        parts = line["texts"]

        # 합계/소계 줄
        if re.search(r'합\s*계|소\s*계|총\s*액|결제|TOTAL', text, re.IGNORECASE):
            amount = _extract_price_from_parts(parts)
            if amount and amount > 0:
                total_amount = amount
            continue

        # 바코드 줄 스킵 (13자리 이상 순수 숫자)
        cleaned_text = text.replace(' ', '')
        if re.match(r'^\d{10,}$', cleaned_text):
            continue

        # 구분선 스킵
        if re.match(r'^[-=*_\s]+$', text):
            continue

        # 마트 상품 줄 파싱
        item = _parse_item_line_mart(text, parts)
        if item:
            items.append(item)

    return items, total_amount


def _parse_item_line_mart(full_text, parts):
    """
    마트 영수증 한 줄 파싱

    full_text 패턴들:
    - "001 더건강한샌드위치햄 1 2,190원"
    - "002 * 재사용봉투20L 1 850원"
    - "003 서울체다SLICE치즈20 1 3,690원"
    """

    # 가격(원) 패턴 추출: "2,190원" 또는 "850원"
    price_match = re.search(r'([\d,]+)\s*원', full_text)
    if not price_match:
        # "원" 없이 가격만 있는 경우도 시도
        # parts에서 가격 추출
        price = _extract_price_from_parts(parts)
        if not price or price < 100:
            return None
    else:
        price = _normalize_price(price_match.group(1))
        if not price or price < 10:
            return None

    # 상품명 추출: 번호와 가격 사이의 텍스트
    # "001 더건강한샌드위치햄 1 2,190원" → "더건강한샌드위치햄"
    # "002 * 재사용봉투20L 1 850원" → "재사용봉투20L"

    # 앞쪽 번호 제거: "001", "002 *" 등
    name_text = re.sub(r'^\d{1,3}\s*[\*]?\s*', '', full_text)

    # 뒤쪽 수량+가격 제거: "1 2,190원", "2 850원"
    name_text = re.sub(r'\s+\d+\s+[\d,]+\s*원?\s*$', '', name_text)
    # "가격원"만 있는 경우
    name_text = re.sub(r'\s*[\d,]+\s*원\s*$', '', name_text)

    name = name_text.strip()

    if len(name) < 1:
        return None

    # 수량 추출: 가격 앞의 숫자
    qty_match = re.search(r'(\d+)\s+[\d,]+\s*원', full_text)
    quantity = int(qty_match.group(1)) if qty_match else 1

    return {
        "name": name,
        "quantity": quantity,
        "amount": price
    }


# ============================================================
# 일반 영수증 파싱 (폴백)
# ============================================================

def _parse_generic_receipt(lines):
    """
    형식을 특정할 수 없는 영수증: 가격 패턴이 있는 모든 줄을 상품으로 시도
    """
    items = []
    total_amount = None

    for line in lines:
        text = line["full_text"]
        parts = line["texts"]

        # 합계/결제 줄
        if re.search(r'합\s*계|소\s*계|결제.*액|TOTAL', text, re.IGNORECASE):
            amount = _extract_price_from_parts(parts)
            if amount and amount > 0:
                total_amount = amount
            continue

        # 바코드/순수숫자 스킵
        cleaned_text = text.replace(' ', '')
        if re.match(r'^\d{10,}$', cleaned_text):
            continue

        # 구분선 스킵
        if re.match(r'^[-=*_\s]+$', text):
            continue

        # 가격 패턴이 있으면 상품 줄로 시도
        if re.search(r'\d{1,3}(?:,\d{3})+|\d+\s*원', text):
            item = _parse_item_line_cafe(parts)
            if item:
                items.append(item)

    return items, total_amount


# ============================================================
# 개별 라인 파싱 헬퍼
# ============================================================

def _parse_item_line_cafe(parts):
    """
    카페/일반 영수증 개별 상품 줄 파싱

    parts: ["T)콜드 브루", "4,500", "1", "4,500"]
    parts: ["1-)아메리카노", "4,100", "2", "8,200"]
    """

    name_parts = []
    numbers = []

    for p in parts:
        p = p.strip()
        if not p:
            continue

        # 순수 숫자/가격 패턴
        price = _normalize_price(p)
        cleaned_for_check = re.sub(r'[,.\s]', '', p)

        if cleaned_for_check.isdigit() and price is not None:
            numbers.append(price)
        else:
            # 화살표, 특수기호만 있는 경우 제외
            if re.match(r'^[-=>\s]+$', p):
                continue
            name_parts.append(p)

    name = " ".join(name_parts).strip()

    if len(name) < 1:
        return None

    if len(numbers) == 0:
        return None

    # 의미없는 줄 필터링 (주문번호, A-01 같은 줄)
    if re.match(r'^[A-Za-z]-?\d+$', name):
        return None

    item = {"name": name}

    if len(numbers) >= 3:
        item["unit_price"] = numbers[0]
        item["quantity"] = numbers[1]
        item["amount"] = numbers[2]
    elif len(numbers) == 2:
        if numbers[0] < 10:
            item["quantity"] = numbers[0]
            item["amount"] = numbers[1]
        else:
            item["unit_price"] = numbers[0]
            item["amount"] = numbers[1]
            if numbers[0] != 0:
                item["quantity"] = numbers[1] // numbers[0]
    elif len(numbers) == 1:
        item["amount"] = numbers[0]
        item["quantity"] = 1

    if item.get("amount", 0) < 100:
        return None

    return item


# ============================================================
# 유틸리티 함수
# ============================================================

def _normalize_price(text):
    """
    가격 문자열 정규화: OCR 오인식 보정

    12.700 → 12700, 12,,700 → 12700, 12,700 → 12700, 2,190원 → 2190
    """
    # "원" 제거
    cleaned = text.replace('원', '').strip()

    # 숫자, 콤마, 점만 남기기
    cleaned = re.sub(r'[^\d.,]', '', cleaned)

    if not cleaned:
        return None

    # 한국 가격 패턴: 12.700 또는 12,700 → 12700
    match = re.match(r'^(\d{1,3})[.,](\d{3})$', cleaned)
    if match:
        return int(match.group(1) + match.group(2))

    # 콤마/점 제거
    cleaned = cleaned.replace(',', '').replace('.', '')

    try:
        return int(cleaned)
    except ValueError:
        return None


def _extract_price_from_parts(parts):
    """
    줄의 개별 텍스트들에서 가격(가장 큰 금액) 추출
    """
    max_price = None

    for p in parts:
        price = _normalize_price(p)
        if price and price >= 100:
            if max_price is None or price > max_price:
                max_price = price

    return max_price


def save_json(data, filename="receipt_result.json"):
    """파싱 결과를 JSON 파일로 저장"""

    with open(filename, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=4)