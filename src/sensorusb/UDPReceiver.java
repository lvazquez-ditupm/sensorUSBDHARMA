package sensorusb;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class receives data from UDP socket
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class UDPReceiver implements Runnable {

    private DatagramSocket socketUDP;

    public UDPReceiver(int UDPport, String ip) {
        try {
            socketUDP = new DatagramSocket(UDPport, InetAddress.getByName(ip));
        } catch (UnknownHostException | SocketException e) {
            System.err.println("Imposible obtener acceso al socket UDP. Terminando sistema...");
            System.exit(0);
        }
    }

    public void run() {
        String log;
        while (true) {
            try {
                byte[] buf = new byte[4096];
                String machine;
                String ID;
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socketUDP.receive(packet);
                    log = new String(packet.getData(), packet.getOffset(), packet.getLength());

                    if (!log.equals("") && log.contains("-")) {
                        machine = log.split("-")[1];
                        ID = log.split("-")[0];
                        SensorUSB.receiveFromSocket(ID, machine);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(SensorUSB.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
