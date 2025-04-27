/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class SimpleFileClient extends JFrame {
    private JTextField serverField;
    private JTextField portField;
    private JTextField filePathField;
    private JButton browseButton;
    private JButton sendButton;
    private JTextArea logArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimpleFileClient().setVisible(true);
        });
    }

    public SimpleFileClient() {
        // Setup window
        setTitle("Simple File Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Top panel with server connection
        JPanel serverPanel = new JPanel();
        serverPanel.add(new JLabel("Server:"));
        serverField = new JTextField("localhost", 10);
        serverPanel.add(serverField);
        
        serverPanel.add(new JLabel("Port:"));
        portField = new JTextField("8000", 5);
        serverPanel.add(portField);
        
        // Middle panel with file selection
        JPanel filePanel = new JPanel();
        filePanel.add(new JLabel("File:"));
        filePathField = new JTextField(20);
        filePathField.setEditable(false);
        filePanel.add(filePathField);
        
        browseButton = new JButton("Browse");
        filePanel.add(browseButton);
        
        sendButton = new JButton("Send");
        filePanel.add(sendButton);
        
        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        
        // Add panels to frame
        JPanel topArea = new JPanel(new BorderLayout());
        topArea.add(serverPanel, BorderLayout.NORTH);
        topArea.add(filePanel, BorderLayout.SOUTH);
        
        add(topArea, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Event listeners
        browseButton.addActionListener(e -> browseFile());
        sendButton.addActionListener(e -> sendFile());
        
        // Center on screen
        setLocationRelativeTo(null);
    }
    
    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            filePathField.setText(file.getAbsolutePath());
        }
    }
    
    private void sendFile() {
        String filePath = filePathField.getText();
        if (filePath.isEmpty()) {
            log("No file selected");
            return;
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            log("File does not exist");
            return;
        }
        
        String server = serverField.getText().trim();
        int port;
        
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            log("Invalid port number");
            return;
        }
        
        // Disable UI during transfer
        sendButton.setEnabled(false);
        browseButton.setEnabled(false);
        
        // Send file in background thread
        new Thread(() -> {
            try {
                log("Connecting to server " + server + ":" + port);
                Socket socket = new Socket(server, port);
                log("Connected to server");
                
                // Send file info
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(file.getName());
                dos.writeLong(file.length());
                
                // Send file data
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int read;
                
                log("Sending file: " + file.getName());
                while ((read = fis.read(buffer)) > 0) {
                    dos.write(buffer, 0, read);
                }
                
                dos.flush();
                fis.close();
                
                // Get response
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                String response = dis.readUTF();
                log("Server response: " + response);
                
                // Close connection
                dis.close();
                dos.close();
                socket.close();
                log("File sent successfully");
                
            } catch (UnknownHostException e) {
                log("Error: Unknown host: " + server);
            } catch (IOException e) {
                log("Error sending file: " + e.getMessage());
            } finally {
                // Re-enable UI
                SwingUtilities.invokeLater(() -> {
                    sendButton.setEnabled(true);
                    browseButton.setEnabled(true);
                });
            }
        }).start();
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
        });
    }
}