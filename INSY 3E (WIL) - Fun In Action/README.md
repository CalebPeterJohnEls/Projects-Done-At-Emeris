# Fun in Action

A sports training and coaching management system designed to support the **Fun in Action Programme**. The system allows coaches to manage players, training sessions, payments and contracts, while parents/clients can access their programme information, view payment history, make payments and sign required contracts.

The project consists of an **ASP.NET Core MVC web application**, an **ASP.NET Core Web API**, a **SQL Server database**, and an **Android mobile application prototype**.

The system includes **JWT authentication, role-based access control, password hashing, Entity Framework Core, SQL Server/Azure SQL support, payment processing, contract signing, training-session management and responsive user interfaces**.

---

## Architecture & Request Flow

Fun in Action uses a layered architecture consisting of an MVC frontend, a REST API and a SQL Server database.

```text
┌──────────────────────────────┐
│      Android Application     │
│       Kotlin / Android       │
└──────────────┬───────────────┘
               │
               │
┌──────────────▼───────────────┐
│      ASP.NET Core MVC        │
│      FunInActionMVC          │
│                             │
│ Controllers / Views / Models │
└──────────────┬───────────────┘
               │ HTTP + JWT
               │
┌──────────────▼───────────────┐
│       ASP.NET Core API       │
│       FunInActionApi         │
│                             │
│ Controllers / Services / DTOs│
└──────────────┬───────────────┘
               │
               │ Entity Framework Core
               │
┌──────────────▼───────────────┐
│        SQL Server            │
│        FunInAction DB        │
└──────────────────────────────┘
```

A typical authenticated request works as follows:

1. **User logs in** through the MVC application.
2. The MVC application sends the user's email and password to the API.
3. The API validates the credentials and creates a **JWT token**.
4. The MVC application stores the JWT token in the user's session.
5. The JWT is automatically attached to subsequent API requests.
6. The API validates the JWT and checks the user's role.
7. The requested controller performs the required operation.
8. **Entity Framework Core** communicates with the SQL Server database.
9. The API returns the result to the MVC application.
10. The MVC application displays the information to the user.

---

# Main Features

## Authentication

The system provides authentication for users through:

* User registration
* User login
* User logout
* JWT token authentication
* Password hashing
* Session-based token storage
* Role-based access control

Supported roles include:

* **Coach**
* **Client**
* **Player**

Coaches have access to management functionality, while clients/players have access to their own programme information.

---

## Coach Features

Coaches can:

* View the coach dashboard
* View and manage clients
* Add clients
* Edit client details
* Delete clients
* View payment history
* Change payment statuses
* View programme information
* View contracts
* Create training sessions
* Delete training sessions
* View their profile
* Change their password

---

## Client / Parent Features

Clients can:

* Access the client portal
* View their dashboard
* View upcoming sessions
* View payment history
* Select a payment plan
* Submit card payment requests
* Upload proof of payment
* View programme fees
* View contracts
* Sign contracts
* View and update their profile

---

## Training Sessions

The system allows coaches to manage training sessions.

Each session can contain:

* Start date and time
* End date and time
* Location
* Notes

Sessions must be scheduled for a future date and the end time must occur after the start time.

---

## Payments

The payment system supports:

* Payment history
* Programme fee information
* Full payment
* Instalment payments
* Card payment processing
* EFT payment with proof of payment
* Payment status management
* Coach approval of payments

The programme fee currently supports a total programme amount of **R1,000**, which can be paid in four equal instalments of **R250**.

The payment gateway supports two development modes:

1. **Simulation mode** when no Stripe test key is configured.
2. **Stripe test mode** when a Stripe `sk_test_...` key is configured.

Live Stripe keys are deliberately rejected by the current development implementation.

---

## Contracts

The system provides digital programme contracts such as:

* Parent / Guardian Consent Form
* Liability & Injury Waiver
* Photography & Media Consent
* Fun in Action Programme Terms
* School Facility Use Agreement

Users can open contracts, read their descriptions and sign them.

The API prevents the same user from signing the same contract more than once.

---

# Security Features

The application implements several security mechanisms.

### JWT Authentication

After successful login, the API creates a signed JWT containing information such as:

* User ID
* Email
* Full name
* Role
* Client ID where applicable

The token is then used to authenticate subsequent API requests.

### Role-Based Access Control

Different actions are restricted based on the user's role.

For example:

