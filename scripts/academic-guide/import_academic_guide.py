"""명지대학교 학사안내 PDF를 통합검색용 지식 문서 JSON으로 변환한다.

사용 예:
  python scripts/academic-guide/import_academic_guide.py \
    "C:/Users/USER/Desktop/1)2026-2학기 학사안내문(v1.6) (1).pdf" \
    src/main/resources/academic/academic_guide_2026_2.json
"""

from __future__ import annotations

import json
import re
import sys
from dataclasses import dataclass
from pathlib import Path

from pypdf import PdfReader


SOURCE_URL = "https://bangmok.mju.ac.kr/bbs/mjukr/1725/233916/artclView.do"
PUBLISHED_AT = "2026-08-04T00:00:00Z"


@dataclass(frozen=True)
class Section:
    start: int
    end: int
    code: str
    title: str
    admission_year_from: int | None = None
    admission_year_to: int | None = None
    keywords: tuple[str, ...] = ()


SECTIONS = (
    Section(1, 2, "course_registration_system", "수강신청 시스템", keywords=("수강신청", "미리담기")),
    Section(3, 4, "academic_calendar", "2026학년도 학사력", keywords=("학사일정", "학사력")),
    Section(5, 13, "course_registration_notes", "수강신청 유의사항", keywords=("수강신청", "수강제한")),
    Section(14, 17, "course_registration", "수강신청 안내", keywords=("수강신청", "신청학점")),
    Section(18, 18, "graduation_overview", "졸업 이수학점 안내", keywords=("졸업요건", "졸업학점")),
    Section(19, 29, "graduation_2009_2014", "2009~2014학번 졸업요건", 2009, 2014, ("졸업요건", "학문기초교양", "학기교")),
    Section(30, 36, "graduation_2015_2017", "2015~2017학번 졸업요건", 2015, 2017, ("졸업요건", "학문기초교양", "학기교")),
    Section(37, 44, "graduation_2018_2024", "2018~2024학번 졸업요건", 2018, 2024, ("졸업요건", "학문기초교양", "학기교")),
    Section(45, 50, "graduation_2025_plus", "2025학번 이후 졸업요건", 2025, None, ("졸업요건", "학문기초교양", "학기교")),
    Section(51, 61, "major_requirements_until_2024", "2024학번까지 전공필수", None, 2024, ("전공필수", "졸업요건")),
    Section(62, 66, "major_requirements_2025_plus", "2025학번부터 전공필수", 2025, None, ("전공필수", "졸업요건")),
    Section(67, 72, "transfer_credits", "편입생 학점", keywords=("편입생", "인정학점")),
    Section(73, 78, "required_courses", "특정 교과목 이수안내", keywords=("필수과목", "졸업인증")),
    Section(79, 79, "credit_exchange", "대학간 학점교류", keywords=("학점교류",)),
    Section(80, 81, "reference_tables", "교양·수업시간 참고표", keywords=("일반교양", "수업시간")),
    Section(82, 89, "classes", "수업 제도", keywords=("수강철회", "계절수업", "유고결석")),
    Section(90, 91, "grades", "재수강·성적포기", keywords=("재수강", "성적포기")),
    Section(92, 140, "multiple_major", "다전공·조기졸업", keywords=("다전공", "복수전공", "복전", "부전공", "융합전공")),
    Section(141, 141, "grade_calculation", "성적 계산", keywords=("평균평점", "백분율")),
    Section(142, 147, "academic_status", "학적", keywords=("휴학", "복학", "제적", "재입학", "전과", "초과학기")),
    Section(148, 148, "student_services", "학생증·증명서", keywords=("학생증", "증명서")),
    Section(149, 152, "legacy_before_1999", "1998학번 이전 학사안내", None, 1998, ("졸업요건",)),
    Section(153, 156, "legacy_1999_2004", "1999~2004학번 학사안내", 1999, 2004, ("졸업요건",)),
    Section(157, 160, "legacy_2005", "2005학번 학사안내", 2005, 2005, ("졸업요건",)),
    Section(161, 167, "legacy_2006_2008", "2006~2008학번 학사안내", 2006, 2008, ("졸업요건",)),
    Section(168, 170, "legacy_curriculum_changes", "2008학번 이전 교과과정 개편", None, 2008, ("교과과정", "대체과목")),
    Section(171, 173, "college_contacts", "단과대학 교학팀 연락처", keywords=("교학팀", "문의", "전화번호")),
)

