/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.associations;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author rousakis
 */
public class Association {

    private Set<String> oldValues, newValues;

    public Association() {
        oldValues = new HashSet<>();
        newValues = new HashSet<>();
    }

    public void addOldValue(String oldValue) {
        oldValues.add(oldValue);
    }

    public void addOldValues(Set<String> oldValues) {
        oldValues.addAll(oldValues);
    }

    public void addNewValue(String newValue) {
        newValues.add(newValue);
    }

    public void addNewValues(Set<String> newValues) {
        newValues.addAll(newValues);
    }

    public Set<String> getOldValues() {
        return oldValues;
    }

    public Set<String> getNewValues() {
        return newValues;
    }

    @Override
    public String toString() {
        return "oldValues=" + oldValues + ",\n"
                + "newValues=" + newValues;
    }

}
