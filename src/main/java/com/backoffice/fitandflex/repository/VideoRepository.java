package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findByS3Key(String s3Key);
}
