package nova.mjs.comment.service;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import nova.mjs.comment.DTO.CommentResponseDto;
import nova.mjs.comment.entity.Comment;
import nova.mjs.comment.exception.CommentNotFoundException;
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
        // 2) 댓글 목록 조회
        List<Comment> comments = commentRepository.findByCommunityBoard(board);

        // 댓글이 없다면 바로 빈 리스트 리턴
        if (comments.isEmpty()) {
            return List.of();
        }

        // 3) 비로그인 사용자면 -> isLiked = false로
        if (email == null) {
            return comments.stream()
                    .map(c -> CommentResponseDto.CommentSummaryDto.fromEntity(c, false))
                    .toList();
        }

        // 4) 로그인된 사용자 조회
        Member member = memberRepository.findByEmail(email)
                .orElse(null);
        // 만약 email이 있는데 회원 정보가 없으면 -> isLiked = false
        if (member == null) {
            return comments.stream()
                    .map(c -> CommentResponseDto.CommentSummaryDto.fromEntity(c, false))
                    .toList();
        }

        // 5) 댓글 UUID 목록 추출
        List<UUID> commentUuids = comments.stream()
                .map(Comment::getUuid)
                .toList();

        // 6) 사용자가 좋아요한 댓글의 UUID들을 가져오기
        List<UUID> likedUuids = commentLikeRepository.findCommentUuidsLikedByMember(member, commentUuids);

        // 7) 조회된 UUID를 Set으로 변환(contains()용)
        Set<UUID> likedSet = new java.util.HashSet<>(likedUuids);

        // 8) 각 댓글마다 isLiked 여부 매핑
        return comments.stream()
                .map(comment -> {
                    boolean isLiked = likedSet.contains(comment.getUuid());
                    return CommentResponseDto.CommentSummaryDto.fromEntity(comment, isLiked);
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
        return CommentResponseDto.CommentSummaryDto.fromEntity(savedComment, false);
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

    private Comment getExistingCommentByUuid(UUID commentUuid) {
        return commentRepository.findByUuid(commentUuid)
                .orElseThrow(() -> {
                    log.warn("[MJS] 요청한 댓글을 찾을 수 없습니다. UUID = {}", commentUuid);
                    return new CommentNotFoundException();
                });
    }

}