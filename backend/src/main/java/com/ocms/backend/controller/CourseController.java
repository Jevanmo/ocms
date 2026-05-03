package com.ocms.backend.controller;

import com.ocms.backend.model.Course;
import com.ocms.backend.model.Enrollment;
import com.ocms.backend.model.Role;
import com.ocms.backend.model.User;
import com.ocms.backend.repository.CourseRepository;
import com.ocms.backend.repository.EnrollmentRepository;
import com.ocms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody Map<String, String> payload, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() != Role.INSTRUCTOR) {
            return ResponseEntity.badRequest().body("Only instructors can create courses");
        }
        Course course = new Course();
        course.setTitle(payload.get("title"));
        course.setDescription(payload.getOrDefault("description", ""));
        course.setInstructor(currentUser);
        return ResponseEntity.ok(courseRepository.save(course));
    }

    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<?> enroll(@PathVariable Long courseId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() != Role.STUDENT) {
            return ResponseEntity.badRequest().body("Only students can enroll");
        }
        if (enrollmentRepository.findByStudentIdAndCourseId(currentUser.getId(), courseId).isPresent()) {
            return ResponseEntity.badRequest().body("Already enrolled");
        }
        Course course = courseRepository.findById(courseId).orElseThrow();
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(currentUser);
        enrollment.setCourse(course);
        return ResponseEntity.ok(enrollmentRepository.save(enrollment));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyCourses(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() == Role.INSTRUCTOR) {
            return ResponseEntity.ok(courseRepository.findAll().stream()
                    .filter(c -> c.getInstructor().getId().equals(currentUser.getId()))
                    .toList());
        }
        return ResponseEntity.ok(enrollmentRepository.findByStudentId(currentUser.getId()).stream()
                .map(Enrollment::getCourse).toList());
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElseThrow();
    }
}
