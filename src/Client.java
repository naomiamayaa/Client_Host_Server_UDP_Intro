import java.io.*;
import java.net.*;

public class Client {

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendReceiveSocket;

    DatagramParser parse; //parse the datagram packet

    int BYTE_SIZE = 100;

    public Client()
    {
        parse = new DatagramParser();

        try {
            sendReceiveSocket = new DatagramSocket(); //send and receive datagram packets
        } catch (SocketException se) {   // Can't create the socket.
            se.printStackTrace();
            System.exit(1);
        }
    }

    private byte[] createOpcode(int type) {
        // Creates the opcode based on the type (1 for read, 2 for write)
        byte[] opcode = new byte[2];
        opcode[0] = 0;
        opcode[1] = (byte) type;
        return opcode;
    }

    public void sendRequest(int readOrWrite, String filename, String mode, int serverPort){
        byte[] request = new byte[BYTE_SIZE];
        byte[] opcode = createOpcode(readOrWrite);  // 1 for read request

        System.arraycopy(opcode, 0, request, 0, opcode.length); // Copy the opcode to the request array

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
        if(readOrWrite == 1){
            System.out.println("Read Request");
        } else {
            System.out.println("Write Request");
        }


        System.out.println("Len: " + sendPacket.getLength());
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        parse.parseRequest(sendPacket); //parse the request packet

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

        System.out.println("Client: Waiting for packet.");
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
        parse.parseRequest(receivePacket); //parse the request packet

        // close the socket.
        sendReceiveSocket.close();
    }

    public static void main(String[] args) {
            int intermediateHostPort = 23;

            Client c = new Client();
            c.sendRequest(1, "test.txt", "netascii", intermediateHostPort);
            //c.readRequest("test.txt", "netascii", intermediateHostPort);
            c.receiveData();
    }

}
