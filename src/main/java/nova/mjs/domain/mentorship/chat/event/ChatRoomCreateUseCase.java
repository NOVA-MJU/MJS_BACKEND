package nova.mjs.domain.mentorship.chat.event;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.repository.MemberRepository;
import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomCreateUseCase {

    private final ChatRoomService chatRoomService;
    private final MemberRepository memberRepository;

    public ChatRoomCreatedEventDto createRoom(ChatRoomCreateRequestDto req, Authentication authentication) {

        UUID menteeUuid = requireMenteeUuid(authentication);
        UUID mentorUuid = requireMentorUuid(req);

        if (mentorUuid.equals(menteeUuid)) {
            throw new BusinessBaseException(ErrorCode.INVALID_REQUEST);
        }

        Long menteeId = memberRepository.findIdByUuid(menteeUuid)
                .orElseThrow(() -> new BusinessBaseException(ErrorCode.MEMBER_NOT_FOUND));

        Member mentor = memberRepository.findByUuid(mentorUuid)
                .orElseThrow(() -> new BusinessBaseException(ErrorCode.MENTOR_NOT_FOUND));

        if (mentor.getRole() != Member.Role.MENTOR) {
            throw new BusinessBaseException(ErrorCode.MENTOR_NOT_FOUND);
        }

        Long mentorId = mentor.getId();
        if (mentorId == null) {
            throw new BusinessBaseException(ErrorCode.MENTOR_NOT_FOUND);
        }

        ChatRoomDocument room = chatRoomService.createOrGetRoom(menteeId, mentorId);

        return ChatRoomCreatedEventDto.builder()
                .roomId(room.getId())
                .menteeUuid(menteeUuid)
                .mentorUuid(mentorUuid)
                .build();
    }

    private UUID requireMenteeUuid(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessBaseException(ErrorCode.TOKEN_NOT_PROVIDED);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            throw new BusinessBaseException(ErrorCode.TOKEN_INVALID);
        }

        UUID uuid = userPrincipal.getUuid();
        if (uuid == null) {
            throw new BusinessBaseException(ErrorCode.TOKEN_INVALID);
        }
        return uuid;
    }

    private UUID requireMentorUuid(ChatRoomCreateRequestDto req) {
        if (req == null || req.getMentorUuid() == null) {
            throw new BusinessBaseException(ErrorCode.INVALID_REQUEST);
        }
        return req.getMentorUuid();
    }
}
