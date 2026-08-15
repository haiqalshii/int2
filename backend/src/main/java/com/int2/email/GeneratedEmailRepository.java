package com.int2.email;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratedEmailRepository extends JpaRepository<GeneratedEmail, UUID> {}

