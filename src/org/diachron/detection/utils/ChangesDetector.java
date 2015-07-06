/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diachron.detection.utils;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.openrdf.repository.RepositoryException;
import org.diachron.detection.repositories.JDBCVirtuosoRep;

/**
 * This class is responsible for the change detection between two named graph
 * versions. The change detection considers a set of SPARQL update queries (one
 * per change type) and populates accordingly a named graph which is called
 * changes ontology. The detected changes are stored w.r.t. a changes ontology
 * schema.
 *
 * @author rousakis
 */
public class ChangesDetector {

    private JDBCVirtuosoRep jdbc;
    private String changesOntology;
    private String changesOntologySchema;
    private String simpleChangesFolder;
    private String[] simpleChanges;

    /**
     * Creates a new ChangesDetector instance w.r.t. a properties file given as
     * parameter.
     *
     * @param prop The properties file which denotes a) the credentials for a
     * JDBC Virtuoso connection, b) the changes ontology which will store the
     * detected changes, c) the changes ontology schema and d) the folder which
     * contains the SPARQL update queries.
     * @throws ClassNotFoundException
     * @throws RepositoryException
     * @throws SQLException
     */
    public ChangesDetector(Properties prop) throws ClassNotFoundException, RepositoryException, SQLException {
        initialize(prop);
    }

    public ChangesDetector(String propFile) throws ClassNotFoundException, IOException, RepositoryException, SQLException {
        Properties prop = new Properties();
        InputStream inputStream = new FileInputStream(propFile);
        prop.load(inputStream);
        initialize(prop);
        inputStream.close();
    }

    /**
     * Creates a new ChangesDetector instance w.r.t. a properties file given as
     * parameter, a changes ontology and a changes ontology schema.
     *
     * @param prop The properties file which denotes a) the credentials for a
     * JDBC Virtuoso connection and b) the folder which contains the SPARQL
     * update queries.
     * @param changesOnt The changes ontology.
     * @param changesOntSchem The changes ontology schema.
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws RepositoryException
     * @throws SQLException
     */
    public ChangesDetector(Properties prop, String changesOnt, String changesOntSchem) throws ClassNotFoundException, IOException, RepositoryException, SQLException {
        initialize(prop);
        this.changesOntology = changesOnt;
        this.changesOntologySchema = changesOntSchem;
    }

    public ChangesDetector(String propFile, String changesOnt, String changesOntSchem) throws ClassNotFoundException, IOException, RepositoryException, SQLException {
        Properties prop = new Properties();
        InputStream inputStream = new FileInputStream(propFile);
        prop.load(inputStream);
        initialize(prop);
        inputStream.close();
        this.changesOntology = changesOnt;
        this.changesOntologySchema = changesOntSchem;
    }

    private void initialize(Properties prop) throws NumberFormatException, SQLException, RepositoryException, ClassNotFoundException {
        String ip = prop.getProperty("Repository_IP");
        String username = prop.getProperty("Repository_Username");
        String password = prop.getProperty("Repository_Password");
        int port = Integer.parseInt(prop.getProperty("Repository_Port"));
        jdbc = new JDBCVirtuosoRep(ip, port, username, password);
        changesOntology = prop.getProperty("Changes_Ontology");
        changesOntologySchema = prop.getProperty("Changes_Ontology_Schema");
        simpleChangesFolder = prop.getProperty("Simple_Changes_Folder");
        simpleChanges = prop.getProperty("Simple_Changes").split(",");
    }

    public void detectAssociations(String oldVersion, String newVersion, String associationsGraph) {
        if (associationsGraph == null) {
            return;
        }
        System.out.println("-------------");
        System.out.println("Associations Detection among versions:");
        System.out.println(oldVersion + " (" + jdbc.triplesNum(oldVersion) + " triples)");
        System.out.println(newVersion + " (" + jdbc.triplesNum(newVersion) + " triples)");
        System.out.println("-------------");
        long oldSize = jdbc.triplesNum(changesOntology);
        System.out.print("Detecting simple change associations...");
        long start = System.currentTimeMillis();
        String query = null;
        String prefixes = "PREFIX diachron: <http://www.diachron-fp7.eu/resource/>\n"
                + "PREFIX efo:<http://www.ebi.ac.uk/efo/>\n"
                + "PREFIX co:<http://www.diachron-fp7.eu/changes/>\n";
        query = "INSERT INTO <" + changesOntology + "> {\n"
                + "?assoc a co:Association;\n"
                + "    co:assoc_p1 ?a;\n"
                + "    co:assoc_p2 ?b.\n"
                + "}\n"
                + "WHERE {\n"
                + "GRAPH <" + associationsGraph + "> { \n"
                + "?s co:old_value ?a.\n"
                + "?s co:new_value ?b.\n"
                + "}\n"
                + "BIND(concat(str(?a), str(?b)) as ?url) .\n"
                + "BIND(IRI(CONCAT('http://assoc/',SHA1(?url))) AS ?assoc).\n"
                + "}";
//            System.out.println("Detecting " + simpleChange);
        jdbc.executeUpdateQuery("sparql " + prefixes + query, true);
        consumeSCFromAssoc(prefixes);
        System.out.println("DONE in " + (System.currentTimeMillis() - start));
        System.out.println("Association triples size: " + (jdbc.triplesNum(changesOntology) - oldSize));
    }

