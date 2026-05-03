package com.ocms.backend.controller;

import com.ocms.backend.model.*;
import com.ocms.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final SubmissionRepository submissionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @PostMapping("/courses/{courseId}/assignments")
    public ResponseEntity<?> createAssignment(@PathVariable Long courseId, @RequestBody Map<String, String> payload,
                                              Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Course course = courseRepository.findById(courseId).orElseThrow();
        if (currentUser.getRole() != Role.INSTRUCTOR || !course.getInstructor().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("Only course instructor can create assignment");
        }
        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        assignment.setTitle(payload.get("title"));
        assignment.setDescription(payload.getOrDefault("description", ""));
        if (payload.containsKey("dueDate")) {
            assignment.setDueDate(LocalDateTime.parse(payload.get("dueDate")));
        }
        return ResponseEntity.ok(assignmentRepository.save(assignment));
    }

    @GetMapping("/courses/{courseId}/assignments")
    public List<Assignment> listAssignments(@PathVariable Long courseId) {
        return assignmentRepository.findByCourseId(courseId);
    }

    @PostMapping("/assignments/{assignmentId}/submit")
    public ResponseEntity<?> submitAssignment(@PathVariable Long assignmentId,
                                              @RequestParam("file") MultipartFile file,
                                              Authentication authentication) throws Exception {
        User currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() != Role.STUDENT) {
            return ResponseEntity.badRequest().body("Only students can submit");
        }
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow();
        if (enrollmentRepository.findByStudentIdAndCourseId(currentUser.getId(), assignment.getCourse().getId()).isEmpty()) {
            return ResponseEntity.badRequest().body("Student not enrolled in this course");
        }

        Path dirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dirPath);
        String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = dirPath.resolve(storedFileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, currentUser.getId())
                .orElseGet(Submission::new);
        submission.setAssignment(assignment);
        submission.setStudent(currentUser);
        submission.setFileName(file.getOriginalFilename());
        submission.setFilePath(storedFileName);
        submission.setSubmittedAt(LocalDateTime.now());
        return ResponseEntity.ok(submissionRepository.save(submission));
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<?> listSubmissions(@PathVariable Long assignmentId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow();
        if (currentUser.getRole() != Role.INSTRUCTOR ||
                !assignment.getCourse().getInstructor().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("Only course instructor can view submissions");
        }
        return ResponseEntity.ok(submissionRepository.findByAssignmentId(assignmentId));
    }

    @PutMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<?> gradeSubmission(@PathVariable Long submissionId,
                                             @RequestBody Map<String, String> payload,
                                             Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Submission submission = submissionRepository.findById(submissionId).orElseThrow();
        if (currentUser.getRole() != Role.INSTRUCTOR ||
                !submission.getAssignment().getCourse().getInstructor().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("Only course instructor can grade submissions");
        }
        submission.setGrade(Integer.parseInt(payload.get("grade")));
        submission.setFeedback(payload.getOrDefault("feedback", ""));
        return ResponseEntity.ok(submissionRepository.save(submission));
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElseThrow();
    }
}
