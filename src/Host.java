import java.io.*;
import java.net.*;
public class Host {
    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendReceiveSocket; // for sending and receiving datagram packets
    DatagramSocket receiveSocket; // for receiving on port 23
    DatagramParser parse; //parse the datagram packet

    public Host()
    {
        try {
            sendReceiveSocket = new DatagramSocket(); //send and receive datagram packets
            receiveSocket = new DatagramSocket(23); //receive datagram packets from client
        } catch (SocketException se) {   // Can't create the socket.
            se.printStackTrace();
            System.exit(1);
        }
    }

    public void receivePacket( DatagramSocket socket){
        // Construct a DatagramPacket for receiving packets
        byte data[] = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);
        System.out.println("Host: Waiting for Packet.\n");

        // Block until a datagram packet is received from receiveSocket.
        try {
            System.out.println("Waiting..."); // so we know we're waiting
            socket.receive(receivePacket);
        } catch (IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }

        // Process the received datagram.
        System.out.println("Host: Received packet containing:");
        parse.parseRequest(receivePacket); //parse the request packet
    }

    public void sendPacket(DatagramSocket socket, byte[] data, InetAddress address, int port){
        sendPacket = new DatagramPacket(data, data.length, address, port);
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Host: Packet sent.\n");
    }

    public static void main(String[] args) {
        Host h = new Host();
        h.receivePacket(h.receiveSocket);
        h.sendPacket(h.sendReceiveSocket, h.receivePacket.getData(), h.receivePacket.getAddress(), 69);
    }
}
