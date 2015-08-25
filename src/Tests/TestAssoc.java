/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import java.io.FileReader;
import java.util.Properties;
import org.diachron.detection.associations.AssocManager;

/**
 *
 * @author lenovo
 */
public class TestAssoc {

    public static void main(String[] args) throws Exception {
        String datasetUri = "http://www.ebi.ac.uk/efo/";
        Properties prop = new Properties();
        prop.load(new FileReader("config.properties"));
        AssocManager mgr = new AssocManager(prop, datasetUri, true);
//        Association assoc = new Association();
//        assoc.addOldValue("http://a");
//        assoc.addNewValue("http://b");
//        mgr.addAssociation(assoc);
        String v1 = "http://www.diachron-fp7.eu/resource/recordset/efo/2.38";
        String v2 = "http://www.diachron-fp7.eu/resource/recordset/efo/2.39";
        mgr.createAssocGraph(v1, v2, false);
//
//        DatasetsManager dManager = new DatasetsManager(mgr.getJdbc(), datasetUri);
//        List<String> versions = new ArrayList(dManager.fetchDatasetVersions().keySet());
//        for (int i = 1; i < versions.size(); i++) {
//            String v1 = versions.get(i - 1);
//            String v2 = versions.get(i);
//            mgr.createAssocGraph(v1, v2, false);
//        }
        mgr.terminate();

    }
}
