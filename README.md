# Xpenso — Personal Expense Tracker

Xpenso is a full-stack expense tracker for managing income and expenses, monitoring financial activity, and viewing a personalized dashboard.

## Features
- User registration, login, and JWT-based authentication
- Income and expense management with categories
- Dashboard with financial summaries and charts
- Profile management with user-specific data access
- Excel reports for income and expenses, including email delivery
- Email-based account activation and profile image storage

## Tech Stack
- **Frontend:** React, Tailwind CSS
- **Backend:** Java, Spring Boot, Spring Security
- **Database:** MySQL locally, PostgreSQL in production
- **Integrations:** Brevo, Cloudinary
- **Testing & CI:** JUnit, Mockito, GitHub Actions

## Live Demo
[Open Xpenso](https://xpenso-app.netlify.app/)

## Run Locally

**Prerequisites:** Java 25, Node.js, npm, and a configured database.

1. Clone the repository.
2. Configure the backend environment variables and database.
3. Start the backend:

   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

4. In another terminal, start the frontend:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

Configure the database credentials, `JWT_SECRET`, Brevo credentials (`BREVO_API_KEY`, `BREVO_FROM_EMAIL`, `BREVO_FROM_NAME`), `XPENSO_FRONTEND_URL`, and `XPENSO_BACKEND_URL`. Set the frontend API URL according to your local backend configuration.

## CI
GitHub Actions runs backend tests and builds the frontend on pushes and pull requests to the configured branches.

## Screenshots
<img width="1763" height="805" alt="Screenshot_18-10-2026_174757_xpenso-app netlify app" src="https://github.com/user-attachments/assets/6ffd0025-350d-4cfc-bfbf-83059aff211e" />
<img width="1763" height="805" alt="Screenshot_18-10-2026_174821_xpenso-app netlify app" src="https://github.com/user-attachments/assets/3ccc0826-9375-4c7d-a7df-afe75f7f9f93" />
<img width="1763" height="805" alt="Screenshot_18-10-2026_174851_xpenso-app netlify app" src="https://github.com/user-attachments/assets/ef176260-ec1a-4b73-bcc9-b8f77e48a2fc" />
<img width="1763" height="805" alt="Screenshot_18-10-2026_17499_xpenso-app netlify app" src="https://github.com/user-attachments/assets/b4c02e59-f570-4526-97ac-ffccbb61faab" />
<img width="1910" height="872" alt="screenshot-1792326171971" src="https://github.com/user-attachments/assets/1beb7b15-d366-481d-880c-e6702a9c9922" />

