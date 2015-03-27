/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.change_detection_utils;

/**
 * The Multidimensional Simple Changes which are considered.
 * @author rousakis
 */
public enum MultidimensionalSimpleChangesType {

    ADD_ATTRIBUTE, ADD_CODELIST, ADD_DIMENSION, ADD_DIMENSION_VALUE_TO_OBSERVATION,
    ADD_FACT_TABLE, ADD_INSTANCE, ADD_INSTANCE_TO_PARENT, ADD_MEASURE, ADD_MEASURE_VALUE_TO_OBSERVATION,
    ADD_OBSERVATION, ATTACH_ATTR_TO_DIMENSION, ATTACH_ATTR_TO_MEASURE, ATTACH_CODELIST_TO_DIMENSION,
    ATTACH_DATATYPE_TO_DIMENSION, ATTACH_DIMENSION_TO_FT, ATTACH_INSTANCE_TO_CODELIST,
    ATTACH_MEASURE_TO_FT, ATTACH_OBSERVATION_TO_FT, ATTACH_TYPE_TO_MEASURE, DELETE_ATTRIBUTE,
    DELETE_CODELIST, DELETE_DIMENSION, DELETE_DIMENSION_VALUE_FROM_OBSERVATION, DELETE_FACT_TABLE,
    DELETE_INSTANCE, DELETE_INSTANCE_FROM_PARENT, DELETE_MEASURE, DELETE_MEASURE_VALUE_FROM_OBSERVATION,
    DELETE_OBSERVATION, DETACH_ATTR_FROM_DIMENSION, DETACH_ATTR_FROM_MEASURE, DETACH_CODELIST_FROM_DIMENSION,
    DETACH_DATATYPE_FROM_DIMENSION, DETACH_DIMENSION_FROM_FT, DETACH_INSTANCE_FROM_CODELIST,
    DETACH_MEASURE_FROM_FT, DETACH_OBSERVATION_FROM_FT, DETACH_TYPE_FROM_MEASURE;

    public static MultidimensionalSimpleChangesType fromString(String change) {
        if (change != null) {
            for (MultidimensionalSimpleChangesType b : MultidimensionalSimpleChangesType.values()) {
                if (change.equalsIgnoreCase(b.toString())) {
                    return b;
                }
            }
        }
        return null;
    }
}
