# AI ResearchHub – Java Web Marking Rubric Verification & Mapping

This document provides explicit mapping between every mark category in the **Java Web Based Projects Marking Rubric** and the exact code implementations in this repository.

---

## 1. Problem Understanding & Solution Design (8 Marks)
* **Architecture:** Enterprise Multi-Tier Layered Architecture:
  1. **Presentation / Web Tier:** `HttpServlet` hierarchy with JSON REST APIs + Responsive Single-Page Application (HTML5, Vanilla CSS Design System, HTML5 Canvas).
  2. **Service Tier:** Business logic for datasets, machine learning training, experiment tracking, AI research assistant, and administrative governance.
  3. **Machine Learning & Analytics Tier:** In-memory tabular `DataFrame` processor, statistical analysis engine, and polymorphic algorithms.
  4. **Data Access Tier:** Generic DAO Pattern (`GenericDao<T, ID>`, `BaseDao<T, ID>`) decoupling SQL persistence from domain logic.
  5. **Persistence Tier:** JDBC connection manager (`DatabaseManager`) with dual support for embedded SQLite and MySQL.
* **Domain Entities:** Model entities mapped cleanly to relational schema:
  - [`User.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/entity/User.java)
  - [`Project.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/entity/Project.java)
  - [`ProjectMember.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/entity/ProjectMember.java)
  - [`Dataset.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/entity/Dataset.java)
  - [`TrainingJob.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/entity/TrainingJob.java)
  - [`Experiment.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/entity/Experiment.java)
  - [`ProjectTask.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/entity/ProjectTask.java)
  - [`ActivityLog.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/entity/ActivityLog.java)
  - [`ResourceAllocation.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/entity/ResourceAllocation.java)
  - [`ChatMessage.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/entity/ChatMessage.java)

---

