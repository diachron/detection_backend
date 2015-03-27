/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.change_detection_utils;

import org.diachron.detection.complex_change.CCManager;
import org.diachron.detection.complex_change.CCDefinitionError.CODE;
import org.diachron.detection.complex_change.SCDefinition;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.diachron.detection.complex_change.Presence;
import org.diachron.detection.complex_change.VersionFilter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.repository.RepositoryException;
import org.diachron.detection.repositories.SesameVirtRep;

/**
 * This class contains a set of static methods which are used for the definition of a complex change using 
 * a JSON string as input. This json string must have the following form: <br>
 * <br>
 * { <br>
 * "Complex_Change": "Mark_as_Obsolete_v2", <br>
 * "Priority": 1, <br>
 * "Complex_Change_Parameters": [ <br>
 * { <br>
 * "obs_class": "sc1:-subclass" <br>
 * } <br>
 * ], <br>
 * "Simple_Changes": [ <br>
 * { <br>
 * "Simple_Change": "ADD_SUPERCLASS", <br>
 * "Simple_Change_Uri": "sc1", <br>
 * "Is_Optional": false, <br>
 * "Selection_Filter": "sc1:-superclass =
 * <http://www.geneontology.org/formats/oboInOwl#ObsoleteClass>", <br>
 * "Mapping_Filter": "", <br>
 * "Join_Filter": "" <br>
 * } <br>
 * ], 
 * "Version_Filters" : [ 
 * { <br>
 * "Subject" : "sc1:ADD_SUPERCLASS:-subject", <br>
 * "Predicate" : "rdfs:subClassOf", <br>
 * "Object" : "rdfs:Resource", <br>
 * "Presence" : "EXISTS_IN_V2" <br>
 * } <br>
 * ] <br>
 * } <br>
 * @author rousakis
 */
public class JSONMessagesParser {

