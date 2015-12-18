package org.diachron.detection.utils;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class contains methods which refer on a change detection of a specific
 * versions pair.
 *
 * @author rousakis
 */
public class SCDUtils {

    private Properties propFile;
    private String datasetURI;
    private String associations;

    /**
     * Creates a new SCDUtils instance w.r.t. a properties file given as
     * parameter and a namedgraph which contains associations among URIs.
     *
     * @param prop The path to a properties file which denotes a) the
     * credentials for a JDBC Virtuoso connection, b) the dataset URI whose
     * versions will be considered and c) the folder which contains the SPARQL
     * update queries.
     * @param assoc The named graph URI which contains association relations
     * @throws Exception
     */
    public SCDUtils(Properties prop, String assoc) throws Exception {
        propFile = prop;
        datasetURI = propFile.getProperty("Dataset_URI");
        associations = assoc;
    }

    /**
     * Creates a new SCDUtils instance w.r.t. a properties file given as
     * parameter, a dataset URI and a namedgraph which contains associations
     * among URIs.
     *
     * @param prop A properties file which denotes a) the credentials for a JDBC
     * Virtuoso connection and b) the folder which contains the SPARQL update
     * queries.
     * @param datasetUri the dataset URI whose versions will be considered for
     * change detection
     * @param assoc The named graph URI which contains association relations
     * @throws Exception
     */
    public SCDUtils(Properties prop, String datasetUri, String assoc) throws Exception {
        propFile = prop;
        associations = assoc;
        this.datasetURI = datasetUri;
    }

    /**
     *
     * @param oldV The old version named graph.
     * @param newV The new version named graph.
     * @param simpleChanges An array of simple changes names which may be
     * potentially be considered instead of all the defined simple changes. If
     * the param is null, then the method considers all the simple changes which
     * appear in the properties file.
     * @param complexChanges An array of complex change names which may be
     * potentially be considered instead of all the defined complex changes.
     * Again, if null is given, then all the defined complex changes will be
     * considered.
     * @throws Exception
     */
    public void customCompareVersions(String oldV, String newV, String[] simpleChanges, String[] complexChanges) throws Exception {
        ChangesManager cManager = new ChangesManager(propFile, datasetURI, oldV, newV, false);
        String ontology = cManager.getChangesOntology();
        cManager.terminate();
        String tmpUri = datasetURI;
        if (datasetURI.endsWith("/")) {
            tmpUri = datasetURI.substring(0, datasetURI.length() - 1);
        }
        ChangesDetector detector = new ChangesDetector(propFile, ontology, tmpUri + "/changes/schema", associations);
        detector.detectSimpleChanges(oldV, newV, simpleChanges);
        if (associations != null) {
            detector.detectAssociations(oldV, newV);
        }
        if (complexChanges == null || complexChanges.length > 0) {
            detector.detectComplexChanges(oldV, newV, complexChanges);
        }
        detector.terminate();
    }

    public void customCompareVersions(String oldV, String newV, String ontology, String[] simpleChanges, String[] complexChanges) throws Exception {
        String tmpUri = datasetURI;
        if (datasetURI.endsWith("/")) {
            tmpUri = datasetURI.substring(0, datasetURI.length() - 1);
        }
        ChangesDetector detector = new ChangesDetector(propFile, ontology, tmpUri + "/changes/schema", associations);
        detector.detectAssociations(oldV, newV);
        detector.detectSimpleChanges(oldV, newV, simpleChanges);
        if (complexChanges == null || complexChanges.length > 0) {
            detector.detectComplexChanges(oldV, newV, complexChanges);
        }
        detector.terminate();
    }

}
