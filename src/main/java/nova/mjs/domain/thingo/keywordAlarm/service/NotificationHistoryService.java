package nova.mjs.domain.thingo.keywordAlarm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.keywordAlarm.dto.NotificationHistoryDTO;
import nova.mjs.domain.thingo.keywordAlarm.entity.NotificationHistory;
import nova.mjs.domain.thingo.keywordAlarm.exception.NotificationHistoryNotFoundException;
import nova.mjs.domain.thingo.keywordAlarm.repository.NotificationHistoryRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.exception.MemberNotFoundException;
import nova.mjs.domain.thingo.member.repository.MemberRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림함(발송 내역) 조회/읽음 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationHistoryService {

    private final NotificationHistoryRepository notificationHistoryRepository;
    private final MemberRepository memberRepository;

    /** 내 알림 내역(미읽음 최신순 -> 읽음 최신순)과 전체 미읽음 수 조회 */
    public NotificationHistoryDTO.Response.Inbox getMyNotifications(String email, Pageable pageable) {
        Member member = findMember(email);
        var notifications = notificationHistoryRepository.findInbox(member, pageable)
                .map(NotificationHistoryDTO.Response.Detail::from);
        long unreadCount = notificationHistoryRepository.countByMemberAndReadFalse(member);
        return NotificationHistoryDTO.Response.Inbox.of(notifications, unreadCount);
    }

    /** 메인 화면 알림 배지용 경량 미읽음 상태 조회 */
    public NotificationHistoryDTO.Response.UnreadStatus getUnreadStatus(String email) {
        Member member = findMember(email);
        return NotificationHistoryDTO.Response.UnreadStatus.of(
                notificationHistoryRepository.countByMemberAndReadFalse(member));
    }

    /** 단건 읽음 처리 후 갱신된 알림 반환. 이미 읽은 알림에도 멱등이다. */
    @Transactional
    public NotificationHistoryDTO.Response.Detail markAsRead(String email, Long notificationId) {
        Member member = findMember(email);
        NotificationHistory history = notificationHistoryRepository.findByIdAndMember(notificationId, member)
                .orElseThrow(NotificationHistoryNotFoundException::new);
        history.markAsRead();
        return NotificationHistoryDTO.Response.Detail.from(history);
    }

    /** 전체 읽음 처리 결과 반환 */
    @Transactional
    public NotificationHistoryDTO.Response.ReadAllResult markAllAsRead(String email) {
        Member member = findMember(email);
        return NotificationHistoryDTO.Response.ReadAllResult.of(
                notificationHistoryRepository.markAllAsRead(member));
    }

    private Member findMember(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
    }
}
