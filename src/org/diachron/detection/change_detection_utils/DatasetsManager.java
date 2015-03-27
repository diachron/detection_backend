/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.change_detection_utils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.diachron.detection.repositories.JDBCVirtuosoRep;

/**
 * This class is responsible for the management of various dataset versions
 * which refer on a specific dataset URI. Each dataset version is stored in the
 * corresponding named graph within Virtuoso.
 *
 * @author rousakis
 */
public class DatasetsManager {

    private JDBCVirtuosoRep rep;

    /**
     * Creates a new DatasetManager instance w.r.t. a properties file. The
     * properties file contains the credentials for Virtuoso along with the URI
     * of the dataset which will be considered.
     *
     * @param propFile The full path of the properties file.
     * @throws Exception
     */
    public DatasetsManager(String propFile) throws Exception {
        rep = new JDBCVirtuosoRep(propFile);
    }

    public DatasetsManager(Properties propFile) throws Exception {
        rep = new JDBCVirtuosoRep(propFile);
    }

    public DatasetsManager(JDBCVirtuosoRep jdbc) throws Exception {
        rep = jdbc;
    }

    /**
     * This method copies the versions which are assigned on a dataset URI to a
     * new dataset URI.
     *
     * @param datasetSrc The source dataset whose versions will be copied.
     * @param datasetDst The destination dataset.
     */
    public void copyVersionsFromDataset(String datasetSrc, String datasetDst) {
        String update = "sparql insert into <http://datasets> { "
                + "<" + datasetDst + "> rdfs:member ?o."
                + "?o rdfs:label ?lab."
                + "}\n"
                + "where {"
                + "graph  <http://datasets> { "
                + "<" + datasetSrc + "> rdfs:member ?o ."
                + "optional {?o rdfs:label ?lab.}"
                + "} }";
        rep.executeUpdateQuery(update, false);
    }

    /**
     * Deletes the relation between the given dataset version and the dataset
     * URI.
     *
     * @param datasetUri The dataset URI whose version will be deleted.
     * @param namedgraph The version which will be deleted.
     */
    public void deleteVersionFromDataset(String datasetUri, String namedgraph) {
        String update = "sparql delete where {\n"
                + "graph <http://datasets> { \n"
                + "<" + datasetUri + "> rdfs:member <" + namedgraph + ">.\n"
                + "OPTIONAL {<" + namedgraph + "> rdfs:label ?l.}\n"
                + "}\n"
                + "}";
        rep.executeUpdateQuery(update, false);
    }

    /**
     * Deletes all the triples of the given named graph. Moreover, it deletes
     * the relation between the given dataset version and the dataset URI.
     *
     * @param datasetUri The dataset URI whose version will be deleted.
     * @param namedgraph The version which will be deleted.
     */
    public void deleteVersion(String datasetUri, String namedgraph) {
        deleteVersionFromDataset(datasetUri, namedgraph);
        rep.clearGraph(namedgraph, true);
    }

    /**
     * Associates a dataset version (i.e., named graph) and its optional label
     * with a given dataset URI given as parameter. Note that the dataset
     * version must be already imported within Virtuoso. All these
     * dataset-versions relationships are stored within namedgraph
     * http://datasets.
     *
     * @param datasetUri The dataset URI which will be connected with the new
     * dataset version.
     * @param namedgraph The named graph which contains the triples of the new
     * version which will be attached.
     * @param label A human understandable label for the dataset version (can be
     * null).
     */
    public void assignVersionToDataset(String datasetUri, String namedgraph, String label) {
        String s = "";
        if (label != null) {
            s = "<" + namedgraph + "> rdfs:label '" + label + "'.";
        }
        String update = "sparql insert data into <http://datasets> { "
                + "<" + datasetUri + "> rdfs:member <" + namedgraph + ">."
                + s
                + "}";
        rep.executeUpdateQuery(update, false);
    }

