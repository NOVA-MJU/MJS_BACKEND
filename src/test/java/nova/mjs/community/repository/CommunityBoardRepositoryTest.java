package nova.mjs.community.repository;

import nova.mjs.comment.entity.Comment;
import nova.mjs.comment.repository.CommentRepository;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.entity.enumList.CommunityCategory;
import nova.mjs.member.Member;
import nova.mjs.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommunityBoardRepositoryTest {

    @Autowired
    private CommunityBoardRepository communityBoardRepository;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("UUID로 게시글 조회")
    void testFindByUuid() {
        Member member = createAndSaveMember("user1@example.com");
        CommunityBoard board = createAndSaveBoard("Test Title", member);

        Optional<CommunityBoard> result = communityBoardRepository.findByUuid(board.getUuid());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test Title");
    }

    @Test
    @DisplayName("작성자 기준 게시글 목록 조회")
    void testFindByAuthor() {
        Member member = createAndSaveMember("user2@example.com");
        createAndSaveBoard("Title 1", member);
        createAndSaveBoard("Title 2", member);

        List<CommunityBoard> boards = communityBoardRepository.findByAuthor(member);

        assertThat(boards).hasSize(2);
        assertThat(boards).allMatch(b -> b.getAuthor().getEmail().equals("user2@example.com"));
    }

    /*@Test
    @DisplayName("UUID로 댓글 포함한 게시글 조회 (Fetch Join)")
    void testFindByUuidWithComment() {
        Member member = createAndSaveMember("user3@example.com");
        CommunityBoard board = createAndSaveBoard("With Comment", member);

        // 댓글 생성
        Comment comment = Comment.builder()
                .uuid(UUID.randomUUID())
                .communityBoard(board)
                .member(member)
                .content("댓글 내용")
                .likeCount(0)
                .build();

        // null 방지 수동 초기화
        ReflectionTestUtils.setField(board, "comment", new ArrayList<>());
        board.getComment().add(comment);

        // 저장
        commentRepository.save(comment);
        communityBoardRepository.save(board);

        // Fetch Join 테스트
        Optional<CommunityBoard> result = communityBoardRepository.findByUuidWithComment(board.getUuid());

        assertThat(result).isPresent();
        assertThat(result.get().getComment()).hasSize(1);
        assertThat(result.get().getComment().get(0).getContent()).isEqualTo("댓글 내용");
    }*/



    //  테스트 전용 헬퍼 메서드
    private Member createAndSaveMember(String email) {
        Member member = Member.builder()
                .uuid(UUID.randomUUID())
                .email(email)
                .name("Tester")
                .nickname("tester123")
                .password("test1234!")        // ✅ Not Null 필드
                .role(Member.Role.valueOf("USER"))                 // ✅ 필요한 필드 설정
                .studentNumber(20230001)
                .department("컴퓨터공학과")
                .gender(Member.Gender.valueOf("MALE"))
                .build();
        return memberRepository.save(member);
    }

    private CommunityBoard createAndSaveBoard(String title, Member author) {
        CommunityBoard board = CommunityBoard.builder()
                .uuid(UUID.randomUUID())
                .title(title)
                .content("내용입니다")
                .category(CommunityCategory.FREE)
                .published(true)
                .author(author)
                .contentImages(List.of("img1.jpg"))
                .build();
        return communityBoardRepository.save(board);
    }
}
