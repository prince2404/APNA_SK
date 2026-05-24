package com.ask.repository;

import com.ask.entity.HealthCardMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HealthCardMemberRepository extends JpaRepository<HealthCardMember, Long> {
    List<HealthCardMember> findByHealthCardId(Long healthCardId);
    long countByHealthCardId(Long healthCardId);
}
