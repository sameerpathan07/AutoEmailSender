# Cold Mail Sender

A full-stack app to send cold emails (with your resume attached) to recruiters/HR.
Built with **Spring Boot** (backend) + **Angular** (frontend) + **MySQL**.

## Features
- **Home page**: send to a single recruiter (name optional + email), or bulk-import
  recruiters from an **Excel file** or **pasted text**, then send to everyone in the queue.
- **Daily send cap (450/day)**: shared across fresh sends and follow-ups, safely under
  Gmail's ~500/day limit. Once hit, remaining recruiters simply stay queued and go out
  automatically the next day — nothing is lost or needs to be re-triggered manually.
- **Automatic follow-ups**: a background job (runs daily at 10 AM by default) finds anyone
  emailed exactly once, 4+ days ago, and sends one follow-up automatically. *Note: this
  can't detect actual replies (no inbox access) — it follows up on a timer only.*
- **Duplicate protection**: bulk uploads skip any email already in your list and tell you
  exactly how many were skipped vs. newly added. The single-send form warns you if that
  HR was already contacted (with a "Send anyway" override) instead of silently re-sending.
- **Profile page**: upload/replace your resume PDF, set your default email subject + body
  template (supports `{{name}}` placeholder, falls back to "Hiring Manager"), and an
  optional separate follow-up template.
- **Logs page**: history of every email sent, with success/failure status.

---

## 1. Backend setup (Spring Boot)

### Prerequisites
- Java 17+
- Maven
- MySQL running locally

### Steps
1. Create a MySQL database (or let it auto-create — see `application.properties`, it uses
   `createDatabaseIfNotExist=true`).
2. Open `backend/src/main/resources/application.properties` and fill in:
   - `spring.datasource.username` / `password` — your MySQL credentials
   - `spring.mail.username` — your Gmail address
   - `spring.mail.password` — a **Gmail App Password** (NOT your normal password)
     - Enable 2-Step Verification on your Google account
     - Generate one at: https://myaccount.google.com/apppasswords
3. Run it:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
4. Backend runs at `http://localhost:8080`.

### Key endpoints
| Method | Endpoint                       | Purpose                                  |
|--------|--------------------------------|-------------------------------------------|
| GET    | `/api/profile`                 | Get your profile                          |
| PUT    | `/api/profile`                 | Update name/subject/description/sender/follow-up templates |
| POST   | `/api/profile/resume`          | Upload resume PDF (multipart `file`)      |
| GET    | `/api/recruiters`               | List all recruiters                       |
| POST   | `/api/recruiters/upload-excel` | Bulk import from Excel (skips duplicates) |
| POST   | `/api/recruiters/upload-text`  | Bulk import from pasted text (skips duplicates) |
| POST   | `/api/recruiters/send-all`     | Send to all PENDING, up to remaining daily quota |
| POST   | `/api/recruiters/send-followups` | Manually trigger a follow-up round (also runs automatically daily) |
| DELETE | `/api/recruiters/{id}`         | Remove a recruiter from the queue         |
| POST   | `/api/email/send`              | Send to one recruiter directly (blocks duplicates unless `force:true`) |
| GET    | `/api/email/logs`              | Email send history                        |
| GET    | `/api/email/quota`             | Today's send count / remaining daily quota |

### Tuning the limits (`application.properties`)
```properties
coldmail.daily-limit=450          # stay under Gmail's ~500/day cap
coldmail.followup-after-days=4    # days to wait before auto follow-up
coldmail.followup-cron=0 0 10 * * *  # when the follow-up job runs daily
```

---

## 2. Frontend setup (Angular)

### Prerequisites
- Node.js 18+
- Angular CLI (`npm install -g @angular/cli`)

### Steps
```bash
cd frontend
npm install
npm start
```
Frontend runs at `http://localhost:4200` and talks to the backend at `http://localhost:8080`.

---

## 3. Typical workflow
1. Go to **Profile** → upload your resume PDF, write your default subject + body
   (use `{{name}}` in the body, e.g. "Hi {{name}},").
2. Go to **Home**:
   - For one recruiter: type their name (optional) + email → **Send Email**.
   - For many: upload an Excel file (any column order — it auto-detects the email
     column) or paste a list like:
     ```
     John Doe, john@company.com
     jane@company.com
     Amit Verma - amit@company.com
     ```
     then click **Send to all pending**.
3. Check **Logs** to see what was sent and whether it succeeded.

## Notes / things to customize further
- This is currently single-user (no login) — it's built for your personal use.
  If you ever want to share it or use it from multiple devices with different
  identities, you'd add Spring Security + JWT (which you already have experience
  with from your Finance Tracking System / RBAC projects).
- Gmail SMTP caps free accounts at ~500 emails/day — fine for job hunting, but be
  mindful of spacing out bulk sends to avoid being flagged as spam.
- Currently resumes are stored as a BLOB in MySQL; fine at this scale, but could be
  moved to S3/Cloudinary later like your RentEasy project.
"# AutoEmailSender" 
