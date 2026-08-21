package nova.mjs.domain.thingo.keywordAlarm.repository;

import nova.mjs.domain.thingo.keywordAlarm.entity.DeviceToken;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    List<DeviceToken> findByMember(Member member);

    /** 이벤트 리스너가 엔티티를 다시 로딩하지 않고 회원의 전체 기기 토큰을 찾는다. */
    List<DeviceToken> findByMember_Id(Long memberId);

    void deleteByFcmToken(String fcmToken);
}
