# AI-management: AI ResearchHub – Intelligent AI Research, Model Training and Collaboration Platform

**AI ResearchHub** is a comprehensive, production-grade Java Enterprise full-stack web application designed for researchers, machine learning engineers, students, and platform administrators. It provides an end-to-end workspace for dataset exploration and validation, multithreaded machine learning model training, experiment tracking, AI-assisted research copilot, and team project collaboration.

Developed strictly to achieve **100% full marks** on the **Java Web Based Projects Marking Rubric**.

---

## 🌟 Key Features

1. **Role-Based Access Control (RBAC):**
   - **Administrator:** Complete user management (create, activate, deactivate, assign roles, delete), compute hours and storage quota management, system audit logs, and server resource monitoring.
   - **Researcher:** Dataset upload and inspection, model configuration and training, experiment comparison, project task collaboration, and AI research assistant.

2. **Core Java Machine Learning Engine (`com.airesearchhub.ml`):**
   - Real, in-memory implementations of 6 foundational ML algorithms:
     - **Linear Regression** (Ordinary Least Squares with Multivariate Gradient Descent)
     - **Logistic Regression** (Binary/Probabilistic Classification with Cross-Entropy Loss)
     - **Decision Tree** (CART algorithm with Gini Impurity)
     - **Random Forest** (Ensemble Bagging of decorrelated trees with feature subsampling)
     - **K-Nearest Neighbors (KNN)** (Instance-based Euclidean distance voting)
     - **K-Means Clustering** (Lloyd's iterative centroid optimization and inertia convergence)
   - Real Evaluation Metrics:
     - **Classification:** Accuracy, Precision, Recall, F1-Score, Confusion Matrix ($2 \times 2$)
     - **Regression:** $R^2$ Score, Mean Absolute Error (MAE), Mean Squared Error (MSE), Root Mean Squared Error (RMSE)
     - **Clustering:** Inertia (Within-Cluster Sum of Squares), Approximate Silhouette Score, Cluster distribution

3. **In-Memory Tabular Data Analysis (`DataFrame.java`):**
   - CSV parsing with quoted-field support.
   - Descriptive statistics (Mean, Standard Deviation, Min, 25%, Median, 75%, Max).
   - Duplicate row detection and missing-value percentage calculation.
   - Pearson correlation matrix heatmap for numeric features.
   - Train-Test split partitioning.

4. **Multithreaded Asynchronous Job Queue (`ModelTrainingEngine.java`):**
   - Background worker thread pool (`ThreadPoolExecutor`).
   - Non-blocking job execution returning `Future<Experiment>`.
   - Real-time incremental progress polling (10% -> 30% -> 50% -> 75% -> 90% -> 100%).
   - Thread-safe state synchronization and cancellation support.

5. **AI Research Assistant Copilot (`AssistantService.java`):**
   - Interactive research chatbot.
   - Generates project ideas, explains algorithms, recommends suitable models for uploaded datasets, explains evaluation metrics, and generates starter Python/Java code pipelines.
   - Conversation history persistence.

6. **Collaboration & Project Kanban:**
   - Multi-user research projects.
   - Team member assignments and project roles.
   - Interactive Kanban Milestone Board (`TODO`, `IN_PROGRESS`, `REVIEW`, `DONE`) with priority badges.

---

## 🏗️ Architecture & Rubric Alignment

| Rubric Component | Marks | Implementation in Project |
| :--- | :---: | :--- |
| **Problem Understanding & Design** | 8 | Multi-tier Architecture (Presentation $\to$ Service $\to$ ML Engine $\to$ DAO $\to$ Database) |
| **Core Java Concepts** | 10 | Strong Typing, Enums, Streams, Exception hierarchy, SHA-256 Hashing |
| **OOP Implementation** | 10 | Polymorphic `MachineLearningModel`, Abstract `BaseModel`/`BaseDao`/`BaseServlet`, Custom Exceptions |
| **Collections & Generics** | 6 | `GenericDao<T, ID>`, `ApiResponse<T>`, `List`, `Map`, `Set`, `ConcurrentHashMap` |
| **Multithreading & Synchronization**| 4 | Worker thread pool, `Callable<Experiment>`, `Future`, `synchronized` progress updates |
| **Classes for Database Operations** | 7 | Complete DAO Pattern: `UserDao`, `ProjectDao`, `DatasetDao`, `TrainingJobDao`, `ExperimentDao` |
| **Database Connectivity (JDBC)** | 11 | `PreparedStatement`, Connection management, Dual SQLite / MySQL support |
| **Servlets & Web Integration** | 7 | `BaseServlet extends HttpServlet`, REST endpoints, modern responsive web UI |

*For complete file-by-file mapping, see [RUBRIC_MAPPING.md](file:///f:/AI/RUBRIC_MAPPING.md).*

---

## 🚀 Quick Start Guide

### Prerequisites
- JDK 17+ or JDK 21 (The included scripts automatically detect your system JDK or Eclipse JustJ JDK).
- Zero external database installation required (uses embedded SQLite JDBC by default).

### 1. Compile & Build the Project
Double-click `build.bat` or run in terminal:
```powershell
.\build.bat
```

### 2. Launch the Application
Double-click `run.bat` or run:
```powershell
.\run.bat
```
The application will launch on **http://localhost:8080** and automatically open in your default browser.

### 3. Run Automated Rubric Tests
Double-click `test.bat` or run:
```powershell
.\test.bat
```
Runs the 31 comprehensive verification tests validating Database, JDBC, ML Models, Data Analysis, Multithreading, and RBAC.

---

## 🔑 Default Provisioned Accounts

| Role | Username | Password | Purpose |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `Admin@123` | User CRUD, Quotas, System Audit Logs |
| **Researcher** | `vishal` | `Vishal@123` | Datasets, Model Lab, Experiments, AI Assistant |

*Note: You can instantly switch between Admin and Researcher using the quick switcher in the top navigation bar.*

---

## 📂 Project Structure

```
f:\AI\
├── src\main\java\com\airesearchhub\
│   ├── Main.java                         # Application entrypoint
│   ├── PlatformTestSuite.java             # Comprehensive 31-test verification suite
│   ├── config\                           # AppConfig, DatabaseManager (JDBC)
│   ├── dao\                              # GenericDao, BaseDao, UserDao, ProjectDao, DatasetDao...
│   ├── dto\                              # ApiResponse<T>, LoginRequest, TrainingRequestDto...
│   ├── exception\                        # Custom exception hierarchy (AppException, Validation...)
│   ├── ml\
│   │   ├── DataFrame.java                # Tabular data parser & statistical engine
│   │   ├── MachineLearningModel.java     # Polymorphic ML interface
│   │   ├── BaseModel.java                # Abstract base model with evaluation metric logic
│   │   ├── algorithms\                   # LinearRegression, Logistic, DecisionTree, RandomForest, KNN, KMeans
│   │   └── engine\                       # ModelTrainingEngine (Multithreaded ThreadPool), TrainingTask
│   ├── model\
│   │   ├── entity\                       # User, Project, Dataset, TrainingJob, Experiment, Task, ChatMessage...
│   │   └── enums\                        # Role, JobStatus, AlgorithmType, TaskType, ProjectStatus
│   ├── server\                           # LightweightServer, HttpServletAdapter
│   ├── service\                          # UserService, DatasetService, TrainingService, ExperimentService...
│   └── servlet\                          # AuthServlet, AdminServlet, DatasetServlet, TrainingJobServlet...
├── src\main\resources\
│   ├── sample_datasets\                  # housing_regression.csv, churn_classification.csv, customer_segmentation.csv
│   └── web\                              # index.html, css/style.css, js/api.js, js/charts.js, js/app.js
├── lib\                                  # sqlite-jdbc, mysql-connector-j, gson, servlet-api, slf4j
├── build.bat                             # One-click build script
├── run.bat                               # One-click start & browser launch
├── test.bat                              # One-click test runner
├── application.properties                # Configuration settings
├── RUBRIC_MAPPING.md                     # Direct mapping to examination marking rubric
└── README.md                             # Project manual
```
#   A I - m a n a g e m e n t  
 # AI-management
