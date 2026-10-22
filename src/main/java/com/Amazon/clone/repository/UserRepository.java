package com.Amazon.clone.repository;

import com.Amazon.clone.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = {"userRoles.role"})
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);
}
