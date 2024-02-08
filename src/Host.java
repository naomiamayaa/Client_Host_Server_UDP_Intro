import java.io.*;
import java.net.*;
public class Host {
    DatagramPacket receivePacket;
    DatagramSocket sendReceiveSocket, receiveSocket; // for sending and receiving datagram packets
    DatagramParser parse; //parse the datagram packet
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m\t";

    private static final int CLIENT_PORT = 23;
    private static final int SERVER_PORT = 69;
    private static int LENGTH = 100;

    public Host()
    {
        try {
            sendReceiveSocket = new DatagramSocket(); //send and receive datagram packets
            receiveSocket = new DatagramSocket(CLIENT_PORT); //receive datagram packets from client
        } catch (SocketException se) {   // Can't create the socket.
            se.printStackTrace();
            System.exit(1);
        }
    }

    public void receivePacket(){
        // Construct a DatagramPacket for receiving packets
        byte data[] = new byte[LENGTH];
        receivePacket = new DatagramPacket(data, data.length);

        System.out.println("Host: Waiting for Packet from Client\n");

        // Block until a datagram packet is received from receiveSocket.
        try {
            receiveSocket.receive(receivePacket);
        } catch (IOException e) {
            System.out.print("IO Exception: likely:");
            e.printStackTrace();
            System.exit(1);
        }

        // Process the received datagram.
        System.out.println("Host: Packet received from Client:");
        parse.parseRequest(receivePacket); //parse the request packet
    }

    public void sendReceivePacket() throws UnknownHostException, SocketException {

        //create a packet to send to the server
        DatagramPacket sendPacket = new DatagramPacket(
                receivePacket.getData(),
                receivePacket.getLength(),
                receivePacket.getAddress(),
                SERVER_PORT
        );

        // Send packet from client to server
        try {
            System.out.println("\nHost: Sending packet to Server:");
            sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Host: Packet sent to Server.");
        if (parse.parseRequest(sendPacket) == 1){
            sendReceiveSocket.close();
            receiveSocket.close();
            System.exit(1);
        }

        // Construct a DatagramPacket for receiving packets from the server
        byte receiveData[] = new byte[4];
        DatagramPacket responsePacket = new DatagramPacket(receiveData, receiveData.length);

        // Block until a datagram packet is received from the server
        try {
            System.out.println("\nHost: Waiting for packet from Server.");
            sendReceiveSocket.receive(responsePacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Process the received datagram.
        System.out.println("Host: Packet received from Server:");
        System.out.print("Server response in bytes: ");
        for (int i = 0; i < 4; i++) {
            System.out.print(String.format("%02X ", receiveData[i]));
        }
        System.out.println();

        // Send packet from server to client
        DatagramSocket clientSocket = new DatagramSocket();
        DatagramPacket clientPacket = new DatagramPacket(
                receiveData,
                receiveData.length,
                receivePacket.getAddress(),
                receivePacket.getPort()
        );

        try {
            clientSocket.send(clientPacket);
            System.out.println("Host: Packet sent back to Client.");
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static void main(String[] args) throws UnknownHostException, SocketException {
        Host h = new Host();

        while(true) {
            System.out.println( ANSI_GREEN + "\n\n------------------------------------------------" + ANSI_RESET);
            h.receivePacket();
            h.sendReceivePacket(); //send the server response back to the client

            // Introduce a delay between iterations
            try {
                // Sleep for 1 second (1000 milliseconds)
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Handle interrupted exception
                e.printStackTrace();
            }
        }
    }
}
