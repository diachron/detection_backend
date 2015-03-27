/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.complex_change;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

/**
 * This class represents a Version Filter which is essentially an RDF triple which 
 * has a specific presence {@link Presence} on either the old or new version.
 * @author rousakis
 */
public class VersionFilter {

    private String subject, predicate, object;
    private Presence presence;

    public VersionFilter(String subject, String predicate, String object, Presence presence) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.presence = presence;
    }

    public String getObject() {
        return object;
    }

    public String getPredicate() {
        return predicate;
    }

    public Presence getPresence() {
        return presence;
    }

    public List<String> getComplexChangeParameters() {
        List<String> params = new ArrayList<>();
        //if the subject, predicate or object do not start from < or ' and do not contain :- then it is a complex change parameter
        if (!subject.contains(":-") && !subject.startsWith("<") && !subject.startsWith("'")) {
            params.add(subject);
        }
        if (!predicate.contains(":-") && !predicate.startsWith("<") && !predicate.startsWith("'")) {
            params.add(predicate);
        }
        if (!object.contains(":-") && !object.startsWith("<") && !object.startsWith("'")) {
            params.add(object);
        }
        return params;
    }

    public String getSubject() {
        return subject;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("Subject", subject);
        json.put("Predicate", predicate);
        json.put("Object", object);
        json.put("Presence", presence);
        return json;
    }

    @Override
    public String toString() {
        return "VersionFilter{" + "subject=" + subject + ", predicate=" + predicate + ", object=" + object + ", presence=" + presence + '}';
    }
}
