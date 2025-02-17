package nova.mjs.comments.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.comments.DTO.CommentsResponseDto;
import nova.mjs.comments.entity.Comments;
import nova.mjs.comments.repository.CommentsRepository;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.repository.CommunityBoardRepository;
import nova.mjs.member.Member;
import nova.mjs.member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final MemberRepository memberRepository;
    private final CommunityBoardRepository communityBoardRepository;

    //댓글 추가 메서드
    public CommentsResponseDto save(Long id, CommentsResponseDto request, UUID uuid) {
        // UUID로 member 찾기
        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다.: " + uuid));

        // UUID로 Community Board 찾기
        CommunityBoard communityBoard = communityBoardRepository.findByUuid(request.getCommunityBoardUuid())
                .orElseThrow(() -> new IllegalArgumentException("댓글 작성 실패: 해당 게시물이 존재하지 않습니다. " + uuid));

        // DTO 이용해서 엔티티 생성 후 저장
        Comments comment = request.toEntity(communityBoard, member);
        Comments savedComment = commentsRepository.save(comment);

        return CommentsResponseDto.fromEntity(savedComment);
    }

    //댓글 읽어오기
    @Transactional(readOnly = true)
    public List<CommentsResponseDto> findAll(UUID uuid) {
        CommunityBoard communityBoard = communityBoardRepository.findByUuidWithComments(uuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id: " + uuid));

        // DTO 변환
        return communityBoard.getComments()
                .stream()
                .map(CommentsResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
