/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.complex_change;

import java.util.ArrayList;
import java.util.List;
import org.diachron.detection.change_detection_utils.OntologicalSimpleChangesType;
import org.json.simple.JSONObject;

/**
 * This class represents a simple change definition which is associated with the definition 
 * of a complex change. 
 * @author rous
 */
public class SCDefinition {

    private OntologicalSimpleChangesType sChangeType;
    private String sChangeUri;
    private boolean isOptional;
    private List<String> selectionFilters;
    private List<String> joinFilters;

    /**
     * Creates a new instance of a simple change definition.
     * @param sChangeType The simple change type. 
     * @param sChangeUri A simple change uri identifier.
     * @param isOptional A flag which denotes whether the simple change is optional of not. 
     */
    public SCDefinition(OntologicalSimpleChangesType sChangeType, String sChangeUri, boolean isOptional) {
        this.sChangeType = sChangeType;
        this.sChangeUri = sChangeUri;
        this.isOptional = isOptional;
        this.selectionFilters = new ArrayList<>();
        this.joinFilters = new ArrayList<>();
    }

    public OntologicalSimpleChangesType getsChangeType() {
        return sChangeType;
    }

    void setsChangeUri(String sChangeUri) {
        this.sChangeUri = sChangeUri;
    }

    public String getsChangeUri() {
        return sChangeUri;
    }

    public boolean isIsOptional() {
        return isOptional;
    }

    public List<String> getSelectionFilters() {
        return selectionFilters;
    }

    public void setSelectionFilter(String selectionFilter) {
        if (!selectionFilter.equals("") && selectionFilter != null) {
            this.selectionFilters.add(selectionFilter);
        }
    }

    public List<String> getJoinFilters() {
        return joinFilters;
    }

    public void setJoinFilter(String joinFilter) {
        if (!joinFilter.equals("") && joinFilter != null) {
            this.joinFilters.add(joinFilter);
        }
    }

    @Override
    public String toString() {
        return "\"Simple_Change\" : \"" + sChangeType + "\", \n"
                + "\"Simple_Change_Uri\" : \"" + sChangeUri + "\", \n"
                + "\"Is_Optional\" : " + isOptional + ", \n"
                + "\"Selection_Filter\" : \"" + selectionFilters + "\", \n"
                + "}";
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("Simple_Change", sChangeType);
        json.put("Simple_Change_Uri", sChangeUri);
        json.put("Is_Optional", isOptional);
        json.put("Selection_Filter", selectionFilters);
        return json;
    }
}
