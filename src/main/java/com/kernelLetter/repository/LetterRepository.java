package com.kernelLetter.repository;

import com.kernelLetter.domain.entity.Letter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.kernelLetter.domain.entity.User;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {
    Optional<Letter> findBySenderIdAndReceiverName(Long senderId, String receiverName);
    boolean existsByReceiverNameAndPosition(String receiver, int position);
    boolean existsBySenderAndReceiverName(User sender, String receiver);
    List<Letter> findByReceiverId(Long receiverId);

    Optional<Letter> findByReceiverIdAndId(Long userId, Long Id);

    List<Letter> findByReceiverName(String receiverName);

    List<Letter> findAllBySenderId(Long senderId);
}
