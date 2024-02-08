// SimpleEchoServer.java
// This class is the server side of a simple echo server based on
// UDP/IP. The server receives from a client a packet containing a character
// string, then echoes the string back to the client.
// Last edited January 9th, 2016

import java.io.*;
import java.net.*;

public class Server {

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket receiveSocket;
    DatagramParser parse; //parse the datagram packet
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m\t";

    public Server()
    {
        parse = new DatagramParser();
        try {
            receiveSocket = new DatagramSocket(69);

            // to test socket timeout (2 seconds)
            //receiveSocket.setSoTimeout(2000);
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    private byte[] returnConfirmationCode(int type) {
        // Creates the opcode based on the type (1 for read, 2 for write)
        byte[] opcode = new byte[4];
        opcode[0] = 0;
        if(type == 1){
            opcode[1] = 3;
            opcode[2] = 0;
            opcode[3] = 1;
        }else if(type == 2){
            opcode[1] = 4;
            opcode[2] = 0;
            opcode[3] = 0;
        }else{
            throw new RuntimeException("Invalid packet received");
        }
        return opcode;
    }

    public void receiveAndSend() throws SocketException {
        // Construct a DatagramPacket for receiving packets from the host
        byte data[] = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);
        System.out.println("Server: Waiting for Packet.\n");

        try {
            // Block until a datagram is received via sendReceiveSocket.
            receiveSocket.receive(receivePacket);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Process the received datagram.
        System.out.println("Server: Packet received:");
        parse.parseRequest(receivePacket); //parse the request packet

        byte senddata[] = returnConfirmationCode(parse.getOpcode(receivePacket));
        sendPacket = new DatagramPacket(senddata, senddata.length, receivePacket.getAddress(), receivePacket.getPort());

        try {
            DatagramSocket sendSocket = new DatagramSocket(); //create a new socket to send data back to host
            sendSocket.send(sendPacket);
            sendSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.print("\nServer response in bytes: ");
        for (int i = 0; i < 4; i++) {
            System.out.print(String.format("%02X ", senddata[i]));
        }
    }

    public static void main(String[] args) throws SocketException {
        Server s = new Server();

        while(true){
            System.out.println( ANSI_GREEN + "\n\n------------------------------------------------" + ANSI_RESET);
            s.receiveAndSend();

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

