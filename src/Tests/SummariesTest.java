/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.diachron.detection.associations.AssocManager;
import org.diachron.detection.repositories.JDBCVirtuosoRep;
import org.diachron.detection.utils.ChangesManager;
import org.diachron.detection.utils.DatasetsManager;
import org.diachron.detection.utils.SCDUtils;

/**
 *
 * @author rousakis
 */
public class SummariesTest {

    public static void main(String[] args) throws Exception {
        String efoDataset = "http://www.ebi.ac.uk/efo/";
        String efoDataset20 = "http://www.ebi.ac.uk/efo-20/";

        Properties prop = new Properties();
        InputStream inputStream = new FileInputStream("config.properties");
        prop.load(inputStream);
        String v = "http://www.diachron-fp7.eu/resource/recordset/efo/";
//        String v2 = "http://www.diachron-fp7.eu/resource/recordset/efo/2.46";
//        DatasetsManager mgr = new DatasetsManager(prop, efoDataset20);
//        mgr.insertDataset(efoDataset, "EFO");
//        mgr.insertDataset(efoDataset20, "EFO-20%");
//        mgr.insertDatasetVersion("input/efo_v4/_diachron_efo-2.34.owl", RDFFormat.RDFXML, v + "2.34", "2.34", efoDataset);
//        mgr.insertDatasetVersion("input/efo_v4/_diachron_efo-2.35.owl", RDFFormat.RDFXML, v + "2.35", "2.35", efoDataset);
//        mgr.insertDatasetVersion("input/efo_v4/_diachron_efo-2.38.owl", RDFFormat.RDFXML, v + "2.38", "2.38", efoDataset);
//        mgr.insertDatasetVersion("input/efo_v4/_diachron_efo-2.46.owl", RDFFormat.RDFXML, v + "2.46", "2.46", efoDataset);
//        mgr.dereifyDiachronVersion(v + "2.34");
//        mgr.dereifyDiachronVersion(v + "2.35");
//        mgr.dereifyDiachronVersion(v + "2.38");
//        mgr.dereifyDiachronVersion(v + "2.46");
//        insertLabelsOnSummaries(mgr.getJDBCVirtuosoRep(), v + "2.34-20", v + "2.34");
//        insertLabelsOnSummaries(mgr.getJDBCVirtuosoRep(), v + "2.35-20", v + "2.35");
//        insertLabelsOnSummaries(mgr.getJDBCVirtuosoRep(), v + "2.38-20", v + "2.38");
//        insertLabelsOnSummaries(mgr.getJDBCVirtuosoRep(), v + "2.46-20", v + "2.46");
//        mgr.terminate();
        //////////
        String datasetUri = efoDataset20;
        String v1 = v + "2.34";
        String v2 = v + "2.35";
        String v3 = v + "2.38";
        String v4 = v + "2.46";
        String sumV1 = v1 + "-20";
        String sumV2 = v2 + "-20";
        String sumV3 = v3 + "-20";
        String sumV4 = v4 + "-20";
        findChanges(prop, efoDataset, v1, v3);
        findChanges(prop, efoDataset20, sumV1, sumV3);
    }

    private static void findChanges(Properties prop, String datasetUri, String v1, String v2) throws Exception {
        String schema = datasetUri + "changes/schema";
        ChangesManager cManager = new ChangesManager(prop, datasetUri, v1, v2, false);
        DatasetsManagerTest.AnalyzeChanges(cManager.getJDBC(), schema, datasetUri, v1, v2, null, false);
        cManager.getJDBC().clearGraph(cManager.getChangesOntology(), false);
        AssocManager assoc = new AssocManager(cManager.getJDBC(), datasetUri, true);
        String assocGraph = assoc.createAssocGraph(v1, v2, false);
//        assocGraph = null;
        SCDUtils scd = new SCDUtils(prop, datasetUri, assocGraph);
        scd.customCompareVersions(v1, v2, null, new String[]{});
        DatasetsManagerTest.AnalyzeChanges(cManager.getJDBC(), schema, datasetUri, v1, v2, null, false);
        cManager.terminate();
    }

    public static void insertLabelsOnSummaries(JDBCVirtuosoRep jdbc, String summVersion, String origVersion) {
        String sparql = "insert into <" + summVersion + "> {\n"
                + "?s rdfs:label ?label.\n"
                + "} where {\n"
                + "graph <" + summVersion + "> {\n"
                + "?s a rdfs:Class.\n"
                + "}\n"
                + "graph <" + origVersion + "> {\n"
                + "?s a owl:Class.\n"
                + "?s rdfs:label ?label.\n"
                + "}\n"
                + "}";
        jdbc.executeUpdateQuery("sparql " + sparql, false);
    }
}
