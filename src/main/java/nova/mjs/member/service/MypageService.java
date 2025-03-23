package nova.mjs.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.community.DTO.CommunityBoardResponse;
import nova.mjs.community.likes.repository.LikeCommunityRepository;
import nova.mjs.community.repository.CommunityBoardRepository;
import nova.mjs.member.Member;
import nova.mjs.member.MemberRepository;
import nova.mjs.member.exception.MemberNotFoundException;
import nova.mjs.comment.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class MypageService {

    private final CommunityBoardRepository communityBoardRepository;
    private final CommentRepository commentRepository;
    private final LikeCommunityRepository likeCommunityRepository;
    private final MemberRepository memberRepository;

    // 1. 내가 작성한 글 조회
    public List<CommunityBoardResponse> getMyPosts(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        return communityBoardRepository.findByAuthor(member).stream()
                .map(board -> {
                    int likeCount = likeCommunityRepository.countByCommunityBoardUuid(board.getUuid());
                    int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
                    return CommunityBoardResponse.fromEntity(board, likeCount, commentCount);
                })
                .collect(Collectors.toList());
    }

    // 2. 내가 작성한 댓글이 속한 게시물 조회
    public List<CommunityBoardResponse> getMyCommentedPosts(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        return commentRepository.findDistinctCommunityBoardByMember(member).stream()
                .map(board -> {
                    int likeCount = likeCommunityRepository.countByCommunityBoardUuid(board.getUuid());
                    int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
                    return CommunityBoardResponse.fromEntity(board, likeCount, commentCount);
                })
                .collect(Collectors.toList());
    }

    // 3. 내가 찜한 글 조회
    public List<CommunityBoardResponse> getLikedPosts(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        return likeCommunityRepository.findCommunityBoardsByMember(member).stream()
                .map(board -> {
                    int likeCount = likeCommunityRepository.countByCommunityBoardUuid(board.getUuid());
                    int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
                    return CommunityBoardResponse.fromEntity(board, likeCount, commentCount);
                })
                .collect(Collectors.toList());
    }
}
