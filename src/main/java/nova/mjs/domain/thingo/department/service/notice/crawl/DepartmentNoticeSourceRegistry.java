package nova.mjs.domain.thingo.department.service.notice.crawl;

import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;

import java.util.List;

public final class DepartmentNoticeSourceRegistry {

    private DepartmentNoticeSourceRegistry() {
    }

    public static List<DepartmentNoticeSource> sources() {
        return List.of(
                source(College.HUMANITIES, DepartmentName.CHINESE_LITERATURE, "중국언어문화학과", "https://cll.mju.ac.kr/cll/4835/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.JAPANESE_LITERATURE, "일본언어문화학과", "https://nichibun.mju.ac.kr/nichibun/4862/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.ARABIC_STUDIES, "아랍지역학전공", "https://arab.mju.ac.kr/arab/8470/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.KOREAN_LITERATURE, "국어국문학과", "https://kll.mju.ac.kr/kll/8392/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.KOREAN_LITERATURE, "국어국문학과-학교공지", "https://kll.mju.ac.kr/kll/8657/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.ENGLISH_LITERATURE, "영어영문학과", "https://english.mju.ac.kr/english/6923/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.ENGLISH_LITERATURE, "영어영문학과-학사공지", "https://english.mju.ac.kr/english/6910/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.LIBRARY_SCIENCE, "문헌정보학과-학사공지", "https://lis.mju.ac.kr/lis/5973/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.LIBRARY_SCIENCE, "문헌정보학과-전공공지", "https://lis.mju.ac.kr/lis/7505/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.LIBRARY_SCIENCE, "문헌정보학과-취업공지", "https://lis.mju.ac.kr/lis/5974/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.CREATIVE_WRITING, "문예창작학과", "https://writers.mju.ac.kr/writers/7468/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.CREATIVE_WRITING, "문예창작학과-학생활동", "https://writers.mju.ac.kr/writers/7318/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.CREATIVE_WRITING, "문예창작학과-채용/공모전", "https://writers.mju.ac.kr/writers/7319/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.CREATIVE_WRITING, "문예창작학과-행사안내", "https://writers.mju.ac.kr/writers/8682/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.SOCIAL_SCIENCES, DepartmentName.PUBLIC_ADMINISTRATION, "행정학전공", "https://mjpa.mju.ac.kr/mjpa/6392/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.SOCIAL_SCIENCES, DepartmentName.LAW, "법학과", "https://col.mju.ac.kr/col/1299/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.SOCIAL_SCIENCES, DepartmentName.LAW, "법학과-대학원", "https://col.mju.ac.kr/col/1305/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.SOCIAL_SCIENCES, DepartmentName.LAW, "법학연구소", "https://col.mju.ac.kr/col/1323/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.MEDIA_HUMANLIFE, DepartmentName.DIGITAL_MEDIA_STUDIES, "디지털미디어학과", "https://dm.mju.ac.kr/dm/6438/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.MEDIA_HUMANLIFE, DepartmentName.YOUTH_GUIDANCE_STUDIES, "청소년지도전공", "https://youth.mju.ac.kr/youth/6858/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.MEDIA_HUMANLIFE, DepartmentName.CHILD_STUDIES, "아동학전공", "https://child.mju.ac.kr/child/7440/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.AI_SOFTWARE, DepartmentName.DIGITAL_CONTENT_DESIGN_STUDIES, "디지털콘텐츠디자인학과-학생공지", "https://dcd.mju.ac.kr/dcd/7361/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.AI_SOFTWARE, DepartmentName.DIGITAL_CONTENT_DESIGN_STUDIES, "디지털콘텐츠디자인학과-학과공지", "https://dcd.mju.ac.kr/dcd/7377/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.AI_SOFTWARE, DepartmentName.DIGITAL_CONTENT_DESIGN_STUDIES, "디지털콘텐츠디자인학과-취업공지", "https://dcd.mju.ac.kr/dcd/7378/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.ART_HISTORY_DEPARTMENT, "미술사학과-일반공지", "https://arthistory.mju.ac.kr/arthistory/7493/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.ART_HISTORY_DEPARTMENT, "미술사학과-취업공지", "https://arthistory.mju.ac.kr/arthistory/7494/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.ART_HISTORY_DEPARTMENT, "미술사학과-기타", "https://arthistory.mju.ac.kr/arthistory/7495/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.HISTORY_DEPARTMENT, "사학과", "https://mjhistory.mju.ac.kr/bbs/gs/195/artclList.do", DepartmentNoticeSourceType.GNUBOARD),
                source(College.HUMANITIES, DepartmentName.PHILOSOPHY, "철학과-학사공지", "https://phil.mju.ac.kr/philosohpy/6980/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HUMANITIES, DepartmentName.PHILOSOPHY, "철학과-학과공지", "https://phil.mju.ac.kr/philosohpy/7003/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.FUTURE_CONVERGENCE, null, "미래융합대학", "http://future.mju.ac.kr/bbs/board.php?bo_table=notice", DepartmentNoticeSourceType.PHP_BOARD),
                source(College.HUMANITIES, null, "인문대학", "https://www.mju.ac.kr/humanities/2801/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.SOCIAL_SCIENCES, null, "사회과학대학", "https://www.mju.ac.kr/social/2940/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.BUSINESS, null, "경영대학", "https://www.mju.ac.kr/sba/2223/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.AI_SOFTWARE, null, "인공지능·소프트웨어융합대학", "https://www.mju.ac.kr/ict/9829/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.AI_SOFTWARE, DepartmentName.CONVERGENT_SOFTWARE_STUDIES, "융합소프트웨어학부", "https://www.mju.ac.kr/software/9799/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.AI_SOFTWARE, DepartmentName.CONVERGENT_SOFTWARE_STUDIES, "융합소프트웨어학부-취업/특강", "https://www.mju.ac.kr/software/9800/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.HONOR, null, "아너칼리지", "https://www.mju.ac.kr/mjukr/10235/subview.do", DepartmentNoticeSourceType.MJU_SUBVIEW),
                source(College.FUTURE_CONVERGENCE, DepartmentName.SOCIAL_WELFARE, "사회복지학과", "https://mjwelfare.mju.ac.kr/default/05/01.php?topmenu=5&left=1", DepartmentNoticeSourceType.PHP_BOARD),
                source(College.FUTURE_CONVERGENCE, DepartmentName.LAW_ADMINISTRATION, "법행정학과", "https://mjlaw.mju.ac.kr/default/05/01.php?topmenu=5&left=1", DepartmentNoticeSourceType.PHP_BOARD),
                source(College.FUTURE_CONVERGENCE, DepartmentName.PSYCHOLOGY_THERAPY, "심리치료학과", "https://psy.mju.ac.kr/default/05/01.php?topmenu=5&left=1", DepartmentNoticeSourceType.PHP_BOARD),
                source(College.FUTURE_CONVERGENCE, DepartmentName.REAL_ESTATE, "부동산학과", "https://real.mju.ac.kr/default/05/01.php?topmenu=5&left=1", DepartmentNoticeSourceType.PHP_BOARD),
                source(College.FUTURE_CONVERGENCE, DepartmentName.MULTI_DESIGN, "멀티디자인학과", "https://multid.mju.ac.kr/default/05/01.php?topmenu=5&left=1", DepartmentNoticeSourceType.PHP_BOARD),
                source(College.FUTURE_CONVERGENCE, DepartmentName.FUTURE_CONVERGENCE_BUSINESS, "미래융합경영학과", "https://dfba.mju.ac.kr/default/05/01.php?topmenu=5&left=1", DepartmentNoticeSourceType.PHP_BOARD),
                source(College.FUTURE_CONVERGENCE, DepartmentName.ACCOUNTING_TAXATION, "회계·세무학과", "https://actx.mju.ac.kr/default/05/01.php?topmenu=5&left=1", DepartmentNoticeSourceType.PHP_BOARD),
                source(College.FUTURE_CONVERGENCE, null, "비즈HRD학과", "https://bizhrd.mju.ac.kr/default/notice/sub1.php", DepartmentNoticeSourceType.PHP_BOARD),
                source(College.SOCIAL_SCIENCES, DepartmentName.ECONOMICS, "경제학과", "https://www.mjuecon.org/blog", DepartmentNoticeSourceType.WORDPRESS)
        );
    }

    private static DepartmentNoticeSource source(
            College college,
            DepartmentName departmentName,
            String label,
            String url,
            DepartmentNoticeSourceType sourceType
    ) {
        return new DepartmentNoticeSource(college, departmentName, label, url, sourceType);
    }
}
