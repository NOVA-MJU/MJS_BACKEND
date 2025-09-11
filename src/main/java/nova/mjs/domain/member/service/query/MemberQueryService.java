package nova.mjs.domain.member.service.query;

import nova.mjs.domain.member.DTO.MemberDTO;
import nova.mjs.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * 회원 조회용 서비스 인터페이스
 * - 이메일/닉네임 중복 검증
 * - 페이징 처리된 회원 목록 조회
 * - UUID 또는 이메일 ID로 회원 상세 조회
 */
public interface MemberQueryService {

    // 모든 회원 정보를 페이지 단위로 조회
    Page<MemberDTO> getAllMember(Pageable pageable);

    // 회원 상세 조회
    MemberDTO getMemberByUuid(UUID userUUID);

    // 이메일 ID를 이용한 회원 상세 조회 - MemberDTO로 반환
    MemberDTO getMemberDtoByEmailId(String emailId);

    // 이메일 ID를 이용한 회원 상세 조회
    Member getMemberByEmail(String emailId);

    // 이메일 중복 검증
    void validateEmailDuplication(String email);

    // 닉네임 중복 검증
    void validateNicknameDuplication(String nickname);

    // 학번 중복 검증
    void validateStudentNumberDuplication(String studentNumber);

    // email 도메인 검증
    void validateEmailDomain(String email);

    // 학번 검증 메서드
    void validateStudentNumber(String studentNumber);

    // 이메일 존재 여부 검증 (비밀번호 찾기용)
    void validateEmailExistence(String email);
}
