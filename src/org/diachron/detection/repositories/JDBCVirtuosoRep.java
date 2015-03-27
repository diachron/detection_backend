package org.diachron.detection.repositories;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * This class contains a set of methods which are used to handle Virtuoso Triple
 * Store over JDBC interface.
 *
 * @author rous
 */
public class JDBCVirtuosoRep {

    private Connection conn;
    private Statement statement;

    /**
     * Creates a new Virtuoso connection.
     *
     * @param virt_instance The IP of the machine which hosts Virtuoso.
     * @param port The port.
     * @param usr The username of the certified user.
     * @param pwd The password of the certified user.
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public JDBCVirtuosoRep(String virt_instance, int port, String usr, String pwd) throws ClassNotFoundException, SQLException {
        this.conn = null;
        String[] sa = new String[4];
        sa[0] = virt_instance;
        sa[1] = port + "";
        sa[2] = usr;
        sa[3] = pwd;
        Class.forName("virtuoso.jdbc3.Driver");
        conn = DriverManager.getConnection("jdbc:virtuoso://" + sa[0] + ":" + sa[1] + "/charset=UTF-8/log_enable=2", sa[2], sa[3]);
        statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Creates a new Virtuoso connection. The credentials are taken from a
     * properties file.
     *
     * @param propFile The path to the properties file
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    public JDBCVirtuosoRep(String propFile) throws ClassNotFoundException, SQLException, IOException {
        Properties prop = new Properties();
        InputStream inputStream;
        inputStream = new FileInputStream(propFile);
        prop.load(inputStream);
        this.conn = null;
        String[] sa = new String[4];
        sa[0] = prop.getProperty("Repository_IP");
        sa[1] = Integer.parseInt(prop.getProperty("Repository_Port")) + "";
        sa[2] = prop.getProperty("Repository_Username");
        sa[3] = prop.getProperty("Repository_Password");
        Class.forName("virtuoso.jdbc3.Driver");
        conn = DriverManager.getConnection("jdbc:virtuoso://" + sa[0] + ":" + sa[1] + "/charset=UTF-8/log_enable=2", sa[2], sa[3]);
        statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    public JDBCVirtuosoRep(Properties prop) throws ClassNotFoundException, SQLException, IOException {
        this.conn = null;
        String[] sa = new String[4];
        sa[0] = prop.getProperty("Repository_IP");
        sa[1] = Integer.parseInt(prop.getProperty("Repository_Port")) + "";
        sa[2] = prop.getProperty("Repository_Username");
        sa[3] = prop.getProperty("Repository_Password");
        Class.forName("virtuoso.jdbc3.Driver");
        conn = DriverManager.getConnection("jdbc:virtuoso://" + sa[0] + ":" + sa[1] + "/charset=UTF-8/log_enable=2", sa[2], sa[3]);
        statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Executes an update query given as parameter.
     *
     * @param query The update query.
     * @param logging A boolean variable which denotes whether the update query
     * and its execution time will be printed or not.
     */
    public void executeUpdateQuery(String query, boolean logging) {
        try {
            long start = 0;
            if (logging) {
                System.out.println("QUERY: " + query);
                start = System.currentTimeMillis();
            }
            statement.executeQuery("log_enable(3,1)");
            statement.executeUpdate(query);
            if (logging) {
                System.out.println("Done in " + (System.currentTimeMillis() - start) + "ms");
            }
        } catch (SQLException ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured during the update query.");
        }
    }

    /**
     * Executes a SPARQL select query given as parameter.
     *
     * @param query The SPARQL select query.
     * @param logging A boolean variable which denotes whether the select query
     * and its execution time will be printed or not.
     * @return
     */
    public ResultSet executeSparqlQuery(String query, boolean logging) {
        try {
            ResultSet result;
            long start = 0;
            if (logging) {
                System.out.println("QUERY: " + query);
                start = System.currentTimeMillis();
            }
            statement.setFetchSize(1000000);
            result = statement.executeQuery("sparql " + query);
            if (logging) {
                System.out.println("Done in " + (System.currentTimeMillis() - start) + "ms");
            }
            return result;
        } catch (SQLException ex) {
            System.out.println("Exception occured during the select query.");
            return null;
        }
    }

