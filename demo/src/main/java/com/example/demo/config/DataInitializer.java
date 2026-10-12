package com.example.demo.config;

import com.example.demo.dto.ApplicationDto;
import com.example.demo.dto.InterviewScheduleDto;
import com.example.demo.dto.JobPostDto;
import com.example.demo.dto.UserRegisterDto;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.enums.ApprovalStatus;
import com.example.demo.enums.JobStatus;
import com.example.demo.enums.JobType;
import com.example.demo.enums.Role;
import com.example.demo.model.Application;
import com.example.demo.model.Candidate;
import com.example.demo.model.Category;
import com.example.demo.model.Hr;
import com.example.demo.model.JobPost;
import com.example.demo.model.User;
import com.example.demo.repository.CandidateRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.HrRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.CategoryService;
import com.example.demo.service.InterviewService;
import com.example.demo.service.JobPostService;
import com.example.demo.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Initializes demo data on startup so the homepage immediately displays real jobs and categories.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final HrRepository hrRepository;
    private final CandidateRepository candidateRepository;
    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final JobPostService jobPostService;
    private final JobPostRepository jobPostRepository;

    public DataInitializer(CategoryRepository categoryRepository,
                           CategoryService categoryService,
                           UserRepository userRepository,
                           UserService userService,
                           HrRepository hrRepository,
                           CandidateRepository candidateRepository,
                           ApplicationService applicationService,
                           InterviewService interviewService,
                           JobPostService jobPostService,
                           JobPostRepository jobPostRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.hrRepository = hrRepository;
        this.candidateRepository = candidateRepository;
        this.applicationService = applicationService;
        this.interviewService = interviewService;
        this.jobPostService = jobPostService;
        this.jobPostRepository = jobPostRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            // 1. Seed Categories
            Category dev = categoryService.createCategory("Software Development", "bi-code-slash");
            Category data = categoryService.createCategory("Data Science & AI", "bi-cpu");
            Category design = categoryService.createCategory("Design & Creative", "bi-palette");
            Category marketing = categoryService.createCategory("Marketing & Sales", "bi-megaphone");

            // 2. Seed Demo HR
            UserRegisterDto hrDto = new UserRegisterDto();
            hrDto.setEmail("recruiter@techcorp.com");
            hrDto.setPassword("hr123");
            hrDto.setRole(Role.ROLE_HR);
            hrDto.setCompanyName("TechCorp Innovations");
            hrDto.setHrName("Sarah Jenkins");
            hrDto.setMobile("+91 9876543210");
            User hrUser = userService.registerUser(hrDto);

            // Approve HR for posting
            Hr hr = hrRepository.findByUserId(hrUser.getId()).orElseThrow();
            hr.setApprovalStatus(ApprovalStatus.APPROVED);
            hrRepository.save(hr);

            // Seed Demo Candidate
            UserRegisterDto candidateDto = new UserRegisterDto();
            candidateDto.setEmail("candidate@test.com");
            candidateDto.setPassword("candidate123");
            candidateDto.setRole(Role.ROLE_CANDIDATE);
            candidateDto.setCandidateName("John Doe");
            candidateDto.setMobile("+91 9811122233");
            User candidateUser = userService.registerUser(candidateDto);

            Candidate candidate = candidateRepository.findByUserId(candidateUser.getId()).orElseThrow();
            candidate.setEducation("B.Tech in Computer Science & Engineering (2023)");
            candidate.setSkills("Java 21, Spring Boot, MySQL, REST APIs, React");
            candidate.setExperience("2 Years as Software Engineer");
            candidate.setResumePath("sample_john_doe_resume.pdf");
            candidateRepository.save(candidate);

            // Seed Demo Admin
            User adminUser = new User();
            adminUser.setEmail("admin@careerlink.com");
            adminUser.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("admin123"));
            adminUser.setRole(Role.ROLE_ADMIN);
            adminUser.setEnabled(true);
            userRepository.save(adminUser);

            // 3. Seed Demo Jobs
            JobPostDto job1 = new JobPostDto();
            job1.setTitle("Senior Full Stack Java Developer");
            job1.setDescription("Looking for an experienced Spring Boot and React developer to build scalable enterprise cloud microservices.");
            job1.setSkillsRequired("Java 21, Spring Boot, React, Docker, PostgreSQL");
            job1.setQualification("B.Tech / MCA or equivalent degree in Computer Science");
            job1.setExperienceLevel("3 - 5 Years");
            job1.setSalaryMin(1200000.0);
            job1.setSalaryMax(1800000.0);
            job1.setLocation("Bangalore (Hybrid)");
            job1.setJobType(JobType.FULL_TIME);
            job1.setCategoryId(dev.getId());
            job1.setLastDate(LocalDate.now().plusMonths(1));
            JobPost posted1 = jobPostService.createJobPost(hr.getId(), job1);
            posted1.setStatus(JobStatus.APPROVED);
            jobPostRepository.save(posted1);

            // Seed Demo Application for John Doe on Job 1 with attached resume
            ApplicationDto demoApp = new ApplicationDto();
            demoApp.setJobId(posted1.getId());
            demoApp.setCoverNote("Excited to apply! I have 2 years of backend experience building microservices with Spring Boot.");
            demoApp.setResumePath("sample_john_doe_resume.pdf");
            Application app = applicationService.applyForJob(candidate.getId(), demoApp);

            // 1. Shortlist application (triggers candidate notification)
            applicationService.updateStatus(app.getId(), ApplicationStatus.SHORTLISTED);

            // 2. Schedule live interview (triggers candidate notification and sets status to INTERVIEW_SCHEDULED)
            InterviewScheduleDto interviewDto = new InterviewScheduleDto();
            interviewDto.setApplicationId(app.getId());
            interviewDto.setInterviewDate(LocalDateTime.now().plusDays(2).withHour(11).withMinute(0).withSecond(0));
            interviewDto.setModeOrLink("https://meet.google.com/crk-tech-interview");
            interviewService.scheduleInterview(interviewDto);

            JobPostDto job2 = new JobPostDto();
            job2.setTitle("Frontend UI/UX Designer & Engineer");
            job2.setDescription("Design modern intuitive user interfaces with Figma and implement responsive dashboards using Tailwind CSS & Bootstrap.");
            job2.setSkillsRequired("Figma, HTML5, CSS3, JavaScript, Bootstrap, Responsive Design");
            job2.setQualification("Any Graduate with strong UI/UX portfolio");
            job2.setExperienceLevel("1 - 3 Years");
            job2.setSalaryMin(700000.0);
            job2.setSalaryMax(1100000.0);
            job2.setLocation("Remote");
            job2.setJobType(JobType.REMOTE);
            job2.setCategoryId(design.getId());
            job2.setLastDate(LocalDate.now().plusMonths(2));
            JobPost posted2 = jobPostService.createJobPost(hr.getId(), job2);
            posted2.setStatus(JobStatus.APPROVED);
            jobPostRepository.save(posted2);

            JobPostDto job3 = new JobPostDto();
            job3.setTitle("Data Analyst & Python Specialist");
            job3.setDescription("Analyze product datasets, create business intelligence dashboards, and extract insights using SQL and Pandas.");
            job3.setSkillsRequired("Python, Pandas, SQL, Tableau, PowerBI");
            job3.setQualification("Bachelor's in Statistics, Mathematics, or CS");
            job3.setExperienceLevel("0 - 2 Years (Freshers Welcome)");
            job3.setSalaryMin(500000.0);
            job3.setSalaryMax(800000.0);
            job3.setLocation("Mumbai");
            job3.setJobType(JobType.FULL_TIME);
            job3.setCategoryId(data.getId());
            job3.setLastDate(LocalDate.now().plusWeeks(3));
            JobPost posted3 = jobPostService.createJobPost(hr.getId(), job3);
            posted3.setStatus(JobStatus.APPROVED);
            jobPostRepository.save(posted3);

            // Job 4
            JobPostDto job4 = new JobPostDto();
            job4.setTitle("DevOps & Cloud Infrastructure Engineer");
            job4.setDescription("Maintain CI/CD pipelines, manage Kubernetes clusters on AWS, and automate infrastructure deployments using Terraform.");
            job4.setSkillsRequired("AWS, Kubernetes, Docker, Terraform, Jenkins, Linux");
            job4.setQualification("B.Tech / B.E. in Computer Science or IT");
            job4.setExperienceLevel("2 - 5 Years");
            job4.setSalaryMin(1400000.0);
            job4.setSalaryMax(2000000.0);
            job4.setLocation("Hyderabad (Hybrid)");
            job4.setJobType(JobType.FULL_TIME);
            job4.setCategoryId(dev.getId());
            job4.setLastDate(LocalDate.now().plusMonths(1));
            JobPost posted4 = jobPostService.createJobPost(hr.getId(), job4);
            posted4.setStatus(JobStatus.APPROVED);
            jobPostRepository.save(posted4);

            // Job 5
            JobPostDto job5 = new JobPostDto();
            job5.setTitle("AI & Machine Learning Researcher");
            job5.setDescription("Train deep learning LLM architectures, fine-tune models, and deploy high-throughput inference endpoints with PyTorch.");
            job5.setSkillsRequired("PyTorch, TensorFlow, NLP, Python, Deep Learning");
            job5.setQualification("Master's or Ph.D. in AI, Machine Learning, or CS");
            job5.setExperienceLevel("3 - 6 Years");
            job5.setSalaryMin(2000000.0);
            job5.setSalaryMax(3000000.0);
            job5.setLocation("Bangalore");
            job5.setJobType(JobType.FULL_TIME);
            job5.setCategoryId(data.getId());
            job5.setLastDate(LocalDate.now().plusMonths(2));
            JobPost posted5 = jobPostService.createJobPost(hr.getId(), job5);
            posted5.setStatus(JobStatus.APPROVED);
            jobPostRepository.save(posted5);

            // Job 6
            JobPostDto job6 = new JobPostDto();
            job6.setTitle("Social Media & Growth Marketing Manager");
            job6.setDescription("Lead digital marketing campaigns, optimize Google & Meta Ads, and increase organic user acquisition via SEO.");
            job6.setSkillsRequired("SEO, Google Ads, Meta Ads, Content Strategy, Google Analytics");
            job6.setQualification("MBA or Bachelor's in Marketing or Communications");
            job6.setExperienceLevel("2 - 4 Years");
            job6.setSalaryMin(800000.0);
            job6.setSalaryMax(1300000.0);
            job6.setLocation("Delhi NCR");
            job6.setJobType(JobType.FULL_TIME);
            job6.setCategoryId(marketing.getId());
            job6.setLastDate(LocalDate.now().plusWeeks(4));
            JobPost posted6 = jobPostService.createJobPost(hr.getId(), job6);
            posted6.setStatus(JobStatus.APPROVED);
            jobPostRepository.save(posted6);

            // Job 7
            JobPostDto job7 = new JobPostDto();
            job7.setTitle("Junior Web Development Intern");
            job7.setDescription("Assist frontend engineering teams with HTML, CSS, JavaScript, and responsive layout testing on mobile devices.");
            job7.setSkillsRequired("HTML, CSS, JavaScript, Git basics");
            job7.setQualification("Pre-final or Final year students (Any Branch)");
            job7.setExperienceLevel("Freshers");
            job7.setSalaryMin(250000.0);
            job7.setSalaryMax(400000.0);
            job7.setLocation("Remote");
            job7.setJobType(JobType.INTERNSHIP);
            job7.setCategoryId(dev.getId());
            job7.setLastDate(LocalDate.now().plusWeeks(2));
            JobPost posted7 = jobPostService.createJobPost(hr.getId(), job7);
            posted7.setStatus(JobStatus.APPROVED);
            jobPostRepository.save(posted7);

            // Job 8
            JobPostDto job8 = new JobPostDto();
            job8.setTitle("Product Brand Designer & Illustrator");
            job8.setDescription("Create company branding assets, marketing illustrations, icons, and visual design systems across web and mobile.");
            job8.setSkillsRequired("Figma, Adobe Illustrator, Photoshop, Visual Design");
            job8.setQualification("Degree in Graphic Design, Fine Arts, or portfolio equivalent");
            job8.setExperienceLevel("1 - 3 Years");
            job8.setSalaryMin(600000.0);
            job8.setSalaryMax(950000.0);
            job8.setLocation("Pune");
            job8.setJobType(JobType.FULL_TIME);
            job8.setCategoryId(design.getId());
            job8.setLastDate(LocalDate.now().plusMonths(1));
            JobPost posted8 = jobPostService.createJobPost(hr.getId(), job8);
            posted8.setStatus(JobStatus.APPROVED);
            jobPostRepository.save(posted8);
        }
    }
}
