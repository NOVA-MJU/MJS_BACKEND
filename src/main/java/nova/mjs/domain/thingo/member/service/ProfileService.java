package nova.mjs.domain.thingo.member.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.domain.thingo.community.comment.entity.Comment;
import nova.mjs.domain.thingo.community.DTO.CommunityBoardResponse;
import nova.mjs.domain.thingo.community.comment.likes.repository.CommentLikeRepository;
import nova.mjs.domain.thingo.community.entity.CommunityBoard;
import nova.mjs.domain.thingo.community.likes.repository.CommunityLikeRepository;
import nova.mjs.domain.thingo.community.repository.CommunityBoardRepository;
import nova.mjs.domain.thingo.community.repository.projection.UuidCount;
import nova.mjs.domain.thingo.member.DTO.CommentWithBoardResponse;
import nova.mjs.domain.thingo.member.DTO.ProfileCountResponse;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.exception.MemberNotFoundException;
import nova.mjs.domain.thingo.member.repository.MemberRepository;
import nova.mjs.domain.thingo.community.comment.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class ProfileService {

    private final CommunityBoardRepository communityBoardRepository;
    private final CommentRepository commentRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final MemberRepository memberRepository;

// 1. 내가 작성한 글 조회 (페이지네이션)
    public Page<CommunityBoardResponse.SummaryDTO> getMyPosts(String email, int page, int size) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<CommunityBoard> boardPage = communityBoardRepository.findByAuthor(member, pageable);
        List<CommunityBoard> myBoards = boardPage.getContent();
        if (myBoards.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> boardUuids = myBoards.stream()
                .map(CommunityBoard::getUuid)
                .toList();

// 내가 좋아요한 게시글 UUID -> Map<UUID, Boolean>
        Map<UUID, Boolean> likedBoardMap = new HashMap<>();
        for (UUID id : communityLikeRepository.findCommunityUuidsLikedByMember(member, boardUuids)) {
            likedBoardMap.put(id, Boolean.TRUE);
        }

        List<CommunityBoardResponse.SummaryDTO> dtoList = myBoards.stream()
                .map(board -> {
                    int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
                    int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
                    boolean isLiked = likedBoardMap.containsKey(board.getUuid());
                    return CommunityBoardResponse.SummaryDTO.fromEntityPreview(
                            board, likeCount, commentCount, isLiked);
                })
                .toList();

// 원래 totalElements 유지
        return new PageImpl<>(dtoList, pageable, boardPage.getTotalElements());
    }

// 3. 내가 찜한 글 조회 (페이지네이션)
    public Page<CommunityBoardResponse.SummaryDTO> getLikedPosts(String email, int page, int size) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<CommunityBoard> likedBoardPage =
                communityLikeRepository.findCommunityBoardsByMember(member, pageable);
        List<CommunityBoard> likedBoards = likedBoardPage.getContent();
        if (likedBoards.isEmpty()) {
            return Page.empty(pageable);
        }

        List<CommunityBoardResponse.SummaryDTO> dtoList = likedBoards.stream()
                .map(board -> {
                    int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
                    int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
                    return CommunityBoardResponse.SummaryDTO.fromEntityPreview(
                            board, likeCount, commentCount, true);
                })
                .toList();

        return new PageImpl<>(dtoList, pageable, likedBoardPage.getTotalElements());
    }

// 4. 내 댓글 + 게시글 정보 (페이지네이션)
    public Page<CommentWithBoardResponse> getMyCommentListWithBoard(String email, int page, int size) {
        Member me = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Comment> commentPage = commentRepository.findByMember(me, pageable);
        List<Comment> myComments = commentPage.getContent();
        if (myComments.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> boardUuids = myComments.stream()
                .map(c -> c.getCommunityBoard().getUuid())
                .distinct()
                .toList();

        List<UUID> commentUuids = myComments.stream()
                .map(Comment::getUuid)
                .toList();


// 내가 좋아요한 보드/댓글 UUID -> Map<UUID, Boolean>

        Map<UUID, Boolean> likedBoardMap = new HashMap<>();
        for (UUID id : communityLikeRepository.findCommunityUuidsLikedByMember(me, boardUuids)) {
            likedBoardMap.put(id, Boolean.TRUE);
        }

        Map<UUID, Boolean> likedCommentMap = new HashMap<>();
        for (UUID id : commentLikeRepository.findCommentUuidsLikedByMember(me, commentUuids)) {
            likedCommentMap.put(id, Boolean.TRUE);
        }

// 게시글별 댓글 수 한 번에 조회 (N+1 방지)
        List<UuidCount> rows = commentRepository.countCommentsByBoardUuids(boardUuids);
        Map<UUID, Integer> boardCommentCountMap = rows.stream()
                .collect(Collectors.toMap(
                        UuidCount::getUuid,
                        u -> Math.toIntExact(u.getCnt())
                ));



        List<CommentWithBoardResponse> dtoList = myComments.stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                .map(comment -> {
                    CommunityBoard board = comment.getCommunityBoard();
                    Member author = board.getAuthor();

                    UUID boardId = board.getUuid();
                    UUID commentId = comment.getUuid();

                    int boardLikeCount = communityLikeRepository.countByCommunityBoardUuid(boardId);
                    boolean boardIsLiked = likedBoardMap.containsKey(boardId);

                    int commentLikeCount = commentLikeRepository.countByCommentUuid(commentId);
                    boolean commentIsLiked = likedCommentMap.containsKey(commentId);

                    int boardCommentCount = boardCommentCountMap.getOrDefault(boardId, 0);

                    return CommentWithBoardResponse.builder()

// 게시글

                            .boardUuid(boardId)
                            .boardTitle(board.getTitle())
                            .boardPreviewContent(board.getPreviewContent())
                            .boardCommentCount(boardCommentCount)
                            .boardPublished(board.getPublished())
                            .boardCreatedAt(board.getCreatedAt())
                            .boardLikeCount(boardLikeCount)
                            .boardIsLiked(boardIsLiked)
                            .author(author.getNickname())
// 댓글

                            .commentUuid(commentId)
                            .commentPreviewContent(comment.getPreviewContent())
                            .commentLikeCount(commentLikeCount)
                            .commentIsLiked(commentIsLiked)
                            .commentCreatedAt(comment.getCreatedAt())
                            .build();
                })
                .toList();

        return new PageImpl<>(dtoList, pageable, commentPage.getTotalElements());
    }


    public ProfileCountResponse getMyProfileSummary(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        int postCount = communityBoardRepository.countByAuthor(member);
        int commentCount = commentRepository.countByMember(member);
        int likedPostCount = communityLikeRepository.countByMember(member);

        return ProfileCountResponse.builder()
                .nickname(member.getNickname())
                .postCount(postCount)
                .commentCount(commentCount)
                .likedPostCount(likedPostCount)
                .build();
    }
}

