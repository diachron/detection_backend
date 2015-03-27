/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.complex_change;

/**
 * This class represents a complex change parameter. Each complex change parameter 
 * is connected with a property and has a name as well. 
 * @author rousakis
 */
public class CCParameter {

    private String paramProp;
    private String paramNameString;
    private String scBoundParam;

    public CCParameter(String paramName, String scParam) {
        this.paramNameString = paramName;
        this.scBoundParam = scParam;
    }

    public String getParamNameString() {
        return paramNameString;
    }

    public void setParamNameString(String paramNameString) {
        this.paramNameString = paramNameString;
    }

    public String getParamFilter() {
        return scBoundParam;
    }

    public void setParamFilter(String paramFilter) {
        this.scBoundParam = paramFilter;
    }

    public String getParamProp() {
        return paramProp;
    }

    public void setParamProp(String paramProp) {
        this.paramProp = paramProp;
    }

    public String getScBoundParam() {
        return scBoundParam;
    }

    public void setScBoundParam(String scBoundParam) {
        this.scBoundParam = scBoundParam;
    }
}
