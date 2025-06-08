/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.hashing;

/**
 *
 * @author ahmdsaif
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.mindrot.jbcrypt.BCrypt;
import com.lambdaworks.crypto.SCryptUtil;

public class HashingApp extends JFrame {
    private JTextArea inputArea;
    private JComboBox<String> algorithmBox;
    private JButton hashButton, fileButton;
    private JTextArea outputArea;

    public HashingApp() {
        setTitle("Hashing App - PBKDF2 / bcrypt / scrypt");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        inputArea = new JTextArea(5, 40);
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputArea.setLineWrap(true);

        String[] algorithms = {"PBKDF2", "bcrypt", "scrypt"};
        algorithmBox = new JComboBox<>(algorithms);

        hashButton = new JButton("Hash Text");
        fileButton = new JButton("Hash File");

        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        JScrollPane outputScroll = new JScrollPane(outputArea);

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Choose Algorithm:"));
        topPanel.add(algorithmBox);
        topPanel.add(hashButton);
        topPanel.add(fileButton);

        add(topPanel, BorderLayout.NORTH);
        add(inputScroll, BorderLayout.CENTER);
        add(outputScroll, BorderLayout.SOUTH);

        hashButton.addActionListener(e -> hashText());
        fileButton.addActionListener(e -> hashFile());
    }

    private void hashText() {
        String input = inputArea.getText();
        String algo = (String) algorithmBox.getSelectedItem();
        String result = switch (algo) {
            case "PBKDF2" -> pbkdf2Hash(input);
            case "bcrypt" -> bcryptHash(input);
            case "scrypt" -> scryptHash(input);
            default -> "Unknown algorithm";
        };
        outputArea.setText(result);
    }

    private void hashFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            try {
                byte[] fileBytes = Files.readAllBytes(selected.toPath());
                String content = new String(fileBytes);
                inputArea.setText(content);
                hashText();
            } catch (IOException e) {
                outputArea.setText("Error reading file: " + e.getMessage());
            }
        }
    }

    private String pbkdf2Hash(String input) {
        try {
            byte[] salt = "randomSalt123456".getBytes(); // Ideally, generate securely
            PBEKeySpec spec = new PBEKeySpec(input.toCharArray(), salt, 10000, 256);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return "PBKDF2 Error: " + e.getMessage();
        }
    }

    private String bcryptHash(String input) {
        return BCrypt.hashpw(input, BCrypt.gensalt());
    }

    private String scryptHash(String input) {
        return SCryptUtil.scrypt(input, 16384, 8, 1);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HashingApp().setVisible(true));
    }
}
