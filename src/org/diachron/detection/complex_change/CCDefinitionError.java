/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.complex_change;

/**
 * This class represents the errors which may arise during the definition of a 
 * Complex Change. It has an error code and a description. 
 * @author rousakis
 */
public class CCDefinitionError {

    /**
     * Error codes which can be found on the definition of a complex change.
     */
    public static enum CODE {

        NON_UNIQUE_CC_NAME,
        NON_UNIQUE_CC_PARAM_NAME,
        NO_CC_PARAM,
        INVALID_SPARQL,
        INVALID_JSON,
        SELECTION_FILTER_ERROR,
        JOIN_FILTER_ERROR,
        CC_PARAM_FILTER_ERROR,
        NON_UNIQUE_CC_PRIORITY
    };
    private String description;
    private CODE errorCode;

    public CCDefinitionError() {
        this.errorCode = null;
        this.description = "No error exists.";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CODE getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(CODE errorCode) {
        this.errorCode = errorCode;
    }

    public String toString() {
        return "ErrorCode: " + errorCode + "\n"
                + "Description: " + description;
    }
}