COLLEGES = (
    "인문대학", "사회과학대학", "미디어·휴먼라이프대학", "경영대학",
    "인공지능·소프트웨어융합대학", "미래융합대학", "화학·생명과학대학",
    "스마트시스템공과대학", "반도체·ICT대학", "스포츠예술대학", "건축대학",
    "아너칼리지", "글로벌학부",
)

COLLEGE_DEPARTMENTS = {
    "인문대학": (
        "인문콘텐츠학부", "국어국문학전공", "영어영문학전공", "미술사·역사학전공", "문헌정보학전공",
        "글로벌문화콘텐츠학전공", "아시아·중동어문학부", "중어중문학전공", "일어일문학전공",
        "아랍지역학전공", "글로벌한국어학전공", "문예창작학과",
    ),
    "사회과학대학": (
        "공공인재학부", "행정학전공", "정치외교학전공", "경상·통계학부", "경제학전공",
        "국제통상학전공", "응용통계학전공", "법학과",
    ),
    "미디어·휴먼라이프대학": (
        "디지털미디어학부", "청소년지도·아동학부", "청소년지도학전공", "아동학전공",
    ),
    "경영대학": ("경영학부", "경영학전공", "글로벌비즈니스학전공", "경영정보학과"),
    "인공지능·소프트웨어융합대학": (
        "융합소프트웨어학부", "응용소프트웨어전공", "데이터사이언스전공", "인공지능전공",
        "디지털콘텐츠디자인학과",
    ),
    "미래융합대학": (
        "창의융합인재학부", "사회복지학과", "부동산학과", "법무행정학과", "심리치료학과",
        "미래융합경영학과", "회계세무학과", "멀티디자인학과",
    ),
    "화학·생명과학대학": (
        "화학·에너지융합학부", "화학나노학전공", "융합에너지학전공", "융합바이오학부",
        "식품영양학전공", "시스템생명과학전공", "수학과", "물리학과",
    ),
    "스마트시스템공과대학": (
        "기계시스템공학부", "기계공학전공", "로봇공학전공", "스마트인프라공학부", "건설환경공학전공",
        "환경시스템공학전공", "스마트모빌리티공학전공", "화공신소재공학부", "화학공학전공", "신소재공학전공",
    ),
    "반도체·ICT대학": (
        "반도체공학부", "전기전자공학부", "전기공학전공", "전자공학전공", "컴퓨터정보통신공학부",
        "컴퓨터공학전공", "정보통신공학전공", "산업경영공학과",
    ),
    "스포츠예술대학": (
        "디자인학부", "비주얼커뮤니케이션디자인전공", "인더스트리얼디자인전공", "영상애니메이션디자인전공",
        "패션디자인전공", "스포츠학부", "체육학전공", "스포츠산업학전공", "스포츠지도학전공",
        "아트앤멀티미디어음악학부", "건반음악전공", "보컬뮤직전공", "작곡전공", "공연예술학부",
        "연극·영화전공", "뮤지컬공연전공",
    ),
    "건축대학": ("건축학부", "건축학전공", "전통건축전공", "공간디자인학과"),
    "아너칼리지": ("자율전공학부(인문)", "자율전공학부(자연)"),
    "글로벌학부": ("글로벌한국어학전공",),
}

