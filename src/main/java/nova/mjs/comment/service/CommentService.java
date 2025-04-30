package nova.mjs.comment.service;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import nova.mjs.comment.DTO.CommentResponseDto;
import nova.mjs.comment.entity.Comment;
import nova.mjs.comment.exception.CommentNotFoundException;
import nova.mjs.comment.exception.CommentReplyDepthException;
import nova.mjs.comment.likes.repository.CommentLikeRepository;
import nova.mjs.comment.repository.CommentRepository;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.repository.CommunityBoardRepository;
import nova.mjs.member.Member;
import nova.mjs.member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import nova.mjs.community.exception.CommunityNotFoundException;
import nova.mjs.member.exception.MemberNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
        // 1) 게시글 존재 여부 확인
        CommunityBoard board = getExistingBoard(communityBoardUuid);

        // 2) 전체 댓글 목록 조회
        List<Comment> allComments = commentRepository.findByCommunityBoard(board);
        if (allComments.isEmpty()) {
            return List.of();
        }

        // 3) 최상위 댓글(부모가 null)만 필터링
        List<Comment> topLevelComments = allComments.stream()
                .filter(c -> c.getParent() == null)
                .toList();

        // 4) 비로그인 사용자면 -> isLiked = false (likedSet=null)
        Set<UUID> likedSet = null;
        if (email != null) {
            Member member = memberRepository.findByEmail(email).orElse(null);
            if (member != null) {
                List<UUID> allUuids = allComments.stream()
                        .map(Comment::getUuid)
                        .toList();
                List<UUID> likedUuids = commentLikeRepository.findCommentUuidsLikedByMember(member, allUuids);
                likedSet = new HashSet<>(likedUuids);
            }
        }
        // ★ 여기가 핵심
        final Set<UUID> finalLikedSet = likedSet;

        // 5) 부모 + 자식(대댓글)까지 트리 구조로 DTO 변환
        return topLevelComments.stream()
                .map(comment -> {
                    boolean isLiked = (finalLikedSet != null && finalLikedSet.contains(comment.getUuid()));
                    return CommentResponseDto.CommentSummaryDto.fromEntityWithReplies(comment, isLiked, finalLikedSet);
                })
                .toList();
    }


    // 2. POST 댓글 작성, 로그인 연동 추가
    @Transactional
    public CommentResponseDto.CommentSummaryDto createComment(UUID communityBoardUuid, String content, String email) {
        // 이메일을 이용하여 현재 로그인한 회원 정보 가져오기
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
        CommunityBoard communityBoard = getExistingBoard(communityBoardUuid);

        Comment comment = Comment.create(communityBoard, member, content);
        Comment savedComment = commentRepository.save(comment);

        log.debug("댓글 작성 성공. UUID = {}, 작성자 : {}", savedComment.getUuid(), email);
        return CommentResponseDto.CommentSummaryDto.fromEntity(savedComment);
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
        // 1) 부모 댓글 조회
        Comment parentComment = commentRepository.findByUuid(parentCommentUuid)
                .orElseThrow(CommentNotFoundException::new);

        // 2) parentComment가 이미 "자식 댓글"(= 대댓글)인지 확인
        if (parentComment.getParent() != null) {
            // 로그 남기기
            log.error("[MJS] 대댓글 생성 실패: 이미 대댓글인 댓글에는 다시 대댓글을 달 수 없습니다. parentCommentUuid={}", parentCommentUuid);

            // 커스텀 예외 던지기
            throw new CommentReplyDepthException();
        }


        // 3) 작성자 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        // 4) 대댓글 생성
        Comment reply = Comment.createReply(parentComment, member, content);

        // 5) DB 저장
        Comment savedReply = commentRepository.save(reply);

        // 6) DTO 변환 (isLiked=false 초기값)
        return CommentResponseDto.CommentSummaryDto.fromEntity(savedReply);
    }


    private Comment getExistingCommentByUuid(UUID commentUuid) {
        return commentRepository.findByUuid(commentUuid)
                .orElseThrow(() -> {
                    log.warn("[MJS] 요청한 댓글을 찾을 수 없습니다. UUID = {}", commentUuid);
                    return new CommentNotFoundException();
                });
    }

}