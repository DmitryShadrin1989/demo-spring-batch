package ru.sm.lab.demo.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sm.lab.demo.models.jpa.AuthorJpa;

public interface AuthorJpaRepository extends JpaRepository<AuthorJpa, Long> {

}