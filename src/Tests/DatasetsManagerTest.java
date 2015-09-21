/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Properties;
import org.diachron.detection.associations.AssocManager;
import org.diachron.detection.repositories.JDBCVirtuosoRep;
import org.diachron.detection.utils.ChangesManager;
import org.diachron.detection.utils.DatasetsManager;
import org.diachron.detection.utils.MCDUtils;
import org.diachron.detection.utils.SCDUtils;
import org.openrdf.rio.RDFFormat;

/**
 *
 * @author rousakis
 */
public class DatasetsManagerTest {

    public static void main(String[] args) throws Exception {
        String efoDataset = "http://www.ebi.ac.uk/efo-test/";
        String schema = efoDataset + "changes/schema";

        Properties prop = new Properties();
        InputStream inputStream = new FileInputStream("config.properties");
        prop.load(inputStream);
//        String v = "http://www.diachron-fp7.eu/resource/recordset/efo/";
//        String v2 = "http://www.diachron-fp7.eu/resource/recordset/efo/2.46";
        DatasetsManager mgr = new DatasetsManager(prop, efoDataset);
        mgr.deleteDataset(true, true);

//        mgr.getJDBCVirtuosoRep().renameGraph("http://www.ebi.ac.uk/efo/changes/2.48-2.49",
//                "http://www.ebi.ac.uk/efo/guest/changes/2.48-2.49");
//        mgr.getJDBCVirtuosoRep().renameGraph("http://www.ebi.ac.uk/efo/changes/2.49-2.50",
//                "http://www.ebi.ac.uk/efo/guest/changes/2.49-2.50");
//        mgr.dereifyDiachronVersion(v1);
//        mgr.getJDBCVirtuosoRep().dereifyDiachronData(v1,v1+"_ORIG");
//        mgr.getJDBCVirtuosoRep().dereifyDiachronData(v2,v2+"_ORIG");
//        AnalyzeChanges(schema, efoDataset, v1, v2, null, false);
//        ChangesManager cManager = new ChangesManager(prop, efoDataset, v1, v2, false);
//        cManager.getJDBC().clearGraph(cManager.getChangesOntology(), false);
//        cManager.deleteChangesOntology();
        mgr.terminate();
//        AssocManager assoc = new AssocManager(cManager.getJDBC(), efoDataset, true);
//        String assocGraph = assoc.createAssocGraph(v1, v2, true);
//        assocGraph = null;
//        SCDUtils scd = new SCDUtils(prop, efoDataset, assocGraph);
//        scd.customCompareVersions(v1, v2, null, null);
//        AnalyzeChanges(schema, efoDataset, v1, v2, null, false);
//        cManager.terminate();
//        MCDUtils mcd = new MCDUtils(prop, efoDataset, true);
//        mcd.getJDBCRepository().clearGraph(efoDataset, true);
//        mcd.getJDBCRepository().copyGraph("http://www.ebi.ac.uk/efo/guest/user1/changes/schema", schema);
//        mcd.detectDatasets(false);
//        mcd.terminate();

    }

    static void AnalyzeChanges(JDBCVirtuosoRep jdbc, String ontologySchema, String datasetUri, String v1, String v2, String changeType, boolean tempOntology) throws Exception {
        LinkedHashMap analysis = Analysis.analyzeChanges(jdbc, ontologySchema, datasetUri, v1, v2, changeType, tempOntology);
        System.out.println(v1 + " - " + v2);
        for (Object key : analysis.keySet()) {
            System.out.println(key + "\t" + analysis.get(key));
        }

//        Set<Change> changes = fetcher.fetchChangesForNewVersion(v2, ontology, ontologySchema, "All_Complex_Changes", 100);
//        for (Change change : changes) {
//            System.out.println(change);
//        }
        //       System.out.println("Analysis..."+analysis.toString());
    }
}
