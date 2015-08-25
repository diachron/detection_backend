/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.associations;

import java.util.HashMap;
import java.util.Properties;
import org.diachron.detection.repositories.JDBCVirtuosoRep;

/**
 *
 * @author rousakis
 */
public class AssocManager {

    private String datasetUri;
    private boolean removeExist;
    private final JDBCVirtuosoRep jdbc;
    private final HashMap<String, String> namespaces;

    public AssocManager(Properties propFile, String datasetURI, boolean removeExisting) throws Exception {
        namespaces = new HashMap<>();
        namespaces.put("co", "http://www.diachron-fp7.eu/changes/");
        namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        this.datasetUri = datasetURI;
        jdbc = new JDBCVirtuosoRep(propFile);
        removeExist = removeExisting;
    }

    public AssocManager(JDBCVirtuosoRep jdbc, String datasetURI, boolean removeExisting) throws Exception {
        namespaces = new HashMap<>();
        namespaces.put("co", "http://www.diachron-fp7.eu/changes/");
        namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        this.datasetUri = datasetURI;
        this.jdbc = jdbc;
        this.removeExist = removeExisting;
    }

    private void init(String assocGraph) {
        jdbc.addLitTriple(namespaces.get("co") + "Association", namespaces.get("rdf") + "type", namespaces.get("rdf") + "Class", assocGraph);
        jdbc.addLitTriple(namespaces.get("co") + "old_value", namespaces.get("rdf") + "type", namespaces.get("rdf") + "Property", assocGraph);
        jdbc.addLitTriple(namespaces.get("co") + "new_value", namespaces.get("rdf") + "type", namespaces.get("rdf") + "Property", assocGraph);
        jdbc.addLitTriple(namespaces.get("co") + "Association", namespaces.get("co") + "new_value", namespaces.get("rdfs") + "Resource", assocGraph);
        jdbc.addLitTriple(namespaces.get("co") + "Association", namespaces.get("co") + "old_value", namespaces.get("rdfs") + "Resource", assocGraph);
    }

    public JDBCVirtuosoRep getJdbc() {
        return jdbc;
    }

    public void terminate() {
        jdbc.terminate();
    }

    public String getAssocGraph(String oldVersion, String newVersion) {
        int start = oldVersion.lastIndexOf("/");
        if (datasetUri.endsWith("/")) {
            datasetUri = datasetUri.substring(0, datasetUri.length() - 1);
        }
        String newV = newVersion.substring(start + 1);
        String oldV = oldVersion.substring(start + 1);
        return datasetUri + "/associations/" + oldV + "-" + newV;
    }

    public String createAssocGraph(String oldVersion, String newVersion, boolean diachronicDatasets) {
        String block1, block2;
        if (!diachronicDatasets) {
            block1 = "?new_value rdfs:label ?b. \n";
            block2 = "?old_value rdfs:label ?b. \n";
        } else {
            block1 = "?r1 diachron:subject ?new_value;\n"
                    + "   diachron:hasRecordAttribute ?ratt1.\n"
                    + "?ratt1 diachron:predicate rdfs:label;\n"
                    + "      diachron:object ?b.\n";
            block2 = "?r2 diachron:subject ?old_value;\n"
                    + "   diachron:hasRecordAttribute ?ratt2.\n"
                    + "?ratt2 diachron:predicate rdfs:label;\n"
                    + "      diachron:object ?b.\n";
        }
        String assocGraph = getAssocGraph(oldVersion, newVersion);
        if (removeExist) {
            jdbc.clearGraph(assocGraph, false);
            init(assocGraph);
        }
        String sparql = "insert into <" + assocGraph + "> { \n"
                + "?assoc rdf:type co:Association; \n"
                + "       co:old_value ?old_value; \n"
                + "       co:new_value ?new_value. \n"
                + "} \n"
                + "where { \n"
                + "graph <" + newVersion + "> { \n"
                + block1
                + "} \n"
                + "graph <" + oldVersion + "> { \n"
                + block2
                + "} \n"
                + "filter (?new_value != ?old_value). \n"
                + "bind (concat (str(?old_value),str(?new_value)) as ?url)"
                + "bind(IRI(CONCAT('http://assoc/',SHA1(?url))) AS ?assoc)."
                + "}\n";
        jdbc.executeUpdateQuery("sparql " + sparql, false);
        System.out.println(oldVersion + " (" + jdbc.triplesNum(oldVersion) + " triples)");
        System.out.println(newVersion + " (" + jdbc.triplesNum(newVersion) + " triples)");
        System.out.println((jdbc.triplesNum(assocGraph) / 3) + " associations where detected.");
        System.out.println("Assoc. Graph: " + assocGraph);
        return assocGraph;
    }
}