    private static List<VersionFilter> createVersionFilters(String json) {
        List<VersionFilter> versionFilters = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(json);
            JSONArray jsonVersFilts = (JSONArray) jsonObject.get("Version_Filters");
            if (jsonVersFilts == null) {
                return versionFilters;
            }
            for (int i = 0; i < jsonVersFilts.size(); i++) {
                JSONObject filt = (JSONObject) jsonVersFilts.get(i);
                Presence presence = Presence.fromString((String) filt.get("Presence"));
                String subject = (String) filt.get("Subject");
                String predicate = (String) filt.get("Predicate");
                String object = (String) filt.get("Object");
                VersionFilter filter = new VersionFilter(subject, predicate, object, presence);
                versionFilters.add(filter);
            }
        } catch (ParseException ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured .");
            return null;
        }
        return versionFilters;
    }

    private static List<SCDefinition> createSCDefinitions(String json) {
        List<SCDefinition> sChanges = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(json);
            JSONArray jsonSCs = (JSONArray) jsonObject.get("Simple_Changes");
            if (jsonSCs == null) {
                return null;
            }
            for (int i = 0; i < jsonSCs.size(); i++) {
                JSONObject sc = (JSONObject) jsonSCs.get(i);
                OntologicalSimpleChangesType sChangeType = OntologicalSimpleChangesType.fromString((String) sc.get("Simple_Change"));
                String sChangeUri = (String) sc.get("Simple_Change_Uri");
                boolean isOptional = (Boolean) sc.get("Is_Optional");
                SCDefinition scd = new SCDefinition(sChangeType, sChangeUri, isOptional);
                if (sc.get("Join_Filter") instanceof JSONArray) {
                    JSONArray joinFilters = (JSONArray) sc.get("Join_Filter");
                    for (int j = 0; j < joinFilters.size(); j++) {
                        scd.setJoinFilter((String) joinFilters.get(j));
                    }
                } else {
                    scd.setJoinFilter((String) sc.get("Join_Filter"));
                }
                //
                if (sc.get("Selection_Filter") instanceof JSONArray) {
                    JSONArray selFilters = (JSONArray) sc.get("Selection_Filter");
                    for (int j = 0; j < selFilters.size(); j++) {
                        scd.setSelectionFilter((String) selFilters.get(j));
                    }
                } else {
                    scd.setSelectionFilter((String) sc.get("Selection_Filter"));
                }
                sChanges.add(scd);
            }
        } catch (ParseException ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured .");
            return null;
        }
        return sChanges;
    }

    private static boolean isValidCCName(String ccName, Properties prop, CCManager ccDef, String changesOntology) throws Exception {
        if (ccName == null) {
            ccDef.setCcDefError("Error in complex change name parsing.", CODE.INVALID_JSON);
            return false;
        } else {
            SesameVirtRep sesame = new SesameVirtRep(prop);
            String ontology;
            if (changesOntology == null) {
                ontology = prop.getProperty("Changes_Ontology");
            } else {
                ontology = changesOntology;
            }
            String query = "select count(*) from <" + ontology + "> where { ?cc co:name \"" + ccName + "\"}";
            long size = Long.parseLong(sesame.queryExec(query).next().getValue("callret-0").stringValue());
            if (size == 1) {
                ccDef.setCcDefError("There already exists a complex change with the same name.", CODE.NON_UNIQUE_CC_NAME);
                return false;
            }
            sesame.terminate();
        }
        return true;
    }
    
    /**
     * This static method stores the definition of a complex change into the changes ontology schema. The complex change definition 
     * is encoded into a json representation.
     * @param prop A properties file which contains the Virtuoso credentials. 
     * @param json The json representation of the complex change.
     * @param changesOntologySchema The changes ontology schema into which the complex change will be stored.
     * @return An instance of {@link CCManager} which is the class for the Complex Change representation.
     * @throws ClassNotFoundException
     * @throws RepositoryException
     * @throws SQLException
     */
    public static CCManager createCCDefinition(Properties prop, String json, String changesOntologySchema) throws ClassNotFoundException, RepositoryException, SQLException {
        List<VersionFilter> versionFilter = createVersionFilters(json);
        List<SCDefinition> sChanges = createSCDefinitions(json);
        JSONParser jsonParser = new JSONParser();
        CCManager ccDef = new CCManager(prop, changesOntologySchema);
        if (sChanges == null) {
            ccDef.setCcDefError("Error in complex change parameters parsing.", CODE.INVALID_JSON);
            return ccDef;
        }
        String ccName = null;
        Double priority;
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(json);
            ccName = (String) jsonObject.get("Complex_Change");
            if (!isValidCCName(ccName, prop, ccDef, changesOntologySchema)) {
                return ccDef;
            }
            priority = (Double) jsonObject.get("Priority");
            if (priority == null) {
                ccDef.setCcDefError("Error in priority parsing.", CODE.INVALID_JSON);
                return ccDef;
            }

            JSONArray jsonCCParams = (JSONArray) jsonObject.get("Complex_Change_Parameters");
            if (jsonCCParams == null) {
                ccDef.setCcDefError("Error in complex change parameters parsing.", CODE.INVALID_JSON);
                return ccDef;
            }

            ccDef = new CCManager(prop, changesOntologySchema);
            for (int i = 0; i < jsonCCParams.size(); i++) {
                JSONObject sc = (JSONObject) jsonCCParams.get(i);
                String key = (String) sc.keySet().iterator().next();
                String value = (String) sc.get(key);
                if (!ccDef.addCCParameter(key, value)) {
                    ccDef.setCcDefError("There is already given a complex change parameter with the same name.",
                            CODE.NON_UNIQUE_CC_PARAM_NAME);
                    return ccDef;
                }
                if (value == null) {
                    ccDef.setCcDefError("Complex change parameter is not mapped with any simple change parameter.",
                            CODE.NO_CC_PARAM);
                    return ccDef;
                }
            }
            for (SCDefinition sc : sChanges) {
                ccDef.addSimpleChange(sc);
            }
            ccDef.setCcName(ccName);
            ccDef.setCcPriority(priority);
            ccDef.groupFiltersPerPresence(versionFilter);
            return ccDef;
        } catch (Exception ex) {
            ccDef = new CCManager(prop, null);
            ccDef.setCcDefError("Error in JSON input message.", CODE.INVALID_JSON);
            return ccDef;
        }
    }
}
