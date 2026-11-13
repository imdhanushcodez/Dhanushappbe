package com.developersfab.Dhanushappbe.vault;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaultRepository extends JpaRepository<VaultEntry, Long>, JpaSpecificationExecutor<VaultEntry> {

    Optional<VaultEntry> findByIdAndUserId(Long id, Long userId);

    List<VaultEntry> findByUserId(Long userId, Sort sort);

    boolean existsByUserIdAndSourceIgnoreCaseAndLoginIdIgnoreCase(Long userId, String source, String loginId);

    boolean existsByUserIdAndSourceIgnoreCaseAndLoginIdIgnoreCaseAndIdNot(Long userId, String source, String loginId, Long id);

    @Query("SELECT DISTINCT v.source FROM VaultEntry v WHERE v.user.id = :userId ORDER BY v.source ASC")
    List<String> findDistinctSourcesByUserId(@Param("userId") Long userId);
}
