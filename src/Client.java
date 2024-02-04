
import java.io.*;
import java.net.*;

public class Client {

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendReceiveSocket;

    int BYTE_SIZE = 100;

    public Client()
    {
        try {
            sendReceiveSocket = new DatagramSocket(); //send and receive datagram packets
        } catch (SocketException se) {   // Can't create the socket.
            se.printStackTrace();
            System.exit(1);
        }
    }
    public void readRequest(String filename, String mode, int serverPort){
        byte[] request = new byte[BYTE_SIZE];
        request[0] = 0;
        request[1] = 2;

        byte filenameBytes[] = filename.getBytes();
        byte modeBytes[] = mode.getBytes();

        System.arraycopy(filenameBytes, 0, request, 2, filenameBytes.length); //copy filename after the first two bytes
        request[filenameBytes.length + 2] = 0; // add 0 after filename
        System.arraycopy(modeBytes, 0, request, filenameBytes.length + 3, modeBytes.length); //copy mode after 0
        request[filenameBytes.length + modeBytes.length + 3] = 0; // add 0 after mode

        try{
            sendPacket = new DatagramPacket(request, request.length,
                    InetAddress.getLocalHost(), serverPort); //send packet to host
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }

        int len = filenameBytes.length + modeBytes.length + 4;

        System.out.println("Client: Sending packet containing:");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        System.out.println("Length: " + len);
        System.out.print("FILENAME: ");
        System.out.println(new String(sendPacket.getData(),2, filenameBytes.length));
        System.out.print("MODE: ");
        System.out.println(new String(sendPacket.getData(),filenameBytes.length + 3, modeBytes.length));

        // Print the packet content in bytes
        System.out.print("Packet in bytes: ");
        for (int i = 0; i < len; i++) {
            System.out.print(String.format("%02X ", request[i]));
        }
        System.out.println(); // Add a newline for better formatting


        // Send the datagram packet to the server via the send/receive socket.

        try {
            sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Client: Packet sent.\n");
    }

    public void receiveData(){
        byte data[] = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);

        try {
            // Block until a datagram is received via sendReceiveSocket.
            sendReceiveSocket.receive(receivePacket);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Process the received datagram.
        System.out.println("Client: Packet received:");
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("Host port: " + receivePacket.getPort());
        int len = receivePacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");

        // Form a String from the byte array.
        String received = new String(data,0,len);
        System.out.println(received);

        // We're finished, so close the socket.
        sendReceiveSocket.close();
    }

    public static void main(String[] args) {
            int intermediateHostPort = 23;

            Client c = new Client();
            c.readRequest("test.txt", "netascii", intermediateHostPort);
            //c.readRequest("test.txt", "netascii", intermediateHostPort);
            c.receiveData();
    }

}
