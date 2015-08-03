/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.repositories;

/**
 *
 * @author rousakis
 */
public class TripleString {

    private final String triple;

    public TripleString(String s, String p, String o, TripleType type) {
        if (type == TripleType.LITERAL) {
            triple = "<" + s + "> <" + p + "> \"" + o + "\"";
        } else {
            triple = "<" + s + "> <" + p + "> <" + o + ">";
        }
    }

    public String getTripleString() {
        return triple;
    }
}
