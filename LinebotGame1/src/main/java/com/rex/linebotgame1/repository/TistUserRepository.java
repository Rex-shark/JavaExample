package com.rex.linebotgame1.repository;


import com.rex.linebotgame1.entity.TistUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@SuppressWarnings("unused")
@Repository
public interface TistUserRepository extends JpaRepository<TistUser, Long> {

    @NotNull Optional<TistUser> findById(@NotNull Long id);
    boolean existsById(@NotNull Long id);

    Optional<TistUser> findByTistIdAndSystexId(String tistId, String systexId);

    Optional<TistUser> findByTistId(String tistId);
    boolean existsByTistId(String tistId);

    Optional<TistUser> findByLineId(String lineId);
    boolean existsByLineId(String lineId);
}
