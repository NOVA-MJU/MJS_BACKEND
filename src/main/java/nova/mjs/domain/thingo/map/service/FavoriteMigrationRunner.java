package nova.mjs.domain.thingo.map.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.map.entity.FavoriteGroup;
import nova.mjs.domain.thingo.map.entity.FavoritePlace;
import nova.mjs.domain.thingo.map.entity.Pin;
import nova.mjs.domain.thingo.map.entity.PinFavorite;
import nova.mjs.domain.thingo.map.repository.FavoritePlaceRepository;
import nova.mjs.domain.thingo.map.repository.PinFavoriteRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 레거시 즐겨찾기(PinFavorite) → 그룹 모델('내 장소' FavoritePlace) 1회성 마이그레이션.
 *
 * 즐겨찾기가 그룹 모델로 통합되면서, 기존 회원의 단순 즐겨찾기가 유실되지 않도록
 * 애플리케이션 기동 시 각 PinFavorite 를 회원의 '내 장소' 그룹 멤버십으로 복사한다.
 * (그룹, 핀) 존재 여부로 중복을 방지하므로 재기동해도 안전(멱등)하다.
 *
 * 기존 map_pin_favorite 행은 삭제하지 않고 남겨둔다(안전). 이후 안정화되면 별도로 정리한다.
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class FavoriteMigrationRunner implements ApplicationRunner {

    private final PinFavoriteRepository pinFavoriteRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final FavoriteGroupProvisioner provisioner;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<PinFavorite> legacy = pinFavoriteRepository.findAll();
        if (legacy.isEmpty()) {
            return;
        }

        // 회원별로 묶어 '내 장소' 그룹 보장을 회원당 1회만 수행
        Map<Member, List<PinFavorite>> byMember = new LinkedHashMap<>();
        for (PinFavorite pf : legacy) {
            byMember.computeIfAbsent(pf.getMember(), m -> new java.util.ArrayList<>()).add(pf);
        }

        int migrated = 0;
        for (Map.Entry<Member, List<PinFavorite>> entry : byMember.entrySet()) {
            FavoriteGroup myPlaces = provisioner.ensureMyPlaces(entry.getKey());
            for (PinFavorite pf : entry.getValue()) {
                Pin pin = pf.getPin();
                if (!favoritePlaceRepository.existsByGroupAndPin(myPlaces, pin)) {
                    favoritePlaceRepository.save(FavoritePlace.of(myPlaces, pin, null));
                    migrated++;
                }
            }
        }
        log.info("[즐겨찾기 마이그레이션] 레거시 {}건 중 신규 {}건을 '내 장소' 그룹으로 이관", legacy.size(), migrated);
    }
}
