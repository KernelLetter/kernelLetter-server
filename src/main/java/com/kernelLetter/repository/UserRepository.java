package com.kernelLetter.repository;

import com.kernelLetter.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);

    Optional<User> findById(Long id);

    Optional<User> findByKakaoId(String kakaoId);

}
