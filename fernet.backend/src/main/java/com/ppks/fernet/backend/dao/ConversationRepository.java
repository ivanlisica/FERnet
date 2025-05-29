package com.ppks.fernet.backend.dao;

import com.ppks.fernet.backend.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByClientIdAndAdminId(UUID clientId, UUID adminId);
    Optional<Conversation> findByClientIdAndAdminIsNull(UUID clientId);
    List<Conversation> findByAdminIdOrderByLastMessageAtDesc(UUID adminId);
    List<Conversation> findByClientIdOrderByLastMessageAtDesc(UUID clientId);
    Optional<Conversation> findByClientId(UUID clientId);

    List<Conversation> findByAdminIdOrAdminIsNullOrderByLastMessageAtDesc(UUID adminId);

    List<Conversation> findByAdminId(UUID adminId);
}