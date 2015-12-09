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
import org.diachron.detection.utils.ModelType;
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

        expl.addDiachronicDataset(diachrDataset, diachrDatasetLabel);
        expl.addDiachronicDatasetVersion(diachrDataset, dataset1, null);

//        String chVersion = expl.fetchChDetectVersion(dataset1, diachrDataset);
//        System.out.println(chVersion);
//        System.out.println(expl.fetchDiachronDatasetVersion(chVersion));
//        System.out.println(expl.fetchChDetectVersion(dataset2));
//        expl.createArchiveChangeSet(arch, dataset1, dataset2, null, null, true, diachrDataset);
//        expl.terminate();
    }

}
