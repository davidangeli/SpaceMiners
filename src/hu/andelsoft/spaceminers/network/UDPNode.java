package hu.andelsoft.spaceminers.network;

import javafx.util.Pair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.function.BiConsumer;

/**
 * A wrapper class for DatagramSocket, also implementing the Runnable interface,
 * so it has it's own thread waiting for new packets.
 */
public class UDPNode extends DatagramSocket implements Runnable {
    private final int msgLength;
    private BiConsumer<InetAddress,byte[]> msgAction;
    private boolean listens = true;

    /**
     * Constructor.
     * @param port The port where it's socket should be bound to.
     * @param ml The length of messages this Node awaits for.
     * @param ma A BiConsumer instance that will be called for every arriving packet.
     * @throws SocketException
     */
    public UDPNode(int port, int ml, BiConsumer<InetAddress,byte[]> ma) throws SocketException {
        super(port);
        this.msgLength = ml;
        this.msgAction = ma;
    }

    /**
     * Starts waiting for new packets on this DatagramSocket.
     */
    @Override
    public void run (){
        DatagramPacket packet=null;

        while (listens){
            try {
                byte[] buf = new byte[msgLength];
                packet = new DatagramPacket(buf, buf.length);
                this.receive(packet);
            } catch (IOException ex) {
                break;
            }
            if (listens)
                msgAction.accept(packet.getAddress(),packet.getData());
        }
        //normal close after stopped listening
        super.close();

    }

    /**
     * Sends a packet through this DatagramSocket.
     * @param buffer The data to send.
     * @param targetip Target's IP.
     * @param targetport Target's port.
     * @throws IOException
     */
    public void sendMsg(byte[] buffer, InetAddress targetip, int targetport) throws IOException {
        this.send(new DatagramPacket(buffer, buffer.length, targetip, targetport));
    }

    /**
     * Checks if this node listens on the DatagramSocket.
     */
    public boolean isOpen(){
        return listens;
    }

    /**
     * Tells this Node to stop accepting new messages,
     * and sends a last packet to itself to come out of the blocking receive.
     * @throws IOException
     */
    public void stop() throws IOException {
        listens = false;

        if (this.isClosed()) return;
        sendMsg(new byte[msgLength], InetAddress.getLocalHost(), this.getPort());
    }
}