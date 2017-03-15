package sensorusb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class acts as DAO for the MySQL database
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class MySQL {

    /**
     * Establece una conexión con la base de datos
     *
     * @param user usuario
     * @param password contraseña
     */
    private static Connection getConToDB(String user, String password) throws SQLException {
        String dbURL = "jdbc:mysql://localhost:3306/USB_ID"
                + "?verifyServerCertificate=false"
                + "&useSSL=false"
                + "&requireSSL=false";

        return DriverManager.getConnection(dbURL, user, password);
    }

    /**
     * Recupera el estado de la sala en un momento dado
     *
     * @param user usuario DB
     * @param passwd contraseña DB
     * @return el USB está registrado en la base de datos
     */
    public static boolean isRegistered(String user, String passwd, String ID) throws SQLException {
        String res = "";
        Connection con = null;
        try {
            con = getConToDB(user, passwd);
            PreparedStatement stmt = con.prepareStatement("SELECT USB_ID FROM ID WHERE USB_ID=" + ID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                res = rs.getString("USB_ID");
                break;
            }

        } catch (SQLException sqle) {
            System.out.println("Error en la ejecución:"
                    + sqle.getErrorCode() + " " + sqle.getMessage());
        } finally {
            con.close();
        }

        return !res.equals("");
    }

}
