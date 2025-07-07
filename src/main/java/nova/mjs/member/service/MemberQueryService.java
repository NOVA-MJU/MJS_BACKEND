package nova.mjs.member.service;

import nova.mjs.member.MemberDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * 회원 조회 서비스 인터페이스
 * CQRS 패턴의 Query 부분을 담당
 */
public interface MemberQueryService {
    
    /**
     * UUID로 회원 조회
     */
    MemberDTO getMemberByUuid(UUID userUUID);
    
    /**
     * 이메일로 회원 조회
     */
    MemberDTO getMemberByEmailId(String emailId);
    
    /**
     * 모든 회원 조회 (페이지네이션)
     */
    Page<MemberDTO> getAllMember(Pageable pageable);
}