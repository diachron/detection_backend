/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.diachron.detection.exploit.ChangesExploiter;

/**
 *
 * @author rousakis
 */
public class ChangesExplTest {

    public static void main(String[] args) throws Exception {
        String efoDataset = "http://www.ebi.ac.uk/efo/";
        //        System.out.println(exploiter.fetchChangeDefinitions(null));
        String v1 = "http://www.diachron-fp7.eu/resource/recordset/efo/2.34";
        String v2 = "http://www.diachron-fp7.eu/resource/recordset/efo/2.35";
        Properties prop = new Properties();
        InputStream inputStream = new FileInputStream("config.properties");
        prop.load(inputStream);
        ChangesExploiter exploiter = new ChangesExploiter(prop, "EFO", false);
        List<String> changes = new ArrayList<>();
        changes.add("Add Definition");
        changes.add("Add Synonym");
        String resource = "http://www.ebi.ac.uk/efo/EFO_0005102";
//        System.out.println(exploiter.fetchChangesContainValue(resource));
        System.out.println(exploiter.fetchChangesBetweenVersions(v1, v2, null, null, 1000));

//        System.out.println(exploiter.fetchChangeDefinitions("ADD_SUPERCLASS"));
        exploiter.terminate();
    }
}
