# 📘 Day 10: File Upload & Resume Storage (Spring Boot Multipart & Security) — Complete Guide

---

## 🧠 1. The Big Picture: What Did We Build?

In **Day 10**, we implemented an enterprise-grade, secure file upload system for candidate resumes:

1. **Spring Boot Multipart Configuration**:
   - Configured `max-file-size` (5MB) and `max-request-size` (10MB) in `application.yml`.
   - Set up an external file storage root directory (`uploads/resumes`) separated from static web assets.

2. **Secure `FileStorageService`**:
   - **Path Traversal Defense**: Sanitizes file paths using `StringUtils.cleanPath()` and blocks dangerous characters (`..`, `/`, `\`).
   - **Extension Whitelisting**: Allows only valid resume extensions (`.pdf`, `.doc`, `.docx`).
   - **Collision-Proof UUID Renaming**: Every uploaded file is prepended with a random `UUID` to prevent overwriting files with identical names and to stop attackers from guessing filenames.
   - **Automated Directory Provisioning**: Creates the upload directory automatically on system startup if it doesn't exist.

3. **Secure File Streaming Controller (`FileController`)**:
   - Provides `GET /files/resumes/{filename:.+}` for authenticated Candidates, Recruiters, and Admins.
   - Detects MIME type dynamically using `ServletContext.getMimeType()` with octet-stream fallback.
   - Uses `Content-Disposition: inline` so PDFs preview directly in the browser while still enabling one-click download.

4. **Candidate & HR Integration**:
   - **Candidate Profile (`/candidate/profile`)**:
     - Modern file upload widget with `enctype="multipart/form-data"`.
     - Displays current active resume badge with direct preview link.
     - Allows instant replacement of old resume documents (automatically cleans up previous files from disk).
   - **Candidate Applications (`/candidate/applications`)**: Displays an attached resume badge on each submitted job application.
   - **HR Applicants Portal (`/hr/jobs/{jobId}/applications`)**: Provides recruiters with a direct **"View / Download Resume"** button for each applicant.

---

## 📋 2. Day 10 Tasks Completed

- [x] **Step 1**: Configured servlet multipart limits and upload directory in `application.yml`.
- [x] **Step 2**: Created custom `FileStorageException` for file handling errors.
- [x] **Step 3**: Built `FileStorageService` with strict validation, path traversal defense, and UUID renaming.
- [x] **Step 4**: Built `FileController` with secure `GET /files/resumes/{filename:.+}` and inline streaming.
- [x] **Step 5**: Updated `SecurityConfig` to protect `/files/resumes/**` with RBAC and make `/jobs/**` public.
- [x] **Step 6**: Enhanced `CandidateController` and `CandidateService` to process `MultipartFile` uploads.
- [x] **Step 7**: Upgraded `candidate/profile.html` with a modern upload widget, active resume badge, and preview link.
- [x] **Step 8**: Updated `hr/applications.html` with direct candidate resume inspection buttons.
- [x] **Step 9**: Seeded a sample resume PDF in `DataInitializer` for testing.
- [x] **Step 10**: Verified build compilation (`.\mvnw.cmd compile -q` ➔ Clean build).
- [x] **Step 11**: Created permanent documentation (`docs/DAY_10_GUIDE.md`).

---

## 🛡️ 3. Security Deep Dive: File Upload Best Practices

Uploading files to a web server is historically one of the most attacked features in web applications. Here are the five security defenses we implemented:

### 1. Defense Against Path Traversal (Directory Traversal)
- **The Threat**: An attacker uploads a file named `../../../../Windows/System32/evil.dll` or `../../src/main/resources/application.yml`. If saved naively, the application will overwrite system files or source code.
- **Our Defense**:
  ```java
  String cleanFilename = StringUtils.cleanPath(file.getOriginalFilename());
  if (cleanFilename.contains("..") || cleanFilename.contains("/") || cleanFilename.contains("\\")) {
      throw new FileStorageException("Filename contains invalid path characters: " + cleanFilename);
  }
  ```
  And when loading the file:
  ```java
  Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
  if (!filePath.startsWith(this.fileStorageLocation)) {
      throw new FileStorageException("Access denied: File is outside permitted directory.");
  }
  ```

### 2. Extension Whitelisting (Never Blacklisting)
- **The Threat**: Blacklisting only `.exe` allows attackers to upload `.jsp`, `.sh`, `.php`, `.phtml`, or `.bat` files.
- **Our Defense**: We explicitly whitelist only allowed extensions:
  ```java
  private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "doc", "docx");
  ```

### 3. Collision-Proof UUID Renaming
- **The Threat**: If two candidates upload `resume.pdf`, one overwrites the other. Furthermore, predictable file names allow attackers to enumerate and view other candidates' resumes.
- **Our Defense**:
  ```java
  String uniqueFileName = UUID.randomUUID() + "_" + originalFilename.replaceAll("\\s+", "_");
  ```

### 4. Separate Upload Storage vs. Web Root
- **The Threat**: Storing uploaded files inside `src/main/resources/static/` allows them to be executed as web scripts or served without security checks.
- **Our Defense**: Files are stored in an external `uploads/resumes` directory on disk. They are served exclusively through `FileController`, which requires Spring Security authentication.

---

## 🎯 4. Top Interview Questions on File Uploads & Spring Boot

### Q1: How does Spring Boot process file uploads internally?
> **Answer:**
> *"When a browser submits an HTTP `POST` request with `Content-Type: multipart/form-data`, Spring Boot's `StandardServletMultipartResolver` parses the multi-part boundary stream.
> 
> It splits the request into form fields (strings, numbers) and file parts (`Part` or `MultipartFile`). If the file size exceeds `spring.servlet.multipart.max-file-size`, Spring throws a `MaxUploadSizeExceededException` before the file is even read into application memory, protecting the server against denial-of-service (DoS) memory exhaustion."*

---

### Q2: Why should we never store uploaded files directly in the database as a `BLOB`?
> **Answer:**
> *"Storing binary files (like 5MB PDFs or images) directly inside database tables as `BLOB` columns leads to several major issues:
> 1. **Database Bloat**: Database backups, migrations, and index rebuilds become painfully slow and massive in size.
> 2. **Database Connection Pool Exhaustion**: Streaming large binary data through JDBC keeps expensive database connections open for seconds.
> 3. **Higher Cost**: Database storage (e.g. AWS RDS SSD storage) is significantly more expensive than object storage (e.g. AWS S3, Google Cloud Storage, or disk volumes).
> 
> **Industry Best Practice**: Store the file on the file system or in Cloud Object Storage (S3 / Azure Blob / GCS), and store only the **relative file path or S3 key** as a `VARCHAR` in the database table."*

---

### Q3: What is the difference between `Content-Disposition: inline` vs `attachment`?
> **Answer:**
> - **`inline`**: Instructs the browser to render the file directly inside the browser window if supported (e.g. built-in PDF viewer or image display).
> - **`attachment`**: Tells the browser to skip display and force an immediate "Save As" download prompt on the client's machine.
> 
> For resumes, `inline` is preferred because recruiters want to quickly view the PDF in a browser tab without having to clutter their local Downloads folder."*

---

### Q4: Why does `@PathVariable` require the regex `:.+` when downloading files (e.g., `/{filename:.+}`)?
> **Answer:**
> *"By default in older Spring MVC URL matching, Spring trims everything after the last dot because it interpreted dots as format extensions (like `.json` or `.xml` for content negotiation).
> 
> If a user requests `/files/resumes/my_resume.pdf`, Spring would truncate the variable to `my_resume`, stripping `.pdf`. By specifying `{filename:.+}`, we explicitly instruct Spring's URL regex pattern matcher to include all characters including dots in the path variable."*

---

### Q5: How would you transition local disk storage to Cloud Storage (AWS S3) in production?
> **Answer:**
> *"We would create a `StorageService` interface with methods:
> ```java
> public interface StorageService {
>     String storeFile(MultipartFile file);
>     Resource loadFile(String fileName);
>     boolean deleteFile(String fileName);
> }
> ```
> 1. `LocalStorageServiceImpl`: Uses Java `java.nio.file` for local development.
> 2. `S3StorageServiceImpl`: Uses the AWS SDK `S3Client.putObject()` to upload files to an S3 bucket and returns the generated S3 object key or pre-signed URL.
> 
> Using Spring's `@Profile("dev")` and `@Profile("prod")`, the application effortlessly swaps the storage implementation with zero changes to controllers or business logic."*

---

## 🚀 Next Milestone: Day 11
In **Day 11**, we will build **Interview Scheduling, Status Workflows & In-App / Email Notifications**:
- Scheduling interviews with date, time, and meeting link / venue.
- Status transition lifecycle (`APPLIED` ➔ `SHORTLISTED` ➔ `INTERVIEW_SCHEDULED` ➔ `ACCEPTED` / `REJECTED`).
- Notification entity and candidate dashboard notifications alert bell.