| Feature                 | Coach | Client / Player |
| ----------------------- | :---: | :-------------: |
| View dashboard          |   ✓   |        ✓        |
| View clients            |   ✓   |                 |
| Create client           |   ✓   |                 |
| Edit client             |   ✓   |                 |
| Delete client           |   ✓   |                 |
| View own payments       |       |        ✓        |
| View all payments       |   ✓   |                 |
| Change payment status   |   ✓   |                 |
| Submit payment          |       |        ✓        |
| Upload proof of payment |       |        ✓        |
| Sign contracts          |       |        ✓        |
| Create sessions         |   ✓   |                 |
| Delete sessions         |   ✓   |                 |
| View profile            |   ✓   |        ✓        |

### Password Hashing

Passwords are not stored as plain text. The API uses ASP.NET Core's `PasswordHasher<TUser>` to hash user passwords before they are stored.

### JWT Validation

JWT tokens are checked for:

* Valid issuer
* Valid audience
* Valid signing key
* Token expiry
* User role

### Session Security

The MVC application stores the JWT inside an ASP.NET Core session rather than exposing the token directly in the page.

The session cookie is configured as:

* HTTP-only
* Essential
* Two-hour idle timeout

### Authorisation

Protected API controllers use `[Authorize]`, while specific actions use role restrictions such as:

```csharp
[Authorize(Roles = "Coach")]
```

This prevents users from performing actions outside their assigned role.

---

# Technology Stack

## Web Application

| Technology               | Purpose                                  |
| ------------------------ | ---------------------------------------- |
| **ASP.NET Core MVC**     | Web application and user interface       |
| **C#**                   | Application programming language         |
| **Razor Views**          | Dynamic HTML pages                       |
| **ASP.NET Core Session** | Stores authenticated session information |
| **HttpClient**           | Communicates with the API                |

## API

| Technology                    | Purpose                   |
| ----------------------------- | ------------------------- |
| **ASP.NET Core Web API**      | REST API                  |
| **Entity Framework Core**     | Database access           |
| **SQL Server**                | Relational database       |
| **JWT Bearer Authentication** | API authentication        |
| **Scalar**                    | API documentation/testing |
| **OpenAPI**                   | API specification         |

## Payments

| Technology                  | Purpose                      |
| --------------------------- | ---------------------------- |
| **Stripe.NET**              | Stripe payment integration   |
| **Stripe Test Mode**        | Development payment testing  |
| **Payment Gateway Service** | Abstracts payment processing |

## Android Application

| Technology              | Purpose                      |
| ----------------------- | ---------------------------- |
| **Kotlin**              | Android programming language |
| **Android SDK**         | Mobile application framework |
| **AndroidX**            | Android support libraries    |
| **Material Components** | UI components                |
| **ConstraintLayout**    | Screen layouts               |
| **ViewPager2**          | Tutorial screens             |
| **Fragments**           | Application screens          |

---

# How to Run the Application

## Prerequisites

Install the following before running the project:

* .NET 10 SDK
* Visual Studio 2022 or a compatible .NET IDE
* SQL Server or Azure SQL
* SQL Server Management Studio or Azure Data Studio
* Android Studio for the Android project
* Git

---

# API Setup

Navigate to the API project:

```bash
cd FunInActionMVC/FunInActionApi
```

Restore the required NuGet packages:

```bash
dotnet restore
```

Build the project:

```bash
dotnet build
```

Run the API:

```bash
dotnet run
```

The development HTTPS profile runs on:

```text
https://localhost:7037
```

The HTTP profile runs on:

```text
http://localhost:5047
```

---

# Database Setup

The API uses Entity Framework Core with SQL Server.

The database connection is configured through:

```json
"ConnectionStrings": {
    "Default": "your_connection_string"
}
```

For security, the database password should **not** be committed to GitHub.

Use User Secrets or environment variables for development secrets.

For example:

```bash
dotnet user-secrets init
```

Then configure the connection string:

```bash
dotnet user-secrets set "ConnectionStrings:Default" "your_sql_server_connection_string"
```

The application automatically applies Entity Framework Core migrations when the API starts.

The database is also seeded with initial development data, including:

* A coach account
* Programme contracts

---

# JWT Configuration

The API requires a JWT signing key.

Example development configuration:

```json
"Jwt": {
    "Key": "your-development-key-at-least-32-characters",
    "Issuer": "FunInActionApi",
    "Audience": "FunInActionMvc",
    "ExpiryMinutes": 120
}
```

