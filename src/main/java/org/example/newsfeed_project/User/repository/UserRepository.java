package org.example.newsfeed_project.User.repository;

import org.example.newsfeed_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
