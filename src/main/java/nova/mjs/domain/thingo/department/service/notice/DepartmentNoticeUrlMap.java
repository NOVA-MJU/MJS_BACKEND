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

        m.put(new Key(College.HUMANITIES, DepartmentName.CHINESE_LITERATURE),
                "https://cll.mju.ac.kr/cll/4835/subview.do");

        m.put(new Key(College.HUMANITIES, DepartmentName.JAPANESE_LITERATURE),
                "https://nichibun.mju.ac.kr/nichibun/4862/subview.do");

        m.put(new Key(College.HUMANITIES, DepartmentName.ARABIC_STUDIES),
                "https://arab.mju.ac.kr/arab/8470/subview.do");

        m.put(new Key(College.HUMANITIES, DepartmentName.KOREAN_LITERATURE),
                "https://kll.mju.ac.kr/kll/8392/subview.do");

        m.put(new Key(College.HUMANITIES, DepartmentName.ENGLISH_LITERATURE),
                "https://english.mju.ac.kr/english/6923/subview.do");

        m.put(new Key(College.HUMANITIES, DepartmentName.LIBRARY_SCIENCE),
                "https://lis.mju.ac.kr/lis/5973/subview.do");

        m.put(new Key(College.HUMANITIES, DepartmentName.CREATIVE_WRITING),
                "https://writers.mju.ac.kr/writers/7468/subview.do");

        m.put(new Key(College.HUMANITIES, DepartmentName.ART_HISTORY_DEPARTMENT),
                "https://arthistory.mju.ac.kr/arthistory/7493/subview.do");

        m.put(new Key(College.HUMANITIES, DepartmentName.HISTORY_DEPARTMENT),
                "https://mjhistory.mju.ac.kr/bbs/gs/195/artclList.do");

        // ⚠️ 여기 오탈자 가능성: philosohpy vs philosophy
        m.put(new Key(College.HUMANITIES, DepartmentName.PHILOSOPHY),
                "https://phil.mju.ac.kr/philosophy/6980/subview.do");
        // (학과공지) https://phil.mju.ac.kr/philosophy/7003/subview.do


        /* =========================
         * SOCIAL_SCIENCES (사회과학대학)
         * ========================= */

        m.put(new Key(College.SOCIAL_SCIENCES, DepartmentName.PUBLIC_ADMINISTRATION),
                "https://mjpa.mju.ac.kr/mjpa/6392/subview.do");

        m.put(new Key(College.SOCIAL_SCIENCES, DepartmentName.LAW),
                "https://col.mju.ac.kr/col/1299/subview.do");


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

        m.put(new Key(College.AI_SOFTWARE, DepartmentName.DIGITAL_CONTENT_DESIGN_STUDIES),
                "https://dcd.mju.ac.kr/dcd/7377/subview.do");

        m.put(new Key(College.AI_SOFTWARE, DepartmentName.CONVERGENT_SOFTWARE_STUDIES),
                "https://www.mju.ac.kr/software/9799/subview.do");


        /* =========================
         * FUTURE_CONVERGENCE (미래융합대학)
         * ========================= */

        m.put(new Key(College.FUTURE_CONVERGENCE, null),
                "http://future.mju.ac.kr/bbs/board.php?bo_table=notice");

        m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.SOCIAL_WELFARE),
                "https://mjwelfare.mju.ac.kr/default/05/01.php?topmenu=5&left=1");

        m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.LAW_ADMINISTRATION),
                "https://mjlaw.mju.ac.kr/default/05/01.php?topmenu=5&left=1");

        m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.PSYCHOLOGY_THERAPY),
                "https://psy.mju.ac.kr/default/05/01.php?topmenu=5&left=1");

        m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.REAL_ESTATE),
                "https://real.mju.ac.kr/default/05/01.php?topmenu=5&left=1");

        m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.MULTI_DESIGN),
                "https://multid.mju.ac.kr/default/05/01.php?topmenu=5&left=1");

        m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.FUTURE_CONVERGENCE_BUSINESS),
                "https://dfba.mju.ac.kr/default/05/01.php?topmenu=5&left=1");

        m.put(new Key(College.FUTURE_CONVERGENCE, DepartmentName.ACCOUNTING_TAXATION),
                "https://actx.mju.ac.kr/default/05/01.php?topmenu=5&left=1");


        /* =========================
         * HONOR (아너칼리지)
         * ========================= */

        m.put(new Key(College.HONOR, null),
                "https://www.mju.ac.kr/mjukr/10235/subview.do");

        m.put(new Key(College.HONOR, DepartmentName.FREE_MAJOR),
                "https://www.mju.ac.kr/mjukr/10235/subview.do");


        /* =========================
         * 단과대학 공지(대표 도메인 subview)
         * ========================= */

        m.put(new Key(College.HUMANITIES, null), "https://www.mju.ac.kr/humanities/2801/subview.do");
        m.put(new Key(College.SOCIAL_SCIENCES, null), "https://www.mju.ac.kr/social/2940/subview.do");
        m.put(new Key(College.BUSINESS, null), "https://www.mju.ac.kr/sba/2223/subview.do");
        m.put(new Key(College.AI_SOFTWARE, null), "https://www.mju.ac.kr/ict/9829/subview.do");

        URLS = Collections.unmodifiableMap(m);
    }

    public static Optional<String> get(College college, DepartmentName departmentName) {
        String direct = URLS.get(new Key(college, departmentName));
        if (direct != null) return Optional.of(direct);

        return Optional.ofNullable(URLS.get(new Key(college, null)));
    }

    public static Map<Key, String> dump() {
        return URLS;
    }
}