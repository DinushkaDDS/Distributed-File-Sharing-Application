
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dilan
 */
public class GUI extends javax.swing.JPanel {

    /**
     * Creates new form GUI
     */
    
    private Node node;
    private JFrame frame;
    private int serverPort;
    
    public GUI() throws UnknownHostException {
        initComponents();
        
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(this);
        frame.pack();
        frame.setVisible(true);
        
        this.txthostIp.setText(InetAddress.getLocalHost().getHostAddress().trim());
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        lbl_uname = new javax.swing.JLabel();
        lbl_ipaddress = new javax.swing.JLabel();
        lbl_port = new javax.swing.JLabel();
        btn_Connect = new javax.swing.JButton();
        txtUname = new javax.swing.JTextField();
        txthostIp = new javax.swing.JTextField();
        txtHostPort = new javax.swing.JTextField();
        btnServerConfig = new javax.swing.JButton();

        jLabel1.setText("File Share Application");

        lbl_uname.setText("User Name");

        lbl_ipaddress.setText("Host IP Address");

        lbl_port.setText("Host Port");

        btn_Connect.setText("Connect to Server");
        btn_Connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ConnectActionPerformed(evt);
            }
        });

        txtUname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUnameActionPerformed(evt);
            }
        });

        btnServerConfig.setText("Server Config");
        btnServerConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnServerConfigActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(133, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(226, 226, 226))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(btn_Connect)
                            .addGap(98, 98, 98)
                            .addComponent(btnServerConfig)
                            .addContainerGap())
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lbl_ipaddress)
                                .addComponent(lbl_uname)
                                .addComponent(lbl_port))
                            .addGap(72, 72, 72)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtUname, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                                .addComponent(txthostIp)
                                .addComponent(txtHostPort))
                            .addGap(119, 119, 119)))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_uname)
                    .addComponent(txtUname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_ipaddress)
                    .addComponent(txthostIp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_port)
                    .addComponent(txtHostPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(85, 85, 85)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_Connect)
                    .addComponent(btnServerConfig))
                .addContainerGap(54, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents



    // Node Should be Registered to the BS and then GUI should show the file
            // sharing interface. (Add, Remove, Download, Search)
     
    private void btn_ConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_ConnectActionPerformed
        
        Node x = null;
        try {
            
            int hostListenPort = Integer.parseInt(txtHostPort.getText());
            String hostIp = txthostIp.getText();
            String uname = txtUname.getText();
            
            try{
                x = new Node(hostIp, hostListenPort, uname);
            }
            catch(Throwable ex){
                System.out.println("Socket out of range");
                JOptionPane.showMessageDialog(frame,"Socket out of range. Try Again!","Error!",JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            DatagramSocket ds = new DatagramSocket();
            ds.setSoTimeout(1000);
            
            byte msg[] = null;
            
            //Register Command in string form of  0036 REG 129.82.123.45 5001 1234abcd
            String registrationMsg = "REG";
            
            //Change the Message as required
            registrationMsg = registrationMsg + " " + hostIp + " " + hostListenPort +" " + uname;
            registrationMsg = String.format("%04d", registrationMsg.length() + 5) + " " + registrationMsg;
            msg = registrationMsg.getBytes();
            
            //Set the IP and Port of the Server to be registered.
            
//            Node.serverIp = txtServerIp.getText();
            InetAddress serverIP = InetAddress.getByName(Node.serverIp);
            serverPort = Node.serverPort;
            
            DatagramPacket DpSend = new DatagramPacket(msg, msg.length, serverIP, serverPort);
            ds.send(DpSend); 
            
            //Waiting for the Server Reply
            byte[] reply = new byte[65535]; 
            DatagramPacket DpReply = null;
            String s = "";
            
            while(true){
             
                DpReply = new DatagramPacket(reply, reply.length); 
                ds.receive(DpReply);
                
                byte[] data = DpReply.getData();
                s = new String(data, 0, DpReply.getLength());
                System.out.println(s);
                
                StringTokenizer st = new StringTokenizer(s, " ");
                
                int length = Integer.parseInt(st.nextToken());
                String command = st.nextToken();
                
                if(command.equals("REGOK")){
                    //Setting the current node value as registered on the server
                    this.node = x;
                    
                    int numOfMachines = Integer.parseInt(st.nextToken());
                    
                    if(numOfMachines==0){
                        
                        SearchGUI searchGUI = new SearchGUI(this.frame, this.node);
                        this.frame.add(searchGUI);
                        this.frame.getContentPane().remove(this);
                        frame.pack();
                        frame.setVisible(true);
                        return;
                    }
                    else if(numOfMachines==9998){
                        this.node.unRegFromServer();
                        this.node.listenSocket.close();
                    }
                    else if(numOfMachines==9997){
                        JOptionPane.showMessageDialog(frame,"Port is used by another User!","Error!",JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    else{
                        for (int i = 0; i< numOfMachines; i++){
                            String ip = st.nextToken();
                            int p = Integer.parseInt(st.nextToken());   
                            this.node.addNeighbour(ip, p);
                        }
                        break;
                    }
                    
                }
                else{
                    JOptionPane.showMessageDialog(frame,"Server Registration Failed! Try changing the Listening Port","Error!",JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            //informing the connected Nodes before showing the main interface
            String joinMsg = "JOIN";
            joinMsg = joinMsg + " " + hostIp + " "+ hostListenPort;
            joinMsg = String.format("%04d", joinMsg.length() + 5) + " " + joinMsg;
            
            msg = joinMsg.getBytes();
            
            boolean status = this.node.joinToNodes(ds, msg);
            
            if(status){
                //should modify to go the the file sharing interface
                SearchGUI searchGUI = new SearchGUI(this.frame, this.node);
                this.frame.add(searchGUI);
                this.frame.getContentPane().remove(this);
                frame.pack();
                frame.setVisible(true);
                return;
            }
            else{
                JOptionPane.showMessageDialog(frame,"Error Connecting to nodes. Try Again!","Error!",JOptionPane.ERROR_MESSAGE);
                this.node.unRegFromServer();
                this.node.listenSocket.close();
                return;
            }
            
        } catch (SocketTimeoutException ex) {
            System.out.println("Time out for the connection. Try Again");
            JOptionPane.showMessageDialog(frame,"Error Connecting to Server. Try Again!","Error!",JOptionPane.ERROR_MESSAGE);
        } catch (UnknownHostException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Throwable ex) {
            JOptionPane.showMessageDialog(frame,"Error. Try Again!","Error!",JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        x.disposeNode();

    }//GEN-LAST:event_btn_ConnectActionPerformed

    private void txtUnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUnameActionPerformed
        
    }//GEN-LAST:event_txtUnameActionPerformed

    private void btnServerConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnServerConfigActionPerformed
        try {

            ServerConfig configGUI = new ServerConfig();
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_btnServerConfigActionPerformed
    
    public static void main(String[] args) throws UnknownHostException {
        GUI gui = new GUI();
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnServerConfig;
    private javax.swing.JButton btn_Connect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lbl_ipaddress;
    private javax.swing.JLabel lbl_port;
    private javax.swing.JLabel lbl_uname;
    private javax.swing.JTextField txtHostPort;
    private javax.swing.JTextField txtUname;
    private javax.swing.JTextField txthostIp;
    // End of variables declaration//GEN-END:variables
    
}