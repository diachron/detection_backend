/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.diachron.detection.associations.AssocManager;
import org.diachron.detection.utils.DatasetsManager;
import org.diachron.detection.utils.MCDUtils;

/**
 *
 * @author rousakis
 */
public class EFO_Assoc {

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        InputStream inputStream;
        try {
            inputStream = new FileInputStream("config.properties");
            prop.load(inputStream);
        } catch (IOException ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured .");
            return;
        }
        String efoDataset = "http://www.ebi.ac.uk/efo/clean";
        String ontologySchema = efoDataset + "/changes/schema";
        MCDUtils mcd = new MCDUtils(prop, efoDataset, true);
        createAssociations(mcd, efoDataset);
        mcd.detectDatasets(false);
        mcd.terminate();
    }

    private static void createAssociations(MCDUtils mcd, String efoDataset) throws Exception {
        DatasetsManager dManager = new DatasetsManager(mcd.getJDBCRepository(), efoDataset);
        List<String> versions = new ArrayList(dManager.fetchDatasetVersions().keySet());
        AssocManager assoc = new AssocManager(dManager.getJDBCVirtuosoRep(), efoDataset, true);
        for (int i = 1; i < versions.size(); i++) {
            String v1 = versions.get(i - 1);
            String v2 = versions.get(i);
            assoc.createAssocGraph(v1, v2, true);
        }
    }
}
