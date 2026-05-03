package com.ocms.backend.controller;

import com.ocms.backend.model.Course;
import com.ocms.backend.model.CourseContent;
import com.ocms.backend.model.Role;
import com.ocms.backend.model.User;
import com.ocms.backend.repository.CourseContentRepository;
import com.ocms.backend.repository.CourseRepository;
import com.ocms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ContentController {

    private final CourseRepository courseRepository;
    private final CourseContentRepository courseContentRepository;
    private final UserRepository userRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @PostMapping("/api/courses/{courseId}/contents")
    public ResponseEntity<?> uploadContent(
            @PathVariable Long courseId,
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws Exception {
        User currentUser = getCurrentUser(authentication);
        Course course = courseRepository.findById(courseId).orElseThrow();
        if (currentUser.getRole() != Role.INSTRUCTOR || !course.getInstructor().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("Only course instructor can upload content");
        }
        Path dirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dirPath);
        String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = dirPath.resolve(storedFileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        CourseContent content = new CourseContent();
        content.setCourse(course);
        content.setTitle(title);
        content.setFileName(file.getOriginalFilename());
        content.setFilePath(storedFileName);
        return ResponseEntity.ok(courseContentRepository.save(content));
    }

    @GetMapping("/api/courses/{courseId}/contents")
    public List<CourseContent> listContents(@PathVariable Long courseId) {
        return courseContentRepository.findByCourseId(courseId);
    }

    @GetMapping("/api/files/{storedName}")
    public ResponseEntity<Resource> serveFile(@PathVariable String storedName) throws Exception {
        Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(storedName);
        Resource resource = new UrlResource(filePath.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElseThrow();
    }
}
