package com.airesearchhub.exception;

public class ModelTrainingException extends AppException {
    public ModelTrainingException(String message) {
        super(message, 422);
    }

    public ModelTrainingException(String message, Throwable cause) {
        super(message, cause, 422);
    }
}
