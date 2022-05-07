package common;

public enum ErrorMessage {
    VARIABLE_DEFINITION_WARNING("variable defined - should not be present in sigma_out"),
    VARIABLE_DEFINITION_ERROR("variable defined but present in sigma_out"),
    VARIABLE_REFERENCED_WARNING("variable referenced - should be present in sigma_out"),
    VARIABLE_REFERENCED_ERROR("variable referenced but not present in sigma_out");

    ErrorMessage(String message) {
        this.errorMessage = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private String errorMessage;
}
