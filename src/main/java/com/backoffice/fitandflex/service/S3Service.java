package com.backoffice.fitandflex.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
@ConditionalOnExpression("!'${aws.s3.access-key:}'.isEmpty() && !'${aws.s3.secret-key:}'.isEmpty()")
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.video-folder}")
    private String videoFolder;

    @Value("${aws.s3.max-file-size:104857600}")
    private long maxFileSize;

    @Value("${aws.s3.allowed-video-types}")
    private String allowedVideoTypes;

    /**
     * Sube un video a S3
     * 
     * @param file Archivo de video a subir
     * @return La clave (key) del archivo en S3
     * @throws IOException Si hay un error al leer el archivo
     */
    public String uploadVideo(MultipartFile file) throws IOException {
        // Validar el archivo
        validateVideoFile(file);

        // Generar un nombre único para el archivo
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + fileExtension;
        String key = videoFolder + "/" + fileName;

        try {
            // Subir el archivo a S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            InputStream inputStream = file.getInputStream();
            RequestBody requestBody = RequestBody.fromInputStream(inputStream, file.getSize());

            s3Client.putObject(putObjectRequest, requestBody);

            log.info("Video subido exitosamente a S3: {}", key);
            return key;

        } catch (S3Exception e) {
            log.error("Error al subir video a S3: {}", e.getMessage(), e);
            throw new RuntimeException("Error al subir el video a S3: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene un video de S3 como InputStream
     * 
     * @param key La clave del archivo en S3
     * @return InputStream del archivo
     */
    public InputStream getVideo(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObject(getObjectRequest);

        } catch (NoSuchKeyException e) {
            log.error("Video no encontrado en S3: {}", key);
            throw new RuntimeException("Video no encontrado: " + key, e);
        } catch (S3Exception e) {
            log.error("Error al obtener video de S3: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener el video de S3: " + e.getMessage(), e);
        }
    }

    /**
     * Genera una URL presignada para acceder al video (válida por 1 hora)
     * 
     * @param key La clave del archivo en S3
     * @return URL presignada
     */
    public String getPresignedUrl(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();

        } catch (S3Exception e) {
            log.error("Error al generar URL presignada: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar URL presignada: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un video de S3
     * 
     * @param key La clave del archivo en S3
     */
    public void deleteVideo(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Video eliminado de S3: {}", key);

        } catch (S3Exception e) {
            log.error("Error al eliminar video de S3: {}", e.getMessage(), e);
            throw new RuntimeException("Error al eliminar el video de S3: " + e.getMessage(), e);
        }
    }

    /**
     * Lista todos los videos en el bucket
     * 
     * @return Lista de mapas con información de los videos
     */
    public List<Map<String, Object>> listVideos() {
        try {
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(videoFolder + "/")
                    .build();

            ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

            return listObjectsV2Response.contents().stream()
                    .filter(s3Object -> !s3Object.key().endsWith("/")) // Filtrar carpetas
                    .map(s3Object -> {
                        Map<String, Object> videoInfo = new HashMap<>();
                        videoInfo.put("key", s3Object.key());
                        videoInfo.put("fileName", s3Object.key().substring(s3Object.key().lastIndexOf("/") + 1));
                        videoInfo.put("size", s3Object.size());
                        videoInfo.put("lastModified", s3Object.lastModified().toString());
                        // Generar URL presignada para cada video (opcional, puede ser costoso si son
                        // muchos)
                        // Para listar, mejor devolver solo key y que el frontend pida la URL si quiere
                        // reproducir
                        return videoInfo;
                    })
                    .collect(Collectors.toList());

        } catch (S3Exception e) {
            log.error("Error al listar videos de S3: {}", e.getMessage(), e);
            throw new RuntimeException("Error al listar videos de S3: " + e.getMessage(), e);
        }
    }

    /**
     * Valida que el archivo sea un video válido
     */
    private void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    "El archivo excede el tamaño máximo permitido: " + (maxFileSize / 1024 / 1024) + " MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedVideoType(contentType)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Tipos permitidos: " + allowedVideoTypes);
        }
    }

    /**
     * Verifica si el tipo de contenido es un video permitido
     */
    private boolean isAllowedVideoType(String contentType) {
        List<String> allowedTypes = List.of(allowedVideoTypes.split(","));
        return allowedTypes.stream()
                .anyMatch(type -> contentType.toLowerCase().contains(type.trim().toLowerCase()));
    }

    /**
     * Obtiene la extensión del archivo
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}
