/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.complex_change;

import org.diachron.detection.change_detection_utils.OntologicalSimpleChangesType;
import java.util.HashMap;

/**
 *
 * @author rousakis
 */
public class OntologicalSimpleChangesBlocks {

    private static String getAdd_Superclass() {
        return "[uri] a co:Add_Superclass; \n"
                + "  co:asc_p1 [uri]1;\n"
                + "  co:asc_p2 [uri]2.";
    }

    private static String getAdd_Label() {
        return "[uri] a co:Add_Label; \n"
                + "  co:al_p1 [uri]1;\n"
                + "  co:al_p2 [uri]2.";
    }

    private static String getAdd_Comment() {
        return "[uri] a co:Add_Comment; \n"
                + "  co:ac_p1 [uri]1;\n"
                + "  co:ac_p2 [uri]2.";
    }

    private static String getAdd_Domain() {
        return "[uri] a co:Add_Domain; \n"
                + "  co:ad_p1 [uri]1;\n"
                + "  co:ad_p2 [uri]2.";
    }

    private static String getAdd_Range() {
        return "[uri] a co:Add_Range; \n"
                + "  co:ar_p1 [uri]1;\n"
                + "  co:ar_p2 [uri]2.";
    }

    private static String getAdd_Property_Instance() {
        return "[uri] a co:Add_Property_Instance;\n"
                + "  co:api_p1 [uri]1;\n"
                + "  co:api_p2 [uri]2;\n"
                + "  co:api_p3 [uri]3.";
    }

    private static String getAdd_Type_Class() {
        return "[uri] a co:Add_Type_Class;\n"
                + "  co:atc_p1 [uri]1.";
    }

    private static String getAdd_Type_Property() {
        return "[uri] a co:Add_Type_Property;\n"
                + "  co:atp_p1 [uri]1.";
    }

    private static String getAdd_Type_To_Individual() {
        return "[uri] a co:Add_Type_To_Individual;\n"
                + "  co:atti_p1 [uri]1;\n"
                + "  co:atti_p2 [uri]2.";
    }

    private static String getDelete_Superclass() {
        return "[uri] a co:Add_Superclass; \n"
                + " co:asc_p1 [uri]1;\n"
                + " co:asc_p2 [uri]2.";
    }

    private static String getDelete_Label() {
        return "[uri] a co:Delete_Label; \n"
                + "  co:dl_p1 [uri]1;\n"
                + "  co:dl_p2 [uri]2.";
    }

    private static String getDelete_Comment() {
        return "[uri] a co:Delete_Comment; \n"
                + "  co:dc_p1 [uri]1;\n"
                + "  co:dc_p2 [uri]2.";
    }

    private static String getDelete_Property_Instance() {
        return "[uri] a co:Delete_Property_Instance;\n"
                + "  co:dpi_p1 [uri]1;\n"
                + "  co:dpi_p2 [uri]2;\n"
                + "  co:dpi_p3 [uri]3.";
    }

    private static String getDelete_Type_Class() {
        return "[uri] a co:Delete_Type_Class;\n"
                + "  co:dtc_p1 [uri]1.";
    }

    private static String getDelete_Type_Property() {
        return "[uri] a co:Delete_Type_Property;\n"
                + "  co:dtp_p1 [uri]1.";
    }

    private static String getDelete_Type_From_Individual() {
        return "[uri] a co:Delete_Type_From_Individual;\n"
                + "  co:dtfi_p1 [uri]1;\n"
                + "  co:dtfi_p2 [uri]2.";
    }

    private static String getDelete_Domain() {
        return "[uri] a co:Delete_Domain; \n"
                + "  co:dd_p1 [uri]1;\n"
                + "  co:dd_p2 [uri]2.";
    }

    private static String getDelete_Range() {
        return "[uri] a co:Add_Range; \n"
                + "  co:dr_p1 [uri]1;\n"
                + "  co:dr_p2 [uri]2.";
    }

