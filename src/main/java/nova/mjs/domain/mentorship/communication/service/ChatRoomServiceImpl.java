package nova.mjs.domain.mentorship.communication.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.communication.dto.ChatRoomDTO;
import nova.mjs.domain.mentorship.communication.entity.ChatRoom;
import nova.mjs.domain.mentorship.communication.repository.ChatMessageRepository;
import nova.mjs.domain.mentorship.communication.repository.ChatRoomRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomServiceImpl {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public UUID requestChat(Member requester, Member mentor) {
        ChatRoom room = ChatRoom.create(requester, mentor);
        chatRoomRepository.save(room);
        return room.getChatUuid();
    }

    @Transactional
    public ChatRoomDTO.CreateResponse createChatRoom(ChatRoomDTO.CreateRequest request) {
        validateCreateRequest(request);

        Member requester = getMemberByUuid(request.getRequesterUuid());
        Member responder = getMemberByUuid(request.getResponderUuid());

        validateParticipants(requester, responder);

        chatRoomRepository.findFirstByRequesterAndResponderAndStatusIn(
                requester,
                responder,
                List.of(ChatRoom.ChatStatus.WAITING, ChatRoom.ChatStatus.IN_PROGRESS)
        ).ifPresent(room -> {
            throw new IllegalStateException("이미 대기 중이거나 진행 중인 채팅방이 존재합니다. chatUuid=" + room.getChatUuid());
        });

        ChatRoom chatRoom = ChatRoom.create(requester, responder);
        chatRoomRepository.save(chatRoom);

        return ChatRoomDTO.CreateResponse.builder()
                .chatUuid(chatRoom.getChatUuid())
                .requesterUuid(requester.getUuid())
                .responderUuid(responder.getUuid())
                .status(chatRoom.getStatus())
                .build();
    }

    @Transactional
    public ChatRoomDTO.DeleteResponse deleteChatRoom(UUID chatUuid) {
        if (chatUuid == null) {
            throw new IllegalArgumentException("chatUuid는 필수입니다.");
        }

        ChatRoom room = getByChatUuid(chatUuid);

        chatMessageRepository.deleteByChatUuid(chatUuid);
        chatRoomRepository.delete(room);

        return ChatRoomDTO.DeleteResponse.builder()
                .chatUuid(chatUuid)
                .message("채팅방이 삭제되었습니다.")
                .build();
    }

    public List<ChatRoomDTO.SummaryResponse> getMyChatRooms(UUID memberUuid) {
        if (memberUuid == null) {
            throw new IllegalArgumentException("memberUuid는 필수입니다.");
        }

        Member me = getMemberByUuid(memberUuid);

        List<ChatRoom> chatRooms = chatRoomRepository.findByRequesterOrResponderOrderByCreatedAtDesc(me, me);

        return chatRooms.stream()
                .map(chatRoom -> {
                    Member partner = chatRoom.getRequester().getUuid().equals(me.getUuid())
                            ? chatRoom.getResponder()
                            : chatRoom.getRequester();

                    return ChatRoomDTO.SummaryResponse.builder()
                            .chatUuid(chatRoom.getChatUuid())
                            .myUuid(me.getUuid())
                            .partnerUuid(partner.getUuid())
                            .partnerName(partner.getName())
                            .partnerProfileImageUrl(partner.getProfileImageUrl())
                            .status(chatRoom.getStatus())
                            .createdAt(chatRoom.getCreatedAt())
                            .build();
                })
                .toList();
    }

    public ChatRoom getByChatUuid(UUID chatUuid) {
        return chatRoomRepository.findByChatUuid(chatUuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다. chatUuid=" + chatUuid));
    }

    @Transactional
    public void startChatIfWaiting(ChatRoom room) {
        if (room.getStatus() == ChatRoom.ChatStatus.WAITING) {
            room.startChat();
        }
    }

    private Member getMemberByUuid(UUID uuid) {
        return memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. uuid=" + uuid));
    }

    private void validateCreateRequest(ChatRoomDTO.CreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("채팅방 생성 요청은 필수입니다.");
        }
        if (request.getRequesterUuid() == null) {
            throw new IllegalArgumentException("requesterUuid는 필수입니다.");
        }
        if (request.getResponderUuid() == null) {
            throw new IllegalArgumentException("responderUuid는 필수입니다.");
        }
        if (request.getRequesterUuid().equals(request.getResponderUuid())) {
            throw new IllegalArgumentException("본인과의 채팅방은 생성할 수 없습니다.");
        }
    }

    private void validateParticipants(Member requester, Member responder) {
        if (responder.getRole() != Member.Role.MENTOR) {
            throw new IllegalArgumentException("응답자는 멘토여야 합니다.");
        }
    }
}