package nova.mjs.domain.community.comment.service;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import nova.mjs.domain.community.comment.DTO.CommentResponseDto;
import nova.mjs.domain.community.comment.entity.Comment;
import nova.mjs.domain.community.comment.exception.CommentNotFoundException;
import nova.mjs.domain.community.comment.exception.CommentReplyDepthException;
import nova.mjs.domain.community.comment.likes.repository.CommentLikeRepository;
import nova.mjs.domain.community.comment.repository.CommentRepository;
import nova.mjs.domain.community.entity.CommunityBoard;
import nova.mjs.domain.community.repository.CommunityBoardRepository;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import nova.mjs.domain.community.exception.CommunityNotFoundException;
import nova.mjs.domain.member.exception.MemberNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)

public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final CommunityBoardRepository communityBoardRepository;
    private final CommentLikeRepository commentLikeRepository;


    // 1. GEt 댓글 목록 (게시글 ID 기반, 페이지네이션 제거)
    public List<CommentResponseDto.CommentSummaryDto> getCommentsByBoard(UUID communityBoardUuid, String email) {
        CommunityBoard board = getExistingBoard(communityBoardUuid);

        List<Comment> allComments = commentRepository.findByCommunityBoard(board);
        if (allComments.isEmpty()) return List.of();

        List<Comment> topLevelComments = allComments.stream()
                .filter(c -> c.getParent() == null)
                .toList();

        // 로그인 사용자
        Member me = null;
        Set<UUID> likedSet = null;
        if (email != null) {
            me = memberRepository.findByEmail(email).orElse(null);
            if (me != null) {
                List<UUID> allUuids = allComments.stream().map(Comment::getUuid).toList();
                List<UUID> likedUuids = commentLikeRepository.findCommentUuidsLikedByMember(me, allUuids);
                likedSet = new HashSet<>(likedUuids);
            }
        }
        final Set<UUID> finalLikedSet = likedSet;
        final Member finalMe = me;

        return topLevelComments.stream()
                .map(comment -> {
                    boolean isLiked = (finalLikedSet != null && finalLikedSet.contains(comment.getUuid()));
                    // ✅ me 전달해서 부모/자식 모두 isAuthor 채움
                    return CommentResponseDto.CommentSummaryDto.fromEntityWithReplies(
                            comment, isLiked, finalLikedSet, finalMe
                    );
                })
                .toList();
    }

    // 2. POST 댓글 작성, 로그인 연동 추가
    @Transactional
    public CommentResponseDto.CommentSummaryDto createComment(UUID communityBoardUuid, String content, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
        CommunityBoard communityBoard = getExistingBoard(communityBoardUuid);

        Comment comment = Comment.create(communityBoard, member, content);
        Comment savedComment = commentRepository.save(comment);

        log.debug("댓글 작성 성공. UUID = {}, 작성자 : {}", savedComment.getUuid(), email);
        return CommentResponseDto.CommentSummaryDto.fromEntity(savedComment, /*isAuthor=*/true);
    }


    // 3. DELETE 댓글 삭제, 로그인 연동 추가
    @Transactional
    public void deleteCommentByUuid(UUID commentUuid, String email) {
        Comment comment = getExistingCommentByUuid(commentUuid);
        // 현재 로그인한 사용자가 댓글 작성자인지 체크
        if (!comment.getMember().getEmail().equals(email)) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
        log.debug("댓글 삭제 성공. ID = {}, 작성자: {}", commentUuid, email);
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

    // 6. 대댓글 작성
    @Transactional
    public CommentResponseDto.CommentSummaryDto createReply(UUID parentCommentUuid, String content, String email) {
        Comment parentComment = commentRepository.findByUuid(parentCommentUuid)
                .orElseThrow(CommentNotFoundException::new);

        if (parentComment.getParent() != null) {
            log.error("[MJS] 대댓글 생성 실패: 이미 대댓글인 댓글에는 다시 대댓글을 달 수 없습니다. parentCommentUuid={}", parentCommentUuid);
            throw new CommentReplyDepthException();
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        Comment reply = Comment.createReply(parentComment, member, content);
        Comment savedReply = commentRepository.save(reply);

        return CommentResponseDto.CommentSummaryDto.fromEntity(savedReply, /*isAuthor=*/true);
    }


    private Comment getExistingCommentByUuid(UUID commentUuid) {
        return commentRepository.findByUuid(commentUuid)
                .orElseThrow(() -> {
                    log.warn("[MJS] 요청한 댓글을 찾을 수 없습니다. UUID = {}", commentUuid);
                    return new CommentNotFoundException();
                });
    }

}