COLLEGE_MAPPING_SOURCE_PAGE = {
    **{college: 6 for college in tuple(COLLEGE_DEPARTMENTS)[:6]},
    **{college: 7 for college in tuple(COLLEGE_DEPARTMENTS)[6:11]},
    "아너칼리지": 9,
    "글로벌학부": 50,
}

DEPARTMENTS = tuple(dict.fromkeys(
    department
    for departments in COLLEGE_DEPARTMENTS.values()
    for department in departments
))

DEPARTMENT_ALIASES = {
    "국어국문학전공": ("국문", "국문과"), "영어영문학전공": ("영문", "영문과"),
    "문헌정보학전공": ("문정", "문헌정보"), "글로벌문화콘텐츠학전공": ("글문콘", "글로벌문화콘텐츠"),
    "문예창작학과": ("문창", "문창과"), "행정학전공": ("행정", "행정학과"),
    "정치외교학전공": ("정외", "정치외교"), "경제학전공": ("경제", "경제학과"),
    "국제통상학전공": ("국통", "국제통상"), "응용통계학전공": ("응통", "응용통계"),
    "법학과": ("법학",), "디지털미디어학부": ("디미", "디지털미디어"),
    "청소년지도학전공": ("청지", "청소년지도"), "아동학전공": ("아동", "아동학과"),
    "경영학전공": ("경영", "경영학과"), "글로벌비즈니스학전공": ("글비", "글로벌비즈니스"),
    "경영정보학과": ("경정", "경영정보"), "융합소프트웨어학부": ("융소", "융합소프트웨어"),
    "응용소프트웨어전공": ("응소", "응용소프트웨어"), "데이터사이언스전공": ("데사", "데이터사이언스"),
    "인공지능전공": ("인공지능", "AI전공"), "디지털콘텐츠디자인학과": ("디콘디", "디지털콘텐츠디자인"),
    "사회복지학과": ("사복", "사회복지"), "법무행정학과": ("법행", "법무행정"),
    "심리치료학과": ("심치", "심리치료"), "미래융합경영학과": ("미융경", "미래융합경영"),
    "회계세무학과": ("회세", "회계세무"), "멀티디자인학과": ("멀디", "멀티디자인"),
    "식품영양학전공": ("식영", "식품영양"), "컴퓨터공학전공": ("컴공", "컴퓨터공학"),
    "정보통신공학전공": ("정통", "정보통신"), "산업경영공학과": ("산경", "산업경영"),
    "건축학전공": ("건축", "건축학과"), "공간디자인학과": ("공디", "공간디자인"),
}

ALIASES = {
    "인문대학": ("인문대",),
    "사회과학대학": ("사과대", "사회대"),
    "미디어·휴먼라이프대학": ("미휴", "미휴대", "미디어휴먼라이프"),
    "경영대학": ("경영대",),
    "인공지능·소프트웨어융합대학": ("인소융", "인소대", "인공지능소프트웨어융합대학"),
    "미래융합대학": ("미융대", "미래융합"),
    "화학·생명과학대학": ("화생대", "화학생명과학대학"),
    "스마트시스템공과대학": ("스공대", "스마트시스템공과"),
    "반도체·ICT대학": ("반도체ICT대학", "반도체대"),
    "스포츠예술대학": ("스예대", "스포츠·예술대학"),
    "건축대학": ("건축대",),
    "학문기초교양": ("학기교", "기초교양"),
    "복수전공": ("복전", "다전공"),
}
ALIASES.update(DEPARTMENT_ALIASES)


def normalize_text(value: str) -> str:
    value = value.replace("\u00a0", " ").replace("\x00", "")
    lines = [re.sub(r"[ \t]+", " ", line).strip() for line in value.splitlines()]
    return "\n".join(line for line in lines if line)


def section_for(document_page: int) -> Section:
    return next(section for section in SECTIONS if section.start <= document_page <= section.end)


def detected_values(text: str, candidates: tuple[str, ...]) -> list[str]:
    return [candidate for candidate in candidates if candidate in text]


