package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;

public class H2DataExporter implements DataExporter {

    @Override
    public void export(Map<byte[], byte[]> data) throws SQLException,IOException {

        final String connectionString = "jdbc:h2:"+ calculateExportPath();
        
        try (Connection conn = DriverManager.getConnection(connectionString)) {

            try (Statement stmt = conn.createStatement()) {
                
               boolean tableCreated =  stmt.execute("CREATE TABLE ENCRYPTED_TRANSACTION "
                        + "(ID BIGINT NOT NULL, "
                        + "ENCODED_PAYLOAD LONGVARBINARY NOT NULL, "
                        + "HASH LONGVARBINARY NOT NULL UNIQUE, PRIMARY KEY (ID))");

               if(!tableCreated) throw new IllegalStateException("UNABLE to Create table");
               
                stmt.execute("CREATE SEQUENCE ENC_TX_SEQ INCREMENT BY 50 START WITH 50");
            }

            try (PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO ENCRYPTED_TRANSACTION (ID,HASH,ENCODED_PAYLOAD) VALUES (ENC_TX_SEQ.NEXTVAL,?,?)")) {
                for (Entry<byte[], byte[]> values : data.entrySet()) {
                    insertStatement.setBytes(1, values.getKey());
                    insertStatement.setBytes(2, values.getValue());
                    insertStatement.execute();
                }
            }

        }
    }

    @Override
    public String dbId() {
        return "h2";
    }

}
