package nova.mjs.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.community.DTO.CommunityBoardResponse;
import nova.mjs.member.exception.DuplicateEmailException;
import nova.mjs.member.exception.MemberNotFoundException;
import nova.mjs.member.exception.PasswordIsInvalidException;
import nova.mjs.util.exception.ErrorCode;
import nova.mjs.util.exception.request.RequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    public MemberDTO getMemberByUuid(UUID userUUID) {
        Member member = memberRepository.findByUuid(userUUID)
                .orElseThrow(MemberNotFoundException::new);

        return MemberDTO.fromEntity(member);
    }

    public Page<MemberDTO> getAllMember(Pageable pageable) {
        return memberRepository.findAll(pageable).map(MemberDTO::fromEntity);
    }

    // 회원 가입
    @Transactional
    public Member registerMember(MemberDTO.MemberRequestDTO requestDTO) {
        if (memberRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateEmailException();
        }

        String encodePassword = passwordEncoder.encode(requestDTO.getPassword());
        log.info(encodePassword);
        Member newMember = Member.create(requestDTO, encodePassword);
        return memberRepository.save(newMember);
    }

    // 회원 정보 수정
    @Transactional
    public Member updateMember(UUID userUUID, MemberDTO requestDTO) {
        Member member = memberRepository.findByUuid(userUUID)
                .orElseThrow(MemberNotFoundException::new);

        member.update(requestDTO);
        return memberRepository.save(member);
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(UUID userUUID, MemberDTO.PasswordRequestDTO requestDTO) {
        Member member = memberRepository.findByUuid(userUUID)
                .orElseThrow(MemberNotFoundException::new);
        if (!passwordEncoder.matches(requestDTO.getPassword(), member.getPassword())) {
            throw new PasswordIsInvalidException(); // 기존 비밀번호가 틀린 경우 예외 발생
        }

        if (requestDTO.getNewPassword() == null || requestDTO.getNewPassword().isBlank()) {
            throw new RequestException(ErrorCode.INVALID_REQUEST); // 새 비밀번호가 비어 있는 경우 예외 발생
        }

        // 기존 비밀번호와 새 비밀번호가 동일한지 체크
        if (passwordEncoder.matches(requestDTO.getNewPassword(), member.getPassword())) {
            throw new RequestException(ErrorCode.SAME_PASSWORD_NOT_ALLOWED); // 동일한 비밀번호로 변경 불가
        }

        String encodedNewPassword = passwordEncoder.encode(requestDTO.getNewPassword());
        member.updatePassword(encodedNewPassword);
        memberRepository.save(member);
    }

    // 회원 삭제
    @Transactional
    public void deleteMember(UUID userUUID, MemberDTO.PasswordRequestDTO requestPassword) {
        Member member = memberRepository.findByUuid(userUUID)
                .orElseThrow(MemberNotFoundException::new);

        // 비밀번호 검증
        boolean passwordMatches = passwordEncoder.matches(requestPassword.getPassword(), member.getPassword());

        if (!passwordMatches) {
            throw new PasswordIsInvalidException();
        }
        memberRepository.delete(member);
        log.info("회원 삭제 - UUID: {}", userUUID);
    }
}

