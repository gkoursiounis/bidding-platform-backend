package com.Auctions.backEnd.repositories;

import com.Auctions.backEnd.models.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {

    UserMessage findUserMessageById(Long id);
}