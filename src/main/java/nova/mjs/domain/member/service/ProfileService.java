package nova.mjs.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.domain.community.comment.entity.Comment;
import nova.mjs.domain.community.DTO.CommunityBoardResponse;
import nova.mjs.domain.community.comment.likes.repository.CommentLikeRepository;
import nova.mjs.domain.community.entity.CommunityBoard;
import nova.mjs.domain.community.likes.repository.CommunityLikeRepository;
import nova.mjs.domain.community.repository.CommunityBoardRepository;
import nova.mjs.domain.community.repository.projection.UuidCount;
import nova.mjs.domain.member.DTO.CommentWithBoardResponse;
import nova.mjs.domain.member.DTO.ProfileCountResponse;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.exception.MemberNotFoundException;
import nova.mjs.domain.member.repository.MemberRepository;
import nova.mjs.domain.community.comment.repository.CommentRepository;
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

    // 1. 내가 작성한 글 조회 (Set -> Map 대체)
    public List<CommunityBoardResponse.SummaryDTO> getMyPosts(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        List<CommunityBoard> myBoards = communityBoardRepository.findByAuthor(member);
        if (myBoards.isEmpty()) return List.of();

        List<UUID> boardUuids = myBoards.stream()
                .map(CommunityBoard::getUuid)
                .toList();

        // 내가 좋아요한 게시글 UUID -> Map<UUID, Boolean>
        Map<UUID, Boolean> likedBoardMap = new HashMap<>();
        for (UUID id : communityLikeRepository.findCommunityUuidsLikedByMember(member, boardUuids)) {
            likedBoardMap.put(id, Boolean.TRUE);
        }

        return myBoards.stream()
                .map(board -> {
                    int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
                    int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
                    boolean isLiked = likedBoardMap.containsKey(board.getUuid());
                    return CommunityBoardResponse.SummaryDTO.fromEntityPreview(board, likeCount, commentCount, isLiked);
                })
                .toList();
    }

    // 3. 내가 찜한 글 조회 (기존 로직 그대로, Set 미사용)
    public List<CommunityBoardResponse.SummaryDTO> getLikedPosts(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        List<CommunityBoard> likedBoards = communityLikeRepository.findCommunityBoardsByMember(member);
        if (likedBoards.isEmpty()) return List.of();

        return likedBoards.stream()
                .map(board -> {
                    int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
                    int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
                    return CommunityBoardResponse.SummaryDTO.fromEntityPreview(board, likeCount, commentCount, true);
                })
                .toList();
    }

    // 4. 내 댓글 + 게시글 정보 (Set -> Map 대체)
    public List<CommentWithBoardResponse> getMyCommentListWithBoard(String email) {
        Member me = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        List<Comment> myComments = commentRepository.findByMember(me);
        if (myComments.isEmpty()) return List.of();

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

        // ✅ 게시글별 댓글 수 한 번에 조회해서 맵으로 (N+1 방지)
        List<UuidCount> rows = commentRepository.countCommentsByBoardUuids(boardUuids);
        Map<UUID, Integer> boardCommentCountMap = rows.stream()
                .collect(Collectors.toMap(
                        UuidCount::getUuid,
                        u -> Math.toIntExact(u.getCnt())
                ));

        return myComments.stream()
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

                    // ✅ 정확한 댓글 수 넣기
                    int boardCommentCount = boardCommentCountMap.getOrDefault(boardId, 0);

                    return CommentWithBoardResponse.builder()
                            // 게시글
                            .boardUuid(boardId)
                            .boardTitle(board.getTitle())
                            .boardPreviewContent(board.getPreviewContent())
                            .boardCommentCount(boardCommentCount)   // ✅ 여기!
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
