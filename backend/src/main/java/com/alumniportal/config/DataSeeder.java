package com.alumniportal.config;

import com.alumniportal.entity.*;
import com.alumniportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final JobRepository jobRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Data already present, skipping seeding.");
            return;
        }

        log.info("Seeding sample data...");

        // ---- Admin ----
        User admin = User.builder()
                .name("Portal Admin")
                .email("admin@alumniportal.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .approved(true)
                .active(true)
                .build();
        userRepository.save(admin);

        // ---- Alumni #1 (approved, mentorship enabled) ----
        User alumniUser1 = User.builder()
                .name("Priya Sharma")
                .email("priya.sharma@example.com")
                .password(passwordEncoder.encode("alumni123"))
                .role(Role.ALUMNI)
                .approved(true)
                .active(true)
                .build();
        userRepository.save(alumniUser1);

        AlumniProfile alumniProfile1 = AlumniProfile.builder()
                .user(alumniUser1)
                .company("Google")
                .domain("Software Engineering")
                .skills("java, spring boot, mysql, aws, docker")
                .location("Bengaluru, India")
                .graduationYear(2018)
                .currentRole("Senior Software Engineer")
                .experience(6)
                .achievements("Led migration to microservices; 2x internal hackathon winner")
                .bio("Backend engineer passionate about distributed systems and mentoring juniors.")
                .linkedinUrl("https://linkedin.com/in/priyasharma-example")
                .availableForMentorship(true)
                .profileCompleteness(100)
                .build();
        alumniProfileRepository.save(alumniProfile1);

        // ---- Alumni #2 (approved, partial profile) ----
        User alumniUser2 = User.builder()
                .name("Rahul Verma")
                .email("rahul.verma@example.com")
                .password(passwordEncoder.encode("alumni123"))
                .role(Role.ALUMNI)
                .approved(true)
                .active(true)
                .build();
        userRepository.save(alumniUser2);

        AlumniProfile alumniProfile2 = AlumniProfile.builder()
                .user(alumniUser2)
                .company("Microsoft")
                .domain("Cloud Computing")
                .skills("c#, azure, .net, sql")
                .graduationYear(2020)
                .currentRole("Software Development Engineer II")
                .availableForMentorship(false)
                .build();
        alumniProfile2.setProfileCompleteness(43); // partially filled on purpose (missing location, experience, bio, linkedin)
        alumniProfileRepository.save(alumniProfile2);

        // ---- Alumni #3 (pending approval) ----
        User alumniUser3 = User.builder()
                .name("Sana Iyer")
                .email("sana.iyer@example.com")
                .password(passwordEncoder.encode("alumni123"))
                .role(Role.ALUMNI)
                .approved(false)
                .active(true)
                .build();
        userRepository.save(alumniUser3);
        alumniProfileRepository.save(AlumniProfile.builder().user(alumniUser3).build());

        // ---- Students ----
        User studentUser1 = User.builder()
                .name("Ananya Gupta")
                .email("ananya.gupta@example.com")
                .password(passwordEncoder.encode("student123"))
                .role(Role.STUDENT)
                .approved(true)
                .active(true)
                .build();
        userRepository.save(studentUser1);
        studentProfileRepository.save(StudentProfile.builder()
                .user(studentUser1)
                .branch("Computer Science")
                .graduationYear(2026)
                .skills("java, spring boot, mysql, git")
                .bio("Final-year CS student interested in backend development.")
                .build());

        User studentUser2 = User.builder()
                .name("Vikram Nair")
                .email("vikram.nair@example.com")
                .password(passwordEncoder.encode("student123"))
                .role(Role.STUDENT)
                .approved(true)
                .active(true)
                .build();
        userRepository.save(studentUser2);
        studentProfileRepository.save(StudentProfile.builder()
                .user(studentUser2)
                .branch("Information Technology")
                .graduationYear(2027)
                .skills("python, machine learning, pandas")
                .bio("Exploring data science and ML opportunities.")
                .build());

        // ---- Sample jobs ----
        Job job1 = Job.builder()
                .postedBy(alumniUser1)
                .companyName("Google")
                .title("Backend Software Engineer Intern")
                .description("Work on scalable backend services for Google Cloud products.")
                .requiredSkills("java, spring boot, mysql, docker")
                .experienceRequired(0)
                .location("Bengaluru, India")
                .applyLink("https://careers.google.com")
                .type(JobType.INTERNSHIP)
                .status(JobStatus.APPROVED)
                .build();
        jobRepository.save(job1);

        Job job2 = Job.builder()
                .postedBy(alumniUser2)
                .companyName("Microsoft")
                .title("Associate Software Engineer")
                .description("Join the Azure team building next-gen cloud infrastructure.")
                .requiredSkills("c#, azure, .net, sql")
                .experienceRequired(1)
                .location("Hyderabad, India")
                .applyLink("https://careers.microsoft.com")
                .type(JobType.JOB)
                .status(JobStatus.PENDING)
                .build();
        jobRepository.save(job2);

        // ---- Sample event ----
        eventRepository.save(Event.builder()
                .title("Alumni Homecoming & Networking Night")
                .description("An evening of networking, panel discussions and campus tours.")
                .location("Main Campus Auditorium")
                .eventDate(java.time.LocalDateTime.now().plusDays(30))
                .createdBy(admin)
                .build());

        log.info("Sample data seeded. Login with: admin@alumniportal.com / admin123, " +
                "priya.sharma@example.com / alumni123, ananya.gupta@example.com / student123");
    }
}
