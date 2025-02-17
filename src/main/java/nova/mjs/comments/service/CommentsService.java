package nova.mjs.comments.service;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import nova.mjs.comments.DTO.CommentsResponseDto;
import nova.mjs.comments.entity.Comments;
import nova.mjs.comments.exception.CommentNotFoundException;
import nova.mjs.comments.repository.CommentsRepository;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.repository.CommunityBoardRepository;
import nova.mjs.member.Member;
import nova.mjs.member.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import nova.mjs.community.exception.CommunityNotFoundException;
import nova.mjs.member.exception.MemberNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)

public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final MemberRepository memberRepository;
    private final CommunityBoardRepository communityBoardRepository;

    // 1. GEt 댓글 목록 (게시글 ID 기반)
    public Page<CommentsResponseDto> getCommentsByBoard(UUID communityBoardUuid, Pageable pageable) {
        CommunityBoard board = getExistingBoard(communityBoardUuid);
        return commentsRepository.findByCommunityBoard(board, pageable)
                .map(CommentsResponseDto::fromEntity);
    }


    // 2. GET 단일 댓글 조회
    public CommentsResponseDto getCommentByUuid(UUID commentUuid) {
        Comments comment = getExistingCommentByUuid(commentUuid);
        return CommentsResponseDto.fromEntity(comment);
    }

    // 3. POST 댓글 작성
    @Transactional
    public CommentsResponseDto createComment(UUID communityBoardUuid, CommentsResponseDto request, UUID memberUuid) {
        Member member = getExistingMember(memberUuid);
        CommunityBoard communityBoard = getExistingBoard(communityBoardUuid);

        Comments comment = request.toEntity(communityBoard, member);
        Comments savedComment = commentsRepository.save(comment);

        log.debug("댓글 작성 성공. UUID = {}", savedComment.getUuid());
        return CommentsResponseDto.fromEntity(savedComment);
    }

    // 4. DELETE 댓글 삭제
    @Transactional
    public void deleteCommentByUuid(UUID commentUuid) {
        Comments comment = getExistingCommentByUuid(commentUuid);
        commentsRepository.delete(comment);
        log.debug("댓글 삭제 성공. ID = {}", commentUuid);
    }

    // 5. 특정 게시글 존재 여부 확인
    private CommunityBoard getExistingBoard(UUID uuid) {
        return communityBoardRepository.findByUuid(uuid)
                .orElseThrow(CommunityNotFoundException::new);
    }

    // 6. 특정 회원 존재 여부 확인
    private Member getExistingMember(UUID uuid) {
        return memberRepository.findByUuid(uuid)
                .orElseThrow(MemberNotFoundException::new);
    }

    // 7. 특정 댓글 존재 여부 확인
    private Comments getExistingCommentByUuid(UUID commentUuid) {
        return commentsRepository.findByUuid(commentUuid)
                .orElseThrow(() -> new CommentNotFoundException(commentUuid));
    }
}