The JWT key must contain at least 32 characters.

For production, the key should be stored securely and not committed to source control.

---

# Stripe Configuration

Stripe configuration is stored under the `Stripe` section.

Example:

```json
"Stripe": {
    "SecretKey": "",
    "Currency": "zar"
}
```

When the Stripe secret key is empty, the application uses a simulated payment gateway.

For Stripe test payments, configure a test key:

```text
sk_test_...
```

Only Stripe test keys are accepted by the current development implementation.

**Do not use a live Stripe key in this project.**

---

# MVC Setup

Navigate to the MVC project:

```bash
cd FunInActionMVC/FunInActionMVC
```

Restore dependencies:

```bash
dotnet restore
```

Build:

```bash
dotnet build
```

Run:

```bash
dotnet run
```

The MVC application communicates with the API using the configured API base URL.

The JWT stored in the user's session is automatically attached to outgoing API requests by the `JwtAttachHandler`.

---

# API Documentation

The API uses OpenAPI and Scalar.

When the API is running, the OpenAPI document is available at:

```text
https://localhost:7037/openapi/v1.json
```

Scalar can be accessed at:

```text
https://localhost:7037/scalar
```

Scalar can be used to inspect and test the API endpoints during development.

---

# Android Application

The repository also contains the Fun in Action Android prototype.

Navigate to:

```text
Fun-in-Action-App-Demo-main/
```

Open the folder in Android Studio.

The Android application uses:

* Kotlin
* AndroidX
* Material Components
* ConstraintLayout
* ViewPager2
* Fragments

The application currently provides a mobile prototype containing:

* Splash screen
* Welcome screen
* Tutorial
* Login
* Registration
* Home
* Practice
* Progress
* Profile
* Logout

---

# Android User Flow

The Android application follows this flow:

```text
Splash Screen
      ↓
Welcome Screen
      ↓
Tutorial
      ↓
Login / Register
      ↓
Home
      ↓
Practice / Progress / Profile
```

The tutorial contains five main concepts:

1. Meet your coach
2. Skeleton tracking
3. Practising hockey stances
4. Colour-based feedback
5. Progression and certificates

The prototype also uses a bottom navigation bar for the main application screens.

---

# API Endpoints

## Authentication

### Register

```http
POST /api/auth/register
```

Registers a new user.

### Login

```http
POST /api/auth/login
```

Authenticates an existing user and returns a JWT.

---

## Clients

Base route:

```text
/api/clients
```

| Method   | Endpoint            | Description          | Access |
| -------- | ------------------- | -------------------- | ------ |
| `GET`    | `/api/clients`      | Retrieves clients    | Coach  |
| `GET`    | `/api/clients/{id}` | Retrieves one client | Coach  |
| `POST`   | `/api/clients`      | Creates a client     | Coach  |
| `PUT`    | `/api/clients/{id}` | Updates a client     | Coach  |
| `DELETE` | `/api/clients/{id}` | Deletes a client     | Coach  |

---

## Payments

Base route:

```text
/api/payments
```

| Method | Endpoint                     | Description               |
| ------ | ---------------------------- | ------------------------- |
| `GET`  | `/api/payments`              | Retrieves payment history |
| `POST` | `/api/payments/card`         | Processes a card payment  |
| `POST` | `/api/payments/proof`        | Records proof of payment  |
| `PUT`  | `/api/payments/{id}/confirm` | Confirms an EFT payment   |
| `PUT`  | `/api/payments/{id}/status`  | Changes payment status    |

Coach-only operations are protected with role-based authorisation.

---

## Programme and Contracts

Base route:

```text
/api
```

| Method | Endpoint                   | Description                         |
| ------ | -------------------------- | ----------------------------------- |
| `GET`  | `/api/payment-methods`     | Retrieves supported payment methods |
| `GET`  | `/api/programme-fee`       | Retrieves programme fee information |
| `GET`  | `/api/contracts`           | Retrieves contracts                 |
| `POST` | `/api/contracts/{id}/sign` | Signs a contract                    |

---

## Training Sessions

Base route:

```text
/api/sessions
```

| Method   | Endpoint             | Description                 |
| -------- | -------------------- | --------------------------- |
| `GET`    | `/api/sessions`      | Retrieves training sessions |
| `POST`   | `/api/sessions`      | Creates a training session  |
| `DELETE` | `/api/sessions/{id}` | Deletes a training session  |

