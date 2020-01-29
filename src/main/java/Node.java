
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import java.security.DigestInputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

class Node implements Runnable {

    private String ipAddress;
    private int listenPort;
    private String uname;
    private Map<Integer, String> neighbours;
    String location = "./Sharable_Files/";
    DefaultListModel<String> fileDetailsList;

    DatagramSocket listenSocket;

    public static String serverIp = "127.0.0.1";
    public static int serverPort = 55555;

    private volatile boolean listen;

    Node(String hostIp, int hostListenPort, String uname) throws SocketException {
        this.ipAddress = hostIp;
        this.listenPort = hostListenPort;
        this.uname = uname;
        this.neighbours = new HashMap<>();
        this.listen = true;
        this.listenSocket = new DatagramSocket(this.listenPort);
        this.fileDetailsList = new DefaultListModel<>();

    }

    public void addNeighbour(String ip, int port) {
        neighbours.put(port, ip);
    }

    //Write the code to respond to join, leave, ser and error messages sent from the peers
    @Override
    public void run() {

        try {
            byte[] reply = new byte[65535];
            DatagramPacket DpRequest = null;
            String s = "";

            while (listen) {

                DpRequest = new DatagramPacket(reply, reply.length);
                listenSocket.receive(DpRequest);

                byte[] data = DpRequest.getData();
                s = new String(data, 0, DpRequest.getLength());
                System.out.println("Incoming --> " + s);

                StringTokenizer st = new StringTokenizer(s, " ");

                int length = Integer.parseInt(st.nextToken());
                String command = st.nextToken();

                if (command.equals("JOIN")) {

                    String nodeIp = st.nextToken();
                    int nodePort = Integer.parseInt(st.nextToken());

                    this.neighbours.put(nodePort, nodeIp);

                    String response = "0014 JOINOK 0";

                    byte[] responseMsg = response.getBytes();
                    InetAddress nodeIP = InetAddress.getByName(nodeIp);

                    //Set the IP and Port of the Node joined.
                    DatagramPacket DpSend = new DatagramPacket(responseMsg, responseMsg.length, nodeIP, DpRequest.getPort());
                    listenSocket.send(DpSend);
                    System.out.println("Outgoing --> " + response);

                } else if (command.equals("LEAVE")) {

                    String nodeIp = st.nextToken();
                    int nodePort = Integer.parseInt(st.nextToken());

                    boolean x = this.neighbours.remove(nodePort, nodeIp);
                    String response = "";
                    if (x) {
                        response = "0015 LEAVENOK 0";
                    } else {
                        response = "0018 JOINOK 9999";
                    }

                    byte[] responseMsg = response.getBytes();
                    InetAddress nodeIP = InetAddress.getByName(nodeIp);

                    //Set the IP and Port of the Node joined.
                    DatagramPacket DpSend = new DatagramPacket(responseMsg, responseMsg.length, nodeIP, DpRequest.getPort());
                    listenSocket.send(DpSend);
                    System.out.println("Outgoing --> " + response);

                } else if (command.equals("SER")) {

                    String nodeIp = st.nextToken();
                    int nodePort = Integer.parseInt(st.nextToken());

                    String fileNamePart = st.nextToken();
                    fileNamePart = fileNamePart.replaceAll("@@@@@", " ");

                    int hops = Integer.parseInt(st.nextToken());

                    //Stopping network congestion
                    if (hops > 4) {
                        continue;
                    }
                    if (nodePort == this.listenPort && nodeIp.equals(this.ipAddress)) {
                        continue;
                    }

                    ArrayList<String> matchedFiles = searchFile(fileNamePart);

                    if (!matchedFiles.isEmpty()) {

                        byte msg[] = null;

                        String searchMsg = "SEROK";

                        //Change the Message as required
                        searchMsg = searchMsg + " " + matchedFiles.size() + " " + this.getIp() + " " + this.getPort() + " " + hops;

                        for (String x : matchedFiles) {
                            searchMsg = searchMsg + " " + x;
                        }

                        searchMsg = String.format("%04d", searchMsg.length() + 5) + " " + searchMsg;
                        msg = searchMsg.getBytes();

                        InetAddress nodeIP = InetAddress.getByName(nodeIp);
                        DatagramPacket DpSend = new DatagramPacket(msg, msg.length, nodeIP, nodePort);
                        listenSocket.send(DpSend);
                        System.out.println("Outgoing --> " + searchMsg);

                    }

                    //Avoiding Network congestion
                    if (hops < 4) {

                        hops = hops + 1;
                        for (Map.Entry<Integer, String> entry : this.neighbours.entrySet()) {
                            String ip = entry.getValue();
                            int port = entry.getKey();

                            if (port == DpRequest.getPort() && ip.equals(DpRequest.getAddress().getHostAddress())) {
                                continue;
                            } else if (port == this.listenPort && ip.equals(this.ipAddress)) {
                                continue;
                            } else {
                                InetAddress nodeIP = InetAddress.getByName(ip);

                                byte[] msg = null;
                                fileNamePart = fileNamePart.replaceAll(" ", "@@@@@");
                                String searchMsg = "SER " + nodeIp + " " + nodePort + " " + fileNamePart + " " + hops;
                                searchMsg = String.format("%04d", searchMsg.length() + 5) + " " + searchMsg;
                                msg = searchMsg.getBytes();

                                DatagramPacket DpSend = new DatagramPacket(msg, msg.length, nodeIP, port);
                                listenSocket.send(DpSend);
                                System.out.println("Outgoing --> " + searchMsg);
                            }

                        }
                    }
                } else if (command.equals("SEROK")) {

                    int numOfFiles = Integer.parseInt(st.nextToken());
                    String nodeIp = st.nextToken();
                    int nodePort = Integer.parseInt(st.nextToken());
                    int hops = Integer.parseInt(st.nextToken());

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < numOfFiles; i++) {
                                String fileName = st.nextToken();
                                fileName = fileName.replaceAll("@@@@@", " ");
                                fileDetailsList.addElement(fileName + " | " + nodeIp + " | " + nodePort);
                            }
                        }
                    });

                } else if (command.equals("GET")) {

                    String fileName = st.nextToken();
                    int tcpPort = Integer.parseInt(st.nextToken());
                    fileName = fileName.replaceAll("@@@@@", " ");
                    final String name = fileName;
                    String ip = DpRequest.getAddress().getHostAddress();

                    //Start new thread to send the file to other node
                    Runnable r = new Runnable() {
                        public void run() {

                            try {

                                MessageDigest md = MessageDigest.getInstance("SHA-256");

                                FileInputStream is = new FileInputStream(location + name);

                                DigestInputStream dis = new DigestInputStream(is, md);

                                Socket clientSocket = new Socket(InetAddress.getByName(ip), tcpPort);
                                DataInputStream input = new DataInputStream(dis);
                                DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

                                byte[] data = input.readAllBytes();

                                output.write(data);
                                output.flush();
                                output.close();

                                byte[] digest = md.digest();
                                String hash = "";
                                for (int i = 0; i < digest.length; i++) {
                                    hash = hash + Byte.toString(digest[i]);
                                }

                                double size = data.length;

                                String infoMessage = "File Name: " + name + " File Size(bytes): " + size + "\nHash of File: " + hash;

                                JOptionPane.showMessageDialog(null, infoMessage, "File Sent Successfully!", JOptionPane.INFORMATION_MESSAGE);

                                is.close();
                                dis.close();
                                input.close();

                                clientSocket.close();
                                System.out.println("Success --> Successfully Sent the file");

                            } catch (UnknownHostException ex) {
                                System.out.println("Error --> Socket biding failed!");
                                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                System.out.println("Error --> File Not Found");
                                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (NoSuchAlgorithmException ex) {
                                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    };
                    new Thread(r).start();

                }
            }

            System.out.println("Stopped Listening");

        } catch (SocketException ex) {
            System.out.println("Socket Closed");
        } catch (IOException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //Joining to other nodes peers map while adding them to this nodes neighbours map.
    public boolean joinToNodes(DatagramSocket ds, byte[] msg) {

        boolean out = false;

        for (Map.Entry<Integer, String> entry : neighbours.entrySet()) {
            System.out.println("---------------------------------------");
            String ip = entry.getValue();
            int nodePort = entry.getKey();

            try {
                InetAddress nodeIP = InetAddress.getByName(ip);

                //Set the IP and Port of the Node to be joined.
                DatagramPacket DpSend = new DatagramPacket(msg, msg.length, nodeIP, nodePort);
                ds.send(DpSend);

                //Waiting for the node to Reply
                byte[] reply = new byte[65535];
                DatagramPacket DpReply = null;
                String s = "";

                DpReply = new DatagramPacket(reply, reply.length);
                ds.receive(DpReply);

                byte[] data = DpReply.getData();
                s = new String(data, 0, DpReply.getLength());
                System.out.println(s);

                StringTokenizer st = new StringTokenizer(s, " ");

                int length = Integer.parseInt(st.nextToken());
                String command = st.nextToken();

                if (command.equals("JOINOK")) {

                    int response = Integer.parseInt(st.nextToken());
                    if (response == 0) {
                        System.out.println("Successfully joined to : " + ip + " " + nodePort);
                        out = true;
                    } else {
                        System.out.println("Problem joining to : " + ip + " " + nodePort);
                    }
                }

            } catch (SocketTimeoutException ex) {
                continue;
            } catch (IOException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return out;
    }

    public boolean unRegFromServer() throws SocketTimeoutException, UnknownHostException, IOException {

        boolean out = false;

        String cmd = "UNREG";

        DatagramSocket ds = new DatagramSocket();
        ds.setSoTimeout(1000);
        byte msg[] = null;

        InetAddress serverAddress = InetAddress.getByName(serverIp);

        cmd = cmd + " " + this.ipAddress + " " + this.listenPort + " " + this.uname;
        cmd = String.format("%04d", cmd.length() + 5) + " " + cmd;
        msg = cmd.getBytes();

        DatagramPacket DpSend = new DatagramPacket(msg, msg.length, serverAddress, serverPort);
        ds.send(DpSend);

        byte[] reply = new byte[65535];
        DatagramPacket DpReply = new DatagramPacket(reply, reply.length);
        ds.receive(DpReply);

        byte[] data = DpReply.getData();
        String s = new String(data, 0, DpReply.getLength());
        System.out.println(s);

        StringTokenizer st = new StringTokenizer(s, " ");

        int length = Integer.parseInt(st.nextToken());
        String command = st.nextToken();

        if (command.equals("UNROK")) {
            //Setting the current node value as registered on the server
            int response = Integer.parseInt(st.nextToken());

            if (response == 0) {
                System.out.println("Successfully Unregistered from the server!");
                out = true;
            } else {
                System.out.println("Unregistering Unsuccssfull. Try again!");
            }
        }
        this.listenSocket.close();
        return out;
    }

    public boolean leaveNodes() throws SocketException, UnknownHostException, IOException {

        boolean out = true;

        DatagramSocket ds = new DatagramSocket();
        ds.setSoTimeout(1000);
        byte msg[] = null;

        String cmd = "LEAVE";

        cmd = cmd + " " + this.ipAddress + " " + this.listenPort;
        cmd = String.format("%04d", cmd.length() + 5) + " " + cmd;
        msg = cmd.getBytes();

        for (Map.Entry<Integer, String> entry : neighbours.entrySet()) {
            System.out.println("---------------------------------------");
            String ip = entry.getValue();
            int nodePort = entry.getKey();

            InetAddress nodeIP = InetAddress.getByName(ip);
            try {
                //Set the IP and Port of the Node to be joined.
                DatagramPacket DpSend = new DatagramPacket(msg, msg.length, nodeIP, nodePort);
                ds.send(DpSend);

                //Waiting for the node to Reply
                byte[] reply = new byte[65535];
                DatagramPacket DpReply = null;
                String s = "";

                DpReply = new DatagramPacket(reply, reply.length);
                ds.receive(DpReply);

                byte[] data = DpReply.getData();
                s = new String(data, 0, DpReply.getLength());
                System.out.println(s);

                StringTokenizer st = new StringTokenizer(s, " ");

                int length = Integer.parseInt(st.nextToken());
                String command = st.nextToken();

                if (command.equals("LEAVEOK")) {

                    int response = Integer.parseInt(st.nextToken());
                    if (response == 0) {
                        System.out.println("Successfully left the Node : " + ip + " " + nodePort);
                    } else {
                        System.out.println("Node not existed on : " + ip + " " + nodePort);
                    }
                    this.neighbours.remove(nodePort, ip);
                }
            } catch (SocketTimeoutException er) {
                System.out.println("Cannot contact node: " + ip + " " + nodePort);
                out = true;
            }

        }

        return out;

    }

    public void disposeNode() {
        this.ipAddress = null;
        this.listen = false;
        this.listenSocket.close();
        this.listenPort = 0;
        this.neighbours = null;
        this.uname = null;

    }

    public String viewPeers() {
        String x = "";
        for (Map.Entry<Integer, String> entry : this.neighbours.entrySet()) {
            String ip = entry.getValue();
            int nodePort = entry.getKey();

            x = x + ip + " " + nodePort + "\n";

        }
        return x;
    }

    public String getName() {
        return this.uname;
    }

    public String getIp() {
        return this.ipAddress;
    }

    public String getPort() {
        return Integer.toString(this.listenPort);
    }

    public Map getNeighbours() {
        return this.neighbours;
    }

    private ArrayList searchFile(String fileNamePart) {

        ArrayList<String> matchedFiles = new ArrayList<>();
        //Reading shared files from the folder
        File folder = new File(this.location);

        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {

                    String[] tokens = listOfFile.getName().split("\\s|\\.");

                    String[] fileNameParts = fileNamePart.split(" ");

                    boolean allAvailable = true;
                    boolean gg = false;

                    for (String x : fileNameParts) {
                        x = x.toLowerCase();
                        for (String y : tokens) {
                            y = y.toLowerCase();
                            if (x.equals(y)) {
                                gg = true;
                                break;
                            }
                        }
                        if (!gg) {
                            allAvailable = false;
                            break;
                        }
                        gg = false;
                    }

                    if (allAvailable && tokens.length >= fileNameParts.length) {
                        String file = listOfFile.getName();
                        file = file.replaceAll(" ", "@@@@@");

                        matchedFiles.add(file);
                    }
                }
            }
        }

        return matchedFiles;
    }

}
