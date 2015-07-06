/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.diachron.detection.repositories.JDBCVirtuosoRep;

/**
 * Similar to {@link DatasetsManager} where a dataset URI is connected with its
 * versions, in this class we have a dataset changes URI which is connected with
 * its changes ontologies. Moreover, we connect each changes ontology with its
 * old and new version named graph. All these relations are stored in named
 * graph http://datasets.
 *
 * @author rousakis
 */
public class ChangesManager {

    private JDBCVirtuosoRep rep;
    private String changesOntology;
    private String datasetChanges;
    private String oldVersion, newVersion;

    /**
     * Creates a new ChangesManager instance. The constructor considers a
     * properties file which contains the credentials for Virtuoso, a dataset
     * URI along with two dataset versions of it which are going to be compared.
     *
     * @param propFile The properties file.
     * @param datasetUri The dataset URI.
     * @param oldVersion The old version of the given dataset URI.
     * @param newVersion The new version of the given dataset URI.
     * @param tempOntology A flag which denotes where the created changes
     * ontology is a temporary or not.
     * @throws Exception
     */
    public ChangesManager(Properties propFile, String datasetUri, String oldVersion, String newVersion, boolean tempOntology) throws Exception {
        this.rep = new JDBCVirtuosoRep(propFile);
        init(datasetUri, oldVersion, newVersion, tempOntology);
    }

    public ChangesManager(JDBCVirtuosoRep rep, String datasetUri, String oldVersion, String newVersion, boolean tempOntology) throws Exception {
        this.rep = rep;
        init(datasetUri, oldVersion, newVersion, tempOntology);
    }

    private void init(String datasetUri, String oldVersion, String newVersion, boolean tempOntology) {
        int start = oldVersion.lastIndexOf("/");
        if (datasetUri.endsWith("/")) {
            datasetUri = datasetUri.substring(0, datasetUri.length() - 1);
        }
        this.datasetChanges = datasetUri + "/changes";
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        String newV = newVersion.substring(start + 1);
        String oldV = oldVersion.substring(start + 1);
        String temp = "";
        if (tempOntology) {
            temp = "/temp";
        }
        this.changesOntology = datasetUri + "/changes/" + oldV + "-" + newV + temp;
        String update = "sparql insert data into <http://datasets> { \n"
                + "<" + datasetChanges + "> rdfs:member <" + changesOntology + ">.\n"
                + "<" + changesOntology + "> co:old_version <" + oldVersion + ">.\n"
                + "<" + changesOntology + "> co:new_version <" + newVersion + ">.\n" + "}";
        rep.executeUpdateQuery(update, false);
    }

    /**
     * The method checks if the given versions a corresponding dataset URI are
     * compared.
     *
     * @param datasetUri The dataset URI.
     * @param oldVersion The old version of the given dataset URI.
     * @param newVersion The new version of the given dataset URI.
     * @return True if the give versions are already compared, false otherwise.
     */
    public boolean changeDetectionExists(String datasetUri, String oldVersion, String newVersion) {
        String query = "select * from <" + changesOntology + "> where { \n"
                + "?s ?p ?o.\n"
                + "} limit 1";
        try {
            ResultSet results = rep.executeSparqlQuery(query, false);
            return results.next();
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        return false;
    }

    /**
     * Deletes the current changes ontology. Moreover it deletes the connection
     * of the changes ontology with the dataset changes URI.
     */
    public void deleteChangesOntology() {
        rep.clearGraph(changesOntology, false);
        String update = "sparql delete data from graph <http://datasets> {\n"
                + "<" + datasetChanges + "> rdfs:member <" + changesOntology + ">.\n"
                + "<" + changesOntology + "> co:old_version <" + oldVersion + ">.\n"
                + "<" + changesOntology + "> co:new_version <" + newVersion + ">.\n"
                + "}";
        rep.executeUpdateQuery(update, true);
    }

    /**
     * Returns the changes ontology.
     *
     * @return
     */
    public String getChangesOntology() {
        return this.changesOntology;
    }

    public JDBCVirtuosoRep getJDBC() {
        return rep;
    }

    /**
     * Terminates the current JDBC connection.
     */
    public void terminate() {
        rep.terminate();
    }
}
