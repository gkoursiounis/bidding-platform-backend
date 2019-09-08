package com.Auctions.backEnd.repositories;

import com.Auctions.backEnd.models.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {

    UserMessage findUserMessageById(Long id);
}