## 2. Core Java Concepts (10 Marks)
* **Strong Type System & Enums:**
  - Type-safe enumerations with custom properties and lookups: [`Role.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/enums/Role.java), [`JobStatus.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/enums/JobStatus.java), [`AlgorithmType.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/enums/AlgorithmType.java), [`TaskType.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/enums/TaskType.java), [`ProjectStatus.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/enums/ProjectStatus.java).
* **I/O & Streams:**
  - Robust file streaming and CSV line parsing with quote-handling in [`DataFrame.java`](file:///f:/AI/src/main/java/com/airesearchhub/ml/DataFrame.java).
  - Clean resource management via try-with-resources across all database statements and file streams.
* **Cryptography & Hashing:**
  - Secure SHA-256 password hashing in [`DatabaseManager.hashPassword()`](file:///f:/AI/src/main/java/com/airesearchhub/config/DatabaseManager.java).

---

## 3. OOP Implementation (10 Marks)
* **Polymorphism:**
  - Interface: [`MachineLearningModel.java`](file:///f:/AI/src/main/java/com/airesearchhub/ml/MachineLearningModel.java) defining `fit()`, `predict()`, `evaluate()`, and `getFeatureImportance()`.
  - Concrete polymorphic implementations dynamically dispatched at runtime by the training engine:
    1. [`LinearRegressionModel.java`](file:///f:/AI/src/main/java/com/airesearchhub/ml/algorithms/LinearRegressionModel.java) (Gradient Descent OLS)
    2. [`LogisticRegressionModel.java`](file:///f:/AI/src/main/java/com/airesearchhub/ml/algorithms/LogisticRegressionModel.java) (Sigmoid cross-entropy)
    3. [`DecisionTreeModel.java`](file:///f:/AI/src/main/java/com/airesearchhub/ml/algorithms/DecisionTreeModel.java) (CART algorithm with Gini impurity)
    4. [`RandomForestModel.java`](file:///f:/AI/src/main/java/com/airesearchhub/ml/algorithms/RandomForestModel.java) (Bootstrap ensemble bagging)
    5. [`KNNModel.java`](file:///f:/AI/src/main/java/com/airesearchhub/ml/algorithms/KNNModel.java) (Euclidean distance voting)
    6. [`KMeansClusteringModel.java`](file:///f:/AI/src/main/java/com/airesearchhub/ml/algorithms/KMeansClusteringModel.java) (Lloyd's iterative centroid optimization)
* **Inheritance:**
  - Base classes: [`BaseEntity.java`](file:///f:/AI/src/main/java/com/airesearchhub/model/entity/BaseEntity.java), [`BaseModel.java`](file:///f:/AI/src/main/java/com/airesearchhub/ml/BaseModel.java), [`BaseDao.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/BaseDao.java), [`BaseServlet.java`](file:///f:/AI/src/main/java/com/airesearchhub/servlet/BaseServlet.java).
* **Exception Handling Hierarchy:**
  - Custom base exception: [`AppException.java`](file:///f:/AI/src/main/java/com/airesearchhub/exception/AppException.java).
  - Derived application exceptions: [`AuthenticationException.java`](file:///f:/AI/src/main/java/com/airesearchhub/exception/AuthenticationException.java), [`AuthorizationException.java`](file:///f:/AI/src/main/java/com/airesearchhub/exception/AuthorizationException.java), [`ValidationException.java`](file:///f:/AI/src/main/java/com/airesearchhub/exception/ValidationException.java), [`ResourceNotFoundException.java`](file:///f:/AI/src/main/java/com/airesearchhub/exception/ResourceNotFoundException.java), [`ModelTrainingException.java`](file:///f:/AI/src/main/java/com/airesearchhub/exception/ModelTrainingException.java), [`DatabaseException.java`](file:///f:/AI/src/main/java/com/airesearchhub/exception/DatabaseException.java).
* **Interfaces:**
  - Extensive interface-based abstraction for DAOs (`UserDao`, `ProjectDao`, `DatasetDao`, `TrainingJobDao`, `ExperimentDao`) and Services.

---

## 4. Collections & Generics (6 Marks)
* **Generics:**
  - Generic Data Access Interface: [`GenericDao<T, ID>`](file:///f:/AI/src/main/java/com/airesearchhub/dao/GenericDao.java).
  - Generic Response Wrapper: [`ApiResponse<T>`](file:///f:/AI/src/main/java/com/airesearchhub/dto/ApiResponse.java).
  - Abstract parameter binding: [`BaseDao<T, ID>`](file:///f:/AI/src/main/java/com/airesearchhub/dao/BaseDao.java).
* **Collections Framework:**
  - `List<T>`, `ArrayList<T>`, `Collections.unmodifiableList`
  - `Map<K, V>`, `LinkedHashMap<K, V>` (used in correlation matrix and descriptive statistics)
  - `Set<String>`, `HashSet<String>` (used in duplicate row detection in `DataFrame`)
  - `ConcurrentHashMap<Long, Future<Experiment>>` (used for thread-safe active task tracking)

---

## 5. Multithreading & Synchronization (4 Marks)
* **ThreadPoolExecutor & Asynchronous Task Processing:**
  - Located in [`ModelTrainingEngine.java`](file:///f:/AI/src/main/java/com/airesearchhub/ml/engine/ModelTrainingEngine.java):
    - Multi-worker thread pool: `Executors.newFixedThreadPool(poolSize)`.
    - Asynchronous task wrapper: [`TrainingTask.java`](file:///f:/AI/src/main/java/com/airesearchhub/ml/engine/TrainingTask.java) implementing `Callable<Experiment>`.
    - Non-blocking job submission returning `Future<Experiment>`.
* **Synchronization & Thread Safety:**
  - `synchronized` methods on `submitJob()` and `cancelJob()` preventing race conditions during task registration and lifecycle transitions.
  - Thread-safe state collection using `ConcurrentHashMap<Long, Future<Experiment>>`.
  - Atomic incremental status updates (`PENDING` -> `RUNNING` 10%, 30%, 50%, 75%, 90% -> `COMPLETED` 100%).

---

## 6. Classes for Database Operations (DAO Pattern) (7 Marks)
* Comprehensive Data Access Layer adhering strictly to the DAO architectural pattern:
  - [`UserDao.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/UserDao.java) & [`UserDaoImpl.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/UserDaoImpl.java)
  - [`ProjectDao.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/ProjectDao.java) & [`ProjectDaoImpl.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/ProjectDaoImpl.java)
  - [`DatasetDao.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/DatasetDao.java) & [`DatasetDaoImpl.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/DatasetDaoImpl.java)
  - [`TrainingJobDao.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/TrainingJobDao.java) & [`TrainingJobDaoImpl.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/TrainingJobDaoImpl.java)
  - [`ExperimentDao.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/ExperimentDao.java) & [`ExperimentDaoImpl.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/ExperimentDaoImpl.java)
  - [`ProjectTaskDaoImpl.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/ProjectTaskDaoImpl.java)
  - [`ActivityLogDaoImpl.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/ActivityLogDaoImpl.java)
  - [`ResourceDaoImpl.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/ResourceDaoImpl.java)
  - [`ChatMessageDaoImpl.java`](file:///f:/AI/src/main/java/com/airesearchhub/dao/ChatMessageDaoImpl.java)

---

## 7. Database Connectivity (JDBC) & Integration (3 + 8 = 11 Marks)
* **JDBC Implementation:**
  - Manager: [`DatabaseManager.java`](file:///f:/AI/src/main/java/com/airesearchhub/config/DatabaseManager.java).
  - Explicit driver loading (`Class.forName("org.sqlite.JDBC")` and `Class.forName("com.mysql.cj.jdbc.Driver")`).
  - Parameterized `PreparedStatement` across all queries preventing SQL injection vulnerabilities.
  - Automatic `RETURN_GENERATED_KEYS` retrieval for primary key binding.
  - Zero-configuration embedded SQLite database stored in `data/ai_researchhub.db`, ready out of the box.
  - Configurable local MySQL support via `application.properties` and `.env.example`.
  - Automatic DDL schema bootstrapping and pre-seeding of default roles, initial research projects, and benchmark datasets.

---

## 8. Servlets & Web Integration (7 Marks)
* **Servlet Hierarchy:**
  - Base class: [`BaseServlet.java`](file:///f:/AI/src/main/java/com/airesearchhub/servlet/BaseServlet.java) extending `javax.servlet.http.HttpServlet` with `doGet()`, `doPost()`, `doPut()`, `doDelete()`, and `doOptions()`.
  - REST Servlets:
    1. [`AuthServlet.java`](file:///f:/AI/src/main/java/com/airesearchhub/servlet/AuthServlet.java) (`/api/auth/*`)
    2. [`AdminServlet.java`](file:///f:/AI/src/main/java/com/airesearchhub/servlet/AdminServlet.java) (`/api/admin/*`)
    3. [`ProjectServlet.java`](file:///f:/AI/src/main/java/com/airesearchhub/servlet/ProjectServlet.java) (`/api/projects/*`)
    4. [`DatasetServlet.java`](file:///f:/AI/src/main/java/com/airesearchhub/servlet/DatasetServlet.java) (`/api/datasets/*`)
    5. [`TrainingJobServlet.java`](file:///f:/AI/src/main/java/com/airesearchhub/servlet/TrainingJobServlet.java) (`/api/training-jobs/*`)
    6. [`ExperimentServlet.java`](file:///f:/AI/src/main/java/com/airesearchhub/servlet/ExperimentServlet.java) (`/api/experiments/*`)
    7. [`AssistantServlet.java`](file:///f:/AI/src/main/java/com/airesearchhub/servlet/AssistantServlet.java) (`/api/assistant/*`)
    8. [`UsageServlet.java`](file:///f:/AI/src/main/java/com/airesearchhub/servlet/UsageServlet.java) (`/api/usage/*`)
* **Web Integration:**
  - Embedded Java HTTP server integration: [`LightweightServer.java`](file:///f:/AI/src/main/java/com/airesearchhub/server/LightweightServer.java) and [`HttpServletAdapter.java`](file:///f:/AI/src/main/java/com/airesearchhub/server/HttpServletAdapter.java).
  - Modern Single-Page Dashboard Web UI ([`index.html`](file:///f:/AI/src/main/resources/web/index.html), [`style.css`](file:///f:/AI/src/main/resources/web/css/style.css), [`app.js`](file:///f:/AI/src/main/resources/web/js/app.js), [`charts.js`](file:///f:/AI/src/main/resources/web/js/charts.js)).
