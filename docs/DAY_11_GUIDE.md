# 📘 Day 11: Interview Scheduling, Application Status Lifecycle & In-App Notifications — Complete Guide

---

## 🧠 1. The Big Picture: What Did We Build?

In **Day 11**, we connected the recruiter and job-seeker workflows into a responsive, real-time feedback loop:

1. **Application Lifecycle State Machine**:
   - `APPLIED`: Candidate submits resume and optional cover note.
   - `SHORTLISTED`: Recruiter reviews qualifications and flags the candidate for consideration.
   - `INTERVIEW_SCHEDULED`: Recruiter sets meeting date, time, and video call link (Google Meet / Zoom / Venue).
   - `ACCEPTED`: Offer extended / hired.
   - `REJECTED`: Polite closure notice.

2. **Automated Notification Engine**:
   - **On Application**: Recruiter receives: `"📩 New Application: [Candidate] applied for [Job Title]"`.
   - **On Shortlist**: Candidate receives: `"⭐ Good news! Your application for [Job Title] at [Company] has been SHORTLISTED."`.
   - **On Interview**: Candidate receives: `"📅 Interview Scheduled for [Job Title] at [Company] on [Date/Time]. Mode/Link: [URL]"`.
   - **On Acceptance / Rejection**: Instant status update notification.

3. **Global View Architecture (`@ControllerAdvice`)**:
   - Created `GlobalModelAttributesAdvice` to inject `unreadNotificationCount` into **all** Thymeleaf pages automatically without repeating code across controllers.

4. **Candidate Notification Center & Interactive Navbar Bell**:
   - Navbar displays a real-time red notification badge with the unread count.
   - Dedicated notification portal (`/candidate/notifications`) to inspect alerts, mark single as read, or "Mark All as Read".
   - Candidate dashboard prominently showcases **Upcoming Scheduled Interviews** with direct **"Join Video Call"** action buttons.

---

## 📋 2. Day 11 Tasks Completed

- [x] **Step 1**: Mastered State Machine lifecycle patterns, `@ControllerAdvice`, and observer notification architecture.
- [x] **Step 2**: Enhanced `ApplicationService` to automatically dispatch notifications on application submission and status transitions.
- [x] **Step 3**: Enhanced `InterviewService` to automatically dispatch detailed notifications upon interview scheduling.
- [x] **Step 4**: Created `GlobalModelAttributesAdvice` providing `unreadNotificationCount` globally to all templates.
- [x] **Step 5**: Added notification endpoints to `CandidateController` (`/candidate/notifications`, `/read`, `/mark-all-read`).
- [x] **Step 6**: Created `templates/candidate/notifications.html` with read/unread highlighting and batch actions.
- [x] **Step 7**: Upgraded `templates/candidate/dashboard.html` with an Upcoming Scheduled Interviews section.
- [x] **Step 8**: Added an interactive Notification Bell with unread counter badge to the main navigation header.
- [x] **Step 9**: Seeded a demo shortlisted application and scheduled interview in `DataInitializer`.
- [x] **Step 10**: Verified build compilation (`.\mvnw.cmd compile -q` ➔ Clean build).
- [x] **Step 11**: Created permanent documentation (`docs/DAY_11_GUIDE.md`).

---

## 🏛️ 3. Architectural Deep Dive: `@ControllerAdvice` vs. Duplicated Code

### The Problem:
Every page in the portal needs to display the user's unread notification count on the top navbar. Without an architectural solution, you would have to add:
```java
// Duplicated across HomeController, CandidateController, HrController, etc.!
long unreadCount = notificationService.getUnreadCount(user.getId());
model.addAttribute("unreadNotificationCount", unreadCount);
```
inside every single `@GetMapping` method across 10+ controllers.

