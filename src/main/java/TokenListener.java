import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TokenListener {
    private static final int QR_CODE_WIDTH = 500; // Width in pixels
    private static final int QR_CODE_HEIGHT = 500; // Height in pixels

    public static void main(String[] args) {
        String host_ip = "";
        String wifi_name = "";
        try {
            host_ip = new Socket("8.8.8.8", 53).getLocalAddress().getHostAddress();

            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "netsh wlan show interfaces");
            builder.redirectErrorStream(true);
            BufferedReader input = new BufferedReader(new InputStreamReader(builder.start().getInputStream()));
            String line;
            while((line = input.readLine()) != null) {
                if (line.contains("SSID")) {
                    wifi_name = line.split(":")[1].trim();
                    break;
                }
            }
        } catch (Exception ignored) {
            System.out.println("Can't run the program. Is this computer not connected to a WiFi?");
            System.exit(0);
        }

        for (int i = 0; i < 30; i++) System.out.print('=');
        System.out.println();
        System.out.println("WiFi name: " + wifi_name);
        System.out.println("IP address: " + host_ip);
        for (int i = 0; i < 30; i++) System.out.print('=');
        System.out.println();

        String qrData = wifi_name+"\n"+host_ip;

        QRCodeWriter qrWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrWriter.encode(qrData, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
            BufferedImage image = new BufferedImage(QR_CODE_WIDTH, QR_CODE_HEIGHT, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < QR_CODE_HEIGHT; y++) {
                for (int x = 0; x < QR_CODE_WIDTH; x++) {
                    int grayValue = bitMatrix.get(x, y) ? 0 : 1;
                    image.setRGB(x, y, (grayValue == 1 ? 0xFFFFFF : 0));
                }
            }
            File outFile = new File("qrcode.jpg");
            ImageIO.write(image, "jpg", outFile);

            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("QR Code");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new JLabel(new ImageIcon(image)));
                frame.pack();
                frame.setLocationRelativeTo(null); // Center the window
                frame.setVisible(true);
            });
        } catch (Exception ignored) {}

        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(59876);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("serversocket created.");
        try {
            while (true) {
                socket = serverSocket.accept();
                System.out.println("accepted");

                DataInputStream dis = new DataInputStream(socket.getInputStream());

                String str = dis.readUTF();
                System.out.println("Server : " + str);
                dis.close();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
