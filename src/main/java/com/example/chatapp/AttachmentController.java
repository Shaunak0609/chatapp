package com.example.chatapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/gif",
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final long MAX_SIZE = 10L * 1024 * 1024;

    @Value("${upload.dir:${user.home}/chatapp-uploads}")
    private String uploadDir;

    private final AttachmentRepository attachmentRepository;

    public AttachmentController(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    Principal principal) throws IOException {

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File type not allowed: " + contentType));
        }
        if (file.getSize() > MAX_SIZE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File exceeds the 10 MB limit"));
        }

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = "." + originalName.substring(originalName.lastIndexOf('.') + 1);
        }
        String stored = UUID.randomUUID() + ext;

        Path dest = dir.resolve(stored).normalize();
        if (!dest.startsWith(dir)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid filename"));
        }
        Files.copy(file.getInputStream(), dest);

        Attachment att = new Attachment();
        att.setOriginalFileName(originalName);
        att.setStoredFileName(stored);
        att.setFileType(contentType);
        att.setFileSize(file.getSize());
        att.setFileUrl("/attachments/files/" + stored);
        att.setUploadedBy(principal.getName());
        att.setUploadedAt(LocalDateTime.now());
        attachmentRepository.save(att);

        return ResponseEntity.ok(Map.of(
                "id", att.getId(),
                "url", att.getFileUrl(),
                "name", originalName != null ? originalName : stored,
                "type", contentType
        ));
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws IOException {
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = dir.resolve(filename).normalize();

        if (!filePath.startsWith(dir)) {
            return ResponseEntity.badRequest().build();
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String mediaType = Files.probeContentType(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        mediaType != null ? mediaType : "application/octet-stream"))
                .body(resource);
    }
}
