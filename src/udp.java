import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class udp {
    public static void main(String[] args) {
        DatagramSocket aSocket = null;
		try{
			System.out.println("Start UDP client.");
			aSocket = new DatagramSocket(); //reference of the original socket
            String msg = "purchaseItem";
			byte [] message = msg.getBytes();

			InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 500;

			DatagramPacket request = new DatagramPacket(message, msg.length(), aHost, serverPort);//request packet ready
			aSocket.send(request);//request sent out
			System.out.println("Request message sent from the Server" + " is : "+ new String(request.getData()));

			byte [] buffer = new byte[1000];//to store the received data, it will be populated by what receive method returns
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.

			//Client waits until the reply is received-----------------------------------------------------------------------
			aSocket.receive(reply);//reply received and will populate reply packet now.
            String replymsg = new String(reply.getData());
			System.out.println("Reply received from the server is: "+ replymsg);//print reply message after converting it to a string from bytes

		}
		catch(SocketException e){
			System.out.println("Socket: "+e.getMessage());
		}
		catch(IOException e){
			e.printStackTrace();
			System.out.println("IO: "+e.getMessage());
		}
		finally{
			if(aSocket != null) aSocket.close();//now all resources used by the socket are returned to the OS, so that there is no
												//resource leakage, therefore, close the socket after it's use is completed to release resources.
		}
    }
}
