package nova.mjs.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.domain.community.comment.entity.Comment;
import nova.mjs.domain.community.DTO.CommunityBoardResponse;
import nova.mjs.domain.community.entity.CommunityBoard;
import nova.mjs.domain.community.likes.repository.CommunityLikeRepository;
import nova.mjs.domain.community.repository.CommunityBoardRepository;
import nova.mjs.domain.member.DTO.CommentWithBoardResponse;
import nova.mjs.domain.member.DTO.ProfileCountResponse;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.repository.MemberRepository;
import nova.mjs.domain.member.exception.MemberNotFoundException;
import nova.mjs.domain.community.comment.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class ProfileService {

    private final CommunityBoardRepository communityBoardRepository;
    private final CommentRepository commentRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final MemberRepository memberRepository;

    // 1. 내가 작성한 글 조회
    public List<CommunityBoardResponse.SummaryDTO> getMyPosts(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        // 1) 내가 작성한 게시글 목록
        List<CommunityBoard> myBoards = communityBoardRepository.findByAuthor(member);
        if (myBoards.isEmpty()) {
            return List.of();
        }

        // 2) 모든 게시글 UUID 추출
        List<UUID> boardUuids = myBoards.stream()
                .map(CommunityBoard::getUuid)
                .toList();

        // 3) 내가 좋아요한 게시글 UUID 조회
        List<UUID> likedUuids = communityLikeRepository.findCommunityUuidsLikedByMember(member, boardUuids);
        Set<UUID> likedSet = new HashSet<>(likedUuids);

        // 4) 각 게시글에 대해 likeCount, commentCount, isLiked를 매핑
        return myBoards.stream()
                .map(board -> {
                    int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
                    int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
                    boolean isLiked = likedSet.contains(board.getUuid());
                    return CommunityBoardResponse.SummaryDTO.fromEntityPreview(board, likeCount, commentCount, isLiked);
                })
                .toList();
    }
    /*
    // 2. 내가 작성한 댓글이 속한 게시물 조회
    public List<CommunityBoardResponse.SummaryDTO> getMyCommentedPosts(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        // 1) 내가 댓글 단 게시글 목록 (중복 제거)
        List<CommunityBoard> boards = commentRepository.findDistinctCommunityBoardByMember(member);
        if (boards.isEmpty()) {
            return List.of();
        }

        // 2) 게시글 UUID 추출
        List<UUID> boardUuids = boards.stream()
                .map(CommunityBoard::getUuid)
                .toList();

        // 3) 내가 좋아요한 게시글 UUID 조회
        List<UUID> likedUuids = communityLikeRepository.findCommunityUuidsLikedByMember(member, boardUuids);
        Set<UUID> likedSet = new HashSet<>(likedUuids);

        // 4) 각 게시글에 대해 likeCount, commentCount, isLiked 매핑
        return boards.stream()
                .map(board -> {
                    int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
                    int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
                    boolean isLiked = likedSet.contains(board.getUuid());
                    return CommunityBoardResponse.SummaryDTO.fromEntityPreview(board, likeCount, commentCount, isLiked);
                })
                .toList();
    }*/

    // 3. 내가 찜한 글 조회
    public List<CommunityBoardResponse.SummaryDTO> getLikedPosts(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        // 1) 내가 좋아요한 게시글 목록
        List<CommunityBoard> likedBoards = communityLikeRepository.findCommunityBoardsByMember(member);
        if (likedBoards.isEmpty()) {
            return List.of();
        }

        // 2) 각 게시글에 대해 likeCount, commentCount 조회
        //    여기서 'isLiked = true' (이미 좋아요한 글들이므로)
        return likedBoards.stream()
                .map(board -> {
                    int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
                    int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
                    return CommunityBoardResponse.SummaryDTO.fromEntityPreview(board, likeCount, commentCount, true);
                })
                .toList();
    }

    public List<CommentWithBoardResponse> getMyCommentListWithBoard(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        List<Comment> myComments = commentRepository.findByMember(member);
        if (myComments.isEmpty()) return List.of();

        return myComments.stream()
                .map(comment -> {
                    CommunityBoard board = comment.getCommunityBoard();
                    return CommentWithBoardResponse.builder()
                            .boardUuid(board.getUuid())
                            .boardTitle(board.getTitle())
                            .boardPreviewContent(board.getPreviewContent())
                            .commentUuid(comment.getUuid())
                            .commentPreviewContent(comment.getPreviewContent())
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
                .nickname(member.getNickname()) // 닉네임 포함
                .postCount(postCount)
                .commentCount(commentCount)
                .likedPostCount(likedPostCount)
                .build();
    }
}