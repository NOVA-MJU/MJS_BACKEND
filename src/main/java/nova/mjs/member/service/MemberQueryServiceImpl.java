package nova.mjs.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.member.Member;
import nova.mjs.member.MemberDTO;
import nova.mjs.member.MemberRepository;
import nova.mjs.member.exception.MemberNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 회원 조회 서비스 구현체
 * CQRS 패턴의 Query 부분을 담당
 */
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
    public MemberDTO getMemberByEmailId(String emailId) {
        Member member = getMemberByEmail(emailId);

        return MemberDTO.fromEntity(member);
    }

    @Override
    public Page<MemberDTO> getAllMember(Pageable pageable) {
        return memberRepository.findAll(pageable).map(MemberDTO::fromEntity);
    }

    private Member getMemberByEmail(String emailId) {
        return memberRepository.findByEmail(emailId)
                .orElseThrow(MemberNotFoundException::new);
    }
}