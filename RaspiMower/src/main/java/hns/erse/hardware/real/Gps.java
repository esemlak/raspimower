package hns.erse.hardware.real;

import com.fazecast.jSerialComm.*;
import hns.erse.threads.InfoDisplay;
import hns.erse.util.NMEA;

import java.nio.charset.StandardCharsets;

public class Gps {

    private final static String DesiredSerialPortName = "USB-to-Serial Port (pl2303)";

    private static final String PORT_NAMES[] = {
            "/dev/tty.usbserial-A9007UX1", // Mac OS X
            "/dev/ttyUSB0", // Linux
            "COM3", // Windows
    };

    private SerialPort serialPort;

    private static Gps gps;

    public NMEA.GPSPosition getGpsPosition() {
        return gpsPosition;
    }

    NMEA.GPSPosition gpsPosition;
    NMEA nmea;

    StringBuffer dataString = new StringBuffer();

    public Gps() {
        init();
    }

    public static Gps getInstance() {
        if (gps == null) {
            gps = new Gps();
        }
        return gps;
    }

    private void init() {
        SerialPort[] commPorts = SerialPort.getCommPorts();
        for (SerialPort commPort : commPorts) {
            String com_name = commPort.getDescriptivePortName();
            if (com_name.equals(DesiredSerialPortName)) {
                serialPort = commPort;

            }
        }
        if (serialPort != null) {
            InfoDisplay.getInstance().add_info_line("GPS started on " + serialPort);
            serialPort.setBaudRate(4800);
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(1);
            serialPort.setParity(0);
            serialPort.openPort();

            nmea = new NMEA();

            serialPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
                @Override
                public void serialEvent(SerialPortEvent event)
                {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                        return;
                    byte[] newData = new byte[serialPort.bytesAvailable()];
                    int numRead = serialPort.readBytes(newData, newData.length);
                    //System.out.println("Read " + numRead + " bytes.");
                    String data = new String(newData, StandardCharsets.US_ASCII);
                    dataString.append(data);
                    //System.out.print(data);
                    if (dataString.substring( dataString.length()-1).equals("\n")) {
                        gpsPosition =  nmea.parse(dataString.toString());
                        //System.out.println(gpsPosition.toString());
                    }
                }
            });
        }
    }



}

