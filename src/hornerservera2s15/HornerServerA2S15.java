/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hornerservera2s15;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 *  Robert Horner
 * 
 */
public class HornerServerA2S15 {
    
    public static final int SERVER_PORT = 33312;
    public static final int MAX_UDP_MESSAGE_SIZE = 65507;
    
    public static void main(String[] args) throws SocketException, IOException {
        
        String mp3Path;
        int fileSize;
        DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT);
        
        byte[] rData = new byte[1024];
        byte[] sData = new byte[1024];
        
        DatagramPacket rPacket = new DatagramPacket(rData, rData.length);
        serverSocket.receive(rPacket);
        
        InetAddress cAddress = rPacket.getAddress();
        int cPort = rPacket.getPort();
        
        mp3Path = new String(rPacket.getData());
        String mp3Path2 = mp3Path.trim();
        System.out.println("Specified path is " + mp3Path2 + "@@@");
        
        try{
            
            File mp3 = new File(mp3Path2);
            System.out.println("file Found!");
            fileSize = (int)mp3.length();
            System.out.println("size of file: " + fileSize);
            FileInputStream fIn = new FileInputStream(mp3);
            System.out.println("Input stream created!");
            
            //send filesize to client
            String fileSizeString = Integer.toString(fileSize);
            System.out.println("Sending size of " + fileSizeString + " to client");
            sData = new byte[1024];
            sData = fileSizeString.getBytes();
            DatagramPacket sPacket = new DatagramPacket(sData, sData.length , cAddress, cPort);
            
            serverSocket.send(sPacket);
            //Diagnostic
          
            //setup loop for sending packets
            
            //receive ok from client?
            rData = new byte[1024];
            
            rPacket = new DatagramPacket(rData, rData.length);
            
            serverSocket.receive(rPacket);
            
            if(verifyClientReceivedMessage(rPacket)){
                
                BufferedInputStream inStream = new BufferedInputStream(fIn);
                
                int index = 0;
                
                while(index < fileSize){
                    if(index + MAX_UDP_MESSAGE_SIZE < fileSize){    //test for terminal read
                        
                        sData = new byte[MAX_UDP_MESSAGE_SIZE];
                        // inStream.read(byte[] b, int off, int len)
                        inStream.read(sData);

                        //update index
                        index += (MAX_UDP_MESSAGE_SIZE +1);
                        
                        sPacket = new DatagramPacket(sData, sData.length, cAddress, cPort);
                        //send packet
                        serverSocket.send(sPacket);
                        
                        //Diagnostic - show how many bytes sent
                        System.out.println(index + "\\" + fileSize + " bytes sent");
                        
                        //wait until a response is recieved
                        rData = new byte[1024];
                        rPacket = new DatagramPacket(rData, rData.length);
                        serverSocket.receive(rPacket);
                        if(!verifyClientReceivedMessage(rPacket)){
                            System.err.println("UH OH");
                        }
                            
                        
                    }//end if
                    else{   //terminal read
                        
                        int size_of_last_read = fileSize - index;
                        
                        sData = new byte[size_of_last_read];
                        
                        inStream.read(sData);
                        
                        sPacket = new DatagramPacket(sData, sData.length, cAddress, cPort);
                        //send packet
                        serverSocket.send(sPacket);
                        
                        index += (size_of_last_read +1);
                        System.out.println(index + "\\" + fileSize + " bytes sent");
                        System.out.println("***Terminating Connection***");
                        break;
                    }
                }//end while
            }
            serverSocket.close();
            
        }
        catch(FileNotFoundException e){
            String mp3NotFound = "Invalid path for mp3 file";
            //Diagnostic
            System.out.println(mp3NotFound);
            
            //if path was incorrect, send back negative Long to client
            fileSize = -1;
            String fileSizeString = Long.toString(fileSize);
            
            DatagramPacket sPacket = new DatagramPacket(fileSizeString.getBytes(), 64, cAddress, cPort);
            serverSocket.send(sPacket);
            System.err.println("Ending Server...");
            System.exit(0);
        }
        
        
    }//end of main
    public static boolean verifyClientReceivedMessage(DatagramPacket rPacket){
        String rMessage = new String(rPacket.getData());
        String trimmedMessage = rMessage.trim();
        return(trimmedMessage.equals("success"));
    }        
}//end HornerServer
