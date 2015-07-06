/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.change_detection_utils;

/**
 * The Ontological Simple Changes which are considered.
 * @author rousakis
 */
public enum OntologicalSimpleChangesType {

    ADD_TYPE_CLASS, DELETE_TYPE_CLASS,
    ADD_TYPE_PROPERTY, DELETE_TYPE_PROPERTY,
    ADD_TYPE_TO_INDIVIDUAL, DELETE_TYPE_FROM_INDIVIDUAL,
    ADD_SUPERCLASS, DELETE_SUPERCLASS,
    ADD_SUPERPROPERTY, DELETE_SUPERPROPERTY,
    ADD_LABEL, DELETE_LABEL, ADD_COMMENT, DELETE_COMMENT,
    ADD_DOMAIN, DELETE_DOMAIN, ADD_RANGE, DELETE_RANGE,
    ADD_PROPERTY_INSTANCE, DELETE_PROPERTY_INSTANCE;
    
    public static OntologicalSimpleChangesType fromString(String change) {
        if (change != null) {
            for (OntologicalSimpleChangesType b : OntologicalSimpleChangesType.values()) {
                if (change.equalsIgnoreCase(b.toString())) {
                    return b;
                }
            }
        }
        return null;
    }
}
