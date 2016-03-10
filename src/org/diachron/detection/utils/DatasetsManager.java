/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.utils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.diachron.detection.exploit.ChangesExploiter;
import org.diachron.detection.repositories.JDBCVirtuosoRep;
import org.diachron.detection.repositories.SesameVirtRep;
import org.openrdf.rio.RDFFormat;

/**
 * This class is responsible for the management of various dataset versions
 * which refer on a specific dataset URI. Each dataset version is stored in the
 * corresponding named graph within Virtuoso.
 *
 * @author rousakis
 */
public class DatasetsManager {

    private JDBCVirtuosoRep jdbc;
    private SesameVirtRep sesame;
    private String datasetURI;
    private final String datasetsGraph = "http://datasets";

    /**
     * Creates a new DatasetManager instance w.r.t. a properties file and a
     * dataset URI. The properties file contains the credentials for Virtuoso.
     *
     * @param prop The properties file.
     * @param datasetURI The URI of the dataset which will be considered
     * @throws Exception
     */
    public DatasetsManager(Properties prop, String datasetURI) throws Exception {
        jdbc = new JDBCVirtuosoRep(prop);
        sesame = new SesameVirtRep(prop);
        this.datasetURI = datasetURI;
    }

    public DatasetsManager(JDBCVirtuosoRep jdbc, String datasetURI) throws Exception {
        this.jdbc = jdbc;
        this.datasetURI = datasetURI;
    }

    public void setDatasetURI(String datasetURI) {
        this.datasetURI = datasetURI;
    }

    /**
     * This method copies the versions which are assigned on a dataset URI to a
     * new dataset URI.
     *
     * @param datasetDstURI The destination dataset.
     */
    public void copyVersionsToDataset(String datasetDstURI) {
        String update = "sparql insert into <" + datasetsGraph + "> { "
                + "<" + datasetDstURI + "> rdfs:member ?o."
                + "?o rdfs:label ?lab."
                + "}\n"
                + "where {"
                + "graph  <" + datasetsGraph + "> { "
                + "<" + datasetURI + "> rdfs:member ?o ."
                + "optional {?o rdfs:label ?lab.}"
                + "} }";
        jdbc.executeUpdateQuery(update, false);
        setDatasetLabel(datasetDstURI, fetchDatasetLabel(datasetURI));
    }

    public void copyChangeOntologies(String datasetSrc, String datasetDst) throws Exception {
        ChangesExploiter expl = new ChangesExploiter(jdbc, datasetSrc, true);
        for (String ontology : expl.getChangesOntologies()) {
            String newOntology;
            if (datasetSrc.endsWith("/")) {
                newOntology = ontology.replace(datasetSrc, datasetDst + "/");
            } else {
                newOntology = ontology.replace(datasetSrc, datasetDst);
            }
            if (!jdbc.graphExists(newOntology)) {
                jdbc.copyGraph(ontology, newOntology);
                String assoc = ontology.replace("/changes/", "/associations/");
                String newAssoc = newOntology.replace("/changes/", "/associations/");
                jdbc.copyGraph(assoc, newAssoc);
                jdbc.addTriple(datasetDst + "/changes", "http://www.w3.org/2000/01/rdf-schema#member", newOntology, datasetsGraph);
                String v1 = expl.fetchChangeOntologyVersions(ontology).keySet().iterator().next();
                String v2 = expl.fetchChangeOntologyVersions(ontology).get(v1);
                jdbc.addTriple(newOntology, "http://www.diachron-fp7.eu/changes/old_version", v1, datasetsGraph);
                jdbc.addTriple(newOntology, "http://www.diachron-fp7.eu/changes/new_version", v2, datasetsGraph);
            }
        }
    }

    private void copyChangeOntologiesContents(String ontologySrcBase, String ontologyDstBase) throws SQLException {
        System.out.println("Being used");
        String query = "select ?ontology from <" + datasetsGraph + "> where {\n"
                + "<" + ontologySrcBase + "> rdfs:member ?ontology. \n"
                + "}";
        ArrayList<String> ontologies = new ArrayList<>();
        ResultSet results = jdbc.executeSparqlQuery(query, false);
        try {

            do {
                if (!results.next()) {
                    return;
                }
                ontologies.add(results.getString(1));
            } while (results.next());
        } finally {
            if(results != null){results.close(); results = null;}
        }
    }

