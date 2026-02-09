package nova.mjs.domain.thingo.community.comment.likes;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.domain.thingo.community.comment.entity.Comment;
import nova.mjs.domain.thingo.community.comment.exception.CommentNotFoundException;
import nova.mjs.domain.thingo.community.comment.likes.entity.CommentLike;
import nova.mjs.domain.thingo.community.comment.likes.repository.CommentLikeRepository;
import nova.mjs.domain.thingo.community.comment.repository.CommentRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.exception.MemberNotFoundException;
import nova.mjs.domain.thingo.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    /**
     * 댓글 좋아요 토글
     *
     * 설계 근거
     * - boardUUID를 받는 이유는 "댓글이 해당 게시글 소속인지" 검증하기 위함이다.
     * - 검증 없이 commentUUID만으로 처리하면 다른 게시글의 댓글에 대한 접근 우회가 가능해진다.
     *
     * 트랜잭션
     * - 좋아요 row insert/delete + 댓글 likeCount 증감을 같은 트랜잭션으로 묶는다.
     */
    @Transactional
    public boolean toggleLike(UUID boardUuid, UUID commentUuid, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        Comment comment = commentRepository.findByUuid(commentUuid)
                .orElseThrow(CommentNotFoundException::new);

        validateCommentBelongsToBoard(boardUuid, comment);

        Optional<CommentLike> existingLike = commentLikeRepository.findByMemberAndComment(member, comment);

        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            comment.decreaseLikeCommentCount();

            log.debug("댓글 좋아요 취소: memberEmail={}, boardUuid={}, commentUuid={}", email, boardUuid, commentUuid);
            return false;
        }

        CommentLike newLike = CommentLike.create(member, comment);
        commentLikeRepository.save(newLike);
        comment.increaseLikeCommentCount();

        log.debug("댓글 좋아요 추가: memberEmail={}, boardUuid={}, commentUuid={}", email, boardUuid, commentUuid);
        return true;
    }

    private void validateCommentBelongsToBoard(UUID boardUuid, Comment comment) {
        UUID actualBoardUuid = comment.getCommunityBoard().getUuid();
        if (!boardUuid.equals(actualBoardUuid)) {
            throw new IllegalArgumentException("댓글이 해당 게시글에 속하지 않습니다.");
        }
    }
}
