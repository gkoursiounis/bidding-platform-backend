package com.Auctions.backEnd.repositories;

import com.Auctions.backEnd.models.User;
import com.Auctions.backEnd.models.UserMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {

    UserMessage findUserMessageById(Long id);

    @Query( "SELECT um " +
            "FROM UserMessage um " +
            "WHERE um.recipient = :user " +
            "ORDER BY um.createdAt DESC")
    Page<UserMessage> getReceveivedMessages(@Param("user") User user, Pageable pageable);


    @Query( "SELECT um " +
            "FROM UserMessage um " +
            "WHERE um.sender = :user " +
            "ORDER BY um.createdAt DESC")
    Page<UserMessage> getSentMessages(@Param("user") User user, Pageable pageable);
}