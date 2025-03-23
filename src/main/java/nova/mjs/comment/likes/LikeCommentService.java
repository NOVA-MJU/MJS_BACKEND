package nova.mjs.comment.likes;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.comment.entity.Comment;
import nova.mjs.comment.likes.entity.LikeComment;
import nova.mjs.comment.repository.CommentRepository;
import nova.mjs.comment.exception.CommentNotFoundException;
import nova.mjs.comment.likes.repository.LikeCommentRepository;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.exception.CommunityNotFoundException;
import nova.mjs.community.repository.CommunityBoardRepository;
import nova.mjs.member.Member;
import nova.mjs.member.MemberRepository;
import nova.mjs.member.exception.MemberNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class LikeCommentService {
    private final LikeCommentRepository likeCommentRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final CommunityBoardRepository communityBoardRepository;

    // 1. 좋아요 추가 및 삭제 (토글 방식)
    @Transactional
    public boolean toggleLike(UUID boardUUID, UUID commentsUUID,String emailId) {
        Member member = memberRepository.findByEmail(emailId)
                .orElseThrow(MemberNotFoundException::new);
        Comment comment = commentRepository.findByUuid(commentsUUID)
                .orElseThrow(CommentNotFoundException::new);
        CommunityBoard communityBoard = communityBoardRepository.findByUuid(boardUUID)
                .orElseThrow(CommunityNotFoundException::new);

        Optional<LikeComment> existingLike = likeCommentRepository.findByMemberAndComments(member, comment);

        if (existingLike.isPresent()) {
            likeCommentRepository.delete(existingLike.get());
            comment.decreaseLikeCommentCount();  // 좋아요 감소 메서드
            log.debug("좋아요 삭제 완료: member_emailId={}, boardUUID={} , commentUUID={}", emailId, boardUUID, commentsUUID);
            return false; // 좋아요 취소됨
        } else {
            LikeComment likeComment = new LikeComment(member, comment);
            likeCommentRepository.save(likeComment);
            comment.increaseLikeCommentCount();  // 좋아요 증가 메서드
            log.debug("좋아요 추가 완료: member_emailId={}, boardUUID={}, commentsUUID={}",emailId, boardUUID, commentsUUID);
            return true; // 좋아요 추가됨
        }
    }
}
