# 📘 Day 4: Web Controllers, Thymeleaf Templates & UI (Complete Guide)

---

## 🧠 1. The Big Picture: Spring MVC (Model-View-Controller)

In **Day 1 to 3**, we built the **Model** (Entities, Repositories, Services).
In **Day 4**, we added the **View** and **Controller**:

```
Browser (User clicks /)
       │
       ▼
Controller (HomeController.java)
       │  Calls JobPostService.findApprovedJobs()
       ▼
Model (org.springframework.ui.Model)
       │  Puts "jobs" and "categories" into the Model map
       ▼
View (index.html with Thymeleaf)
       │  Renders dynamic HTML using th:each, th:text, th:if
       ▼
Browser displays the real, styled web page!
```

---

## 📋 2. Day 4 Tasks Completed

- [x] **Step 1**: Understand MVC and Thymeleaf architecture
- [x] **Step 2**: Create reusable UI fragments (`header.html`, `footer.html`) with Bootstrap 5
- [x] **Step 3**: Create Landing Page template (`index.html`) with search bar & dynamic job cards
- [x] **Step 4**: Create Sign-Up template (`register.html`) supporting Candidate & HR roles
- [x] **Step 5**: Create Sign-In template (`login.html`)
- [x] **Step 6**: Create `HomeController` to serve jobs, categories, and keyword/location search
- [x] **Step 7**: Create `AuthController` to handle sign-up and sign-in requests
- [x] **Step 8**: Create `DataInitializer` (seeds demo categories, HR profile, and approved jobs)
- [x] **Step 9**: Build & compile verification (`mvnw compile` ➔ BUILD SUCCESS)
- [x] **Step 10**: Create permanent guide documentation (`docs/DAY_04_GUIDE.md`)

---

## 🎨 3. Key Thymeleaf Syntax Learned

| Thymeleaf Attribute | Meaning | Example |
|---|---|---|
| `th:text="${...}"` | Replace HTML text with Java variable value | `<h5 th:text="${job.title}">Title</h5>` |
| `th:each="item : ${list}"` | Loop over a collection | `<div th:each="job : ${jobs}">...</div>` |
| `th:if="${condition}"` | Show element only if condition is true | `<div th:if="${#lists.isEmpty(jobs)}">No Jobs</div>` |
| `th:href="@{/path}"` | Context-aware link URL | `<a th:href="@{/login}">Login</a>` |
| `th:replace="~{file :: fragment}"` | Include reusable navbar/footer | `<nav th:replace="~{fragments/header :: navbar}"></nav>` |
| `th:action="@{/url}"` | Form submission endpoint | `<form th:action="@{/register}" method="post">` |

---

## 📁 4. Files Created in Day 4

### Controllers (`src/main/java/com/example/demo/controller/`)
- `HomeController.java`: Maps `GET /` with keyword/location search and category filters.
- `AuthController.java`: Maps `GET /login`, `GET /register`, and `POST /register`.

### Initializer (`src/main/java/com/example/demo/config/`)
- `DataInitializer.java`: Seeds 4 categories, 1 approved HR, and 3 active job postings on startup.

### HTML Templates (`src/main/resources/templates/`)
- `fragments/header.html`: Bootstrap 5 styling, icons, and sticky responsive navbar.
- `fragments/footer.html`: Clean dark footer with copyright and script bundles.
- `index.html`: Hero banner, search bar, category pills, and responsive job cards.
- `register.html`: Interactive role-switching registration form.
- `login.html`: Modern sign-in form with alert messages.

---

## 🚀 5. How to Run and Test in Browser

1. In terminal, run:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
2. Open browser at:
   - **`http://localhost:8080/`** ➔ View the live Homepage with Job Cards!
   - **`http://localhost:8080/register`** ➔ View the Registration form!
   - **`http://localhost:8080/login`** ➔ View the Login form!
