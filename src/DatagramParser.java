import java.net.DatagramPacket;

public class DatagramParser {

    // Assuming that the opcode is 2 bytes
    private static final int OPCODE_LENGTH = 2;
    // Method to parse the filename and mode from a DatagramPacket
    public static int parseRequest(DatagramPacket packet) {
        byte[] data = packet.getData();
        int length = packet.getLength();

        // Ensure the packet is at least large enough to contain the opcode
        if (length < OPCODE_LENGTH) {
            System.out.println("Invalid request packet");
            return 1;
        }

        // Extract the opcode (first two bytes)
        byte[] opcodeBytes = new byte[OPCODE_LENGTH];
        System.arraycopy(data, 0, opcodeBytes, 0, OPCODE_LENGTH);

        // Assuming opcode is in network order (big-endian)
        int opcode = (opcodeBytes[0] & 0xFF) << 8 | (opcodeBytes[1] & 0xFF);

        // Check if it's a read request (opcode 1) or write request (opcode 2)
        int modeEnd = 0;
        if (opcode == 1 || opcode == 2) {
            // Locate the null-terminated filename
            int filenameStart = OPCODE_LENGTH;
            int filenameEnd = indexOfNullByte(data, filenameStart);
            String filename = new String(data, filenameStart, filenameEnd - filenameStart);

            // Locate the null-terminated mode
            int modeStart = filenameEnd + 1;
            modeEnd = indexOfNullByte(data, modeStart);
            String mode = new String(data, modeStart, modeEnd - modeStart);

            if(opcode == 1){
                System.out.println("Read Request");
            } else {
                System.out.println("Write Request");
            }
            // Print the parsed values
            System.out.println("From host: " + packet.getAddress());
            System.out.println("Host port: " + packet.getPort());
            System.out.println("Filename: " + filename);
            System.out.println("Mode: " + mode);
        } else {
            System.out.println("Unsupported opcode: " + opcode);
            return 1;
        }

        // Print the packet content in bytes
        System.out.print("Packet in bytes: ");
        for (int i = 0; i < modeEnd +1 ; i++) {
            System.out.print(String.format("%02X ", data[i]));
        }
        System.out.println();
        return 0;
    }

    // Helper method to find the index of the first null byte in the array
    private static int indexOfNullByte(byte[] array, int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            if (array[i] == 0) {
                return i;
            }
        }
        return array.length;  // Return length if null byte not found
    }



    public int getOpcode(DatagramPacket packet) {
        byte[] data = packet.getData();
        int opcode = (data[0] & 0xFF) << 8 | (data[1] & 0xFF);
        return opcode;
    }
}
