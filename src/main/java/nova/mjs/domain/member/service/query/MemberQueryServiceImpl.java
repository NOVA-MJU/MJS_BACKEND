package nova.mjs.domain.member.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.domain.member.DTO.MemberDTO;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.exception.*;
import nova.mjs.domain.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberRepository memberRepository;


    @Override
    public MemberDTO getMemberByUuid(UUID userUUID) {
        Member member = memberRepository.findByUuid(userUUID)
                .orElseThrow(MemberNotFoundException::new);

        return MemberDTO.fromEntity(member);
    }

    @Override
    public MemberDTO getMemberDtoByEmailId(String emailId) {
      Member member = getMemberByEmail(emailId);

      return MemberDTO.fromEntity(member);
    }

    public Member getMemberByEmail(String emailId) {
        return memberRepository.findByEmail(emailId)
                .orElseThrow(MemberNotFoundException::new);
    }

    @Override
    public Page<MemberDTO> getAllMember(Pageable pageable) {
        return memberRepository.findAll(pageable).map(MemberDTO::fromEntity);
    }


    @Override
    public void validateEmailDuplication(String email) {
        if (email == null) {
            throw new EmailIsInvalidException();
        }
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
    }

    @Override
    public void validateNicknameDuplication(String nickname) {
        if (nickname == null || nickname.isBlank()) {throw new NicknameIsInvalidException();}
        if (memberRepository.existsByNickname(nickname)) {
            throw new DuplicateNicknameException();
        }
    }

    // 이메일 도메인 검증 메서드
    @Override
    public void validateEmailDomain(String email) {
        if (email == null || !email.matches("^[a-zA-Z0-9._%+-]+@mju\\.ac\\.kr$")) {
            throw new EmailIsInvalidException();
        }
    }

    @Override
    public void validateStudentNumberDuplication(String studentNumber) {
        validateStudentNumber(studentNumber);
        if (memberRepository.existsByStudentNumber(studentNumber)) {
            throw new DuplicateStudentNumberException();
        }
    }

    @Override
    public void validateStudentNumber(String studentNumber) {
        if (studentNumber == null || studentNumber.length() != 8 || !studentNumber.chars().allMatch(Character::isDigit)) {
            throw new InvalidStudentNumberException();
        }

    }

}

