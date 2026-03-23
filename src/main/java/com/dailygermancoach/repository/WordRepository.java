package com.dailygermancoach.repository;

import com.dailygermancoach.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {
    // Spring Data provides CRUD methods.
}
