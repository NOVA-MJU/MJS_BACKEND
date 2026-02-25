package nova.mjs.domain.thingo.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.domain.thingo.community.DTO.CommunityBoardResponse;
import nova.mjs.domain.thingo.community.comment.entity.Comment;
import nova.mjs.domain.thingo.community.comment.likes.repository.CommentLikeRepository;
import nova.mjs.domain.thingo.community.comment.repository.CommentRepository;
import nova.mjs.domain.thingo.community.entity.CommunityBoard;
import nova.mjs.domain.thingo.community.likes.repository.CommunityLikeRepository;
import nova.mjs.domain.thingo.community.repository.CommunityBoardRepository;
import nova.mjs.domain.thingo.member.DTO.CommentWithBoardResponse;
import nova.mjs.domain.thingo.member.DTO.ProfileCountResponse;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.exception.MemberNotFoundException;
import nova.mjs.domain.thingo.member.repository.MemberRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    public Page<CommunityBoardResponse.SummaryDTO> getMyPosts(String email, int page, int size) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<CommunityBoard> boardPage = communityBoardRepository.findByAuthor(member, pageable);
        List<CommunityBoard> boards = boardPage.getContent();
        if (boards.isEmpty()) {
            return Page.empty(pageable);
        }

        Set<UUID> likedUuids = findLikedBoardUuids(member, boards);

        List<CommunityBoardResponse.SummaryDTO> dtoList = boards.stream()
                .map(board -> CommunityBoardResponse.SummaryDTO.fromEntityPreview(
                        board,
                        board.getLikeCount(),
                        board.getCommentCount(),
                        likedUuids.contains(board.getUuid())
                ))
                .toList();

        return new PageImpl<>(dtoList, pageable, boardPage.getTotalElements());
    }

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
                .map(board -> CommunityBoardResponse.SummaryDTO.fromEntityPreview(
                        board,
                        board.getLikeCount(),
                        board.getCommentCount(),
                        true
                ))
                .toList();

        return new PageImpl<>(dtoList, pageable, likedBoardPage.getTotalElements());
    }

    public Page<CommentWithBoardResponse> getMyCommentListWithBoard(String email, int page, int size) {
        Member me = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Comment> commentPage = commentRepository.findByMember(me, pageable);
        List<Comment> comments = commentPage.getContent();
        if (comments.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> boardUuids = comments.stream()
                .map(c -> c.getCommunityBoard().getUuid())
                .distinct()
                .toList();

        List<UUID> commentUuids = comments.stream()
                .map(Comment::getUuid)
                .toList();

        Set<UUID> likedBoardUuids = new HashSet<>(communityLikeRepository.findCommunityUuidsLikedByMember(me, boardUuids));
        Set<UUID> likedCommentUuids = commentLikeRepository.findByMemberAndComment_UuidIn(me, commentUuids).stream()
                .map(commentLike -> commentLike.getComment().getUuid())
                .collect(java.util.stream.Collectors.toSet());

        List<CommentWithBoardResponse> dtoList = comments.stream()
                .map(comment -> {
                    CommunityBoard board = comment.getCommunityBoard();

                    UUID boardUuid = board.getUuid();
                    UUID commentUuid = comment.getUuid();

                    return CommentWithBoardResponse.builder()
                            .boardUuid(boardUuid)
                            .boardTitle(board.getTitle())
                            .boardPreviewContent(board.getPreviewContent())
                            .boardCommentCount(board.getCommentCount())
                            .boardPublished(board.getPublished())
                            .boardCreatedAt(board.getCreatedAt())
                            .boardLikeCount(board.getLikeCount())
                            .boardIsLiked(likedBoardUuids.contains(boardUuid))
                            .author(board.getAuthor().getNickname())
                            .commentUuid(commentUuid)
                            .commentPreviewContent(comment.getPreviewContent())
                            .commentLikeCount(comment.getLikeCount())
                            .commentIsLiked(likedCommentUuids.contains(commentUuid))
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

    private Set<UUID> findLikedBoardUuids(Member member, List<CommunityBoard> boards) {
        List<UUID> boardUuids = boards.stream()
                .map(CommunityBoard::getUuid)
                .toList();

        if (boardUuids.isEmpty()) {
            return Collections.emptySet();
        }

        return new HashSet<>(communityLikeRepository.findCommunityUuidsLikedByMember(member, boardUuids));
    }
}
