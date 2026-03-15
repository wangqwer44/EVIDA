# OCR 결과를 y좌표 기준으로 같은 줄끼리 묶고, x좌표로 정렬하여 텍스트를 합침


def group_lines(results, y_threshold=15):
    """
    OCR 결과를 y좌표 기준으로 같은 줄끼리 그룹핑

    Args:
        results: list of (bbox, text, score) tuples
                 bbox = [[x1,y1],[x2,y2],[x3,y3],[x4,y4]] (PaddleOCR v5 polygon)
        y_threshold: 같은 줄로 판단하는 y좌표 차이 임계값

    Returns:
        list of dicts: [{
            "texts": ["상품명", "4,500", "1", "4,500"],
            "full_text": "상품명 4,500 1 4,500",
            "bboxes": [...],
            "y": 평균 y좌표
        }]
    """

    if not results:
        return []

    # bbox에서 y좌표(좌상단) 추출하는 헬퍼
    def get_top_y(item):
        bbox = item[0]
        # bbox가 [[x1,y1],[x2,y2],[x3,y3],[x4,y4]] 형태 (polygon)
        if isinstance(bbox[0], (list, tuple)):
            return bbox[0][1]
        # bbox가 [x1,y1,x2,y2] 형태 (flat)
        return bbox[1]

    def get_left_x(item):
        bbox = item[0]
        if isinstance(bbox[0], (list, tuple)):
            return bbox[0][0]
        return bbox[0]

    # y좌표 기준 정렬
    sorted_results = sorted(results, key=get_top_y)

    lines = []
    current_line = [sorted_results[0]]
    last_y = get_top_y(sorted_results[0])

    for item in sorted_results[1:]:

        y = get_top_y(item)

        if abs(y - last_y) < y_threshold:
            # 같은 줄
            current_line.append(item)
        else:
            # 새로운 줄 → 이전 줄 저장
            lines.append(_merge_line(current_line))
            current_line = [item]
            last_y = y

    # 마지막 줄 저장
    if current_line:
        lines.append(_merge_line(current_line))

    return lines


def _merge_line(line_items):
    """
    같은 줄의 항목들을 x좌표 순서로 정렬하고 합침
    """

    def get_left_x(item):
        bbox = item[0]
        if isinstance(bbox[0], (list, tuple)):
            return bbox[0][0]
        return bbox[0]

    # x좌표 기준 왼쪽→오른쪽 정렬
    line_items = sorted(line_items, key=get_left_x)

    texts = [item[1] for item in line_items]
    bboxes = [item[0] for item in line_items]
    scores = [item[2] for item in line_items]

    def get_top_y(item):
        bbox = item[0]
        if isinstance(bbox[0], (list, tuple)):
            return bbox[0][1]
        return bbox[1]

    avg_y = sum(get_top_y(item) for item in line_items) / len(line_items)

    return {
        "texts": texts,
        "full_text": " ".join(texts),
        "bboxes": bboxes,
        "scores": scores,
        "y": avg_y
    }