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
import java.util.List;
import java.util.Properties;
import org.diachron.detection.complex_change.CCManager;
import org.diachron.detection.repositories.JDBCVirtuosoRep;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author rousakis
 */
public class MCDUtils {

    private Properties propFile;
    private String changesOntologySchema;
    private String datasetURI;
    private List<String> changesOntologies;
    private ChangesDetector detector;
    private String associations;

    public MCDUtils(Properties prop, String datasetUri, String assoc) throws IOException, ClassNotFoundException, SQLException, RepositoryException {
        propFile = prop;
        this.datasetURI = datasetUri;
        String tmpUri = datasetUri;
        if (datasetUri.endsWith("/")) {
            tmpUri = datasetUri.substring(0, datasetUri.length() - 1);
        }
        this.changesOntologySchema = tmpUri + "/changes/schema";
        this.detector = new ChangesDetector(prop, null, changesOntologySchema);  //the changes ontology is null initially
        initChangesOntologies();
        associations = assoc;
    }

    public void detectDatasets(boolean complexOnly) throws Exception {
        DatasetsManager dManager = new DatasetsManager(getJDBCRepository(), datasetURI);
        List<String> versions = new ArrayList(dManager.fetchDatasetVersions().keySet());
        String[] complexChanges = {};
        for (int i = 1; i < versions.size(); i++) {
            String v1 = versions.get(i - 1);
            String v2 = versions.get(i);
            ChangesManager cManager = new ChangesManager(getJDBCRepository(), datasetURI, v1, v2, false);
            String changesOntology = cManager.getChangesOntology();
            detector.setChangesOntology(changesOntology);
            if (!complexOnly) {
                detector.detectAssociations(v1, v2, associations);
                detector.detectSimpleChanges(v1, v2, null);
            }
            detector.detectComplexChanges(v1, v2, null);
            System.out.println("-----");
        }
    }

    public boolean deleteMultipleCC(List<String> names) {
        CCManager ccDef = null;
        boolean result = false, retVal = false;
        try {
            ccDef = new CCManager(propFile, changesOntologySchema);
            for (String changesOntology : changesOntologies) {
                retVal = ccDef.deleteComplexChanges(changesOntology, names, true);
                if (retVal) {
                    result = retVal;
                }
            }
            retVal = ccDef.deleteComplexChanges(changesOntologySchema, names, false);
            if (retVal) {
                result = retVal;
            }
            if (result) {
                detectDatasets(true);
            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured .");
        }
        if (ccDef != null) {
            ccDef.terminate();
        }
        return result;
    }

    public boolean deleteCC(String name) throws Exception {
        boolean result = false, retVal = false;
        CCManager ccDef = null;
        ccDef = new CCManager(propFile, changesOntologySchema);
        for (String changesOntology : changesOntologies) {
            retVal = ccDef.deleteComplexChange(changesOntology, name, true);
            if (retVal) {
                result = retVal;
            }
        }
        retVal = ccDef.deleteComplexChange(changesOntologySchema, name, false);
        if (retVal) {
            result = retVal;
        }

        if (ccDef != null) {
            ccDef.terminate();
        }
        if (result) {
            detectDatasets(true);
        }
        return result;
    }

    private void initChangesOntologies() throws SQLException {
        StringBuilder datasetChanges = new StringBuilder();
        if (datasetURI.endsWith("/")) {
            datasetChanges.append(datasetURI.substring(0, datasetURI.length() - 1));
        } else {
            datasetChanges.append(datasetURI);
        }
        datasetChanges.append("/changes");
        this.changesOntologies = new ArrayList<>();
        String query = "select ?ontol from <http://datasets> where {\n"
                + "<" + datasetChanges + "> rdfs:member ?ontol.\n"
                + "?ontol co:old_version ?v1.\n"
                + "}  order by ?v1";
        JDBCVirtuosoRep jdbc = getJDBCRepository();
        ResultSet results = jdbc.executeSparqlQuery(query, false);
        if (results.next()) {
            do {
                this.changesOntologies.add(results.getString(1));
            } while (results.next());
        }
    }

    public JDBCVirtuosoRep getJDBCRepository() {
        return detector.getJdbc();
    }

    public void terminate() {
        this.detector.terminate();
    }

}
