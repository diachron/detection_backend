/*
 *@author: Yannis Roussakis, Ioannis Chrysakis
 */
package org.diachron.detection.utils;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Properties;
import org.diachron.detection.repositories.JDBCVirtuosoRep;
import org.diachron.detection.utils.ChangesManager;
import org.diachron.detection.utils.DatasetsManager;

/**
 * This class used for statistical analysis used in core visualizations
 *
 */
public class Analysis {

    /**
     * Provides statistical analysis by returning a map that contains the change
     * name as key and the change occurrences as value
     *
     * @param jdbc the JDBCVirtuosoRep objects that creates internally the
     * connection to the store
     * @param changesOntolSchema the changes ontology schema
     * @param datasetUri the selected dataset URI
     * @param v1 the selected old version
     * @param v2 the selected new version
     * @param changeType the type of changes to analyze
     * ("Simple_Change","Complex_Change" or null for both cases)
     * @param tempOntology determines if results are located in a temp ontology
     * (custom compare case)
     * @return a map that contains the change name as key and the change
     * occurrences as value
     * @throws Exception general exception
     */
    public static LinkedHashMap analyzeChanges(JDBCVirtuosoRep jdbc, String changesOntolSchema, String datasetUri, String v1, String v2, String changeType, boolean tempOntology) throws Exception {
        ChangesManager cManager = new ChangesManager(jdbc, datasetUri, v1, v2, tempOntology);
        String changesOntology = cManager.getChangesOntology();
        LinkedHashMap<String, Long> changeAnalysis = new LinkedHashMap<>();
        StringBuilder query = new StringBuilder();
        String type;
        if (changeType == null) {
            type = "?ct";
        } else {
            type = "co:" + changeType;
        }

        query.append(""
                + "select ?name count(*) as ?count where {\n"
                + "graph  <" + changesOntolSchema + ">  { \n"
                + "?sc rdfs:subClassOf " + type + "; \n"
                + "co:name ?name.\n"
                + "}\n");
        query.append("graph <" + changesOntology + ">  { \n"
                + "?dsc a ?sc.\n");
        query.append("filter not exists {?dcc co:consumes ?dsc}.\n}\n");

//        query.append("filter (?name = 'ADD_TYPE_CLASS').");
        query.append("} ");
        ResultSet res = jdbc.executeSparqlQuery(query.toString(), false);
        while (res.next()) {
            String name = res.getString("name");
            long number = Long.parseLong(res.getString("count"));
            changeAnalysis.put(name, number);
        }
        res.close();
        return changeAnalysis;
    }

    /**
     * Provides statistical analysis by returning a map that contains the change
     * name as key and the change occurrences as value
     *
     * @param prop the properties file that includes connection's credentials to
     * the store
     * @param changesOntolSchema the changes ontology schema
     * @param datasetUri the selected dataset URI
     * @param v1 the selected old version
     * @param v2 the selected new version
     * @param changeType the type of changes to analyze
     * ("Simple_Change","Complex_Change" or null for both cases)
     * @param tempOntology determines if results are located in a temp ontology
     * (custom compare case)
     * @return a map that contains the change name as key and the change
     * occurrences as value
     * @throws Exception general exception
     */
    public static LinkedHashMap analyzeChanges(Properties prop, String changesOntolSchema, String datasetUri, String v1, String v2, String changeType, boolean tempOntology) throws Exception {
        JDBCVirtuosoRep jdbc = new JDBCVirtuosoRep(prop);
        LinkedHashMap changeAnalysis = analyzeChanges(jdbc, changesOntolSchema, datasetUri, v1, v2, changeType, tempOntology);
        jdbc.terminate();
        return changeAnalysis;
    }

    public static long getChangesNum(DatasetsManager dmgr, String v1, String v2) throws Exception {
        JDBCVirtuosoRep jdbc = dmgr.getJDBCVirtuosoRep();
        String datasetUri = dmgr.getDatasetURI();
        String changesOntolSchema = dmgr.getChangesSchema();
        long sum = 0;
        ChangesManager cManager = new ChangesManager(jdbc, datasetUri, v1, v2, false);
        String changesOntology = cManager.getChangesOntology();
        StringBuilder query = new StringBuilder();
        query.append("select count(*) where {\n").append("graph  <").append(changesOntolSchema).append(">  { \n").
                append("?sc rdfs:subClassOf ?change.\n"
                        + "filter (?change in (co:Simple_Change, co:Complex_Change))\n"
                        + "} \n" + "graph  <").
                append(changesOntology).
                append(">  { ?dsc a ?sc. }\n"
                        + "}");
        ResultSet res = jdbc.executeSparqlQuery(query.toString(), false);
        while (res.next()) {
            return Long.parseLong(res.getString(1));
        }
        res.close();
        return sum;
    }

    public static LinkedHashMap<String, Long> analyzeChangesContainValue(JDBCVirtuosoRep jdbc, String changesOntolSchema, String datasetUri, String v1, String v2, String changeType, boolean tempOntology, String nodeUri) throws Exception {
        ChangesManager cManager = new ChangesManager(jdbc, datasetUri, v1, v2, tempOntology);
        String changesOntology = cManager.getChangesOntology();
        LinkedHashMap<String, Long> changeAnalysis = new LinkedHashMap<>();
        StringBuilder query = new StringBuilder();
        String type;
        if (changeType == null) {
            type = "?ct";
        } else {
            type = "co:" + changeType;
        }

        query.append("select ?name count(*) as ?count where {\n"
                + "graph  <" + changesOntolSchema + ">  { \n"
                + "?sc rdfs:subClassOf " + type + "; \n"
                + "co:name ?name.\n"
                + "}\n");
        query.append("graph <" + changesOntology + ">  { \n"
                + "?dsc a ?sc.\n"
                + "?dsc ?param ?value.\n"
                + "filter (?value = <" + nodeUri + ">).");
        query.append("filter not exists {?dcc co:consumes ?dsc}.\n}\n");

//        query.append("filter (?name = 'ADD_TYPE_CLASS').");
        query.append("} ");
        ResultSet res = jdbc.executeSparqlQuery(query.toString(), false);
        while (res.next()) {
            String name = res.getString("name");
            long number = Long.parseLong(res.getString("count"));
            changeAnalysis.put(name, number);
        }
        res.close();
        return changeAnalysis;
    }

}