    /**
     * Associates a set of dataset versions (i.e., named graphs) and their
     * optional labels stored into a Map with a given dataset URI given as
     * parameter. Again, all the given dataset versions must be already imported
     * within Virtuoso.
     *
     * @param datasetUri The dataset URI which will be connected with the new
     * dataset versions.
     * @param versionUris A map which contains the named graphs (as keys) along
     * with their corresponding values (as values)
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    public void assignVersionsToDataset(String datasetUri, Map<String, String> versionUris) throws ClassNotFoundException, SQLException, IOException {
        for (String version : versionUris.keySet()) {
            assignVersionToDataset(datasetUri, version, versionUris.get(version));
        }
    }

    /**
     * Returns a Map with all the versions of the given dataset URI (as keys)
     * along with their corresponding labels (as values). The versions are
     * returned sorted from the oldest to the newest. These relations are stored
     * within named graph http://datasets.
     *
     * @param datasetUri The dataset URI
     * @return A map with the versions of the given dataset.
     */
    public Map<String, String> fetchDatasetVersions(String datasetUri) {
        Map<String, String> versions = new LinkedHashMap<>();
        String query = "select ?version ?label from <http://datasets> where {"
                + "<" + datasetUri + "> rdfs:member ?version. "
                + "BIND(REPLACE(str(?version), '^.*(#|/)', \"\") AS ?num). "
                + "OPTIONAL {?version rdfs:label ?label.}"
                + "} order by xsd:integer(?num)";
        try {
            ResultSet results = rep.executeSparqlQuery(query, false);
            if (!results.next()) {
                return versions;
            }
            do {
                String label = "";
                if (results.getString(2) != null) {
                    label = results.getString(2);
                }
                versions.put(results.getString(1), label);
            } while (results.next());
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        return versions;
    }

    /**
     * Returns a Map with the versions of the given dataset URI (as keys) along
     * with their corresponding labels (as values) ranging from version
     * <strong>start</strong> to version <strong>end</strong>. The versions are
     * returned sorted from the oldest to the newest. These relations are stored
     * within named graph http://datasets.
     *
     * @param datasetUri The dataset URI
     * @param start The starting version
     * @param end The ending version
     * @return A map with the versions of the given dataset and the given range.
     */
    public Map<String, String> fetchDatasetRangeVersions(String datasetUri, String start, String end) {
        Map<String, String> versions = new LinkedHashMap<>();
        Double start_num = Double.parseDouble(start.substring(start.lastIndexOf("/") + 1));
        Double end_num = Double.parseDouble(end.substring(end.lastIndexOf("/") + 1));
        String query = "select ?version ?label from <http://datasets> where {"
                + "<" + datasetUri + "> rdfs:member ?version. "
                + "BIND(REPLACE(str(?version), '^.*(#|/)', \"\") AS ?num)."
                + "OPTIONAL {?version rdfs:label ?label.}"
                + "} order by xsd:integer(?num)";
        try {
            ResultSet results = rep.executeSparqlQuery(query, false);
            if (!results.next()) {
                return versions;
            }
            do {
                String version = results.getString(1);
                Double version_num = Double.parseDouble(version.substring(version.lastIndexOf("/") + 1));
                if (version_num >= start_num && version_num <= end_num) {
                    String label = "";
                    if (results.getString(2) != null) {
                        label = results.getString(2);
                    }
                    versions.put(results.getString(1), label);
                    versions.put(version, label);
                }
            } while (results.next());
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        return versions;
    }

    /**
     * Returns a Map with the next version (and optional label) of the given
     * version and dataset URI.
     *
     * @param dataset The dataset URI
     * @param curVersion The current version
     * @return
     */
    public Map<String, String> fetchDatasetNextVersion(String dataset, String curVersion) {
        Map<String, String> versions = fetchDatasetVersions(dataset);
        Map<String, String> nextVersion = new LinkedHashMap<>();
        boolean current = false;
        for (String version : versions.keySet()) {
            if (current) {
                nextVersion.put(version, versions.get(version));
                return nextVersion;
            }
            if (version.equals(curVersion)) {
                current = true;
            }
        }
        return null;
    }

    /**
     * Returns an instance of the {@link JDBCVirtuosoRep} connection.
     *
     * @return
     */
    public JDBCVirtuosoRep getJDBCVirtuosoRep() {
        return rep;
    }

    /**
     * Terminates the current JDBC connection.
     */
    public void terminate() {
        rep.terminate();
    }
}
