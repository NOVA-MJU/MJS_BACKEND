package nova.mjs.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.community.DTO.CommunityBoardResponse;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.likes.repository.CommunityLikeRepository;
import nova.mjs.community.repository.CommunityBoardRepository;
import nova.mjs.member.Member;
import nova.mjs.member.MemberRepository;
import nova.mjs.member.exception.MemberNotFoundException;
import nova.mjs.comment.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class MypageService {

    private final CommunityBoardRepository communityBoardRepository;
    private final CommentRepository commentRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final MemberRepository memberRepository;

    // 1. 내가 작성한 글 조회
    public List<CommunityBoardResponse> getMyPosts(String email) {
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
                    return CommunityBoardResponse.fromEntity(board, likeCount, commentCount, isLiked);
                })
                .toList();
    }

    // 2. 내가 작성한 댓글이 속한 게시물 조회
    public List<CommunityBoardResponse> getMyCommentedPosts(String email) {
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
                    return CommunityBoardResponse.fromEntity(board, likeCount, commentCount, isLiked);
                })
                .toList();
    }

    // 3. 내가 찜한 글 조회
    public List<CommunityBoardResponse> getLikedPosts(String email) {
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
                    return CommunityBoardResponse.fromEntity(board, likeCount, commentCount, true);
                })
                .toList();
    }
}