---

## Dashboards

```text
GET /api/dashboard/coach
GET /api/dashboard/parent
```

These endpoints provide dashboard information for the different user roles.

---

## Profile

Base route:

```text
/api/profile
```

| Method | Endpoint                       | Description                          |
| ------ | ------------------------------ | ------------------------------------ |
| `GET`  | `/api/profile`                 | Retrieves the current user's profile |
| `PUT`  | `/api/profile`                 | Updates profile information          |
| `POST` | `/api/profile/change-password` | Changes the user's password          |

---

# Project Structure

```text
insy7315-2026-wil-calebpeterjohnels/
│
├── Fun-in-Action-App-Demo-main/
│   └── app/
│       └── src/
│           └── main/
│               ├── java/
│               │   └── com/example/funinactionproto/
│               │       ├── HomeFragment.kt
│               │       ├── LoginFragment.kt
│               │       ├── MainActivity.kt
│               │       ├── PracticeFragment.kt
│               │       ├── ProfileFragment.kt
│               │       ├── ProgressFragment.kt
│               │       ├── RegisterFragment.kt
│               │       ├── SplashFragment.kt
│               │       ├── TutorialFragment.kt
│               │       ├── TutorialPage.kt
│               │       ├── TutorialPagerAdapter.kt
│               │       ├── TutorialSlideFragment.kt
│               │       └── WelcomeFragment.kt
│               │
│               └── res/
│                   ├── drawable/
│                   ├── layout/
│                   ├── menu/
│                   ├── mipmap/
│                   └── values/
│
├── FunInActionMVC/
│   │
│   ├── FunInActionApi/
│   │   ├── Controllers/
│   │   │   ├── AuthController.cs
│   │   │   ├── CatalogController.cs
│   │   │   ├── ClientsController.cs
│   │   │   ├── DashboardController.cs
│   │   │   ├── PaymentsController.cs
│   │   │   ├── ProfileController.cs
│   │   │   └── SessionController.cs
│   │   │
│   │   ├── Data/
│   │   │   ├── AppDbContext.cs
│   │   │   ├── ContractSignature.cs
│   │   │   ├── DbSeeder.cs
│   │   │   ├── Entities.cs
│   │   │   └── TrainingSession.cs
│   │   │
│   │   ├── Dtos/
│   │   │   ├── Dtos.cs
│   │   │   ├── SessionDtos.cs
│   │   │   └── UpdatePaymentStatusDto.cs
│   │   │
│   │   ├── Migrations/
│   │   │
│   │   ├── Services/
│   │   │   ├── Catalog.cs
│   │   │   ├── IdGenerator.cs
│   │   │   ├── IPaymentGateway.cs
│   │   │   ├── StripePaymentGateway.cs
│   │   │   └── TokenService.cs
│   │   │
│   │   ├── Program.cs
│   │   ├── appsettings.json
│   │   └── FunInActionApi.csproj
│   │
│   ├── FunInActionMVC/
│   │   ├── Controllers/
│   │   │   ├── AccountController.cs
│   │   │   ├── ClientsController.cs
│   │   │   ├── ContractsController.cs
│   │   │   ├── HomeController.cs
│   │   │   ├── PaymentsController.cs
│   │   │   ├── PortalController.cs
│   │   │   ├── ProfileController.cs
│   │   │   └── SessionController.cs
│   │   │
│   │   ├── Filters/
│   │   │   └── RequireLoginAttribute.cs
│   │   │
│   │   ├── Models/
│   │   │   └── AccountModels.cs
│   │   │
│   │   ├── Services/
│   │   │   ├── FunInActionApiClient.cs
│   │   │   ├── IFunInActionApiClient.cs
│   │   │   ├── JwtAttachHandler.cs
│   │   │   └── MockData/
│   │   │       └── MockRepository.cs
│   │   │
│   │   ├── Views/
│   │   │   ├── Account/
│   │   │   ├── Clients/
│   │   │   ├── Contracts/
│   │   │   ├── Home/
│   │   │   ├── Payments/
│   │   │   ├── Portal/
│   │   │   ├── Profile/
│   │   │   ├── Sessions/
│   │   │   └── Shared/
│   │   │
│   │   ├── Program.cs
│   │   └── FunInActionMVC.csproj
│   │
│   └── FunInActionMVC.slnx
│
└── README.md
```

---

# Important Classes

## `AppDbContext` 

