/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.complex_change;

import java.sql.ResultSet;
import org.diachron.detection.utils.OntologicalSimpleChangesType;
import org.diachron.detection.complex_change.CCDefinitionError.CODE;
import org.openrdf.repository.RepositoryException;
import org.diachron.detection.repositories.JDBCVirtuosoRep;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.spi.ErrorCode;
import org.diachron.detection.repositories.SesameVirtRep;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * This class represents a complex change definitions. Moreover, it contains all
 * the required methods for a definition of a complex change.
 *
 * @author rousakis
 */
public class CCManager {

    private SesameVirtRep sesameRepos;
    private JDBCVirtuosoRep jdbcRep;
    private String cChangeURI;
    private String cChangeSparql;
    private String ccName;
    private Double ccPriority;
    private String ccDescription;
    private LinkedHashMap<String, CCParameter> cChangeParameters;
    private String changesOntologySchema;
    private List<SCDefinition> sChanges;
    private CCDefinitionError ccDefError;
    private LinkedHashMap<Presence, List<VersionFilter>> versionFilters;
    private LinkedHashMap<String, CCParameter> versionFiltersCCParameters;

    /**
     * Creates a new instance of CCManager which considers a properties file
     * which contains the Virtuoso credentials and a specific changes ontology
     * schema.
     *
     * @param prop The properties file.
     * @param changesOntologySchema The changes ontology schema
     * @throws ClassNotFoundException
     * @throws RepositoryException
     * @throws SQLException
     */
    public CCManager(Properties prop, String changesOntologySchema) throws Exception {
        String ip = prop.getProperty("Repository_IP");
        String username = prop.getProperty("Repository_Username");
        String password = prop.getProperty("Repository_Password");
        int port = Integer.parseInt(prop.getProperty("Repository_Port"));
        this.jdbcRep = new JDBCVirtuosoRep(ip, port, username, password);
        this.sesameRepos = new SesameVirtRep(ip, port, username, password);
        if (changesOntologySchema == null) {
            this.changesOntologySchema = prop.getProperty("Changes_Ontology_Schema");
        } else {
            this.changesOntologySchema = changesOntologySchema;
        }
        this.cChangeParameters = new LinkedHashMap<>();
        this.sChanges = new ArrayList<>();
        this.ccDefError = new CCDefinitionError();
        this.versionFilters = new LinkedHashMap<>();
    }

    public CCManager(Properties prop) throws Exception {
        try {
            String ip = prop.getProperty("Repository_IP");
            String username = prop.getProperty("Repository_Username");
            String password = prop.getProperty("Repository_Password");
            int port = Integer.parseInt(prop.getProperty("Repository_Port"));
            this.jdbcRep = new JDBCVirtuosoRep(ip, port, username, password);
            this.sesameRepos = new SesameVirtRep(ip, port, username, password);
            this.changesOntologySchema = prop.getProperty("Changes_Ontology_Schema");
            this.cChangeParameters = new LinkedHashMap<>();
            this.sChanges = new ArrayList<>();
            this.ccDefError = new CCDefinitionError();
            this.versionFilters = new LinkedHashMap<>();
        } catch (NumberFormatException ex) {
            System.out.println("Exception: " + ex.toString());
        }
    }

    public CCManager() {
        this.ccDefError = new CCDefinitionError();
    }

