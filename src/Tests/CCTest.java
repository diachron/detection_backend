/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.diachron.detection.utils.ChangesDetector;
import org.diachron.detection.utils.ChangesManager;
import org.diachron.detection.utils.DatasetsManager;
import org.diachron.detection.utils.OntologicalSimpleChangesType;
import org.diachron.detection.complex_change.CCManager;
import org.diachron.detection.complex_change.SCDefinition;

/**
 *
 * @author lenovo
 */
public class CCTest {

    public static void main(String[] args) throws Exception {
        String datasetUri = "http://rous";
        String schema = "http://rous/changes/schema";
        String v1 = "http://www.diachron-fp7.eu/resource/recordset/efo/2.34";
        String v2 = "http://www.diachron-fp7.eu/resource/recordset/efo/2.35";
        Properties prop = new Properties();
        InputStream inputStream = new FileInputStream("config.properties");
        prop.load(inputStream);
        CCManager manager = new CCManager(prop, schema);
        manager.deleteAllComplexChanges(schema, false);
        String ccName = "TestCC";
        double ccPriority = 1.0;
        ////// Associations

        //////  SC definitions 
        List<SCDefinition> scDefs = new ArrayList<>();
        SCDefinition sc1 = new SCDefinition(OntologicalSimpleChangesType.fromString("ASSOCIATION"), "1:ASSOCIATION", false);
//        sc1.setSelectionFilter("1:ASSOCIATION:-superclass = <http://www.geneontology.org/formats/oboInOwl#ObsoleteClass>");
        scDefs.add(sc1);
        ////// CC parameters 
        Map<String, String> ccParams = new LinkedHashMap<>();
//        ccParams.put("obs_class", "1:ADD_SUPERCLASS:-subclass");  //obs_class is the cc parameter, 1:ADD_SUPERCLASS:-subclass is the sc parameter
//        manager.saveCCExtendedDefinition(ccName, ccPriority, scDefs, ccParams, assoc);
        ccParams.put("old", "1:ASSOCIATION:-old_value");
        ccParams.put("new", "1:ASSOCIATION:-new_value");
//        ccParams.put("new_val", "b");
        System.out.println(manager.saveCCExtendedDefinition(ccName, ccPriority, null, scDefs, ccParams, null));

        manager.terminate();
//        System.out.println(query);
    }

}
