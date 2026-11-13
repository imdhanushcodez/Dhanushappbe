package com.developersfab.Dhanushappbe.notes;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {

    Optional<Note> findByIdAndUserId(Long id, Long userId);

    List<Note> findByUserId(Long userId, Sort sort);

    boolean existsByUserIdAndTopicIgnoreCaseAndContent(Long userId, String topic, String content);
}