    /**
     * Deletes the relation between the given dataset version and the dataset
     * URI.
     *
     * @param versionUri The version which will be deleted.
     */
    public void deleteVersionFromDataset(String versionUri) {
        String update = "sparql delete where {\n"
                + "graph <" + datasetsGraph + "> { \n"
                + "<" + datasetURI + "> rdfs:member <" + versionUri + ">.\n"
                //+ "OPTIONAL {<" + versionUri + "> rdfs:label ?l.}\n"   //do not delete the version info
                + "}\n"
                + "}";
        jdbc.executeUpdateQuery(update, false);
    }

    public void setDatasetLabel(String datasetURI, String newLabel) {
        String update = "sparql delete where {\n"
                + "graph <" + datasetsGraph + "> { \n"
                + "<" + datasetURI + "> rdfs:label ?label.\n"
                + "}\n "
                + "}";
        jdbc.executeUpdateQuery(update, false);
        update = "sparql insert into <" + datasetsGraph + "> {\n"
                + "<" + datasetURI + "> rdfs:label '" + newLabel + "'.\n"
                + "}";
        jdbc.executeUpdateQuery(update, false);
    }

    public void setVersionLabel(String versionUri, String newLabel) {
        String update = "sparql delete where {\n"
                + "graph <" + datasetsGraph + "> {\n"
                + "<" + versionUri + "> rdfs:label ?label\n"
                + "}\n"
                + "}";
        jdbc.executeUpdateQuery(update, false);
        assignVersionToDataset(versionUri, newLabel);
    }

    /**
     * Deletes all the triples of the given named graph. Moreover, it deletes
     * all the relations which contain the given named graph URI within the
     * relations named graph.
     *
     * @param versionUri The version which will be deleted.
     * @param deleteVersionContents A flag which denotes where the version
     * contents will be deleted as well.
     * @param deleteAssocChangesOntologies A flag which denotes whether the
     * associated change ontologies will be deleted as well.
     * @throws java.sql.SQLException
     */
    public void deleteDatasetVersion(String versionUri, boolean deleteVersionContents, boolean deleteAssocChangesOntologies) throws SQLException {
        String update = "sparql delete where {\n"
                + "graph <" + datasetsGraph + "> { \n"
                + "?dataset rdfs:member <" + versionUri + ">.\n"
                + "}\n"
                + "}";
        jdbc.executeUpdateQuery(update, false);
        update = "sparql delete where {\n"
                + "graph <" + datasetsGraph + "> { \n"
                + "<" + versionUri + "> rdfs:label ?lab.\n" //do not delete the version info
                + "}\n"
                + "}";
        jdbc.executeUpdateQuery(update, false);
        if (deleteVersionContents) {
            jdbc.clearGraph(versionUri, false);
        }
        if (deleteAssocChangesOntologies) {
            deleteAssocChangesOntologies(versionUri);
        }
    }

    /**
     * Deletes a dataset from the Triplestore by deleting all its versions.
     *
     * @param deleteVersionContents A flag which denotes where the version
     * contents will be deleted as well.
     * @param deleteAssocChangesOntologies A flag which denotes whether the
     * associated change ontologies will be deleted as well.
     * @throws java.sql.SQLException
     */
    public void deleteDataset(boolean deleteVersionContents, boolean deleteAssocChangesOntologies) throws SQLException {
        Map<String, String> versions = fetchDatasetVersions();
        for (String version : versions.keySet()) {
            deleteVersionFromDataset(version);
            if (deleteVersionContents) {
                jdbc.clearGraph(version, false);
                String update = "sparql delete where {\n"
                        + "graph <" + datasetsGraph + "> { \n"
                        + "<" + version + "> ?p ?o.\n"
                        + "}\n"
                        + "}";
                jdbc.executeUpdateQuery(update, false);
            }
            if (deleteAssocChangesOntologies) {
                deleteAssocChangesOntologies(version);
            }
        }
        String query = "sparql delete where {\n"
                + "graph <" + datasetsGraph + "> {\n"
                + "<" + datasetURI + "> rdfs:label ?lab. \n"
                + "}\n"
                + "}";
        jdbc.executeUpdateQuery(query, false);
        String datasetUri = datasetURI;
        if (datasetUri.endsWith("/")) {
            datasetUri = datasetUri.substring(0, datasetUri.length() - 1);
        }
        jdbc.clearGraph(datasetUri + "/changes/schema", false);
    }

