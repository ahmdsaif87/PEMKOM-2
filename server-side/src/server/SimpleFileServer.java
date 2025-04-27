/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package server;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class SimpleFileServer extends JFrame {
    private JButton startButton;
    private JTextField portField;
    private JTextArea logArea;
    private ServerSocket serverSocket;
    private boolean running = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimpleFileServer().setVisible(true);
        });
    }

    public SimpleFileServer() {
        // Setup window
        setTitle("Simple File Server");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Top panel with controls
        JPanel topPanel = new JPanel();
        JLabel portLabel = new JLabel("Port:");
        portField = new JTextField("8000", 5);
        startButton = new JButton("Start");
        
        topPanel.add(portLabel);
        topPanel.add(portField);
        topPanel.add(startButton);
        
        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        
        // Add components to frame
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Event listeners
        startButton.addActionListener(e -> toggleServer());
        
        // Center on screen
        setLocationRelativeTo(null);
    }
    
    private void toggleServer() {
        if (!running) {
            startServer();
        } else {
            stopServer();
        }
    }
    
    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            serverSocket = new ServerSocket(port);
            running = true;
            
            startButton.setText("Stop");
            portField.setEnabled(false);
            log("Server started on port " + port);
            
            // Accept clients in background thread
            new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        if (running) {
                            log("Error: " + e.getMessage());
                        }
                    }
                }
            }).start();
            
        } catch (IOException e) {
            log("Error starting server: " + e.getMessage());
        } catch (NumberFormatException e) {
            log("Invalid port number");
        }
    }
    
    private void stopServer() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            startButton.setText("Start");
            portField.setEnabled(true);
            log("Server stopped");
        } catch (IOException e) {
            log("Error stopping server: " + e.getMessage());
        }
    }
    
    private void handleClient(Socket clientSocket) {
        new Thread(() -> {
            try {
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                log("Client connected: " + clientAddress);
                
                // Create downloads directory if it doesn't exist
                File downloadsDir = new File("downloads");
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdir();
                }
                
                // Read file info
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                String fileName = dis.readUTF();
                long fileSize = dis.readLong();
                
                log("Receiving file: " + fileName + " (" + fileSize + " bytes)");
                
                // Save file
                FileOutputStream fos = new FileOutputStream("downloads/" + fileName);
                byte[] buffer = new byte[4096];
                int read;
                long remaining = fileSize;
                
                while ((read = dis.read(buffer, 0, (int)Math.min(buffer.length, remaining))) > 0) {
                    fos.write(buffer, 0, read);
                    remaining -= read;
                }
                
                fos.close();
                
                // Send confirmation
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                dos.writeUTF("File received successfully");
                dos.close();
                
                log("File saved: downloads/" + fileName);
                clientSocket.close();
                log("Client disconnected: " + clientAddress);
                
            } catch (IOException e) {
                log("Error handling client: " + e.getMessage());
            }
        }).start();
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
        });
    }
}