package nova.mjs.domain.thingo.department.service.notice;

import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;

import java.util.*;

public final class DepartmentNoticeUrlMap {

    private DepartmentNoticeUrlMap() {}

    public record Key(College college, DepartmentName departmentName) {}

    private static final Map<Key, String> URLS;

    static {
        Map<Key, String> m = new LinkedHashMap<>();

        /* =========================
         * HUMANITIES (인문대학)
         * ========================= */

        // 중국언어문화학과 -> (enum에 "중국언어문화학과"가 직접 없음)
        // 현 enum 기준 가장 근접: CHINESE_LITERATURE("중어중문학전공")
        // ⚠️ 실제로 "중국언어문화학과"와 동일 조직인지 확인 필요
        m.put(new Key(College.HUMANITIES, DepartmentName.CHINESE_LITERATURE),
                "https://cll.mju.ac.kr/cll/4835/subview.do");

        // 일본언어문화학과 -> JAPANESE_LITERATURE("일어일문학전공")
        m.put(new Key(College.HUMANITIES, DepartmentName.JAPANESE_LITERATURE),
                "https://nichibun.mju.ac.kr/nichibun/4862/subview.do");

        // 아랍지역학전공
        m.put(new Key(College.HUMANITIES, DepartmentName.ARABIC_STUDIES),
                "https://arab.mju.ac.kr/arab/8470/subview.do");

        // 국어국문학전공
        m.put(new Key(College.HUMANITIES, DepartmentName.KOREAN_LITERATURE),
                "https://kll.mju.ac.kr/kll/8392/subview.do");

        // 국어국문학과 (학교공지) — enum에 “학교공지” 분기가 없음
        // => 운영상 필요하면 OTHER로 별도 키를 만들 수 없으니,
        //    1) DepartmentName에 분기 enum을 추가하거나
        //    2) URL을 하나만 쓰거나(현재는 전공공지로)
        // 여기서는 “학교공지” URL을 별도 항목으로 저장하지 못하므로 주석 처리합니다.
        // m.put(new Key(College.HUMANITIES, DepartmentName.KOREAN_LITERATURE_SCHOOL_NOTICE), "https://kll.mju.ac.kr/kll/8657/subview.do");

        // 영어영문학전공
        m.put(new Key(College.HUMANITIES, DepartmentName.ENGLISH_LITERATURE),
                "https://english.mju.ac.kr/english/6923/subview.do");

        // 영어영문학과 (학사공지) — enum에 분기 없음(동일 이슈)
        // m.put(new Key(College.HUMANITIES, DepartmentName.ENGLISH_LITERATURE_ACADEMIC_NOTICE), "https://english.mju.ac.kr/english/6910/subview.do");

        // 문헌정보학전공 (여러 분기 URL이 있으나 enum 분기가 없음)
        // 일단 대표로 "학사공지" 하나만 연결(원하시는 분기 기준으로 바꾸세요)
        m.put(new Key(College.HUMANITIES, DepartmentName.LIBRARY_SCIENCE),
                "https://lis.mju.ac.kr/lis/5973/subview.do");
        // (전공공지) https://lis.mju.ac.kr/lis/7505/subview.do
        // (취업공지) https://lis.mju.ac.kr/lis/5974/subview.do

        // 문예창작학과 (분기 URL 4개, enum 분기 없음) => 대표 URL만 연결
        m.put(new Key(College.HUMANITIES, DepartmentName.CREATIVE_WRITING),
                "https://writers.mju.ac.kr/writers/7468/subview.do");
        // (학생활동) https://writers.mju.ac.kr/writers/7318/subview.do
        // (채용/공모전) https://writers.mju.ac.kr/writers/7319/subview.do
        // (행사안내) https://writers.mju.ac.kr/writers/8682/subview.do

        // 미술사학과: enum에 ART_HISTORY_DEPARTMENT(미술사학과) 있음
        // 분기 URL(일반/취업/기타) 있는데 enum 분기가 없음 -> 대표 1개만 연결
        m.put(new Key(College.HUMANITIES, DepartmentName.ART_HISTORY_DEPARTMENT),
                "https://arthistory.mju.ac.kr/arthistory/7493/subview.do");
        // (취업공지) https://arthistory.mju.ac.kr/arthistory/7494/subview.do
        // (기타) https://arthistory.mju.ac.kr/arthistory/7495/subview.do

        // 사학과 (artclList.do 형태)
        m.put(new Key(College.HUMANITIES, DepartmentName.HISTORY_DEPARTMENT),
                "https://mjhistory.mju.ac.kr/bbs/gs/195/artclList.do");

        // 철학과 (학사/학과 공지 분기) — enum 분기 없음 -> 대표 1개 연결(학사공지)
        m.put(new Key(College.HUMANITIES, DepartmentName.PHILOSOPHY),
                "https://phil.mju.ac.kr/philosohpy/6980/subview.do");
        // (학과공지) https://phil.mju.ac.kr/philosohpy/7003/subview.do


        /* =========================
         * SOCIAL_SCIENCES (사회과학대학)
         * ========================= */

        m.put(new Key(College.SOCIAL_SCIENCES, DepartmentName.PUBLIC_ADMINISTRATION),
                "https://mjpa.mju.ac.kr/mjpa/6392/subview.do");

        // 법학과는 enum상 SOCIAL_SCIENCES에 있음
        m.put(new Key(College.SOCIAL_SCIENCES, DepartmentName.LAW),
                "https://col.mju.ac.kr/col/1299/subview.do");
        // (대학원) https://col.mju.ac.kr/col/1305/subview.do
        // (연구소) https://col.mju.ac.kr/col/1323/subview.do


        /* =========================
         * MEDIA_HUMANLIFE (미디어휴먼라이프대학)
         * ========================= */

        m.put(new Key(College.MEDIA_HUMANLIFE, DepartmentName.DIGITAL_MEDIA_STUDIES),
                "https://dm.mju.ac.kr/dm/6438/subview.do");

        m.put(new Key(College.MEDIA_HUMANLIFE, DepartmentName.YOUTH_GUIDANCE_STUDIES),
                "https://youth.mju.ac.kr/youth/6858/subview.do");

        m.put(new Key(College.MEDIA_HUMANLIFE, DepartmentName.CHILD_STUDIES),
                "https://child.mju.ac.kr/child/7440/subview.do");


        /* =========================
         * AI_SOFTWARE (인공지능·소프트웨어융합대학)
         * ========================= */

        // 디지털콘텐츠디자인학과: 분기(학생/학과/취업) -> 대표 1개 연결(학과공지)
        m.put(new Key(College.AI_SOFTWARE, DepartmentName.DIGITAL_CONTENT_DESIGN_STUDIES),
                "https://dcd.mju.ac.kr/dcd/7377/subview.do");
        // (학생공지) https://dcd.mju.ac.kr/dcd/7361/subview.do
        // (취업공지) https://dcd.mju.ac.kr/dcd/7378/subview.do

        // 융합소프트웨어학부: enum에 CONVERGENT_SOFTWARE_STUDIES 존재
        m.put(new Key(College.AI_SOFTWARE, DepartmentName.CONVERGENT_SOFTWARE_STUDIES),
                "https://www.mju.ac.kr/software/9799/subview.do");
        // (취업/특강) https://www.mju.ac.kr/software/9800/subview.do


        /* =========================
         * FUTURE_CONVERGENCE (미래융합대학)
         * ========================= */

        // 단과대 공지(학과 null일 때): departmentName=null로 키 사용
        m.put(new Key(College.FUTURE_CONVERGENCE, null),
                "http://future.mju.ac.kr/bbs/board.php?bo_table=notice");

        // 사회복지학과
        m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.SOCIAL_WELFARE),
                "https://mjwelfare.mju.ac.kr/default/05/01.php?topmenu=5&left=1");

        // 법행정학과 (요청: "법행정학과", enum: LAW_ADMINISTRATION("법무행정학과"))
        // ⚠️ 명칭 차이 있음. 실조직 동일 여부 확인 필요
        m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.LAW_ADMINISTRATION),
                "https://mjlaw.mju.ac.kr/default/05/01.php?topmenu=5&left=1");

        // 심리치료학과
        m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.PSYCHOLOGY_THERAPY),
                "https://psy.mju.ac.kr/default/05/01.php?topmenu=5&left=1");

        // 부동산학과
        m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.REAL_ESTATE),
                "https://real.mju.ac.kr/default/05/01.php?topmenu=5&left=1");

        // 멀티디자인학과
        m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.MULTI_DESIGN),
                "https://multid.mju.ac.kr/default/05/01.php?topmenu=5&left=1");

        // 미래융합경영학과
        m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.FUTURE_CONVERGENCE_BUSINESS),
                "https://dfba.mju.ac.kr/default/05/01.php?topmenu=5&left=1");

        // 회계·세무학과 -> enum: ACCOUNTING_TAXATION("회계세무학과")
        // ⚠️ 요청표기(회계·세무학과) vs enum 라벨(회계세무학과) 차이만 있음(동일 조직으로 봄)
        m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.ACCOUNTING_TAXATION),
                "https://actx.mju.ac.kr/default/05/01.php?topmenu=5&left=1");

        // 비즈HRD학과 -> enum에 없음 => OTHER로는 특정 학과를 표현 못하므로 매핑 불가
        // 해결: DepartmentName enum에 BIZ_HRD 추가(권장) 또는 CONTRACT/OTHER를 재활용(비추)
        // m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.BIZ_HRD), "https://bizhrd.mju.ac.kr/default/notice/sub1.php");


        /* =========================
         * HONOR (아너칼리지)
         * ========================= */

        // 단과/조직 레벨 공지
        m.put(new Key(College.HONOR, null),
                "https://www.mju.ac.kr/mjukr/10235/subview.do");

        // FREE_MAJOR(자율전공학부(인문))을 별도 학부로 취급한다면 아래처럼도 가능
        // 다만 URL이 “아너칼리지 공지” 하나만이라서 통상 null 키로 충분합니다.
        m.put(new Key(College.HONOR, DepartmentName.FREE_MAJOR),
                "https://www.mju.ac.kr/mjukr/10235/subview.do");


        /* =========================
         * 단과대학 공지(대표 도메인 subview)
         * - DepartmentName null 키로 처리
         * ========================= */

        m.put(new Key(College.HUMANITIES, null), "https://www.mju.ac.kr/humanities/2801/subview.do");
        m.put(new Key(College.SOCIAL_SCIENCES, null), "https://www.mju.ac.kr/social/2940/subview.do");
        m.put(new Key(College.BUSINESS, null), "https://www.mju.ac.kr/sba/2223/subview.do");
        m.put(new Key(College.AI_SOFTWARE, null), "https://www.mju.ac.kr/ict/9829/subview.do");
        // MEDIA_HUMANLIFE 단과 공지 URL이 제공 목록에는 없음 (필요 시 추가)

        URLS = Collections.unmodifiableMap(m);
    }

    /**
     * 학과 공지 URL 조회:
     * - 학과(College+DepartmentName) 우선
     * - 없으면 단과대(College+null) fallback
     */
    public static Optional<String> get(College college, DepartmentName departmentName) {
        String direct = URLS.get(new Key(college, departmentName));
        if (direct != null) return Optional.of(direct);

        return Optional.ofNullable(URLS.get(new Key(college, null)));
    }

    public static Map<Key, String> dump() {
        return URLS;
    }
}