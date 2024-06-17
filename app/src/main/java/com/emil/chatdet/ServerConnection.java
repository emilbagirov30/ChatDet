package com.emil.chatdet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ServerConnection {

    private BufferedReader reader ;
    private Socket socket;
    private BufferedWriter writer ;

    public ServerConnection(String ip, int port) throws IOException {
        this.socket = new Socket(ip, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

    }

    public void sendText(String message) throws IOException {
       writer.write(message.replace("\n", ChatDetPrivateKey.NEW_LINE_KEY));
       writer.newLine();
       writer.flush();
    }

    public String receiveText() throws IOException, ClassNotFoundException {
        return reader.readLine();
    }

    public void close() throws IOException {
       socket.close();
       reader.close();
      writer.close();
    }
}