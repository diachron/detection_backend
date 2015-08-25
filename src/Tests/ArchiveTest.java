/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Properties;
import org.diachron.detection.associations.AssocManager;
import org.diachron.detection.exploit.ArchiveExploiter;
import org.diachron.detection.repositories.JDBCVirtuosoRep;
import org.diachron.detection.utils.ChangesManager;
import org.diachron.detection.utils.SCDUtils;

/**
 *
 * @author rousakis
 */
public class ArchiveTest {

    public static void main(String[] args) throws Exception {
        Properties chDet = new Properties();
        Properties arch = new Properties();
        chDet.load(new FileReader("intrasoft-config.properties"));
        arch.load(new FileReader("intrasoft-config.properties"));

        String diachrDataset = "http://www.diachron-fp7.eu/resource/diachronicDataset/EFO_Test_Strategies/CDAAF2AE5D9F7726789EFE06C84386E8";
        String diachrDatasetLabel = "EFO Test Diachronic Dataset";
        String dataset1 = "http://dev.diachron-fp7.eu/resource/dataset/efo/2.37";
        String dataset2 = "http://dev.diachron-fp7.eu/resource/dataset/efo/2.38";

        ArchiveExploiter expl = new ArchiveExploiter(chDet);

        //        expl.addDiachronicDataset(diachrDataset, diachrDatasetLabel);
//        expl.addDiachronicDatasetVersion(diachrDataset, dataset1, null);
//        expl.addDiachronicDatasetVersion(diachrDataset, dataset2, null);
//        expl.addDiachronicDatasetVersion(diachrDataset, dataset3, null);
//        System.out.println(expl.fetchChDetectVersion(dataset1));   //an kopsw apo to teleutaio / k metapairnw to graph me ta periexomena
//        System.out.println(expl.fetchChDetectVersion(dataset2));   //an kopsw apo to teleutaio / k metapairnw to graph me ta periexomena
//        createArchiveChangeSet(arch, dataset1, dataset2, diachrDataset);
        expl.terminate();
    }

}
