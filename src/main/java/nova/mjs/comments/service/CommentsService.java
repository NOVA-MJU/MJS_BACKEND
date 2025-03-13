package nova.mjs.comments.service;

import jakarta.annotation.PostConstruct;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import nova.mjs.comments.DTO.CommentsResponseDto;
import nova.mjs.comments.entity.Comments;
import nova.mjs.comments.exception.CommentNotFoundException;
import nova.mjs.comments.repository.CommentsRepository;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.entity.enumList.CommunityCategory;
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



    // 1. GEt 댓글 목록 (게시글 ID 기반, 페이지네이션 제거)
    public List<CommentsResponseDto.CommentSummaryDto> getCommentsByBoard(UUID communityBoardUuid) {
        CommunityBoard board = getExistingBoard(communityBoardUuid);
        List<Comments> comments = commentsRepository.findByCommunityBoard(board);
        return comments.stream()
                .map(CommentsResponseDto.CommentSummaryDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 2. POST 댓글 작성, 로그인 연동 추가
    @Transactional
    public CommentsResponseDto.CommentSummaryDto createComment(UUID communityBoardUuid, String content, String email) {
        // 이메일을 이용하여 현재 로그인한 회원 정보 가져오기
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
        CommunityBoard communityBoard = getExistingBoard(communityBoardUuid);

        Comments comment = Comments.create(communityBoard, member,content);
        Comments savedComment = commentsRepository.save(comment);

        log.debug("댓글 작성 성공. UUID = {}, 작성자 : {}", savedComment.getUuid(), email);
        return CommentsResponseDto.CommentSummaryDto.fromEntity(savedComment);
    }

    // 3. DELETE 댓글 삭제, 로그인 연동 추가
    @Transactional
    public void deleteCommentByUuid(UUID commentUuid, String email) {
        Comments comment = getExistingCommentByUuid(commentUuid);
        // 현재 로그인한 사용자가 댓글 작성자인지 체크
        if (!comment.getMember().getEmail().equals(email)) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        commentsRepository.delete(comment);
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

    private Comments getExistingCommentByUuid(UUID commentUuid) {
        return commentsRepository.findByUuid(commentUuid)
                .orElseThrow(() -> {
                    log.warn("[MJS] 요청한 댓글을 찾을 수 없습니다. UUID = {}", commentUuid);
                    return new CommentNotFoundException();
                });
    }

}