    public static HashMap<String, String> fetchSCParamsURIs(OntologicalSimpleChangesType change, String uri) {
        HashMap<String, String> scParamsMap = new HashMap<>();
        switch (change) {
            case ADD_SUPERCLASS:
            case DELETE_SUPERCLASS:
                scParamsMap.put("subclass", uri + "1");
                scParamsMap.put("superclass", uri + "2");
                break;
            case ADD_LABEL:
            case DELETE_LABEL:
                scParamsMap.put("subject", uri + "1");
                scParamsMap.put("label", uri + "2");
                break;
            case ADD_COMMENT:
            case DELETE_COMMENT:
                scParamsMap.put("subject", uri + "1");
                scParamsMap.put("comment", uri + "2");
                break;
            case ADD_DOMAIN:
            case DELETE_DOMAIN:
                scParamsMap.put("property", uri + "1");
                scParamsMap.put("domain", uri + "2");
                break;
            case ADD_RANGE:
            case DELETE_RANGE:
                scParamsMap.put("property", uri + "1");
                scParamsMap.put("range", uri + "2");
                break;
            case ADD_TYPE_CLASS:
            case DELETE_TYPE_CLASS:
                scParamsMap.put("class", uri + "1");
                break;
            case ADD_TYPE_PROPERTY:
            case DELETE_TYPE_PROPERTY:
                scParamsMap.put("property", uri + "1");
                break;
            case ADD_PROPERTY_INSTANCE:
            case DELETE_PROPERTY_INSTANCE:
                scParamsMap.put("subject", uri + "1");
                scParamsMap.put("property", uri + "2");
                scParamsMap.put("object", uri + "3");
                break;
            case ADD_TYPE_TO_INDIVIDUAL:
            case DELETE_TYPE_FROM_INDIVIDUAL:
                scParamsMap.put("individual", uri + "1");
                scParamsMap.put("type", uri + "2");
                break;
            default:
                return null;
        }
        return scParamsMap;

    }

    public static String fetchSPARQLBlock(OntologicalSimpleChangesType change, String uri) {
        String res;
        switch (change) {
            case ADD_SUPERCLASS:
                res = getAdd_Superclass();
                break;
            case DELETE_SUPERCLASS:
                res = getDelete_Superclass();
                break;
            case ADD_LABEL:
                res = getAdd_Label();
                break;
            case DELETE_LABEL:
                res = getDelete_Label();
                break;
            case ADD_COMMENT:
                res = getAdd_Comment();
                break;
            case DELETE_COMMENT:
                res = getDelete_Comment();
                break;
            case ADD_TYPE_CLASS:
                res = getAdd_Type_Class();
                break;
            case DELETE_TYPE_CLASS:
                res = getDelete_Type_Class();
                break;
            case ADD_TYPE_PROPERTY:
                res = getAdd_Type_Property();
                break;
            case DELETE_TYPE_PROPERTY:
                res = getDelete_Type_Property();
                break;
            case ADD_PROPERTY_INSTANCE:
                res = getAdd_Property_Instance();
                break;
            case DELETE_PROPERTY_INSTANCE:
                res = getDelete_Property_Instance();
                break;
            case ADD_TYPE_TO_INDIVIDUAL:
                res = getAdd_Type_To_Individual();
                break;
            case DELETE_TYPE_FROM_INDIVIDUAL:
                res = getDelete_Type_From_Individual();
                break;
            case ADD_DOMAIN:
                res = getAdd_Domain();
                break;
            case DELETE_DOMAIN:
                res = getDelete_Domain();
                break;
            case ADD_RANGE:
                res = getAdd_Range();
                break;
            case DELETE_RANGE:
                res = getDelete_Range();
                break;

            default:
                return "Change: " + change + " is not currently supported";
        }
        return res.replaceAll("\\[uri\\]", "?" + uri);
    }
}