The Entity Framework Core database context manages the application's database entities, including:

* Users
* Clients
* Payments
* Contracts
* Contract signatures
* Training sessions

It also defines relationships and database constraints.

---

## `TokenService`

Responsible for creating JWT authentication tokens.

The token contains claims for:

* User ID
* Email
* Name
* Role
* Client ID

---

## `StripePaymentGateway`

Implements the payment gateway abstraction used by the API.

It supports:

* Simulated payments
* Stripe test payments
* Payment references
* Payment success/failure results

Live Stripe payments are intentionally disabled in the current implementation, before handed to the client it will be implemented. 

---

## `FunInActionApiClient`

The MVC application uses this service to communicate with the API.

It provides methods for:

* Clients
* Payments
* Contracts
* Sessions
* Dashboards
* Profiles

The MVC controllers therefore do not communicate directly with the database.

---

## `JwtAttachHandler`

Automatically retrieves the JWT stored in the MVC session and adds it to outgoing API requests as:

```text
Authorization: Bearer <token>
```

This allows authenticated MVC requests to access protected API endpoints.

---

## `RequireLoginAttribute`

The MVC application uses a custom authorization filter to protect pages.

It checks whether a user is logged in and, where required, whether the user has the appropriate role.

---

# Database Entities

The main database entities are:

### AppUser

Stores authentication and account information.

Includes:

* ID
* Email
* Full name
* Phone
* Password hash
* Role
* Client ID

### Client

Stores programme client information.

### Payment

Stores payment transactions and payment status.

### Contract

Stores programme contracts.

### ContractSignature

Stores which user has signed which contract and the signing date.

### TrainingSession

Stores scheduled programme training sessions.

---

# Payment Status

Payments can have the following statuses:

```text
Pending
Confirmed
Failed
```

Coaches can update payment statuses from the payment management screen.

---

# Contract Status

Contracts use statuses including:

```text
Pending
Signed
```

A contract becomes signed for a specific user once that user submits their agreement.

---

# Development Seed Data

The API automatically seeds initial development data when the database is empty.

This includes:

* Coach account
* Programme contracts

The coach account should be configured through the `Seed` configuration rather than committing real credentials to source control.

Example:

```json
"Seed": {
    "CoachEmail": "coach@funinaction.co.za",
    "CoachPassword": "your-development-password"
}
```

---

# Testing

The solution contains standard .NET and Android test projects.

The API and MVC projects can be built using:

```bash
dotnet build
```

The .NET test suite can be executed using:

```bash
dotnet test
```

The Android project contains:

* Unit test support
* Android instrumentation test support
* Espresso support

Android tests can be run through Android Studio or Gradle.


---

# Troubleshooting

## API cannot connect to the database

Check:

1. SQL Server/Azure SQL is running.
2. The connection string is correct.
3. The database user has the correct permissions.
4. The Azure SQL firewall allows the development machine.
5. The required Entity Framework migrations have been applied.

---

## JWT Key Error

If the API displays:

```text
Jwt:Key must be at least 32 characters.
```

provide a JWT key containing at least 32 characters.

---

## MVC Cannot Connect to API

Check:

1. The API is running.
2. The MVC `Api:BaseUrl` points to the correct API address.
3. The HTTPS development certificate is trusted.
4. The API endpoint is reachable.
5. The JWT has not expired.

---

## Stripe Payments Are Simulated

If no Stripe test key is configured, the application intentionally uses the simulated payment gateway.

This allows the application to demonstrate the payment flow without requiring a Stripe account.

---

# Security Notes

The following information should **never be committed to GitHub**:

* SQL Server passwords
* Azure SQL credentials
* JWT signing keys
* Stripe secret keys
* Production passwords
* Other application secrets

Use:

* .NET User Secrets
* Environment variables
* Azure configuration
* Another secure secrets-management solution

for sensitive configuration.

---

# Future Improvements

Possible future improvements include:

* Connecting the Android application directly to the API
* Implementing real-time camera/skeleton tracking
* Expanding player progress tracking
* Adding automated certificates
* Adding richer coaching feedback
* Adding production-ready Stripe checkout
* Adding email notifications
* Adding push notifications
* Adding automated payment reminders
* Adding more comprehensive automated testing
* Deploying the API and MVC application to Azure
* Improving production logging and monitoring

---

# Project Purpose

Fun in Action is designed to provide a central digital platform for managing a sports training programme.