    private void deleteAssocChangesOntologies(String deletedVersionUri) throws SQLException {
        ResultSet results = null;
        try {
            String changesUri;
            if (datasetURI.endsWith("/")) {
                changesUri = datasetURI + "changes";
            } else {
                changesUri = datasetURI + "/changes";
            }
            StringBuilder query = new StringBuilder();
            query.append("select ?ontology from <" + datasetsGraph + "> where {\n"
                    + "<" + changesUri + "> rdfs:member ?ontology. \n");
            if (deletedVersionUri != null) {
                query.append("?ontology ?p <" + deletedVersionUri + ">.\n");
            }
            query.append("}");
            ArrayList<String> ontologies = new ArrayList<>();
            results = jdbc.executeSparqlQuery(query.toString(), false);
            while (results.next()) {
                ontologies.add(results.getString(1));
            }
            for (String ontology : ontologies) {
                jdbc.clearGraph(ontology, false);
                String assoc = ontology.replace("/changes/", "/associations/");
                jdbc.clearGraph(assoc, false);
            }
            query = new StringBuilder();
            query.append("sparql delete where {\n"
                    + "graph <" + datasetsGraph + "> {\n"
                    + "<" + changesUri + "> rdfs:member ?ontology. \n"
                    + "?ontology co:old_version ?old. \n"
                    + "?ontology co:new_version ?new. \n");
            if (deletedVersionUri != null) {
                query.append("filter((?new = <" + deletedVersionUri + "> && ?old != <" + deletedVersionUri + ">) || (?new != <" + deletedVersionUri + "> && ?old = <" + deletedVersionUri + ">)).");
            }
            query.append("}\n"
                    + "}");
            jdbc.executeUpdateQuery(query.toString(), false);
            return;
        } finally {
            if(results != null){results.close();}
        }
    }

    /**
     * Associates a dataset version (i.e., named graph) and its optional label
     * with a given dataset URI given as parameter. Note that the dataset
     * version must be already imported within Virtuoso. All these
     * dataset-versions relationships are stored within namedgraph
     * http://datasets.
     *
     * @param versionGraph The named graph which contains the triples of the new
     * version which will be attached.
     * @param versionLabel A human understandable label for the dataset version
     * (can be null).
     */
    public void assignVersionToDataset(String versionGraph, String versionLabel) {
        String s = "";
        if (versionLabel != null) {
            s = "<" + versionGraph + "> rdfs:label '" + versionLabel + "'.";
        }
        String update = "sparql insert data into <" + datasetsGraph + "> { "
                + "<" + datasetURI + "> rdfs:member <" + versionGraph + ">."
                + s
                + "}";
        jdbc.executeUpdateQuery(update, false);
    }

    /**
     * Associates a set of dataset versions (i.e., named graphs) and their
     * optional labels stored into a Map with a given dataset URI given as
     * parameter. Again, all the given dataset versions must be already imported
     * within Virtuoso.
     *
     * @param versionUris A map which contains the named graphs (as keys) along
     * with their corresponding values (as values)
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    public void assignVersionsToDataset(Map<String, String> versionUris) throws ClassNotFoundException, SQLException, IOException {
        for (String version : versionUris.keySet()) {
            assignVersionToDataset(version, versionUris.get(version));
        }
    }

    /**
     * Returns a Map with all the versions of the given dataset URI (as keys)
     * along with their corresponding labels (as values). The versions are
     * returned sorted from the oldest to the newest. These relations are stored
     * within named graph http://datasets.
     *
     * @return A map with the versions of the given dataset.
     */
    public Map<String, String> fetchDatasetVersions() {
        Map<String, String> versions = new LinkedHashMap<>();
        String query = "select ?version ?label from <" + datasetsGraph + "> where {\n"
                + "<" + datasetURI + "> rdfs:member ?version. \n"
                + "BIND(REPLACE(str(?version), '^.*(#|/)', \"\") AS ?num). \n"
                + "OPTIONAL {?version rdfs:label ?label.}\n"
                + "} order by xsd:float(?num)";
        ResultSet results = null;
        try {
            results = jdbc.executeSparqlQuery(query, false);
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
        } finally {
            try {if(results != null){results.close();}} catch (SQLException e) {e.printStackTrace();}
        }
        return versions;
    }

