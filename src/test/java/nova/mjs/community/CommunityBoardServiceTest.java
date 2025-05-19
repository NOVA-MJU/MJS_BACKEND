package nova.mjs.community;

import nova.mjs.comment.repository.CommentRepository;
import nova.mjs.community.DTO.CommunityBoardRequest;
import nova.mjs.community.DTO.CommunityBoardResponse;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.entity.enumList.CommunityCategory;
import nova.mjs.community.exception.CommunityNotFoundException;
import nova.mjs.community.likes.repository.CommunityLikeRepository;
import nova.mjs.community.repository.CommunityBoardRepository;
import nova.mjs.community.service.CommunityBoardService;
import nova.mjs.member.Member;
import nova.mjs.member.MemberRepository;
import nova.mjs.member.exception.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CommunityBoardServiceTest {

    @InjectMocks
    private CommunityBoardService communityBoardService;

    @Mock
    private CommunityBoardRepository communityBoardRepository;

    @Mock
    private CommunityLikeRepository communityLikeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CommentRepository commentRepository;

    private Member member;
    private CommunityBoard board;

    @BeforeEach
    void setup() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("Tester")
                .build();

        board = CommunityBoard.create(
                "title", "content", CommunityCategory.FREE,
                true, List.of("img1.jpg", "img2.jpg"), member
        );
    }

    @Test
    @DisplayName("게시글 목록 조회 - 로그인 사용자")
    void testGetBoardsWithLogin() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CommunityBoard> boardPage = new PageImpl<>(List.of(board));

        when(communityBoardRepository.findAll(pageable)).thenReturn(boardPage);
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(member));
        when(communityLikeRepository.countByCommunityBoardUuid(any())).thenReturn(5);
        when(commentRepository.countByCommunityBoardUuid(any())).thenReturn(3);
        when(communityLikeRepository.findCommunityUuidsLikedByMember(any(), anyList()))
                .thenReturn(List.of(board.getUuid()));

        Page<CommunityBoardResponse> result = communityBoardService.getBoards(pageable, "test@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).isLiked()).isTrue();
    }

    @Test
    @DisplayName("게시글 상세 조회 - 비로그인")
    void testGetBoardDetailWithoutLogin() {
        when(communityBoardRepository.findByUuid(board.getUuid())).thenReturn(Optional.of(board));
        when(communityLikeRepository.countByCommunityBoardUuid(board.getUuid())).thenReturn(2);
        when(commentRepository.countByCommunityBoardUuid(board.getUuid())).thenReturn(1);

        CommunityBoardResponse response = communityBoardService.getBoardDetail(board.getUuid(), null);

        assertThat(response.getTitle()).isEqualTo("title");
        assertThat(response.isLiked()).isFalse();
    }

    @Test
    @DisplayName("게시글 등록")
    void testCreateBoard() {
        CommunityBoardRequest request = CommunityBoardRequest.builder()
                .title("New Post")
                .content("Post content")
                .published(true)
                .contentImages(List.of("img1.png"))
                .build();

        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(member));
        when(communityBoardRepository.save(any())).thenReturn(board);
        when(communityLikeRepository.countByCommunityBoardUuid(any())).thenReturn(0);
        when(commentRepository.countByCommunityBoardUuid(any())).thenReturn(0);

        CommunityBoardResponse response = communityBoardService.createBoard(request, "test@example.com");

        assertThat(response.getTitle()).isEqualTo("New Post");
        assertThat(response.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("게시글 수정")
    void testUpdateBoard() {
        CommunityBoardRequest request = CommunityBoardRequest.builder()
                .title("Updated Title")
                .content("Updated Content")
                .published(true)
                .contentImages(List.of("imgX.jpg"))
                .build();

        when(communityBoardRepository.findByUuid(board.getUuid())).thenReturn(Optional.of(board));
        when(communityLikeRepository.countByCommunityBoardUuid(any())).thenReturn(2);
        when(commentRepository.countByCommunityBoardUuid(any())).thenReturn(1);
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(member));
        when(communityLikeRepository.findByMemberAndCommunityBoard(member, board)).thenReturn(Optional.empty());

        CommunityBoardResponse response = communityBoardService.updateBoard(board.getUuid(), request, "test@example.com");

        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.isLiked()).isFalse();
    }

    @Test
    @DisplayName("게시글 삭제 - 작성자 본인")
    void testDeleteBoard() {
        // 작성자가 본인인 board 객체 생성
        CommunityBoard authoredBoard = CommunityBoard.builder()
                .title("title")
                .content("content")
                .published(true)
                .category(CommunityCategory.FREE)
                .contentImages(List.of("img1.jpg"))
                .author(member)  // 작성자 지정
                .build();

        when(communityBoardRepository.findByUuid(authoredBoard.getUuid())).thenReturn(Optional.of(authoredBoard));
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(member));
        doNothing().when(communityBoardRepository).delete(authoredBoard);

        communityBoardService.deleteBoard(authoredBoard.getUuid(), "test@example.com");

        verify(communityBoardRepository, times(1)).delete(authoredBoard);
    }

    @Test
    @DisplayName("게시글 삭제 - 본인 아님")
    void testDeleteBoardNotAuthor() {
        Member another = Member.builder().email("other@example.com").build();

        CommunityBoard authoredByAnother = CommunityBoard.builder()
                .title("title")
                .content("content")
                .published(true)
                .category(CommunityCategory.FREE)
                .contentImages(List.of("img1.jpg"))
                .author(another)  // 다른 작성자
                .build();

        when(communityBoardRepository.findByUuid(authoredByAnother.getUuid())).thenReturn(Optional.of(authoredByAnother));
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(member));

        assertThatThrownBy(() ->
                communityBoardService.deleteBoard(authoredByAnother.getUuid(), "test@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인이 작성한 게시글만 삭제할 수 있습니다.");
    }


    @Test
    @DisplayName("게시글 상세 조회 - 존재하지 않는 게시글")
    void testGetBoardDetailNotFound() {
        UUID fakeId = UUID.randomUUID();
        when(communityBoardRepository.findByUuid(fakeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityBoardService.getBoardDetail(fakeId, null))
                .isInstanceOf(CommunityNotFoundException.class);
    }

    @Test
    @DisplayName("게시글 작성 - 존재하지 않는 유저")
    void testCreateBoardFailMemberNotFound() {
        when(memberRepository.findByEmail("none@example.com")).thenReturn(Optional.empty());

        CommunityBoardRequest request = CommunityBoardRequest.builder()
                .title("fail")
                .content("fail")
                .published(false)
                .contentImages(List.of())
                .build();

        assertThatThrownBy(() ->
                communityBoardService.createBoard(request, "none@example.com"))
                .isInstanceOf(MemberNotFoundException.class);
    }
}