    private void consumeSCFromAssoc(String prefixes) {
        String query;
        query = "SELECT ?sc FROM <" + changesOntologySchema + "> WHERE {\n"
                + "?sc rdfs:subClassOf co:Simple_Change.\n"
                + "filter (regex(?sc, \"Add_\")).\n"
                + "}";
        ResultSet result = jdbc.executeSparqlQuery(query, true);
        List<String> sChanges = new ArrayList<>();
        try {
            while (result.next()) {
                sChanges.add(result.getString(1));
            }
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured .");
        }
        String deletedSc;
        for (String addedSc : sChanges) {
            if (addedSc.equals("Add_Type_To_Individual")) {
                deletedSc = "Delete_Type_From_Individual";
            } else {
                deletedSc = addedSc.replace("Add_", "Delete_");
            }
            query = "INSERT INTO <" + changesOntology + "> {\n"
                    + "?assoc co:consumes ?dsc1;\n"
                    + "       co:consumes ?dsc2.\n"
                    + "}\n"
                    + "WHERE {\n"
                    + "GRAPH <" + changesOntology + "> {\n"
                    + "?assoc a co:Association;\n"
                    + "     co:assoc_p1 ?old;\n"
                    + "     co:assoc_p2 ?new.\n"
                    + "?dsc1 a <" + deletedSc + ">;\n"
                    + "     ?param1 ?param_value1.\n"
                    + "?dsc2 a <" + addedSc + ">;\n"
                    + "     ?param2 ?param_value2.\n"
                    + "filter not exists {?cc1 co:consumes ?dsc1. ?cc2 co:consumes ?dsc2.}.\n"
                    + "filter (?param_value1 = ?old) .\n"
                    + "filter (?param_value2 = ?new) .\n"
                    + "}\n"
                    + "}";
            jdbc.executeUpdateQuery("sparql " + prefixes + query, true);
        }
    }

    /**
     * Detects the Simple Changes among two named graph versions and stores the
     * detected changes into the changes ontology.
     *
     * @param oldVersion The old version named graph.
     * @param newVersion The new version named graph.
     * @param simpleChanges An array of simple changes which may be potentially
     * be considered instead of all the defined simple changes.
     */
    public void detectSimpleChanges(String oldVersion, String newVersion, String[] simpleChanges) {
        System.out.println("-------------");
        System.out.println("Simple Change Detection among versions:");
        System.out.println(oldVersion + " (" + jdbc.triplesNum(oldVersion) + " triples)");
        System.out.println(newVersion + " (" + jdbc.triplesNum(newVersion) + " triples)");
        System.out.println("-------------");
        long oldSize = jdbc.triplesNum(changesOntology);
        System.out.print("Detecting simple changes...");
        long start = System.currentTimeMillis();
        String query = null;
        if (simpleChanges != null) {
            this.simpleChanges = simpleChanges;
        }
        for (String simpleChange : this.simpleChanges) {
            StringBuilder prefixes = new StringBuilder("PREFIX diachron: <http://www.diachron-fp7.eu/resource/>\n"
                    + "PREFIX efo:<http://www.ebi.ac.uk/efo/>\n"
                    + "PREFIX co:<http://www.diachron-fp7.eu/changes/>\n");
            prefixes.append(IOOps.readData(simpleChangesFolder + File.separator + simpleChange));
            query = prefixes.toString();
            query = query.replaceAll("changesOntology", changesOntology);
            query = query.replaceAll("'v1'", "'" + oldVersion + "'");
            query = query.replaceAll("'v2'", "'" + newVersion + "'");
            query = query.replaceAll("<v1>", "<" + oldVersion + ">");
            query = query.replaceAll("<v2>", "<" + newVersion + ">");
//            System.out.println("Detecting " + simpleChange);
            jdbc.executeUpdateQuery("sparql " + query, false);
        }
        System.out.println("DONE in " + (System.currentTimeMillis() - start));
        System.out.println("Simple change triples size: " + (jdbc.triplesNum(changesOntology) - oldSize));
    }

