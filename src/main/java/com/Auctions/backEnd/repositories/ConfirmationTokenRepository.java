package com.Auctions.backEnd.repositories;
import com.Auctions.backEnd.models.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    ConfirmationToken findByConfirmationToken(String confirmationToken);
    ConfirmationToken findByAccount_Id(Long accountId);
}