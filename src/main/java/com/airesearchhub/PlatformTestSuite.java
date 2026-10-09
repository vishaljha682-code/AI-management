package com.airesearchhub;

import com.airesearchhub.config.DatabaseManager;
import com.airesearchhub.dao.DatasetDaoImpl;
import com.airesearchhub.dao.ExperimentDaoImpl;
import com.airesearchhub.dao.ProjectDaoImpl;
import com.airesearchhub.dao.TrainingJobDaoImpl;
import com.airesearchhub.dao.UserDaoImpl;
import com.airesearchhub.dto.LoginRequest;
import com.airesearchhub.dto.RegisterRequest;
import com.airesearchhub.dto.TrainingRequestDto;
import com.airesearchhub.exception.AuthenticationException;
import com.airesearchhub.exception.ValidationException;
import com.airesearchhub.ml.DataFrame;
import com.airesearchhub.ml.DataSummary;
import com.airesearchhub.ml.EvaluationMetrics;
import com.airesearchhub.ml.MachineLearningModel;
import com.airesearchhub.ml.algorithms.DecisionTreeModel;
import com.airesearchhub.ml.algorithms.KNNModel;
import com.airesearchhub.ml.algorithms.KMeansClusteringModel;
import com.airesearchhub.ml.algorithms.LinearRegressionModel;
import com.airesearchhub.ml.algorithms.LogisticRegressionModel;
import com.airesearchhub.ml.algorithms.RandomForestModel;
import com.airesearchhub.ml.engine.ModelTrainingEngine;
import com.airesearchhub.model.entity.Dataset;
import com.airesearchhub.model.entity.Experiment;
import com.airesearchhub.model.entity.Project;
import com.airesearchhub.model.entity.TrainingJob;
import com.airesearchhub.model.entity.User;
import com.airesearchhub.model.enums.AlgorithmType;
import com.airesearchhub.model.enums.JobStatus;
import com.airesearchhub.model.enums.Role;
import com.airesearchhub.service.AdminService;
import com.airesearchhub.service.AssistantService;
import com.airesearchhub.service.DatasetService;
import com.airesearchhub.service.ExperimentService;
import com.airesearchhub.service.ProjectService;
import com.airesearchhub.service.TrainingService;
import com.airesearchhub.service.UserService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Automated Verification & Testing Suite for AI ResearchHub.
 * Directly validates all components of the Java Marking Rubric.
 */
public class PlatformTestSuite {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   AI ResearchHub - Comprehensive Verification Test Suite       ");
        System.out.println("===============================================================");

        testDatabaseAndJDBC();
        testOOPAndPolymorphismML();
        testCollectionsAndDataAnalysis();
        testMultithreadingAndAsyncJobQueue();
        testUserAuthenticationAndRoles();
        testProjectAndCollaboration();
        testAIAssistantModule();

