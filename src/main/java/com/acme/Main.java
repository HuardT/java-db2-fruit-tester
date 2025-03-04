package com.acme;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class Main {
    private static Logger logger = LogManager.getLogger(Main.class);

    private final Tracer tracer;

    public Main(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer(Main.class.getName());
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        logger.info("Started application");

        logger.info("Loading JDBC Driver");
        Class.forName("com.ibm.db2.jcc.DB2Driver");
        logger.info("Loaded JDBC Driver");

        logger.info("Building Main class");
        Main main = new Main(GlobalOpenTelemetry.get());
        logger.info("Main class built");

        logger.info("Performing 3 calls");
        main.performDb2Calls(1);
        main.performDb2Calls(2);
        main.performDb2Calls(3);
        logger.info("End of process");
    }

    private void performDb2Calls(int value) throws SQLException {
        var mainSpan = tracer.spanBuilder("main" + value).startSpan();
        String jdbcUrl = "jdbc:db2://db2:50000/DB2LOCAL:currentSchema=DB2LOCAL;traceFile=trace-java-db2;traceFileAppend=false;traceLevel=-1;traceDirectory=/tmp/trace;";
        String user = "DB2INST1";
        String password = "password";

        Connection connection = null;

        try (Scope ignore = mainSpan.makeCurrent()) {
            // Load JDBC driver in classpath

            logger.info("Connecting to database...");
            connection = DriverManager.getConnection(jdbcUrl, user, password);
            logger.info("Connected to database");

            logger.info("Read data");
            readData(connection, mainSpan);
        } catch (Exception e) {
            logger.error("Error during process", e);
        } finally {
            if (connection != null) {
                logger.info("Closing connection...");
                connection.close();
                logger.info("Closed connection");
            }
            // Close the span
            mainSpan.end();
        }
    }

    private void readData(Connection connection, Span parentSpan) throws Exception {
        // Create a new span for this db call
        var childSpan = tracer.spanBuilder("readData").setParent(Context.current().with(parentSpan)).startSpan();
        try {
            logger.info("Converting SQL to dialect...");
            String sql = connection.nativeSQL("SELECT * FROM FRUIT WHERE COLOR IS NOT NULL ORDER BY NAME ASC");
            logger.info("Converted SQL");

            if (connection != null) {
                logger.info("Creating statement...");
                Statement stmt = connection.createStatement();
                logger.info("Created statement");

                logger.info("Executing SQL...");
                ResultSet rs = stmt.executeQuery(sql);
                logger.info("Executed SQL");

                while (rs.next()) {
                    logger.info("Read fruit : {}, {}, {}", rs.getString("ID"), rs.getString("NAME"), rs.getString("COLOR"));
                }
            }
        } finally {
            // Close the span
            childSpan.end();
        }
    }
}
