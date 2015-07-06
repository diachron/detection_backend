/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.associations;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import org.diachron.detection.repositories.JDBCVirtuosoRep;
import org.diachron.detection.repositories.SesameVirtRep;

/**
 *
 * @author rousakis
 */
public class AssocManager {

    private String assocGraph;
    private JDBCVirtuosoRep jdbc;
    private SesameVirtRep sesame;
    private HashMap<String, String> namespaces;

    public AssocManager(String propFile, String assocGraph, boolean removeExisting) throws Exception {
        namespaces = new HashMap<>();
        namespaces.put("co", "http://www.diachron-fp7.eu/changes/");
        namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        this.assocGraph = assocGraph;
        Properties prop = new Properties();
        InputStream inputStream = new FileInputStream("config.properties");
        prop.load(inputStream);
        jdbc = new JDBCVirtuosoRep(prop);
        sesame = new SesameVirtRep(prop);
        if (removeExisting) {
            jdbc.clearGraph(assocGraph, false);
//            init(assocGraph);
        }
    }

    private void init(String assocGraph) {
        sesame.addLitTriple(namespaces.get("co") + "Association", namespaces.get("rdf") + "type", namespaces.get("rdf") + "Class", assocGraph);
        sesame.addLitTriple(namespaces.get("co") + "old_value", namespaces.get("rdf") + "type", namespaces.get("rdf") + "Property", assocGraph);
        sesame.addLitTriple(namespaces.get("co") + "new_value", namespaces.get("rdf") + "type", namespaces.get("rdf") + "Property", assocGraph);
        sesame.addLitTriple(namespaces.get("co") + "Association", namespaces.get("co") + "new_value", namespaces.get("rdfs") + "Resource", assocGraph);
        sesame.addLitTriple(namespaces.get("co") + "Association", namespaces.get("co") + "old_value", namespaces.get("rdfs") + "Resource", assocGraph);
    }

    public void addAssociation(Association assoc) throws Exception {
        StringBuilder sb = new StringBuilder();
        String subject = "http://assoc/" + SHAUtils.SHA1(sb.append(assoc.toString()).toString());
        sesame.addTriple(subject, namespaces.get("rdf") + "type", namespaces.get("co") + "Association", assocGraph);
        for (String oldV : assoc.getOldValues()) {
            sesame.addTriple(subject, namespaces.get("co") + "old_value", oldV, assocGraph);
        }
        for (String newV : assoc.getNewValues()) {
            sesame.addTriple(subject, namespaces.get("co") + "new_value", newV, assocGraph);
        }
    }

    public void terminate() {
        sesame.terminate();
        jdbc.terminate();
    }

    public static void main(String[] args) throws Exception {
        String assocGraph = "http://assoc";
        AssocManager mgr = new AssocManager("config.properties", assocGraph, true);
        Association assoc = new Association();
        assoc.addOldValue("http://a");
        assoc.addNewValue("http://b");
        mgr.addAssociation(assoc);
        mgr.terminate();
    }

}