The application brings together:

* **Coaching management**
* **Player/client management**
* **Training sessions**
* **Programme payments**
* **Digital contracts**
* **Authentication**
* **Progress and feedback concepts**
* **Mobile application support**

The goal is to reduce manual administration while giving coaches and clients a simple way to manage their participation in the Fun in Action Programme.

---

# License

Android Developers. 2026a. CameraX architecture. Available at: <https://developer.android.com/media/camera/camerax/architecture> [Accessed 3 October 2026] 

Android Developers. 2026b. Configuration options | CameraX. Available at: <https://developer.android.com/media/camera/camerax/configuration> [Accessed 3 October 2026]

Android Developers. 2026c. Migrate from Camera2 to CameraX. Available at: <https://developer.android.com/agents/skills/camera/camerax/references/camera2-to-camerax >[Accessed 3 October 2026]

Android Developers. 2026d. Fragment | Android Developers. Available at: <https://developer.android.com/reference/androidx/fragment/app/Fragment> [Accessed 3 October 2026]

Android Developers. 2026e. Save simple data with SharedPreferences. Available at: <https://developer.android.com/training/data-storage/shared-preferences> [Accessed 5 October 2026].

Android Developers. 2026b. Implement dark theme. Available at: <https://developer.android.com/develop/ui/views/theming/darktheme> [Accessed 5 October 2026].

Google. 2026. MediaPipe Pose Landmarker. Available at: <https://ai.google.dev/edge/mediapipe/solutions/vision/pose_landmarker/android> [Accessed 3 October 2026]

Microsoft, 2026a. Controller action return types in ASP.NET Core web API. Available at: <https://learn.microsoft.com/en-us/aspnet/core/web-api/action-return-types> [Accessed: 1 October 2026].

Microsoft, 2026b. Make HTTP requests using IHttpClientFactory in ASP.NET Core. Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/http-requests> [Accessed: 1 October 2026].

Microsoft, 2026c. Prevent Cross-Site Request Forgery (XSRF/CSRF) attacks in ASP.NET Core. Available at: <https://learn.microsoft.com/en-us/aspnet/core/security/anti-request-forgery> [Accessed: 1 October 2026].

Microsoft, 2026d. Role-based authorization in ASP.NET Core. Available at: <https://learn.microsoft.com/en-us/aspnet/core/security/authorization/roles> [Accessed: 1 October 2026].

Microsoft, 2026e. Routing in ASP.NET Core. Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/routing> [Accessed: 1 October 2026].

Microsoft, 2026f. Tag Helpers in forms in ASP.NET Core. Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/views/working-with-forms> [Accessed: 1 October 2026].

Microsoft, 2026g. Saving data - EF Core. Available at: <https://learn.microsoft.com/en-us/ef/core/saving/> [Accessed: 1 October 2026].

Microsoft, 2026h. Configure JWT bearer authentication in ASP.NET Core. Available at: <https://learn.microsoft.com/en-us/aspnet/core/security/authentication/> [Accessed: 5 October 2026].

MDN Web Docs, 2026a. backface-visibility. Available at: <https://developer.mozilla.org/en-US/docs/Web/CSS/backface-visibility> [Accessed: 2 October 2026].

MDN Web Docs, 2026b. transform-style. Available at: <https://developer.mozilla.org/en-US/docs/Web/CSS/transform-style> [Accessed: 2 October 2026].

MDN Web Docs, 2026c. perspective. Available at: <https://developer.mozilla.org/en-US/docs/Web/CSS/perspective> [Accessed: 2 October 2026].

MDN Web Docs, 2026d. Using CSS transitions. Available at: <https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_transitions/Using_CSS_transitions> [Accessed: 2 October 2026].

Square, 2026. Retrofit. Available at: <https://square.github.io/retrofit/> [Accessed: 5 October 2026].

Stripe, 2026a. Accept a payment. Available at: <https://docs.stripe.com/payments/accept-a-payment> [Accessed: 1 October 2026].

Stripe, 2026b. Test your integration. Available at: <https://docs.stripe.com/testing> [Accessed: 1 October 2026].

W3Schools, 2026. How TO - Flip Card. Available at: <https://www.w3schools.com/howto/howto_css_flip_card.asp> [Accessed: 1 October 2026].

---

# License

This project was developed as part of the **INSY7315 Work Integrated Learning (WIL) Portfolio of Evidence**. 

© 2026 Fun in Action.