    /**
     * Returns the statement instance of this JDBC connection.
     *
     * @return
     */
    public Statement getStatement() {
        return statement;
    }

    /**
     * Returns the connection instance of this JDBC connection.
     *
     * @return
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Returns the number of the triples contained in the named graph given as parameter.
     * @param graph The named graph whose triples are counted.
     * @return The number of triples.
     */
    public long triplesNum(String graph) {
        try {
            String query = "SPARQL SELECT count(*) from <" + graph + "> where {?s ?p ?o}";
            ResultSet result = statement.executeQuery(query);
            ResultSetMetaData meta = result.getMetaData();
            int count = meta.getColumnCount();
            long triples = 0;
            while (result.next()) {
                for (int c = 1; c <= count; c++) {
                    triples = Long.parseLong(result.getString(c));
                }
            }
            return triples;
        } catch (SQLException ex) {
            System.out.println("Exception " + ex.getMessage() + "occured during the count of triples.");
            return 0;
        }
    }

    /**
     * Imports a single RDF/XML file into Virtuoso. The file must must belong within the machine which hosts Virtuoso as 
     * it is a server side import.  
     * @param filename The full path of the file which contains the RDF/XML data.
     * @param graph The graph which will receive the data.
     * @param logging A boolean variable which denotes whether the import execution time will be printed or not.
     */
    public void importSingleRDFFile(String filename, String graph, boolean logging) {
        String query = "RDF_LOAD_RDFXML_MT(file_to_string_output('" + filename + "'), '', '" + graph + "')";
        executeUpdateQuery(query, logging);
    }

    /**
     * Imports a single N3 file into Virtuoso. The file must must belong within the machine which hosts Virtuoso as 
     * it is a server side import.  
     * @param filename The full path of the file which contains the N3 data.
     * @param graph The graph which will receive the data.
     * @param logging A boolean variable which denotes whether the import execution time will be printed or not.
     */
    public void importSingleN3File(String filename, String graph, boolean logging) {
        String query = "TTLP_MT(file_to_string_output('" + filename + "'), '', '" + graph + "')";
        executeUpdateQuery(query, logging);
    }

    /**
     * Clears the named graph given as parameter.
     * @param graph The named graph to be cleared. 
     * @param logging A boolean variable which denotes whether the clear execution time will be printed or not.
     */
    public void clearGraph(String graph, boolean logging) {
        executeUpdateQuery("SPARQL CLEAR GRAPH <" + graph + ">", logging);
    }

    /**
     * Terminates the JDBC connection.
     */
    public void terminate() {
        try {
            statement.close();
            conn.close();
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured during the close of statement and connection.");
        }
    }

    /**
     * Copies the contents of a named graph into another. 
     * @param source The source named graph.
     * @param destination The destination named graph.
     */
    public void copyGraph(String source, String destination) {
        String query = "sparql "
                + "INSERT INTO <" + destination + "> {"
                + "?s ?p ?o "
                + "}\n"
                + "WHERE {"
                + "graph <" + source + "> { ?s ?p ?o }"
                + "}";
        executeUpdateQuery(query, true);
    }

    /**
     * Renames a named graph.
     * @param oldName The old name of the named graph.
     * @param newName The new name of the named graph.
     */
    public void renameGraph(String oldName, String newName) {
        String query = "UPDATE DB.DBA.RDF_QUAD TABLE OPTION (index RDF_QUAD_GS) "
                + "SET g = iri_to_id ('" + newName + "') "
                + "WHERE g = iri_to_id ('" + oldName + "', 0)";
        executeUpdateQuery(query, true);
    }

 }