    /**
     * Returns a Map with all the datasets URIs (as keys) along with their
     * corresponding labels (as values). These datasets are stored within named
     * graph http://datasets.
     *
     * @return A map with all the datasets which are examined by the change
     * detection module.
     */
    public Map<String, String> fetchDatasets() {
        Map<String, String> datasets = new LinkedHashMap<>();
        String query = "select ?dataset ?label from <" + datasetsGraph + "> where {"
                + "?dataset rdfs:member ?o. "
                + "?dataset rdfs:label ?label."
                + "filter (!regex(?dataset, '/changes'))."
                + "} order by ?label";
        ResultSet results = null;
        try {
            results = jdbc.executeSparqlQuery(query, false);
            if (!results.next()) {
                return datasets;
            }
            do {
                String label = "";
                if (results.getString(2) != null) {
                    label = results.getString(2);
                }
                datasets.put(results.getString(1), label);
            } while (results.next());
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage());
        } finally {
            try {if(results != null){results.close();}} catch (SQLException e) {e.printStackTrace();}
        }
        return datasets;
    }

    /**
     * Returns the named graph URI of a specific dataset with label given as
     * parameter.
     *
     * @param datasetLabel The label of the dataset.
     * @return The dataset URI or null if there is no dataset found.
     */
    public String fetchDatasetUri(String datasetLabel) {
        String query = "select distinct ?s from <" + datasetsGraph + "> where {\n"
                + "?s rdfs:member ?o;\n"
                + "   rdfs:label  ?label.\n"
                + "filter (?label = '" + datasetLabel + "').\n"
                + "filter (!regex(?s, '/changes')).\n"
                + "}";
        ResultSet results = null;
        try {
            results = jdbc.executeSparqlQuery(query, false);
            if (!results.next()) {
                return null;
            }
            do {
                if (results.getString(1) != null) {
                    return results.getString(1);
                }
            } while (results.next());
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage());
        } finally {
            try {if(results != null){results.close();}} catch (SQLException e) {e.printStackTrace();}
        }
        return null;
    }

    /**
     * Returns the named graph URI of a specific version with label given as
     * parameter.
     *
     * @param versionLabel The label of the dataset version.
     * @return The version URI or null if there is no dataset found.
     */
    public String fetchVersionUri(String versionLabel) {
        String query = "select ?version from <" + datasetsGraph + "> where {\n"
                + "<" + datasetURI + "> rdfs:member ?version.\n"
                + "?version rdfs:label '" + versionLabel + "'.\n"
                + "}";
        ResultSet results = null;
        try {
            results = jdbc.executeSparqlQuery(query, false);
            if (!results.next()) {
                return null;
            }
            do {
                if (results.getString(1) != null) {
                    return results.getString(1);
                }
            } while (results.next());
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage());
        } finally {
            try {if(results != null){results.close();}} catch (SQLException e) {e.printStackTrace();}
        }
        return null;
    }

    /**
     * Returns the label of a specific dataset with URI as parameter.
     *
     * @param datasetUri The URI of the dataset.
     * @return The dataset label or null if there is no dataset found or if the
     * dataset has no label.
     */
    public String fetchDatasetLabel(String datasetUri) {
        String query = "select distinct ?label from <" + datasetsGraph + "> where {\n"
                + "?s rdfs:label  ?label.\n"
                + "filter (?s = <" + datasetUri + ">).\n"
                + "filter (!regex(?s, '/changes')).\n"
                + "}";
        ResultSet results = null;
        try {
            results = jdbc.executeSparqlQuery(query, false);
            if (!results.next()) {
                return null;
            }
            do {
                if (results.getString(1) != null) {
                    return results.getString(1);
                }
            } while (results.next());
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage());
        } finally {
            try {if(results != null){results.close();}} catch (SQLException e) {e.printStackTrace();}
        }
        return null;
    }

    /**
     * Returns a Map with the versions of the given dataset URI (as keys) along
     * with their corresponding labels (as values) ranging from version
     * <strong>start</strong> to version <strong>end</strong>. The versions are
     * returned sorted from the oldest to the newest. These relations are stored
     * within named graph http://datasets.
     *
     * @param start The starting version
     * @param end The ending version
     * @return A map with the versions of the given dataset and the given range.
     */
    public Map<String, String> fetchDatasetRangeVersions(String start, String end) {
        Map<String, String> versions = new LinkedHashMap<>();
        Double start_num = Double.parseDouble(start.substring(start.lastIndexOf("/") + 1));
        Double end_num = Double.parseDouble(end.substring(end.lastIndexOf("/") + 1));
        String query = "select ?version ?label from <" + datasetsGraph + "> where {"
                + "<" + this.datasetURI + "> rdfs:member ?version. "
                + "BIND(REPLACE(str(?version), '^.*(#|/)', \"\") AS ?num)."
                + "OPTIONAL {?version rdfs:label ?label.}"
                + "} order by xsd:double(?num)";
        ResultSet results = null;
        try {
            results = jdbc.executeSparqlQuery(query, true);
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
        } finally {
            try {if(results != null){results.close();}} catch (SQLException e) {e.printStackTrace();}
        }
        return versions;
    }

    /**
     * Returns a Map with the next version (and optional label) of the given
     * version and dataset URI.
     *
     * @param curVersion The current version
     * @return
     */
    public Map<String, String> fetchDatasetNextVersion(String curVersion) {
        Map<String, String> versions = fetchDatasetVersions();
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
     * Returns a Map with the previous version (and optional label) of the given
     * version and dataset URI.
     *
     * @param curVersion The current version
     * @return
     */
    public Map<String, String> fetchDatasetPrevVersion(String curVersion) {
        Map<String, String> versions = fetchDatasetVersions();
        Map<String, String> prevVersion = new LinkedHashMap<>();
        List<String> versionsList = new ArrayList<>(versions.keySet());
        int pos = versionsList.indexOf(curVersion);
        if (pos == 0) {
            return null;
        } else {
            prevVersion.put(versionsList.get(pos - 1), versions.get(versionsList.get(pos - 1)));
            return prevVersion;
        }
    }

    /**
     * Returns an instance of the {@link JDBCVirtuosoRep} connection.
     *
     * @return
     */
    public JDBCVirtuosoRep getJDBCVirtuosoRep() {
        return jdbc;
    }

    /**
     * Returns an instance of the {@link SesameVirtRep} connection.
     *
     * @return
     */
    public SesameVirtRep getSesameVirtRep() {
        return sesame;
    }

    /**
     * Terminates the current JDBC connection.
     */
    public void terminate() {
        if (jdbc != null) {
            jdbc.terminate();
        }
        if (sesame != null) {
            sesame.terminate();
        }
    }

    /**
     * Imports a dataset version and an attached label, expressed in an RDF
     * format (e.g., RDF/XML, N3 etc.), within a specific named graph within
     * Virtuoso. Moreover, the method attaches the inserted dataset version to a
     * specific dataset URI.
     *
     * @param versionFilename The file which contains the RDF data which will be
     * inserted.
     * @param format The RDF format of the inserted data.
     * @param versionNamedgraph The namedgraph which will host the inserted
     * data.
     * @param versionLabel A human understandable label which describes the
     * inserted dataset version.
     * @param datasetUri The dataset which is correlated with the inserted
     * dataset version.
     * @throws java.lang.Exception
     */
    public void insertDatasetVersion(String versionFilename, RDFFormat format, String versionNamedgraph, String versionLabel, String datasetUri) throws Exception {
        sesame.importFile(versionFilename, format, versionNamedgraph);
        sesame.addTriple(datasetUri, "http://www.w3.org/2000/01/rdf-schema#member", versionNamedgraph, datasetsGraph);
        if (versionLabel != null) {
            sesame.addLitTriple(versionNamedgraph, "http://www.w3.org/2000/01/rdf-schema#label", versionLabel, datasetsGraph);
        }
    }

    /**
     * Attaches a human-understandable label upon a dataset URI
     *
     * @param datasetUri The dataset which will get the label.
     * @param datasetLabel A human understandable label which describes the
     * inserted dataset.
     * @throws java.lang.Exception
     */
    public void insertDataset(String datasetUri, String datasetLabel) throws Exception {
        if (datasetLabel != null) {
            jdbc.addLitTriple(datasetUri, "http://www.w3.org/2000/01/rdf-schema#label", datasetLabel, datasetsGraph);
        }
    }

    public void insertDataset(String datasetUri, String datasetLabel, ModelType model) throws Exception {
        insertDataset(datasetUri, datasetLabel);
        String schema = getChangesSchema();
        if (model == ModelType.ONTOLOGICAL) {
            sesame.importFile("input\\changes_ontology\\ontological\\ChangesOntologySchema.ttl", RDFFormat.TURTLE, schema);
        } else if (model == ModelType.MULTIDIMENSIONAL) {
            sesame.importFile("input\\changes_ontology\\multidimensional\\ChangesOntologySchema.ttl", RDFFormat.TURTLE, schema);
        }
    }

    public void dereifyDiachronVersion(String versionUri) {
        jdbc.dereifyDiachronData(versionUri, versionUri + "_ORIG");
        if (jdbc.graphExists(versionUri + "_ORIG")) {
            jdbc.clearGraph(versionUri, true);
            jdbc.renameGraph(versionUri + "_ORIG", versionUri);
        }
    }

    public String getChangesSchema() {
        if (datasetURI.endsWith("/")) {
            return datasetURI + "changes/schema";
        } else {
            return datasetURI + "/changes/schema";
        }
    }

    public String getDatasetURI() {
        return datasetURI;
    }
}
