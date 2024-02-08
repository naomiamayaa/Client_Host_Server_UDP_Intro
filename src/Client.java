import java.io.*;
import java.net.*;

public class Client {

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendReceiveSocket;

    DatagramParser parse; //parse the datagram packet
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m\t";
    public static final String ANSI_RED = "\u001B[31m";

    int BYTE_SIZE = 100;
    private static final int CLIENT_PORT = 23;

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

    public void sendRequest(int readOrWrite, String filename, String mode){
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
                    InetAddress.getLocalHost(), CLIENT_PORT); //send packet to host

        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }

        parse.parseRequest(sendPacket); //parse the request packet

        // Send the datagram packet to the server via the send/receive socket.
        try {
            sendReceiveSocket.send(sendPacket);
            if(readOrWrite != 1 && readOrWrite != 2){
                sendReceiveSocket.close();
                System.exit(1);
                //throw new RuntimeException("Invalid request type");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("\nClient: Packet sent.");
    }
    public void receiveData(){
        byte data[] = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);

        System.out.println("Client: Waiting for packet.\n");
        try {
            // Block until a datagram is received via sendReceiveSocket.
            sendReceiveSocket.receive(receivePacket);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        byte receivedata[] = receivePacket.getData();

        System.out.println("Client: Packet received from Host:");
        System.out.print("Host response in bytes: ");
        for (int i = 0; i < 4; i++) {
            System.out.print(String.format("%02X ", receivedata[i]));
        }
        System.out.println();
        // close the socket.
        sendReceiveSocket.close();
    }

    public static void main(String[] args) {

        for(int i = 0; i < 11; i++) {

            System.out.println("\n\nIteration: " + ANSI_GREEN + i + ANSI_RESET);
            Client c = new Client();

            if(i % 2 == 0){
                c.sendRequest(1, "test.txt", "netascii");
            }else{
                c.sendRequest(2, "test.txt", "octet");
            }

            c.receiveData();

            // Introduce a delay between iterations
            try {
                // Sleep for 1 second (1000 milliseconds)
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Handle interrupted exception
                e.printStackTrace();
            }
        }

        //11th request should fail
        System.out.println("\n\nIteration: " + ANSI_RED + 11 + ANSI_RESET);
        Client c = new Client();
        c.sendRequest(3, "test.txt", "octet");
        c.receiveData();
    }

}
