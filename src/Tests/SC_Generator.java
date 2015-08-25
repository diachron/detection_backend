package Tests;

import java.io.FileReader;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.diachron.detection.repositories.SesameVirtRep;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rousakis
 */
public class SC_Generator {

    private static void createChangesSchema(SesameVirtRep sesame, String defFile, String graph) throws Exception {
        HashMap<String, String> namespaces = new HashMap<>();
        namespaces.put("co", "http://www.diachron-fp7.eu/changes/");
        namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        JSONParser parser = new JSONParser();
        JSONArray array = (JSONArray) parser.parse(new FileReader(defFile));
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = (JSONObject) array.get(i);
            ////
            String sChangeUri = namespaces.get("co") + (String) object.get("Simple_Change");
            String sChangeName = (String) object.get("Simple_Change_Name");
            System.out.println("Importing: " + sChangeName);
            HashMap<String, String> parameters = new HashMap<>();
            JSONArray params = (JSONArray) object.get("Parameters");
            JSONArray paramNames = (JSONArray) object.get("Parameter_Names");
            for (int j = 0; j < params.size(); j++) {
                String param = (String) params.get(j);
                String paramName = (String) paramNames.get(j);
                parameters.put(param, paramName);
            }
            ////
            sesame.addTriple(sChangeUri, namespaces.get("rdf") + "type", namespaces.get("rdfs") + "Class", graph);
            sesame.addTriple(sChangeUri, namespaces.get("rdfs") + "subClassOf", namespaces.get("co") + "Simple_Change", graph);
            sesame.addLitTriple(sChangeUri, namespaces.get("co") + "name", sChangeName, graph);
            for (String param : parameters.keySet()) {
                String paramName = parameters.get(param);
                sesame.addTriple(namespaces.get("co") + param, namespaces.get("rdf") + "type", namespaces.get("rdf") + "Property", graph);
                sesame.addTriple(namespaces.get("co") + param, namespaces.get("rdfs") + "domain", sChangeUri, graph);
                sesame.addTriple(namespaces.get("co") + param, namespaces.get("rdfs") + "range", namespaces.get("rdfs") + "Resource", graph);
                sesame.addLitTriple(namespaces.get("co") + param, namespaces.get("co") + "name", paramName, graph);
                sesame.addTriple(sChangeUri, namespaces.get("co") + param, namespaces.get("rdfs") + "Resource", graph);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        SesameVirtRep sesame = new SesameVirtRep(
                "139.91.183.65/",
                //                "83.212.99.102", //modip vm
                1111,
                "dba",
                "dba");
        String root = "input/changes_ontology/ontological/";
        String file = root + "ontological_simple_changes.json";
        String graph = "http://www.diachron-fp7.eu/changes/multidimensional/schema";
        graph = "http://www.ebi.ac.uk/efo/changes";
//        graph = "http://rous/changes/schema";
        System.out.println(sesame.triplesNum(graph));
        createChangesSchema(sesame, file, graph);
//        sesame.clearGraphContents(graph);
        System.out.println(sesame.triplesNum(graph));
        sesame.terminate();
    }
}
