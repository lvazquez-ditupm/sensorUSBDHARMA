/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sensorusb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class reads the logs of new USB devices connected and checks if they are
 * registered in a database. If not, generates an alert.
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class SensorUSB {

    public static String input;
    public static String output;
    public static String user;
    public static String passwd;
    public static int socketPort;
    public static String socketIP;

    public static void main(String[] args) {

        input = args[0];
        output = args[1];
        user = args[2];
        passwd = args[3];
        socketPort = Integer.parseInt(args[4]);
        socketIP = args[5];
        

        boolean first = true;

        String machine;
        String ID;
        BufferedReader data;

        try {
            String str;
            data = new BufferedReader(new FileReader(input));

            clearFile(input);
            clearFile(output);
            initJSON(output, "[{\r}]");
            
            UDPReceiver socketRec = new UDPReceiver(socketPort, socketIP);
            Thread t = new Thread(socketRec);
            t.start();

            while (true) {
                try {
                    while (data.ready()) {
                        str = data.readLine();
                        if (!str.equals("") && str.contains("-")) {
                            machine = str.split("-")[1];
                            ID = str.split("-")[0];
                            processLog(user, passwd, ID, output, machine);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SensorUSB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(SensorUSB.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    /**
     * Si el ID del USB no está en la base de datos, escribe una línea en el
     * fichero output indicando la alerta
     *
     * @param user usuario de la base de datos
     * @param passwd contraseña de la base de datos
     * @param ID identificador del dispositivo USB
     * @param path ruta al fichero de salida
     * @param machine identificador de la máquina donde se ha conectado el USB
     */
    private static void processLog(String user, String passwd, String ID, String path, String machine) throws SQLException {

        boolean isRegistered = MySQL.isRegistered(user, passwd, ID);
        if (!isRegistered) {
            System.out.println("Anomalía: " + ID + " - " + machine);

            updateJSON(path, "},{\"" + ID + "\":\"" + machine + "\"");
        }

    }

    /**
     * Inicializa el JSON con un texto determinado
     *
     * @param path ruta al fichero
     * @param text texto a introducir
     */
    private static void initJSON(String path, String text) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(path, "UTF-8");
            writer.println(text);
        } catch (Exception ex) {
            Logger.getLogger(SensorUSB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            writer.close();
        }
    }

    /**
     * Añade un texto a la penúltima línea de un fichero
     *
     * @param path ruta al fichero
     * @param text texto a incluir
     */
    private static void updateJSON(String path, String text) {
        List<String> list = null;
        try {
            list = Files.readAllLines(Paths.get(path));
            list.add(list.size() - 1, text);
            Files.write(Paths.get(path), list);
        } catch (IOException ex) {
            Logger.getLogger(SensorUSB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Borra el contenido de un fichero
     *
     * @param path ruta al fichero
     */
    private static void clearFile(String path) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(path);
            writer.print("");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SensorUSB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            writer.close();
        }
    }

    /**
     * Procesa el contenido recibido del socket UDP
     *
     * @param ID identificador del USB conectado
     * @param machine máquina en la que se ha generado el log
     */
    static void receiveFromSocket(String ID, String machine) {
        try {
            processLog(user, passwd, ID, output, machine);
        } catch (SQLException ex) {
            Logger.getLogger(SensorUSB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
