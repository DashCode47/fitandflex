package com.backoffice.fitandflex.controller;

import com.backoffice.fitandflex.entity.Video;
import com.backoffice.fitandflex.service.S3Service;
import com.backoffice.fitandflex.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Videos", description = "Endpoints para gestión de videos (BD + S3)")
@Profile("!test")
@ConditionalOnExpression("!'${aws.s3.access-key:}'.isEmpty() && !'${aws.s3.secret-key:}'.isEmpty()")
public class VideoController {

    private final VideoService videoService;
    private final S3Service s3Service; // Used for streaming if necessary

    @Operation(summary = "Listar videos", description = "Obtiene una lista de todos los videos registrados en la base de datos con sus URLs presignadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de videos obtenida exitosamente", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error al listar los videos", content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllVideos() {
        try {
            log.info("Solicitud de listado de videos");

            List<Map<String, Object>> videos = videoService.getAllVideos();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("videos", videos);
            response.put("count", videos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al listar videos: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al listar videos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Subir video", description = "Sube un video a S3 y guarda su información en la base de datos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video subido exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Archivo invÃ¡lido o tamaño excedido", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error al subir el video", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @Parameter(description = "Archivo de video a subir", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "Título del video", required = true) @RequestParam("title") String title,
            @Parameter(description = "Descripción del video", required = false) @RequestParam(value = "description", required = false) String description) {
        try {
            log.info("Intento de subir video: '{}' ({} bytes)", title, file.getSize());

            Video video = videoService.uploadVideo(file, title, description);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Video subido exitosamente");
            response.put("video", video);

            log.info("Video subido exitosamente con ID: {}", video.getId());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al subir video: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("Error al subir video: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al subir el video: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Actualizar video", description = "Actualiza el título y/o descripción de un video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video actualizado exitosamente", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Video no encontrado", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error al actualizar el video", content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateVideo(
            @Parameter(description = "ID del video", required = true) @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String title = payload.get("title");
            String description = payload.get("description");

            log.info("Solicitud de actualización de video ID: {}", id);

            Video video = videoService.updateVideo(id, title, description);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Video actualizado exitosamente");
            response.put("video", video);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al actualizar video: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            log.error("Error al actualizar video: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al actualizar el video: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Eliminar video", description = "Elimina un video de S3 y de la base de datos por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video eliminado exitosamente", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Video no encontrado", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error al eliminar el video", content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteVideo(
            @Parameter(description = "ID del video", required = true) @PathVariable Long id) {
        try {
            log.info("Solicitud de eliminación de video con ID: {}", id);

            videoService.deleteVideo(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Video eliminado exitosamente");
            response.put("id", id);

            log.info("Video eliminado exitosamente: {}", id);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al eliminar video: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            log.error("Error al eliminar video: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar el video: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Stream Video", description = "Obtiene el stream del video por ID")
    @GetMapping("/{id}/stream")
    public ResponseEntity<InputStreamResource> streamVideo(@PathVariable Long id) {
        try {
            Video video = videoService.getVideoById(id);
            InputStream videoStream = s3Service.getVideo(video.getS3Key());
            InputStreamResource resource = new InputStreamResource(videoStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.parseMediaType(video.getContentType() != null ? video.getContentType() : "video/mp4"));
            headers.setContentDispositionFormData("inline", video.getFileName()); // inline para streaming

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
