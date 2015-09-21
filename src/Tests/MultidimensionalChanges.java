package Tests;

import org.diachron.detection.utils.ChangesDetector;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.diachron.detection.exploit.ChangesExploiter;
import org.diachron.detection.repositories.JDBCVirtuosoRep;
import org.diachron.detection.repositories.SesameVirtRep;
import org.diachron.detection.utils.ChangesManager;
import org.diachron.detection.utils.DatasetsManager;
import org.diachron.detection.utils.MCDUtils;
import org.diachron.detection.utils.SCDUtils;
import org.openrdf.rio.RDFFormat;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rous
 */
public class MultidimensionalChanges {

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        InputStream inputStream;
        try {
            inputStream = new FileInputStream("intrasoft-config.properties");
            prop.load(inputStream);
        } catch (IOException ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured .");
            return;
        }

        String dataset162h = "http://datamarket-162h";
        String dataset254n = "http://datamarket-254n";
        String dataset1bu4 = "http://datamarket-1bu4";
        String dataset4ag6 = "http://datamarket-4ag6";
        //
//        AssignVersionsToDatasetss(prop, dataset162h, dataset254n, dataset1bu4, dataset4ag6);
//        MCDUtils utils = new MCDUtils(prop, dataset162h, false);
//        MCDUtils utils = new MCDUtils(prop, dataset254n, false);
//        MCDUtils utils = new MCDUtils(prop, dataset1bu4, false);
//        MCDUtils utils = new MCDUtils(prop, dataset4ag6, false);
//        JDBCVirtuosoRep jdbc = utils.getJDBCRepository();
//        ChangesExploiter expl = new ChangesExploiter(jdbc, dataset1bu4, true);
//        for (String ontology : expl.getChangesOntologies()) {
//            jdbc.clearGraph(ontology, true);
//        }
//        utils.detectDatasets(false);
//        utils.terminate();
        /////

        SesameVirtRep sesame = new SesameVirtRep(prop);
//        sesame.exportToFile("ChangesOntologySchema.nt", RDFFormat.NTRIPLES, dataset162h + "/changes/schema");

        sesame.exportToFile("datamarket-162h_1.rdf", RDFFormat.RDFXML, "http://www.diachron-fp7.eu/datamarket-162h/1");
        sesame.exportToFile("datamarket-162h_1.nt", RDFFormat.NTRIPLES, "http://www.diachron-fp7.eu/datamarket-162h/1");
        sesame.exportToFile("datamarket-162h_1.n3", RDFFormat.N3, "http://www.diachron-fp7.eu/datamarket-162h/1");
//        JDBCVirtuosoRep jdbc = new JDBCVirtuosoRep(prop);
//        jdbc.clearGraph(dataset162h + "/changes/schema", true);
//        jdbc.clearGraph(dataset1bu4 + "/changes/schema", true);
//        jdbc.clearGraph(dataset254n + "/changes/schema", true);
//        jdbc.copyGraph(dataset4ag6 + "/changes/schema", dataset254n + "/changes/schema");
//        jdbc.copyGraph(dataset4ag6 + "/changes/schema", dataset1bu4 + "/changes/schema");
//        jdbc.copyGraph(dataset4ag6 + "/changes/schema", dataset162h + "/changes/schema");
        sesame.terminate();
//        jdbc.terminate();
    }

    private static void AssignVersionsToDatasets(Properties prop, String dataset162h, String dataset254n, String dataset1bu4, String dataset4ag6) throws Exception {
        ////////
        // to v1 me to v2 einai ta idia 
        DatasetsManager dManager = new DatasetsManager(prop, dataset162h);
        String v1_162h = "http://www.diachron-fp7.eu/datamarket-162h/1";
        String v2_162h = "http://www.diachron-fp7.eu/datamarket-162h/2";
        String v3_162h = "http://www.diachron-fp7.eu/datamarket-162h/3";
        dManager.assignVersionToDataset(v1_162h, "v1");
        dManager.assignVersionToDataset(v2_162h, "v2");
        dManager.assignVersionToDataset(v3_162h, "v3");
        dManager.terminate();
        ////////
        // to v1 me to v2 einai ta idia
        dManager = new DatasetsManager(prop, dataset254n);
        String v1_254n = "http://www.diachron-fp7.eu/datamarket-254n/1";
        String v2_254n = "http://www.diachron-fp7.eu/datamarket-254n/2";
        String v3_254n = "http://www.diachron-fp7.eu/datamarket-254n/3";
        dManager.assignVersionToDataset(v1_254n, "v1");
        dManager.assignVersionToDataset(v2_254n, "v2");
        dManager.assignVersionToDataset(v3_254n, "v3");
        dManager.terminate();
//        ////////
        dManager = new DatasetsManager(prop, dataset1bu4);
        String v1_1bu4 = "http://www.diachron-fp7.eu/datamarket-1bu4/1";
        String v2_1bu4 = "http://www.diachron-fp7.eu/datamarket-1bu4/2";
        String v3_1bu4 = "http://www.diachron-fp7.eu/datamarket-1bu4/3";
        dManager.assignVersionToDataset(v1_1bu4, "v1");
        dManager.assignVersionToDataset(v2_1bu4, "v2");
        dManager.assignVersionToDataset(v3_1bu4, "v3");
        dManager.terminate();
//        ////////
//        // uparxoun diafores se ola ta datasets
        dManager = new DatasetsManager(prop, dataset4ag6);
        String v1_4ag6 = "http://www.diachron-fp7.eu/datamarket-4ag6/1";
        String v2_4ag6 = "http://www.diachron-fp7.eu/datamarket-4ag6/2";
        String v3_4ag6 = "http://www.diachron-fp7.eu/datamarket-4ag6/3";
        dManager.assignVersionToDataset(v1_4ag6, "v1");
        dManager.assignVersionToDataset(v2_4ag6, "v2");
        dManager.assignVersionToDataset(v3_4ag6, "v3");
        dManager.terminate();
        ////////
    }
}
