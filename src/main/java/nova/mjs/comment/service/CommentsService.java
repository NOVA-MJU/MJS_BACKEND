package nova.mjs.comment.service;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import nova.mjs.comment.DTO.CommentsResponseDto;
import nova.mjs.comment.entity.Comment;
import nova.mjs.comment.exception.CommentNotFoundException;
import nova.mjs.comment.repository.CommentsRepository;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)

public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final MemberRepository memberRepository;
    private final CommunityBoardRepository communityBoardRepository;



    // 1. GEt 댓글 목록 (게시글 ID 기반)
    public Page<CommentsResponseDto.CommentSummaryDto> getCommentsByBoard(UUID communityBoardUuid, Pageable pageable) {
        CommunityBoard board = getExistingBoard(communityBoardUuid);
        return commentsRepository.findByCommunityBoard(board, pageable)
                .map(CommentsResponseDto.CommentSummaryDto::fromEntity);
    }

    // 2. POST 댓글 작성
    @Transactional
    public CommentsResponseDto.CommentSummaryDto createComment(UUID communityBoardUuid, String content, UUID memberUuid) {
        Member member = getExistingMember(memberUuid);
        CommunityBoard communityBoard = getExistingBoard(communityBoardUuid);

        Comment comment = Comment.create(communityBoard, member,content);
        Comment savedComment = commentsRepository.save(comment);

        log.debug("댓글 작성 성공. UUID = {}", savedComment.getUuid());
        return CommentsResponseDto.CommentSummaryDto.fromEntity(savedComment);
    }

    // 3. DELETE 댓글 삭제
    @Transactional
    public void deleteCommentByUuid(UUID commentUuid) {
        Comment comment = getExistingCommentByUuid(commentUuid);
        commentsRepository.delete(comment);
        log.debug("댓글 삭제 성공. ID = {}", commentUuid);
    }

    // 4. 특정 게시글 존재 여부 확인
    private CommunityBoard getExistingBoard(UUID uuid) {
        return communityBoardRepository.findByUuid(uuid)
                .orElseThrow(CommunityNotFoundException::new);
    }

    // 5. 특정 회원 존재 여부 확인
    private Member getExistingMember(UUID uuid) {
        return memberRepository.findByUuid(uuid)
                .orElseThrow(MemberNotFoundException::new);
    }

    private Comment getExistingCommentByUuid(UUID commentUuid) {
        return commentsRepository.findByUuid(commentUuid)
                .orElseThrow(() -> {
                    log.warn("[MJS] 요청한 댓글을 찾을 수 없습니다. ID = {}", commentUuid);
                    return new CommentNotFoundException(commentUuid);
                });
    }

}