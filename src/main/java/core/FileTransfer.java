package core;

import entity.Message;
import network.Messaging;
import state.HostState;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Set;

public class FileTransfer implements Runnable {

    public enum TransferType {SENDER, RECIEVER}

    private final int FILE_AVAILABILITY_TIMEOUT = 100000;
    private final int[] portRange = {6000, 9000};
    Set<InetAddress> hosts;
    InetAddress sendTo;
    String fileName;
    TransferType type;
    HostState state;
    int port;

    public FileTransfer(Set<InetAddress> hosts, String fileName, TransferType type, HostState state) {
        this.hosts = hosts;
        this.fileName = fileName;
        this.type = type;
        this.state = state;
    }

    public FileTransfer(InetAddress sendTo, String fileName, TransferType type, HostState state, int port) {
        this.sendTo = sendTo;
        this.fileName = fileName;
        this.type = type;
        this.state = state;
        this.port = port;
    }

    @Override
    public void run() {
        switch (type) {
            case SENDER: {
                try {
                    Socket socket = new Socket(sendTo, port);
                    File file = new File("./files/" + fileName);

                    // Get the size of the file
                    byte[] bytes = new byte[16 * 1024];
                    InputStream in = new FileInputStream(file);
                    OutputStream out = socket.getOutputStream();
                    int count;
                    while ((count = in.read(bytes)) > 0) {
                        out.write(bytes, 0, count);
                    }
                    out.close();
                    in.close();
                    socket.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case RECIEVER: {
                try {
                    InetAddress localIP = state.getLocalIP();
                    for (InetAddress host : hosts) {

                        if (host.equals(state.getLocalIP())) {
                            if (new File("./files/" + fileName).exists()) {
                                break;
                            } else continue;
                        }

                        Messaging.unicast(host, MessageFactory.getMessage(localIP, Message.MessageType.FILE_REQUEST, fileName));
                        long startTime = System.currentTimeMillis();
                        while (!Thread.interrupted() && System.currentTimeMillis() - startTime < FILE_AVAILABILITY_TIMEOUT)
                            ;

                        if (state.getTransfers().get(fileName).getStatus()) {
                            int port = (portRange[0] + new Random().nextInt(portRange[1] - portRange[0] + 1));
                            Messaging.unicast(host, MessageFactory.getMessage(localIP, Message.MessageType.SEND_FILE, fileName + "," + port));
                            ServerSocket serverSocket = new ServerSocket(port);
                            Socket socket = serverSocket.accept();
                            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                            int count;
                            byte[] buffer = new byte[8192]; // or 4096, or more
                            FileOutputStream fos = new FileOutputStream("./files/" + fileName);
                            while ((count = in.read(buffer)) > 0) {
                                fos.write(buffer, 0, count);
                            }
                            break;
                        }
                    }
                    File f = new File("./files/" + fileName);
                    if (!(f.exists() && !f.isDirectory())) {
                        System.out.println("404 : " + fileName + " could not be found. ");
                    } else {
                        state.getIndex().add(fileName, state.getLocalIP());
                        Messaging.unicast(state.getLeader(), MessageFactory.getMessage(localIP, Message.MessageType.FILE_LIST, state.getIndex().getFiles()));
                        System.out.println("Transfer completed : " + fileName + "(" + f.length() / 1024 + " kB)");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }

        }
        state.getTransfers().remove(fileName);
    }
}
