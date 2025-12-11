package com.backoffice.fitandflex.service;

import com.backoffice.fitandflex.entity.Video;
import com.backoffice.fitandflex.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final S3Service s3Service;
    private final VideoRepository videoRepository;

    /**
     * Sube un video a S3 y guarda los metadatos en la base de datos
     */
    @Transactional
    public Video uploadVideo(MultipartFile file, String title, String description) throws IOException {
        // Subir a S3
        String key = s3Service.uploadVideo(file);

        // Guardar en BD
        Video video = Video.builder()
                .title(title)
                .description(description)
                .s3Key(key)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();

        return videoRepository.save(video);
    }

    /**
     * Lista todos los videos de la base de datos y genera URLs presignadas
     */
    public List<Map<String, Object>> getAllVideos() {
        List<Video> videos = videoRepository.findAll();

        return videos.stream().map(video -> {
            Map<String, Object> videoMap = new HashMap<>();
            videoMap.put("id", video.getId());
            videoMap.put("title", video.getTitle());
            videoMap.put("description", video.getDescription());
            videoMap.put("key", video.getS3Key());
            videoMap.put("fileName", video.getFileName());
            videoMap.put("size", video.getSize());
            videoMap.put("contentType", video.getContentType());
            videoMap.put("createdAt", video.getCreatedAt());

            // Generar URL presignada
            try {
                String url = s3Service.getPresignedUrl(video.getS3Key());
                videoMap.put("url", url);
            } catch (Exception e) {
                log.error("Error al generar URL presignada para video {}: {}", video.getId(), e.getMessage());
                videoMap.put("url", null);
            }

            return videoMap;
        }).collect(Collectors.toList());
    }

    /**
     * Actualiza los metadatos de un video
     */
    @Transactional
    public Video updateVideo(Long id, String title, String description) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video no encontrado con ID: " + id));

        if (title != null && !title.isEmpty()) {
            video.setTitle(title);
        }

        if (description != null) {
            video.setDescription(description);
        }

        return videoRepository.save(video);
    }

    /**
     * Elimina un video de S3 y de la base de datos
     */
    @Transactional
    public void deleteVideo(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video no encontrado con ID: " + id));

        // Eliminar de S3
        s3Service.deleteVideo(video.getS3Key());

        // Eliminar de BD
        videoRepository.delete(video);
    }

    /**
     * Obtiene un video por ID
     */
    public Video getVideoById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video no encontrado con ID: " + id));
    }
}
