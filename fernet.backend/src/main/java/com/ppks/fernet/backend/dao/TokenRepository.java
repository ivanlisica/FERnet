package com.ppks.fernet.backend.dao;

import com.ppks.fernet.backend.domain.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query(value = """
      select t from Token t inner join User u\s
      on t.user.username = u.username\s
      where u.username = :name and (t.expired = false or t.revoked = false)\s
      """)
    List<Token> findAllValidTokenByClient(String name);

    Optional<Token> findByToken(String token);
}
