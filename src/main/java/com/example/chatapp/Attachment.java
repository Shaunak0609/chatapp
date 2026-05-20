package com.example.chatapp;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName;
    private String storedFileName;
    private String fileType;
    private long fileSize;
    private String fileUrl;
    private String uploadedBy;
    private LocalDateTime uploadedAt;

    public Attachment() {}

    public Long getId() { return id; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String v) { this.originalFileName = v; }

    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String v) { this.storedFileName = v; }

    public String getFileType() { return fileType; }
    public void setFileType(String v) { this.fileType = v; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long v) { this.fileSize = v; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String v) { this.fileUrl = v; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String v) { this.uploadedBy = v; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime v) { this.uploadedAt = v; }
}
