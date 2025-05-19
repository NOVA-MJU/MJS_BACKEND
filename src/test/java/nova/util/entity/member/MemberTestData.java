package nova.util.entity.member;

import com.navercorp.fixturemonkey.FixtureMonkey;
import nova.mjs.member.Member;
import nova.mjs.member.Member.Gender;
import nova.mjs.member.Member.Role;

import java.util.UUID;

public class MemberTestData {

    private static final FixtureMonkey fixture = FixtureMonkey.create();

    public static Member sample() {
        return fixture.giveMeBuilder(Member.class)
                .set("uuid", UUID.randomUUID())
                .set("name", "홍길동")
                .set("email", "hong@gildong.com")
                .set("password", "encoded_password")
                .set("gender", Gender.MALE)
                .set("nickname", "길동이")
                .set("department", "컴퓨터공학과")
                .set("studentNumber", 202312345)
                .set("role", Role.USER)
                .sample();
    }

    public static Member sampleWithRole(Role role) {
        Member member = sample();
        return Member.builder()
                .id(member.getId())
                .uuid(member.getUuid())
                .name(member.getName())
                .email(member.getEmail())
                .password(member.getPassword())
                .gender(member.getGender())
                .nickname(member.getNickname())
                .department(member.getDepartment())
                .studentNumber(member.getStudentNumber())
                .role(role)
                .build();
    }

    public static Member sampleWithNickname(String nickname) {
        Member member = sample();
        return Member.builder()
                .id(member.getId())
                .uuid(member.getUuid())
                .name(member.getName())
                .email(member.getEmail())
                .password(member.getPassword())
                .gender(member.getGender())
                .nickname(nickname)
                .department(member.getDepartment())
                .studentNumber(member.getStudentNumber())
                .role(member.getRole())
                .build();
    }
}