    /**
     * Checks if the given complex change name is valid.
     *
     * @param name The complex change name.
     * @return True if the name is valid, false otherwise.
     */
    public boolean isValidCCName(String name) {
        String sparql = "select * from <" + changesOntologySchema + "> where { ?cc co:name \"" + name + "\"}";
        ResultSet results = jdbcRep.executeSparqlQuery(sparql, false);
        try {
            boolean result = results.next();
            results.close();
            return !result;
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Checks if the given complex change priority is valid.
     *
     * @param priority The complex change priority.
     * @return True if the name is valid, false otherwise.
     */
    private boolean isValidCCPriority(double priority) {
        String sparql = "select * from <" + changesOntologySchema + "> where { ?cc co:priority ?pr. \n"
                + "filter (?pr = " + priority + ").\n"
                + "}";
        ResultSet results = jdbcRep.executeSparqlQuery(sparql, false);
        try {
            boolean result = results.next();
            results.close();
            return !result;
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Sets the name of a complex change by checking if the given name is valid.
     *
     * @param ccName The complex change name.
     */
    public void setCcName(String ccName) {
        if (isValidCCName(ccName)) {
            this.ccName = ccName;
        } else {
            setCcDefError("There already exists a complex change with the same name.", CODE.NON_UNIQUE_CC_NAME);
        }
    }

    /**
     * Sets the priority of a complex change.
     *
     * @param ccPriority
     * @param ccName The complex change name.
     */
    public void setCcPriority(Double ccPriority) {
        if (isValidCCPriority(ccPriority)) {
            this.ccPriority = ccPriority;
        } else {
            setCcDefError("There already exists a complex change with the same priority.", CODE.NON_UNIQUE_CC_PRIORITY);
        }
    }

    /**
     * Sets a human-readable description for the complex change/
     *
     * @param description The complex change description.
     */
    public void setCcDescription(String description) {
        this.ccDescription = description;
    }

    public CCDefinitionError getCcDefError() {
        return ccDefError;
    }

    public void setCcDefError(String descr, CODE code) {
        deleteComplexChange(changesOntologySchema, ccName, false);
        this.ccDefError.setDescription(descr);
        this.ccDefError.setErrorCode(code);
    }

    /**
     * Inserts a new complex change parameter into the current complex change
     * definition. The method also checks if the added parameter has a unique
     * name.
     *
     * @param name The complex change parameter name
     * @param scParameter The simple change parameter name which is associated
     * with the complex change parameter.
     * @return True if the complex change parameter is assigned successfully,
     * false otherwise.
     */
    public boolean addCCParameter(String name, String scParameter) {
        CCParameter param = new CCParameter(name, scParameter);
        if (cChangeParameters.get(name) != null) {  //parameter with the same name exists
            setCcDefError("There is already given a complex change parameter with the same name.",
                    CODE.NON_UNIQUE_CC_PARAM_NAME);
            return false;
        } else {
            cChangeParameters.put(name, param);
            return true;
        }
    }

    /**
     * Associates a new simple change definitions with the current comple change
     * definition.
     *
     * @param change The simple change to be associated.
     * @return
     */
    public boolean addSimpleChange(SCDefinition change) {
        return sChanges.add(change);
    }

    /**
     * Return a list of all the associated simple changes.
     *
     * @return
     */
    public List<SCDefinition> getSChanges() {
        return sChanges;
    }

    /**
     * Inserts a complex change definition into the changes ontology schema.
     *
     * @return An instance of {@link CCDefinitionError} whose {@link ErrorCode}
     * is NULL if the complex change is inserted successfully. Otherwise, the
     * {@link ErrorCode} denotes the type of the error.
     * @throws RuntimeException
     */
    public CCDefinitionError insertChangeDefinition() throws RuntimeException {
        if (getCcDefError().getErrorCode() != null) {
            return ccDefError;
        }
        HashMap<String, String> namespaces = new HashMap<>();
        namespaces.put("co", "http://www.diachron-fp7.eu/changes/");
        namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        String ccUri = namespaces.get("co") + ccName.replaceAll(" ", "_");
        this.cChangeURI = ccUri;
        sesameRepos.addTriple(ccUri, namespaces.get("rdfs") + "subClassOf", namespaces.get("co") + "Complex_Change", changesOntologySchema);
        sesameRepos.addLitTriple(ccUri, namespaces.get("co") + "name", ccName, changesOntologySchema);
        sesameRepos.addLitTriple(ccUri, namespaces.get("co") + "priority", ccPriority, changesOntologySchema);
        if (this.ccDescription != null) {
            sesameRepos.addLitTriple(ccUri, namespaces.get("co") + "description", this.ccDescription, changesOntologySchema);
        }
        for (SCDefinition sc : sChanges) {
            sesameRepos.addTriple(ccUri, namespaces.get("co") + "consumes", namespaces.get("co") + sc.getsChangeType(), changesOntologySchema);
        }
        /////
        int i = 1;
        for (String param : cChangeParameters.keySet()) {
            CCParameter parameter = cChangeParameters.get(param);
            String pProp = ccUri + "/p" + i;
            parameter.setParamProp(pProp);
            addCCParamTriples(pProp, namespaces, ccUri, parameter.getParamNameString());  //insert triples into virtuoso
            i++;
        }
        if (cChangeParameters.isEmpty()) {
            setCcDefError("No complex change parameters are provided.",
                    CODE.NO_CC_PARAM);
            return ccDefError;
        }
        // check if complex change parameters which appear in version filters, are defined 
        versionFiltersCCParameters = new LinkedHashMap<>();
        boolean ccParamFound = true;
        for (Presence pr : versionFilters.keySet()) {
            List<VersionFilter> vFilters = versionFilters.get(pr);
            for (VersionFilter vFilt : vFilters) {
                List<String> ccParams = vFilt.getComplexChangeParameters();
                for (String param : ccParams) {
                    //check if a version filter cc parameter is not defined in 
                    //the complex change parameters section
                    if (cChangeParameters.get(param) == null) {
                        ccParamFound = false;
                        break;
                    }
                }
                if (!ccParamFound) {
                    break;
                }
            }
        }
        if (!ccParamFound) {
            setCcDefError("Version filters use undefined complex change parameters.",
                    CODE.CC_PARAM_FILTER_ERROR);
            return ccDefError;
        }
        //////
        this.cChangeSparql = createSPARQLQuery();
        if (this.cChangeSparql == null) {
            deleteComplexChange(changesOntologySchema, ccName, false);
            return ccDefError;
        }
        if (!isValidSPARQL()) {
            setCcDefError("The constructed SPARQL query is invalid.", CODE.INVALID_SPARQL);
            return ccDefError;
        } else {
            sesameRepos.addLitTriple(ccUri, namespaces.get("co") + "sparql", cChangeSparql, changesOntologySchema);
            sesameRepos.addLitTriple(ccUri, namespaces.get("co") + "json", toJSON(ccName, ccPriority), changesOntologySchema);
        }
//        this.deleteComplexChangeInstWithLessPr(, ccName); //update the changes ontology schema accordingly 
        return ccDefError;
    }

    /**
     * Checks if the constructed SPARQL update query, which is responsible for
     * the detection of this complex change, is valid.
     *
     * @return True if the constructed SPARQL update is valid, false otherwise.
     */
    public boolean isValidSPARQL() {
        String sparql = this.cChangeSparql.replace("changesOntologySchema", "changesOntology_test");
        Statement statement = jdbcRep.getStatement();
        try {
            statement.executeQuery("log_enable(3,1)");
            statement.executeUpdate("sparql " + sparql + " limit 1");
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            return false;
        }
        return true;
    }

    /**
     * Deletes all the detected complex change instances contained in the
     * changes ontology given as parameter.
     *
     * @param changesOntology The complex change which will be examined.
     * @param detectedOnly A flag which denotes whether only the detected
     * changes of the defined complex changes will be deleted or not.
     * @return True if there was deleted any complex change detection instance,
     * false otherwise.
     */
    public boolean deleteAllComplexChanges(String changesOntology, boolean detectedOnly) {
        List<String> ccNames = new ArrayList<>();
        String query = "select ?cc_name from <" + changesOntologySchema + "> where { "
                + "?cc rdfs:subClassOf co:Complex_Change; "
                + "co:name ?cc_name. "
                + "} order by ?cc_name ";
        ResultSet results = jdbcRep.executeSparqlQuery(query.toString(), false);
        try {
            if (!results.next()) {
                results.close();
                return false;
            }
            do {
                ccNames.add(results.getString(1));
            } while (results.next());
            results.close();
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        return deleteComplexChanges(changesOntology, ccNames, detectedOnly);
    }

    /**
     * Deletes the detected instances of all with complex changes higher
     * priority value than the complex change given as parameter. This means
     * that these complex changes are less important.
     *
     * @param changesOntology The complex change which will be examined.
     * @param ccName The complex change whose priority is considered.
     * @return
     */
    public boolean deleteComplexChangeInstWithLessPr(String changesOntology, String ccName) {
        HashMap<String, String> namespaces = new HashMap<>();
        namespaces.put("co", "http://www.diachron-fp7.eu/changes/");
        namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        String sparql = "SELECT distinct ?priority FROM <" + changesOntologySchema + "> WHERE {\n"
                + "?ccURI co:name '" + ccName + "';\n"
                + "co:priority ?priority.\n"
                + "}";
        ResultSet results = jdbcRep.executeSparqlQuery(sparql, false);
        String priority = null;  //store priority of the given complex change
        try {
            while (results.next()) {
                priority = results.getString(1);
            }
            results.close();
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage());
            return false;
        }
        if (priority == null) {
            return false;
        }
        sparql = "SELECT distinct ?ccURI FROM <" + changesOntologySchema + "> WHERE {\n"
                + "?ccURI co:name ?name;\n"
                + "co:priority ?priority.\n"
                + "filter (?priority > " + priority + ").\n"
                + "}";
        results = jdbcRep.executeSparqlQuery(sparql, false);
        List<String> cChangesUris = new ArrayList<>();
        try {
            while (results.next()) {  //fetch cc uris with higher priority (i.e., less important)
                cChangesUris.add(results.getString(1));
            }
            results.close();
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage());
            return false;
        }
        for (String ccUri : cChangesUris) {
            deleteDetectedCCUriTriples(changesOntology, ccUri);  //delete the instances of the complex changes
        }
        return true;
    }

    /**
     * Deletes the detected instances of the complex change with name given as
     * parameter from the changes ontology with name also given as parameter.
     *
     * @param changesOntology The changes ontology which contains possible
     * detected instances of the given complex change
     * @param ccName The name of the complex change.
     * @param detectedOnly A flag which denotes whether the detected changes of
     * change with name ccName will be deleted (true) or the definition of
     * change as well.
     * @return True if there were found and deleted detected instances of the
     * given complex change, false otherwise.
     */
    public boolean deleteComplexChange(String changesOntology, String ccName, boolean detectedOnly) {
        HashMap<String, String> namespaces = new HashMap<>();
        namespaces.put("co", "http://www.diachron-fp7.eu/changes/");
        namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        String sparql = "SELECT distinct ?ccURI ?ccParam FROM <" + changesOntologySchema + "> WHERE {\n"
                + "?ccURI co:name '" + ccName + "'.\n"
                + "OPTIONAL {"
                + "?ccURI ?ccParam ?ccParamValue.\n"
                + "?ccParam co:name ?n.\n"
                + "}\n"
                + "}";
        ResultSet results = jdbcRep.executeSparqlQuery(sparql, false);
        String ccUri = null;
        ArrayList<String> ccParams = new ArrayList<>();
//        ArrayList<String> ccParamNames = new ArrayList<>();
        boolean success = false;
        try {
            while (results.next()) {
                success = true;
                ccUri = results.getString("ccURI");
                String param = results.getString("ccParam");
                if (param != null) {
                    ccParams.add(param);
                }
            }
            results.close();
            if (!success) {
                return false;
            }
            deleteCCUriTriples(changesOntology, ccName, ccUri, detectedOnly);
            if (!ccParams.isEmpty()) {
                deleteCCParamTriples(changesOntology, ccParams, detectedOnly);
            }
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured .");
            jdbcRep.terminate();
            return false;
        }
        return success;
    }

    /**
     * Deletes all the detected instances of the complex changes with names
     * given as parameter from the changes ontology with name also given as
     * parameter.
     *
     * @param changesOntology The changes ontology which contains possible
     * detected instances of the given complex change
     * @param ccNames The list of names of the complex changes.
     * @param detectedOnly A flag which denotes whether the detected changes of
     * change with name ccName will be deleted (true) or the definition of
     * change as well.
     * @return True if there were found and deleted detected instances of at
     * least one of the given complex changes, false otherwise.
     */
    public boolean deleteComplexChanges(String changesOntology, List<String> ccNames, boolean detectedOnly) {
        boolean result = false;
        for (String change : ccNames) {
            boolean tmp = deleteComplexChange(changesOntology, change, detectedOnly);
            if (tmp) {
                result = tmp;
            }
        }
        return result;
    }

    /**
     * Stores a complex change into the ontology of changes.
     *
     * @param name The name of the complex change.
     * @param priority The priority of the complex change.
     * @param description
     * @param scDefinitions A list of associated complex changes
     * @param ccParameters A map of complex change parameters which contains the
     * complex change parameter names (as keys) and the optional associated
     * simple change parameter names (as values)
     * @param versionFilters The list of version filters.
     * @param associations
     * @return An instance of {@link CCDefinitionError} whose {@link ErrorCode}
     * is NULL if the complex change is inserted successfully. Otherwise, the
     * {@link ErrorCode} denotes the type of the error.
     */
    public CCDefinitionError saveCCExtendedDefinition(String name, Double priority, String description, List<SCDefinition> scDefinitions, Map<String, String> ccParameters, List<VersionFilter> versionFilters) {
        setCcName(name);
        if (this.ccDefError.getErrorCode() != null) {
            return this.ccDefError;
        }
        setCcPriority(priority);
        if (this.ccDefError.getErrorCode() != null) {
            return this.ccDefError;
        }
        this.ccDescription = description;
//        boolean mandatoryExists = false;
//        for (SCDefinition scDef : scDefinitions) {
//            if (scDef.isIsOptional() == false) {
//                mandatoryExists = true;
//            }
//        }
//        if (!mandatoryExists) {
//            ccDefError.setDescription("No mandatory Simple Change is assigned.");
//            ccDefError.setErrorCode(CODE.NO_MANDATORY_SC_ASSIGNED);
//            return ccDefError;
//        }
        this.sChanges.addAll(scDefinitions);
        if (ccParameters == null || ccParameters.isEmpty()) {
            ccDefError.setDescription("No complex change parameters are provided.");
            ccDefError.setErrorCode(CODE.NO_CC_PARAM);
            return ccDefError;
        }
        for (String key : ccParameters.keySet()) { //add cc parameters
            if (!this.addCCParameter(key, ccParameters.get(key))) {
                return this.ccDefError;
            }
        }
        groupFiltersPerPresence(versionFilters);
        return insertChangeDefinition();
    }

    /**
     * Groups the given list of version filters w.r.t. their presence.
     *
     * @param vfilters The list of version filters.
     */
    public void groupFiltersPerPresence(List<VersionFilter> vfilters) {
        if (vfilters == null) {
            return;
        }
        for (VersionFilter filter : vfilters) {
            if (versionFilters.get(filter.getPresence()) == null) {
                versionFilters.put(filter.getPresence(), new ArrayList<VersionFilter>());
            }
            List<VersionFilter> filters = versionFilters.get(filter.getPresence());
            filters.add(filter);
        }
    }

    public JDBCVirtuosoRep getJdbcRep() {
        return jdbcRep;
    }

    public SesameVirtRep getSesameRep() {
        return sesameRepos;
    }

    /**
     * Terminates the JDBC Virtuoso and Sesame connections.
     */
    public void terminate() {
        if (jdbcRep != null) {
            jdbcRep.terminate();
        }
        if (sesameRepos != null) {
            sesameRepos.terminate();
        }
    }

    private void deleteDetectedCCUriTriples(String changesOntology, String ccUri) {
        String sparul = "DELETE WHERE {\n" //delete detected cc uris
                + "GRAPH <" + changesOntology + "> {\n"
                + "?dcc a <" + ccUri + "> ;"
                + "  ?p ?o.\n"
                + "}\n"
                + "}\n";
        jdbcRep.executeUpdateQuery("sparql " + sparul, false);
    }

    private void deleteCCUriTriples(String changesOntology, String ccName, String ccUri, boolean detectedOnly) {
        String sparul = "DELETE WHERE {\n"
                + "GRAPH <" + changesOntologySchema + "> {\n"
                + "  ?ccURI co:name '" + ccName + "';\n"
                + "     ?ccParam ?ccParamName.\n"
                + "}\n"
                + "}\n";
        if (!detectedOnly) {
            jdbcRep.executeUpdateQuery("sparql " + sparul, false);
        }
        sparul = "DELETE WHERE {\n" //delete detected cc uris
                + "GRAPH <" + changesOntology + "> {\n"
                + "?dcc a <" + ccUri + "> ;"
                + "  ?p ?o.\n"
                + "}\n"
                + "}\n";
        jdbcRep.executeUpdateQuery("sparql " + sparul, false);
    }

    private void deleteCCParamTriples(String changesOntology, ArrayList<String> ccParams, boolean detectedOnly) {
        String sparul;
        for (String ccParam : ccParams) {
            sparul = "DELETE WHERE {\n"
                    + "GRAPH <" + changesOntologySchema + "> {\n"
                    + "<" + ccParam + "> ?p ?o.\n"
                    + "}\n"
                    + "}\n";
            if (!detectedOnly) {
                jdbcRep.executeUpdateQuery("sparql " + sparul, false);
                sparul = "DELETE WHERE {\n"
                        + "GRAPH <" + changesOntologySchema + "> {\n"
                        + "?s <" + ccParam + "> ?o.\n"
                        + "}\n"
                        + "}\n";
                jdbcRep.executeUpdateQuery("sparql " + sparul, false);
            }
            sparul = "DELETE WHERE {\n"
                    + "GRAPH <" + changesOntology + "> {\n"
                    + "?s ?p <" + ccParam + ">.\n"
                    + "}\n"
                    + "}\n";
            jdbcRep.executeUpdateQuery("sparql " + sparul, false);
        }
    }

    private void addCCParamTriples(String pProp, HashMap<String, String> namespaces, String name, String paramName) {
        sesameRepos.addTriple(pProp, namespaces.get("rdf") + "type", namespaces.get("rdf") + "Property", changesOntologySchema);
        sesameRepos.addTriple(pProp, namespaces.get("rdfs") + "domain", name, changesOntologySchema);
        sesameRepos.addTriple(pProp, namespaces.get("rdfs") + "range", namespaces.get("rdfs") + "Resource", changesOntologySchema);
        sesameRepos.addTriple(name, pProp, namespaces.get("rdfs") + "Resource", changesOntologySchema);
        sesameRepos.addLitTriple(pProp, namespaces.get("co") + "name", paramName, changesOntologySchema);
    }

    private String createSPARQLQuery() throws RuntimeException {
        StringBuilder sparqlQuery = new StringBuilder();
        LinkedHashMap<String, String> ccParamsMap = new LinkedHashMap<>(); //associate each cc param value uri with the param's name 

        sparqlQuery.append("PREFIX diachron:<http://www.diachron-fp7.eu/resource/>\n").
                append("PREFIX efo:<http://www.ebi.ac.uk/efo/>\n").
                append("PREFIX co:<http://www.diachron-fp7.eu/changes/>\n");
        sparqlQuery.append("INSERT INTO <changesOntology> {\n");
        sparqlQuery.append("?cc a <" + cChangeURI + ">.\n");
        int i = 1;
        for (String param : cChangeParameters.keySet()) {
            sparqlQuery.append("?cc <" + cChangeParameters.get(param).getParamProp() + "> ?cc_v" + i + ".\n");
            ccParamsMap.put(cChangeParameters.get(param).getParamNameString(), "cc_v" + i);
            i++;
        }
        // add the complex change parameters from the version filters, if any
        for (String param : versionFiltersCCParameters.keySet()) {
            sparqlQuery.append("?cc <" + versionFiltersCCParameters.get(param).getParamProp() + "> ?cc_v" + i + ".\n");
            ccParamsMap.put(versionFiltersCCParameters.get(param).getParamNameString(), "cc_v" + i);
            i++;
        }
        // add the complex change parameters from the associations, if any

        ///
        for (SCDefinition sChange : sChanges) {
            sparqlQuery.append("?cc co:consumes ?" + createVarFromURI(sChange.getsChangeUri()) + ".\n");
        }

        i = 1;
        sparqlQuery.append("}\n");
        sparqlQuery.append("WHERE {\n");
        sparqlQuery.append("GRAPH <changesOntology> {\n");
        if (insertSCBlocks(sparqlQuery, i, ccParamsMap)) {
            return null;
        }
        /// Version Filters
        insertVersFiltBlocks(ccParamsMap, sparqlQuery);
        //url construction
        i = 1;
        String url = "BIND(CONCAT(";
        for (String param : cChangeParameters.keySet()) {
            url += "STR(?cc_v" + i + ") ";
            i++;
            if (i <= cChangeParameters.size()) {
                url += ", ";
            }
        }
        url += ", 'changesOntology') AS ?url) .";
        sparqlQuery.append(url + "\n");
        sparqlQuery.append("BIND(IRI(CONCAT('" + cChangeURI + "/dcc/',SHA1(?url))) AS ?cc).\n");
        sparqlQuery.append("}\n}\n");
        System.out.println(sparqlQuery);
        return sparqlQuery.toString();
    }

    private void insertVersFiltBlocks(LinkedHashMap<String, String> ccParamsMap, StringBuilder sparqlQuery) {
        List<VersionFilter> group;
        cChangeParameters.putAll(versionFiltersCCParameters);
        for (Presence pr : versionFilters.keySet()) {
            group = versionFilters.get(pr);
            StringBuilder versionFilterBlock = new StringBuilder();
            String graph;
            if (pr == Presence.EXISTS_IN_V2) {
                graph = "GRAPH <v2>";
            } else if (pr == Presence.EXISTS_IN_V1) {
                graph = "GRAPH <v1>";
            } else if (pr == Presence.NOT_EXISTS_IN_V1) {
                graph = "FILTER NOT EXISTS { GRAPH <v1>";
            } else {
                graph = "FILTER NOT EXISTS { GRAPH <v2>";
            }
            int i = 0;
            if (pr == Presence.EXISTS_IN_V2 || pr == Presence.EXISTS_IN_V1) {
                versionFilterBlock.append(graph + " {\n");
                for (VersionFilter vFilter : group) {
                    String subject = vFilter.getSubject();
                    String predicate = vFilter.getPredicate();
                    String object = vFilter.getObject();
                    subject = transformVariablePart(subject, ccParamsMap, i);
                    predicate = transformVariablePart(predicate, ccParamsMap, i);
                    object = transformVariablePart(object, ccParamsMap, i);
                    versionFilterBlock.append(subject + " " + predicate + " " + object + ".\n");
                    i++;
                }
                versionFilterBlock.append("}\n");
            } else {
                for (VersionFilter vFilter : group) {
                    versionFilterBlock.append(graph + " {");
                    String subject = vFilter.getSubject();
                    String predicate = vFilter.getPredicate();
                    String object = vFilter.getObject();
                    subject = transformVariablePart(subject, ccParamsMap, i);
                    predicate = transformVariablePart(predicate, ccParamsMap, i);
                    object = transformVariablePart(object, ccParamsMap, i);
                    versionFilterBlock.append(subject + " " + predicate + " " + object + ".");
                    versionFilterBlock.append("} }\n");
                    i++;
                }
            }
            sparqlQuery.append(versionFilterBlock);
        }
    }

    private boolean insertSCBlocks(StringBuilder sparqlQuery, int i, LinkedHashMap<String, String> ccParamsMap) {
        for (SCDefinition sChange : sChanges) {
            if (sChange.isIsOptional()) {
                sparqlQuery.append("OPTIONAL {\n");
            }
            String block = OntologicalSimpleChangesBlocks.fetchSPARQLBlock(sChange.getsChangeType(), createVarFromURI(sChange.getsChangeUri()));
            sparqlQuery.append(block + "\n");
            sparqlQuery.append("FILTER NOT EXISTS { ?dcc" + i + " co:consumes ?" + createVarFromURI(sChange.getsChangeUri()) + ". }.\n");
            // selection filters 
            if (!sChange.getSelectionFilters().isEmpty()) {
                try {
                    for (String filter : sChange.getSelectionFilters()) {
                        String oper = fetchOperator(filter);
                        String[] selFilter = filter.split(oper);
                        String value = selFilter[1].trim();
//                        if (value.startsWith("http")) {
//                            value = "<" + value + ">";
//                        } else {
//                            value = "'" + value + "'";
//                        }
                        String scParam = selFilter[0].trim();
                        String[] arr = scParam.split(":-");
                        String scParUri = OntologicalSimpleChangesBlocks.fetchSCParamsURIs(sChange.getsChangeType(), createVarFromURI(sChange.getsChangeUri())).get(arr[1]);
                        sparqlQuery.append("FILTER (?" + scParUri + oper + value + ").\n");
                    }
//                    String filter = sChange.getSelectionFilters();
                } catch (Exception ex) {
                    setCcDefError("Something is wrong in Selection Filters", CODE.SELECTION_FILTER_ERROR);
                    return true;
                }
            }
            // join filters 
            if (!sChange.getJoinFilters().isEmpty()) {
                try {
                    for (String filter : sChange.getJoinFilters()) {
                        String oper = fetchOperator(filter);
                        String[] joinFilter = filter.split(oper);
                        String s0 = joinFilter[0].trim();
                        String s1 = joinFilter[1].trim();
                        String[] arr0 = s0.split(":-");
                        String[] arr1 = s1.split(":-");
                        OntologicalSimpleChangesType type1 = getChangeTypeFromURI(createVarFromURI(arr1[0]));
                        String scParUri1 = OntologicalSimpleChangesBlocks.fetchSCParamsURIs(type1, createVarFromURI(arr1[0])).get(arr1[1]);
                        OntologicalSimpleChangesType type0 = getChangeTypeFromURI(createVarFromURI(arr0[0]));
                        String scParUri0 = OntologicalSimpleChangesBlocks.fetchSCParamsURIs(type0, createVarFromURI(arr0[0])).get(arr0[1]);
                        sparqlQuery.append("FILTER (?" + scParUri1 + " = ?" + scParUri0 + ").\n");
                    }
                } catch (Exception ex) {
                    setCcDefError("Something is wrong in Join Filters", CODE.JOIN_FILTER_ERROR);
                    return true;
                }
            }
            if (sChange.isIsOptional()) {
                sparqlQuery.append("}\n");
            }
            // parameter filters
            for (String ccParName : cChangeParameters.keySet()) {
                try {
                    CCParameter ccParam = cChangeParameters.get(ccParName);
                    String scParam = ccParam.getScBoundParam();
                    String[] arr = scParam.split(":-");
                    String sChangeUri = createVarFromURI(sChange.getsChangeUri());
                    arr[0] = createVarFromURI(arr[0]);
                    if (sChangeUri.equals(arr[0])) {  //this simple change's param is bound with a complex change param
                        String ccParUri = ccParamsMap.get(ccParName);
                        String scParUri = OntologicalSimpleChangesBlocks.fetchSCParamsURIs(sChange.getsChangeType(), sChangeUri).get(arr[1]);
                        if (!sChange.isIsOptional()) {
                            sparqlQuery.append("BIND (?" + scParUri + " AS ?" + ccParUri + ").\n");
                        } else {
                            sparqlQuery.append("BIND(if(BOUND(?" + scParUri + "),?" + scParUri + ",'UNBOUNDED_VALUE') AS ?" + ccParUri + ").\n");
                        }
                    }
                } catch (Exception ex) {
                    setCcDefError("Something is wrong in CC Parameter Filters", CODE.CC_PARAM_FILTER_ERROR);
                    return true;
                }
            }
            i++;
        }
        return false;
    }

    private String transformVariablePart(String part, HashMap<String, String> ccParamsMap, int varCnt) {
        if (part == null) {
            part = "?var" + varCnt;
        } else if (part.contains(":-")) {  //the triple part is a simple change parameter
            String[] arr = part.split(":-");
            OntologicalSimpleChangesType type1 = getChangeTypeFromURI(createVarFromURI(arr[0]));
            String scParUri1 = OntologicalSimpleChangesBlocks.fetchSCParamsURIs(type1, createVarFromURI(arr[0])).get(arr[1]);
            part = "?" + scParUri1;
        } else if (part.startsWith("<") || part.startsWith("'")) { //the triple part is a specific URI or literal
            ;
        } else { //the triple part is a complex change parameter
            part = "?" + ccParamsMap.get(part);
        }
        return part;
    }

    private String createVarFromURI(String uri) {
        int end = uri.indexOf(":");
        if (end == -1) {
            return uri;
        }
        return "sc" + uri.substring(0, end);
    }

    private String fetchOperator(String filter) {
        String oper = null;
        oper = ">=";
        if (filter.indexOf(oper) == -1) {
            oper = "<=";
        }
        if (filter.indexOf(oper) == -1) {
            oper = "!=";
        }
        if (filter.indexOf(oper) == -1) {
            oper = "=";
        }
        if (filter.indexOf(oper) == -1) {
            oper = "<";
        }
        if (filter.indexOf(oper) == -1) {
            oper = ">";
        }
        return oper;
    }

    private OntologicalSimpleChangesType getChangeTypeFromURI(String uri) {
        for (SCDefinition sChange : sChanges) {
            if (uri.equals(createVarFromURI(sChange.getsChangeUri()))) {
                return sChange.getsChangeType();
            }
        }
        return null;
    }

    private String toJSON(String cChangeName, double cChangePriority) {
        JSONObject ccJSON = new JSONObject();
        ccJSON.put("Complex_Change", cChangeName);
        ccJSON.put("Priority", cChangePriority);
        ccJSON.put("Description", this.ccDescription);
        JSONArray ccJsonParams = new JSONArray();
        for (String ccParamName : cChangeParameters.keySet()) {
            CCParameter parameter = cChangeParameters.get(ccParamName);
            if (parameter.getParamFilter() != null) {
                JSONObject ccParamJSON = new JSONObject();
                ccParamJSON.put(ccParamName, parameter.getParamFilter());
                ccJsonParams.add(ccParamJSON);
            }
        }
        ccJSON.put("Complex_Change_Parameters", ccJsonParams);
        JSONArray scJson = new JSONArray();
        for (SCDefinition sc : sChanges) {
            scJson.add(sc.toJSON());
        }
        ccJSON.put("Simple_Changes", scJson);
        JSONArray jsonArray = new JSONArray();
        for (Presence pr : versionFilters.keySet()) {
            List<VersionFilter> filters = versionFilters.get(pr);
            for (VersionFilter filter : filters) {
                jsonArray.add(filter.toJson());
            }
        }
        ccJSON.put("Version_Filters", jsonArray);
        System.out.println(ccJSON.toJSONString());
        return ccJSON.toJSONString();
    }

    /**
     * Converts a SPARQL update query into a SPARQL select query. Moreover, the
     * query is stored as a {@link Properties} file with all the required meta
     * data.
     *
     * @param sparul
     * @param scUris
     * @return
     */
    public static Properties convertSPARQL(String sparul, List<String> scUris) {
        Properties ccProp = new Properties();
        int start = sparul.indexOf("{");
        int end = sparul.indexOf("}");
        String insertBlock = sparul.substring(start + 1, end);
        String[] insertStmts = insertBlock.split("\\?cc");
        String type = insertStmts[1].trim();
        ccProp.put("type", type.substring(0, type.length() - 1));
        int cnt = 1;
        String stmt;
        for (int i = 2; i < insertStmts.length; i++) {
            stmt = insertStmts[i].trim();
            if (stmt.charAt(0) == '<') {
                ccProp.put("var" + cnt, stmt);
                ccProp.put("type_var" + cnt, "URI,LITERAL");
                cnt++;
            }
        }
        StringBuilder sparqlSelect = new StringBuilder();
        sparqlSelect.append("select ");
        for (int i = 1; i < cnt; i++) {
            sparqlSelect.append("?cc_v").append(i).append(" ");
        }
        for (String scUri : scUris) {
            sparqlSelect.append(scUri).append(" ");
        }
        sparqlSelect.append(sparul.substring(end + 1));
        ccProp.put("query", sparqlSelect.toString());
        return ccProp;
    }

}