def page_title(section: Section, document_page: int, text: str) -> str:
    qualifiers: list[str] = []
    if "학문기초교양" in text:
        qualifiers.append("학문기초교양(학기교)")
    if "미디어·휴먼라이프대학" in text or "미디어ㆍ휴먼라이프대학" in text:
        qualifiers.append("미디어·휴먼라이프대학(미휴)")
    if "복수전공" in text:
        qualifiers.append("복수전공(복전)")
    suffix = " | " + "·".join(qualifiers[:2]) if qualifiers else ""
    return f"2026-2 학사안내문 | {section.title}{suffix} | {document_page}쪽"


def build_page_document(pdf_page: int, text: str) -> dict:
    document_page = max(0, pdf_page - 2)
    section = section_for(document_page)
    colleges = detected_values(text, COLLEGES)
    departments = detected_values(text, DEPARTMENTS)
    keywords = list(dict.fromkeys((*section.keywords, *sum((ALIASES.get(value, ()) for value in colleges + departments), ()))))
    if "학문기초교양" in text:
        keywords.extend(["학문기초교양", "학기교"])
    if "복수전공" in text:
        keywords.extend(["복수전공", "복전"])

    return {
        "id": f"2026-2:p{document_page:03d}",
        "kind": "source_page",
        "title": page_title(section, document_page, text),
        "section": section.code,
        "category": section.code,
        "pdfPage": pdf_page,
        "documentPage": document_page,
        "admissionYearFrom": section.admission_year_from,
        "admissionYearTo": section.admission_year_to,
        "colleges": colleges,
        "departments": departments,
        "keywords": list(dict.fromkeys(keywords)),
        "content": text,
        "sourceUrl": f"{SOURCE_URL}#guide-page-{document_page}",
    }


