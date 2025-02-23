package nova.mjs.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.comments.entity.Comments;
import nova.mjs.comments.repository.CommentsRepository;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.repository.CommunityBoardRepository;
import nova.mjs.member.Member;
import nova.mjs.member.MemberRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
@RequiredArgsConstructor
@Log4j2
public class DataInitializer {
    private final MemberRepository memberRepository;
    private final CommunityBoardRepository communityBoardRepository;
    private final CommentsRepository commentsRepository;

    /*@PostConstruct
    public void initData() {
        log.info("초기 더미 데이터 생성 시작...");
        Member member = memberRepository.findByEmail("test@example.com").orElse(null);

        // Member Dummy Data
        if (memberRepository.count() == 0) { // 데이터가 없다면
            member = Member.builder()
                    .uuid(UUID.randomUUID())
                    .name("TEST현빈")
                    .email("test@example.com")
                    .password("hyunbin1234")
                    .nickname("시크릿가든")
                    .department("융소")
                    .studentNumber(60200000)
                    .build();
            memberRepository.save(member);
            log.info("Member 더미 데이터 추가 완료: {}", member.getEmail());
        } else {
            log.info("Member 데이터가 이미 존재함. 추가하지 않음.");
        }
        // 2️⃣ CommunityBoard 더미 데이터 추가
        CommunityBoard board = communityBoardRepository.findAll().stream().findFirst().orElse(null);
        if (board == null) {
            board = CommunityBoard.create(
                    "1 게시글",
                    "테스트 용이다잉",
                    nova.mjs.community.entity.enumList.CommunityCategory.FREE,
                    true,
                    null
            );
            communityBoardRepository.save(board);
            log.info("CommunityBoard 더미 데이터 추가 완료: {}", board.getTitle());
        } else {
            log.info("CommunityBoard 데이터가 이미 존재하여 추가하지 않음.");
        }

        // 3️⃣ Comments 더미 데이터 추가
        if (commentsRepository.count() == 0) {
            Comments comment = Comments.create(board, member, "테스트 댓글이야");
            commentsRepository.save(comment);
            log.info("Comments 더미 데이터 추가 완료: {}", comment.getContent());
        } else {
            log.info("Comments 데이터가 이미 존재함. 추가하지 않음.");
        }

        log.info("초기 더미 데이터 삽입 완료!");
    }*/
}
