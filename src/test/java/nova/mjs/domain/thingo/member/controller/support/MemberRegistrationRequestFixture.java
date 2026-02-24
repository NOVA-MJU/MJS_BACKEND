package nova.mjs.domain.thingo.member.controller.support;

import com.navercorp.fixturemonkey.FixtureMonkey;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.member.DTO.MemberDTO;

public final class MemberRegistrationRequestFixture {

    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.create();

    private MemberRegistrationRequestFixture() {
    }

    public static MemberDTO.MemberRegistrationRequestDTO validRequest(College college,
                                                                      DepartmentName departmentName,
                                                                      int sequence) {
        return FIXTURE_MONKEY.giveMeBuilder(MemberDTO.MemberRegistrationRequestDTO.class)
                .set("name", "테스트유저" + sequence)
                .set("email", "member" + sequence + "@mju.ac.kr")
                .set("password", "password1234")
                .set("nickname", "닉네임" + sequence)
                .set("gender", "MALE")
                .set("departmentName", departmentName)
                .set("college", college)
                .set("studentNumber", String.format("2024%04d", sequence))
                .set("profileImageUrl", "https://cdn.example.com/profile/default.png")
                .sample();
    }

    public static MemberDTO.MemberRegistrationRequestDTO invalidStudentNumberRequest() {
        return FIXTURE_MONKEY.giveMeBuilder(MemberDTO.MemberRegistrationRequestDTO.class)
                .set("name", "형식오류유저")
                .set("email", "invalid-student-number@mju.ac.kr")
                .set("password", "password1234")
                .set("nickname", "형식오류닉네임")
                .set("gender", "MALE")
                .set("departmentName", DepartmentName.BUSINESS_ADMINISTRATION)
                .set("college", College.BUSINESS)
                .set("studentNumber", "2024ABCD")
                .set("profileImageUrl", "https://cdn.example.com/profile/default.png")
                .sample();
    }
}
