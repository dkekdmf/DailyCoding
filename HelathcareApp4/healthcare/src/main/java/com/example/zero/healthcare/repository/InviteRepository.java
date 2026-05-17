package com.example.zero.healthcare.repository;

import com.example.zero.healthcare.Entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invite, Long> {
    Optional<Invite> findByTokenHashAndActiveTrue(String tokenHash);
}