    private List<String> fetchOrderedComplexChanges(String[] complexChanges) {
        List<String> changeQueries = new ArrayList<>();
        StringBuilder filter = new StringBuilder();
        if (complexChanges != null) {
            if (complexChanges.length == 0) {
                return changeQueries;
            } else {
                filter.append("FILTER (");
                int cnt = 0;
                for (Object change : complexChanges) {
                    filter.append("?name = \"" + change + "\"");
                    cnt++;
                    if (cnt < complexChanges.length) {
                        filter.append(" || ");
                    }
                }
                filter.append(").\n");
            }
        }
        String query = "SELECT distinct ?cc ?pr ?sparql FROM <" + changesOntologySchema + "> WHERE {\n"
                + "?cc rdfs:subClassOf co:Complex_Change;\n"
                + "co:priority ?pr;\n"
                + "co:name ?name;\n"
                + "co:sparql ?sparql.\n"
                + filter
                + "} ORDER BY ?pr";
        ResultSet result = jdbc.executeSparqlQuery(query, false);
        try {
            while (result.next()) {
                String sparql = result.getString("sparql");
                changeQueries.add(sparql);
            }
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured .");
            return null;
        }
        return changeQueries;
    }

    /**
     * Detects the Complex Changes which are defined into the changes ontology
     * schema and stores the detected complex change in the changes ontology.
     *
     * @param oldVersion The old version named graph.
     * @param newVersion The new version named graph.
     * @param complexChages An array of complex changes which may be potentially
     * be considered instead of all the defined complex changes.
     */
    public void detectComplexChanges(String oldVersion, String newVersion, String[] complexChages) {
        System.out.println("-------------");
        System.out.println("Complex Change Detection among versions:");
        System.out.println(oldVersion);
        System.out.println(newVersion);
        System.out.println("-------------");
        System.out.print("Detecting complex changes...");
        long oldSize = jdbc.triplesNum(changesOntology);
        long start = System.currentTimeMillis();
        List<String> queries = fetchOrderedComplexChanges(complexChages);
        if (queries == null) {
            System.out.println("Exception occured while fetching complex change definitions.");
            return;
        }
        for (String query : queries) {
            query = query.replaceAll("changesOntology", changesOntology);
            query = query.replaceAll("'v1'", "'" + oldVersion + "'");
            query = query.replaceAll("'v2'", "'" + newVersion + "'");
            query = query.replaceAll("<v1>", "<" + oldVersion + ">");
            query = query.replaceAll("<v2>", "<" + newVersion + ">");
//            System.out.println(query);
            jdbc.executeUpdateQuery("sparql " + query, false);
        }
        System.out.println("DONE in " + (System.currentTimeMillis() - start));
        System.out.println("Complex change triples size: " + (jdbc.triplesNum(changesOntology) - oldSize));
    }

    /**
     * Terminates the JDBC connection to Virtuoso.
     */
    public void terminate() {
        jdbc.terminate();
    }

    /**
     * Changes the considered changes ontology namedgraph ww.r.t. the one given
     * as parameter.
     *
     * @param changesOntology The new changes ontology named graph to be
     * considered.
     */
    public void setChangesOntology(String changesOntology) {
        this.changesOntology = changesOntology;
    }

    /**
     * Changes the considered changes ontology schema namedgraph ww.r.t. the one
     * given as parameter.
     *
     * @param changesOntologySchema The new changes ontology schema named graph
     * to be considered.
     */
    public void setChangesOntologySchema(String changesOntologySchema) {
        this.changesOntologySchema = changesOntologySchema;
    }

    /**
     * Returns the current JDBC Virtuoso connection.
     */
    public JDBCVirtuosoRep getJdbc() {
        return jdbc;
    }

    /**
     * Returns the changes ontology named graph.
     *
     * @return
     */
    public String getChangesOntology() {
        return changesOntology;
    }

    /**
     * Returns the changes ontology schema named graph.
     *
     * @return
     */
    public String getChangesOntologySchema() {
        return changesOntologySchema;
    }

    /**
     * Returns an array of the SPARQL update queries which will be applied
     * during the simple change detection process.
     *
     * @return
     */
    public String[] getSimpleChanges() {
        return simpleChanges;
    }

    /**
     * Return the full path of the folder which contains the SPARQL update
     * queries.
     *
     * @return
     */
    public String getSimpleChangesFolder() {
        return simpleChangesFolder;
    }
}