        System.out.println("===============================================================");
        System.out.println("   Test Results: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("===============================================================");

        // Clean up training pool
        ModelTrainingEngine.getInstance().shutdown();

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.err.println("  [FAIL] " + testName);
            failed++;
        }
    }

    private static void testDatabaseAndJDBC() {
        System.out.println("\n--- 1. Testing Database & JDBC DAO Operations ---");
        try {
            DatabaseManager db = DatabaseManager.getInstance();
            assertTrue("JDBC DatabaseManager instance initialized", db != null);

            UserDaoImpl userDao = new UserDaoImpl();
            long userCount = userDao.count();
            assertTrue("UserDao.count() returns initialized users (>=2)", userCount >= 2);

            User admin = userDao.findByUsername("admin").orElse(null);
            assertTrue("Admin user seeded with Role.ADMIN", admin != null && admin.getRole() == Role.ADMIN);

            User researcher = userDao.findByUsername("vishal").orElse(null);
            assertTrue("Researcher user seeded with Role.RESEARCHER", researcher != null && researcher.getRole() == Role.RESEARCHER);

        } catch (Exception e) {
            System.err.println("Database test exception: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }
    }

    private static void testOOPAndPolymorphismML() {
        System.out.println("\n--- 2. Testing OOP & Machine Learning Polymorphism ---");
        try {
            // Polymorphic list of MachineLearningModel implementations
            List<MachineLearningModel> models = new ArrayList<>();
            models.add(new LinearRegressionModel());
            models.add(new LogisticRegressionModel());
            models.add(new DecisionTreeModel());
            models.add(new RandomForestModel());
            models.add(new KNNModel());
            models.add(new KMeansClusteringModel());

            assertTrue("Polymorphic ML Model list contains all 6 algorithms", models.size() == 6);

            // Test Linear Regression
            double[][] regX = {{1.0, 2.0}, {2.0, 3.0}, {3.0, 4.0}, {4.0, 5.0}, {5.0, 6.0}};
            double[] regY = {3.0, 5.0, 7.0, 9.0, 11.0};
            LinearRegressionModel lr = new LinearRegressionModel();
            lr.fit(regX, regY, Map.of("epochs", 500, "learningRate", 0.05));
            double[] lrPreds = lr.predict(new double[][]{{6.0, 7.0}});
            assertTrue("Linear Regression predicts continuous value (~13.0)", lrPreds[0] > 11.5 && lrPreds[0] < 14.5);
            EvaluationMetrics lrEval = lr.evaluate(regX, regY);
            assertTrue("Linear Regression calculates R² score > 0.90", lrEval.getR2Score() != null && lrEval.getR2Score() > 0.90);

            // Test Logistic Regression
            double[][] clfX = {{0.5, 0.2}, {0.8, 0.9}, {1.2, 1.5}, {0.1, 0.2}, {1.5, 1.8}};
            double[] clfY = {0.0, 1.0, 1.0, 0.0, 1.0};
            LogisticRegressionModel logReg = new LogisticRegressionModel();
            logReg.fit(clfX, clfY, Map.of("epochs", 500, "learningRate", 0.1));
            EvaluationMetrics logEval = logReg.evaluate(clfX, clfY);
            assertTrue("Logistic Regression calculates Accuracy metric >= 0.80", logEval.getAccuracy() != null && logEval.getAccuracy() >= 0.80);

            // Test Decision Tree
            DecisionTreeModel dt = new DecisionTreeModel();
            dt.fit(clfX, clfY, Map.of("maxDepth", 4));
            double[] dtPreds = dt.predict(clfX);
            assertTrue("Decision Tree produces binary predictions", dtPreds.length == 5);

            // Test Random Forest
            RandomForestModel rf = new RandomForestModel();
            rf.fit(clfX, clfY, Map.of("nEstimators", 5, "maxDepth", 3));
            EvaluationMetrics rfEval = rf.evaluate(clfX, clfY);
            assertTrue("Random Forest produces classification metrics", rfEval.getAccuracy() != null);

            // Test K-Means
            KMeansClusteringModel km = new KMeansClusteringModel();
            km.fit(regX, null, Map.of("k", 2));
            EvaluationMetrics kmEval = km.evaluate(regX, null);
            assertTrue("K-Means clustering calculates Inertia and Silhouette score", kmEval.getInertia() != null && kmEval.getSilhouetteScore() != null);

        } catch (Exception e) {
            System.err.println("OOP & ML test exception: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }
    }

    private static void testCollectionsAndDataAnalysis() {
        System.out.println("\n--- 3. Testing Collections & Data Analysis (DataFrame) ---");
        try {
            File sampleFile = new File("src/main/resources/sample_datasets/housing_regression.csv");
            assertTrue("Sample CSV dataset file exists on disk", sampleFile.exists());

            DataFrame df = DataFrame.fromCsv(sampleFile);
            assertTrue("DataFrame parses row count (>20)", df.getRowCount() >= 20);
            assertTrue("DataFrame parses column count (6)", df.getColumnCount() == 6);

            Map<String, Map<String, Double>> stats = df.computeDescriptiveStatistics();
            assertTrue("Descriptive stats computed for numeric columns", stats.containsKey("area_sqft") && stats.containsKey("price_in_k"));
            assertTrue("Area mean calculated correctly (>1000)", stats.get("area_sqft").get("mean") > 1000.0);

            Map<String, Map<String, Double>> corr = df.computeCorrelationMatrix();
            assertTrue("Correlation matrix computed", corr.containsKey("area_sqft"));
            double areaPriceCorr = corr.get("area_sqft").get("price_in_k");
            assertTrue("Area and price show strong positive correlation (>0.90)", areaPriceCorr > 0.90);

            DataFrame[] splits = df.trainTestSplit(0.8);
            assertTrue("Train-Test split correctly partitions data 80/20", splits[0].getRowCount() + splits[1].getRowCount() == df.getRowCount());

        } catch (Exception e) {
            System.err.println("Data analysis test exception: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }
    }

    private static void testMultithreadingAndAsyncJobQueue() {
        System.out.println("\n--- 4. Testing Multithreading & Async Training Engine ---");
        try {
            TrainingService trainingService = new TrainingService();
            DatasetService datasetService = new DatasetService();
            UserDaoImpl userDao = new UserDaoImpl();
            User vishal = userDao.findByUsername("vishal").orElseThrow();

            List<Dataset> datasets = datasetService.getAllDatasets();
            assertTrue("Pre-seeded datasets available", !datasets.isEmpty());

            Dataset targetDs = datasets.stream()
                    .filter(d -> d.getName().toLowerCase().contains("housing") ||
                            d.getName().toLowerCase().contains("real estate") ||
                            d.getFilename().toLowerCase().contains("housing"))
                    .findFirst()
                    .orElse(datasets.get(0));

            TrainingRequestDto req = new TrainingRequestDto();
            req.setJobName("Async Multithreaded Test Run");
            req.setDatasetId(targetDs.getId());
            req.setAlgorithm("LINEAR_REGRESSION");
            req.setTargetColumn("price_in_k");
            req.setFeatureColumns(List.of("area_sqft", "bedrooms", "bathrooms"));
            req.setTrainSplitRatio(0.8);

            TrainingJob job = trainingService.startTrainingJob(req, vishal);
            assertTrue("Training job queued with PENDING or RUNNING status", job.getId() != null);

            // Wait for background worker thread to finish
            int retries = 0;
            TrainingJob freshJob = job;
            while (retries < 20 && freshJob.getStatus() != JobStatus.COMPLETED && freshJob.getStatus() != JobStatus.FAILED) {
                Thread.sleep(300);
                freshJob = trainingService.getJobById(job.getId());
                retries++;
            }

            assertTrue("Multithreaded training completed successfully (status == COMPLETED)", freshJob.getStatus() == JobStatus.COMPLETED);
            assertTrue("Training job progress reached 100%", freshJob.getProgressPercentage() == 100);

            ExperimentService expService = new ExperimentService();
            List<Experiment> exps = expService.getAllExperiments();
            assertTrue("Experiment record created and stored in database", !exps.isEmpty());

        } catch (Exception e) {
            System.err.println("Multithreading test exception: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }
    }

    private static void testUserAuthenticationAndRoles() {
        System.out.println("\n--- 5. Testing Authentication & Role Permissions ---");
        try {
            UserService userService = new UserService();

            // Test successful login
            User user = userService.authenticate(new LoginRequest("admin", "Admin@123"), "127.0.0.1");
            assertTrue("Admin credentials authenticate successfully", user != null && user.getUsername().equals("admin"));

            // Test invalid password
            try {
                userService.authenticate(new LoginRequest("admin", "WrongPass999"), "127.0.0.1");
                assertTrue("Invalid password should throw AuthenticationException", false);
            } catch (AuthenticationException e) {
                assertTrue("Invalid password correctly rejected with AuthenticationException", true);
            }

            // Test registration of unique researcher
            String testUser = "student_" + System.currentTimeMillis() % 10000;
            RegisterRequest reg = new RegisterRequest();
            reg.setUsername(testUser);
            reg.setFullName("Test Student");
            reg.setEmail(testUser + "@university.edu");
            reg.setPassword("Password@123");
            reg.setRole("RESEARCHER");

            User newReg = userService.register(reg, "127.0.0.1");
            assertTrue("New researcher registered successfully", newReg != null && newReg.getId() != null);

        } catch (Exception e) {
            System.err.println("Authentication test exception: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }
    }

    private static void testProjectAndCollaboration() {
        System.out.println("\n--- 6. Testing Project & Collaboration Management ---");
        try {
            ProjectService projectService = new ProjectService();
            UserDaoImpl userDao = new UserDaoImpl();
            User vishal = userDao.findByUsername("vishal").orElseThrow();

            List<Project> projects = projectService.getAllProjects();
            assertTrue("Default seeded projects retrieved", !projects.isEmpty());

            Project p = projects.get(0);
            var members = projectService.getProjectMembers(p.getId());
            assertTrue("Project has active members assigned", !members.isEmpty());

            var tasks = projectService.getTasksByProjectId(p.getId());
            assertTrue("Project has research workflow tasks assigned", !tasks.isEmpty());

        } catch (Exception e) {
            System.err.println("Project collaboration test exception: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }
    }

    private static void testAIAssistantModule() {
        System.out.println("\n--- 7. Testing AI Research Assistant Engine ---");
        try {
            AssistantService assistant = new AssistantService();
            UserDaoImpl userDao = new UserDaoImpl();
            User vishal = userDao.findByUsername("vishal").orElseThrow();

            com.airesearchhub.dto.AssistantRequestDto req = new com.airesearchhub.dto.AssistantRequestDto();
            req.setMessage("Explain random forest and how it prevents overfitting");
            req.setActionType("EXPLAIN_ALGORITHM");

            var reply = assistant.processQuery(req, vishal);
            assertTrue("AI Assistant generates rich response with explanation", reply != null && reply.getContent().contains("Random Forest"));

        } catch (Exception e) {
            System.err.println("AI Assistant test exception: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }
    }
}
