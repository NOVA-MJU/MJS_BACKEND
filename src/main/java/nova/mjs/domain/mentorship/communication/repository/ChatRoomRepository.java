package nova.mjs.domain.mentorship.communication.repository;

import nova.mjs.domain.mentorship.communication.entity.ChatRoom;
import nova.mjs.domain.mentorship.mentor.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

}
