package com.Auctions.backEnd.repositories;

import com.Auctions.backEnd.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByAccount_Username(String username);
    User findUserById(Long id);


    @Query(
            "select u from User u join u.account a " +
            "where (locate(:query, lower(u.firstName)) <> 0) or " +
            "(locate(:query, lower(u.lastName)) <> 0) or" +
            "(locate(:query, lower(a.username)) <> 0)"
    )
    Set<User> searchUsers(@Param("query") String query);


    @Query( "SELECT u "+
            "FROM User u")
    List<User> getAllUsers(Pageable pageable);


    @Query( "SELECT u "+
            "FROM User u, Account a "+
            "WHERE u.account = a and a.verified = 'false'")
    List<User> getPendingUsers(Pageable pageable);


    @Query( "SELECT u "+
            "FROM User u, Account a "+
            "WHERE u.account = a and a.verified = 'false'")
    List<User> getPendingUsers();
}