### The Spring MVC Solution: `@ControllerAdvice` + `@ModelAttribute`
```java
@ControllerAdvice
public class GlobalModelAttributesAdvice {

    private final UserService userService;
    private final NotificationService notificationService;

    @ModelAttribute("unreadNotificationCount")
    public long addUnreadNotificationCount(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                User user = userService.findByEmail(authentication.getName());
                return notificationService.getUnreadCount(user.getId());
            } catch (Exception ignored) {}
        }
        return 0L;
    }
}
```
**Why this is elegant:**
- Follows the **DRY (Don't Repeat Yourself)** principle.
- Spring MVC executes methods annotated with `@ModelAttribute` in `@ControllerAdvice` classes **before** every controller handler method.
- The model attribute `${unreadNotificationCount}` is instantly available on any Thymeleaf page rendered for an authenticated user.

---

## 🎯 4. Top Interview Questions on Notifications & Workflows

### Q1: How do you pass common attributes (like notification counts, cart totals, or company logos) to all views in Spring Boot?
> **Answer:**
> *"The standard Spring MVC solution is to use `@ControllerAdvice` with `@ModelAttribute`.
> 
> A `@ControllerAdvice` acts as an interceptor for all controllers. By defining a method annotated with `@ModelAttribute("attributeName")`, Spring MVC automatically invokes this method and adds its return value to the `Model` before rendering any view template. This eliminates code duplication across individual controllers."*

---

### Q2: Why is `@Transactional` critical when scheduling an interview?
> **Answer:**
> *"When scheduling an interview, two critical database operations must occur atomically:
> 1. Insert a new record into the `interviews` table.
> 2. Update the status column in the `applications` table to `INTERVIEW_SCHEDULED`.
> 
> If we don't use `@Transactional` and the application update succeeds but the interview insertion fails (e.g. database constraint violation), the database is left in an inconsistent state: the application claims an interview is scheduled, but no meeting record exists.
> 
> With `@Transactional`, Spring manages the database transaction boundary. If any exception occurs, all database changes in that method are automatically **rolled back**."*

---

### Q3: How does the application status workflow prevent invalid transitions in a production system?
> **Answer:**
> *"In a production enterprise system, we implement a State Machine pattern. We define a transition matrix or use Spring State Machine:
> - `APPLIED` can transition to `SHORTLISTED` or `REJECTED`.
> - `SHORTLISTED` can transition to `INTERVIEW_SCHEDULED` or `REJECTED`.
> - `INTERVIEW_SCHEDULED` can transition to `ACCEPTED` or `REJECTED`.
> - `ACCEPTED` or `REJECTED` are terminal states.
> 
> If an API client attempts an illegal transition (e.g. jumping directly from `REJECTED` to `ACCEPTED`), the service layer throws an `IllegalStateException` or custom `InvalidStatusTransitionException`."*

---

### Q4: How would you decouple notification sending from core business logic in a high-scale application?
> **Answer:**
> *"Currently, `ApplicationService` calls `NotificationService.sendNotification()` directly (service orchestration). In a high-traffic production system, this tight coupling can slow down requests if notifications involve third-party API calls (e.g., SendGrid email or Twilio SMS).
> 
> **Better Architecture — Spring Application Events & Messaging:**
> 1. When an application changes status, the service simply publishes an event:
>    ```java
>    eventPublisher.publishEvent(new ApplicationStatusChangedEvent(appId, oldStatus, newStatus));
>    ```
> 2. Independent listeners annotated with `@Async` and `@EventListener` consume the event:
>    - `InAppNotificationListener`: Writes record to the notifications table.
>    - `EmailNotificationListener`: Sends an HTML confirmation email.
>    - `SmsNotificationListener`: Dispatches an SMS alert.
> 3. For distributed microservices, we would replace local Spring events with a message broker like **Apache Kafka** or **RabbitMQ** to achieve true asynchronous decoupling and retry capabilities."*

---

### Q5: What is the difference between `@ControllerAdvice` and `@RestControllerAdvice`?
> **Answer:**
> - **`@ControllerAdvice`**: Used with traditional Spring MVC applications that return HTML views (Thymeleaf, JSP, Freemarker). Handles global model attributes and `@ExceptionHandler` methods that return error view templates.
> - **`@RestControllerAdvice`**: A convenience combination of `@ControllerAdvice` + `@ResponseBody`. Used in RESTful APIs. Any method or exception handler in this advice automatically serializes return values directly into JSON or XML payloads for HTTP response bodies."*

---

## 🚀 Next Milestone: Day 12
In **Day 12**, we will build **Email Notification Service & JavaMailSender**:
- Configuring Spring Boot Mail (`spring-boot-starter-mail`).
- Sending asynchronous emails (`@Async`) to prevent blocking user requests.
- Designing beautiful HTML email templates for welcome emails, application confirmations, and interview invitations using Thymeleaf's template engine.
