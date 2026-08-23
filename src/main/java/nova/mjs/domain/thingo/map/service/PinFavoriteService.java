package nova.mjs.domain.thingo.map.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 핀(건물/장소) 즐겨찾기 토글 서비스 (레거시 별 토글 호환).
 *
 * 즐겨찾기가 그룹 모델로 통합되면서, 단순 별 토글은 '내 장소' 시스템 그룹의
 * 편입/해제로 동작한다. 실제 로직은 {@link FavoritePlaceService#toggleMyPlaces} 에 위임한다.
 *
 * 여러 그룹 선택·메모가 필요한 최신 플로우는 '그룹 선택 바텀시트'
 * (GET/PUT /api/v1/map/favorites/pins/{pinId}) 를 사용한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PinFavoriteService {

    private final FavoritePlaceService favoritePlaceService;

    /**
     * 특정 핀의 즐겨찾기를 토글한다 ('내 장소' 그룹 기준).
     *
     * @param email 현재 로그인 회원 이메일
     * @param pinId 건물/장소 핀 ID
     * @return true: 즐겨찾기 추가됨, false: 즐겨찾기 해제됨
     */
    @Transactional
    public boolean toggleFavorite(String email, Long pinId) {
        boolean added = favoritePlaceService.toggleMyPlaces(email, pinId);
        log.debug("핀 즐겨찾기 토글(내 장소) - email={}, pinId={}, added={}", email, pinId, added);
        return added;
    }
}