def curated_documents() -> list[dict]:
    base = {
        "kind": "verified_rule",
        "category": "academic_rule",
    }
    department_mapping_documents = [
        {
            **base,
            "id": f"2026-2:map:college-departments:{index:02d}",
            "title": f"{college} 소속 학과·전공 | 학기교 검색 연결",
            "section": "department_college_mapping",
            "pdfPage": COLLEGE_MAPPING_SOURCE_PAGE[college] + 2,
            "documentPage": COLLEGE_MAPPING_SOURCE_PAGE[college],
            "sourceUrl": f"{SOURCE_URL}#guide-page-{COLLEGE_MAPPING_SOURCE_PAGE[college]}-department-map",
            "admissionYearFrom": None,
            "admissionYearTo": None,
            "colleges": [college],
            "departments": list(departments),
            "keywords": list(dict.fromkeys([
                college,
                *ALIASES.get(college, ()),
                "학문기초교양",
                "학기교",
                *(alias for department in departments for alias in ALIASES.get(department, ())),
            ])),
            "content": (
                f"{college} 소속 학부·학과·전공은 {', '.join(departments)}이다. "
                "학기교를 검색할 때는 먼저 학과·전공을 이 단과대학에 연결하고, 그다음 본인 학번 구간의 "
                "학문기초교양 필요 학점·지정 과목·학과별 필수 조건을 적용한다. 학과 명칭이 변경된 경우에도 "
                "입학 당시 학번 구간과 현재 안내표의 변경 전후 명칭을 함께 대조한다."
            ),
        }
        for index, (college, departments) in enumerate(COLLEGE_DEPARTMENTS.items(), start=1)
    ]
    return [
        {
            **base,
            "id": "2026-2:rule:find-requirements",
            "title": "학기교·졸업요건 찾는 법 | 학번·단과대·학과별 확인",
            "section": "guide_navigation",
            "pdfPage": 20,
            "documentPage": 18,
            "sourceUrl": f"{SOURCE_URL}#guide-page-18-navigation",
            "admissionYearFrom": None,
            "admissionYearTo": None,
            "colleges": [],
            "departments": [],
            "keywords": ["학문기초교양", "학기교", "졸업요건", "졸업학점", "수강신청"],
            "content": (
                "학사안내문 목차에서 입학년도 구간을 먼저 선택한 뒤, 해당 구간의 학문기초교양 표와 "
                "졸업 최소 이수학점 표를 확인한다. 같은 학과라도 입학년도에 따라 지정과목과 학점이 다를 수 있다. "
                "복수전공자는 주전공과 복수전공 학과의 지정조건을 함께 확인하고, 최종 졸업 가능 여부는 단과대학 "
                "교학팀의 졸업사정으로 확인한다."
            ),
        },
        *department_mapping_documents,
        {
            **base,
            "id": "2026-2:rule:media-human-academic-foundation-by-admission-year",
            "title": "미휴 학기교 | 학번별 필요 학점·지정 과목",
            "section": "graduation_requirements",
            "pdfPage": 24,
            "documentPage": 22,
            "sourceUrl": f"{SOURCE_URL}#guide-page-22-media-human-life-by-admission-year",
            "admissionYearFrom": 2009,
            "admissionYearTo": None,
            "colleges": ["미디어·휴먼라이프대학"],
            "departments": ["디지털미디어학부", "청소년지도학전공", "아동학전공"],
            "keywords": [
                "미디어·휴먼라이프대학", "미디어휴먼라이프", "미휴", "미휴대",
                "학문기초교양", "학기교", "학번별", "필요 학점", "지정 과목",
            ],
            "content": (
                "미디어·휴먼라이프대학(미휴) 학문기초교양(학기교)의 학번별 조건이다. "
                "[2009~2014학번] 필요 학점: 12학점 | 학과별 지정: "
                "디지털미디어학과=인간관계와커뮤니케이션(3), 일반교양 문화와예술(3), "
                "일반교양 인문과학 또는 사회과학(6); "
                "청소년지도학과=청소년지도학(3), 일반교양 인문과학(6), 일반교양 문화와예술(3); "
                "아동학과=결혼과가족(3), 인간심리의이해(3), 일반교양 외국어(3), 일반교양 사회과학(3). "
                "[2015~2017학번] 필요 학점: 12학점 | 지정 과목군: "
                "행정학개론(3), 경제학원론(3), 대중문화와매스컴(3), 결혼과가족(3), 청소년지도학(3), "
                "사회복지개론(3), 유라시아의이해(3), 현대한국정치의쟁점(3), 놀이혁명과창의성(3), "
                "한국정치의이해(3), 국제정치의이해(3), 현대사회와정보(3), 경제학들어가기(3), "
                "직무커뮤니케이션능력개발(3), 인터넷과커뮤니케이션(3), 인간관계와커뮤니케이션(3), "
                "미시경제학원론(3), 거시경제학원론(3), 배려의행복학(3). "
                "[2018~2024학번] 필요 학점: 12학점 | 지정 과목군: "
                "행정학개론(3), 경제학원론(3), 대중문화와매스컴(3), 결혼과가족(3), 청소년지도학(3), "
                "사회복지개론(3), 유라시아의이해(3), 현대한국정치의쟁점(3), 놀이혁명과창의성(3), "
                "한국정치의이해(3), 공직적성의이해(3), 국제정치의이해(3), 현대사회와정보(3), "
                "경제학들어가기(3), 직무커뮤니케이션능력개발(3), 인터넷과커뮤니케이션(3), "
                "인간관계와커뮤니케이션(3), 미시경제학원론(3), 거시경제학원론(3), 배려의행복학(3). "
                "2015~2024학번 표에서 새로 추가된 밑줄 과목은 2023학년도 1학기 이후 이수한 과목부터 인정한다. "
                "[2025학번 이후] 필요 학점: 12학점 | 지정 과목군: "
                "대중문화와매스컴(3), 인터넷과커뮤니케이션(3), 인간관계와커뮤니케이션(3), "
                "청소년지도학(3), 결혼과가족(3), 놀이혁명과창의성(3), 배려의행복학(3), "
                "인간심리의이해(3), 다문화사회의이해(3). "
                "근거는 2026학년도 2학기 학사안내문 22쪽, 33쪽, 40쪽, 47쪽이다."
            ),
        },
        {
            **base,
            "id": "2026-2:rule:2025-social-academic-foundation",
            "title": "2025학번 이후 사회과학대학 학문기초교양(학기교) 12학점",
            "section": "graduation_2025_plus",
            "pdfPage": 49,
            "documentPage": 47,
            "sourceUrl": f"{SOURCE_URL}#guide-page-47-social-sciences",
            "admissionYearFrom": 2025,
            "admissionYearTo": None,
            "colleges": ["사회과학대학"],
            "departments": ["행정학전공", "정치외교학전공", "경제학전공", "국제통상학전공", "응용통계학전공", "법학과"],
            "keywords": ["사회과학대학", "정외", "정치외교학전공", "법학과", "학문기초교양", "학기교", "12학점"],
            "content": (
                "2025학번 이후 사회과학대학의 학문기초교양 이수학점은 12학점이다. 적용 전공은 공공인재학부 "
                "행정학전공·정치외교학전공, 경상·통계학부 경제학전공·국제통상학전공·응용통계학전공, 법학과다. "
                "지정 과목 풀은 행정학개론, 경제학원론, 유라시아의이해, 현대한국정치의쟁점, 한국정치의이해, "
                "국제정치의이해, 미시경제학원론, 거시경제학원론, 경상통계학, 통계학개론, 공직적성의이해, "
                "시장경제와법, 법과제도, 생활법률, 지적재산과현대사회, 공무원과법, 직업생활과법, 법률문장론, "
                "미적분학1, 선형대수학개론, 경영학입문이다. 경제학·국제통상학·응용통계학전공에는 별도 필수조건이 있다."
            ),
        },
        {
            **base,
            "id": "2026-2:rule:2025-media-human-academic-foundation",
            "title": "2025학번 이후 미디어·휴먼라이프대학(미휴) 학문기초교양(학기교) 12학점",
            "section": "graduation_2025_plus",
            "pdfPage": 49,
            "documentPage": 47,
            "sourceUrl": f"{SOURCE_URL}#guide-page-47-media-human-life",
            "admissionYearFrom": 2025,
            "admissionYearTo": None,
            "colleges": ["미디어·휴먼라이프대학"],
            "departments": ["디지털미디어학부", "청소년지도학전공", "아동학전공"],
            "keywords": ["미디어·휴먼라이프대학", "미디어휴먼라이프", "미휴", "미휴대", "학문기초교양", "학기교", "12학점"],
            "content": (
                "2025학번 이후 미디어·휴먼라이프대학(미휴)의 학문기초교양 이수학점은 12학점이다. "
                "적용 학부·전공은 디지털미디어학부와 청소년지도·아동학부의 청소년지도학전공·아동학전공이다. "
                "지정 과목은 대중문화와매스컴, 인터넷과커뮤니케이션, 인간관계와커뮤니케이션, 청소년지도학, "
                "결혼과가족, 놀이혁명과창의성, 배려의행복학, 인간심리의이해, 다문화사회의이해이며 각 3학점이다."
            ),
        },
        {
            **base,
            "id": "2026-2:rule:double-major-academic-foundation",
            "title": "복수전공(복전) 학문기초교양(학기교) 판정 원칙",
            "section": "multiple_major",
            "pdfPage": 52,
            "documentPage": 50,
            "sourceUrl": f"{SOURCE_URL}#guide-page-50-double-major",
            "admissionYearFrom": None,
            "admissionYearTo": None,
            "colleges": ["사회과학대학"],
            "departments": ["정치외교학전공", "법학과"],
            "keywords": ["복수전공", "복전", "학문기초교양", "학기교", "정외", "정치외교학전공", "법학과"],
            "content": (
                "학사안내문은 복수전공자의 학문기초교양을 소속학과 및 복수전공 학과에서 지정한 과목으로 이수하도록 "
                "안내한다. 따라서 정치외교학전공 학생이 법학과를 복수전공하는 경우에도 본인 입학년도의 두 지정조건을 "
                "대조해야 한다. 다만 2025학번 이후 안내표에서는 정치외교학전공과 법학과가 사회과학대학 공통 지정과목 "
                "풀에서 12학점을 이수하는 동일 조건으로 제시된다. 이전 학번은 해당 입학년도 표를 별도로 확인해야 한다."
            ),
        },
        {
            **base,
            "id": "2026-2:rule:premajor-vs-academic-foundation",
            "title": "전공이해 기초교과목과 학문기초교양(학기교)의 차이",
            "section": "required_courses",
            "pdfPage": 79,
            "documentPage": 77,
            "sourceUrl": f"{SOURCE_URL}#guide-page-77-premajor",
            "admissionYearFrom": 2025,
            "admissionYearTo": None,
            "colleges": ["미디어·휴먼라이프대학", "사회과학대학"],
            "departments": ["정치외교학전공", "법학과", "디지털미디어학부", "청소년지도학전공", "아동학전공"],
            "keywords": ["전공이해기초교과", "선이수교과", "학문기초교양", "학기교", "무전공", "아너칼리지"],
            "content": (
                "전공이해 기초교과목은 전공자율선택 입학생이 전공 진입 전에 탐색 목적으로 듣고, 해당 전공 진입 후 "
                "전공학점으로 인정되는 과목이다. 학문기초교양은 전공 진입과 별개의 졸업 교양요건이며 학문기초교양으로 "
                "인정된다. 따라서 전공이해 기초교과목 표를 학기교 과목표로 해석하면 안 된다."
            ),
        },
        {
            **base,
            "id": "2026-2:rule:course-registration-entry",
            "title": "2026-2 수강신청 시스템·미리담기 핵심 안내",
            "section": "course_registration_system",
            "pdfPage": 3,
            "documentPage": 1,
            "sourceUrl": f"{SOURCE_URL}#guide-page-1-registration",
            "admissionYearFrom": None,
            "admissionYearTo": None,
            "colleges": [],
            "departments": [],
            "keywords": ["수강신청", "미리담기", "수강신청 사이트", "수강신청 학점"],
            "content": (
                "수강신청 사이트는 https://class.mju.ac.kr 이다. 수강신청 시작 20분 전부터 미리 진입할 수 있다. "
                "미리담기는 실제 수강신청 완료가 아니며, 수강신청일에 담아 둔 과목의 신청 버튼을 눌러야 한다. "
                "미리담기 단계에서는 과목·시간 중복을 제한하지 않지만 실제 수강신청 단계에서는 중복을 검사한다."
            ),
        },
    ]


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("usage: import_academic_guide.py INPUT.pdf OUTPUT.json")

    input_path = Path(sys.argv[1])
    output_path = Path(sys.argv[2])
    reader = PdfReader(input_path)

    documents = curated_documents()
    for index, page in enumerate(reader.pages, start=1):
        if index <= 2:
            continue
        document_page = index - 2
        if document_page > 173:
            break
        text = normalize_text(page.extract_text() or "")
        if text:
            documents.append(build_page_document(index, text))

    payload = {
        "version": 1,
        "guide": "2026-2",
        "revision": "v1.6",
        "publishedAt": PUBLISHED_AT,
        "sourceUrl": SOURCE_URL,
        "documentCount": len(documents),
        "documents": documents,
    }
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"wrote {len(documents)} documents to {output_path}")


if __name__ == "__main__":
    main()
