package com.kernelLetter.repository;

import com.kernelLetter.domain.entity.Letter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {
    Optional<Letter> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

}
