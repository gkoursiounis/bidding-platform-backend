package com.Auctions.backEnd.repositories;

import com.Auctions.backEnd.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByAccount_Username(String username);
    User findUserById(Long id);

    @Query( "SELECT u "+
            "FROM User u")
    Page<User> getAllUsers(Pageable pageable);


    @Query( "SELECT u "+
            "FROM User u, Account a "+
            "WHERE u.account = a and a.verified = 'false'")
    Page<User> getPendingUsers(Pageable pageable);


    @Query( "SELECT u "+
            "FROM User u, Account a "+
            "WHERE u.account = a and a.verified = 'false'")
    List<User> getPendingUsers();
}

