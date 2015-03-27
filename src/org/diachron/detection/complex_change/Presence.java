/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.complex_change;

/**
 * 
 * @author rousakis
 */
public enum Presence {

    EXISTS_IN_V1,
    EXISTS_IN_V2,
    NOT_EXISTS_IN_V1,
    NOT_EXISTS_IN_V2;

    public static Presence fromString(String presence) {
        if (presence != null) {
            for (Presence b : Presence.values()) {
                if (presence.equalsIgnoreCase(b.toString())) {
                    return b;
                }
            }
        }
        return null;
    }
}
