import CORBA_A2.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class ON_Server {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("ON_SERVER");
        FileHandler fh = null;
        try {
            fh = new FileHandler("src/ON_SERVER/ON_SERVER.log", true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        try {
            //---------------------------ORB--------------------------
            // create and initialize the ORB
            ORB orb= ORB.init(args, null);
            // get reference to rootpoa& activate the POAManager
            POA rootpoa= POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();
            // create servant and register it with the ORB
            FunctionImpl functionImpl= new FunctionImpl(logger);
            functionImpl.setORB(orb);
            //get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(functionImpl);
            Functions href= (Functions) FunctionsHelper.narrow(ref);
            //get the root naming context
            //NameService invokes the name service
            org.omg.CORBA.Object objRef= orb.resolve_initial_references("NameService");
            //Use NamingContextExt which is part of the Interoperable Naming Service (INS) specification.
            NamingContextExt ncRef= NamingContextExtHelper.narrow(objRef);
            //bind the Object Reference in Naming
            String name = "ON_Server";
            NameComponent path[] = ncRef.to_name( name );
            ncRef.rebind(path, href);
            System.out.println("ON_SERVER ready and waiting ...");
            logger.info("ON_SERVER ready and waiting ...");
            //wait for invocations from clients

            //---------------------------UDP--------------------------
            String findItem = "";

            DatagramSocket aSocket = null;

            try {
                aSocket = new DatagramSocket(400);
                byte[] buffer = new byte[1000];
                System.out.println("UDP Server started.....");
                logger.info("UDP Server started.....");
                while (true) {
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(request);
                    String requestData = new String(request.getData(), 0, request.getLength());
                    System.out.println(requestData);
//                  log.logfile(userInterfaceImplementationClass.con_server, userInterfaceImplementationClass.con_server, "Request received in concordia server", "Request received", "Request received to server..." + requestData);
                    // TODO res
                    String res = requestData;
                    String[] rm = requestData.split(",");
                    if (rm[0].equals("purchaseitem")){
                        res = functionImpl.purchaseItem(rm[1], rm[2], rm[3], rm[4]);
                    } else if (rm[0].equals("CheckBudget")){
                        res = functionImpl.checkBudget(rm[1]);
                    } else if (rm[0].equals("updateBudget")){
                        functionImpl.updateBudget(rm[1], rm[2]);
                        res = "";
                    } else if (rm[0].equals("findItem")) {
                        res = functionImpl.findItem(rm[1], "ON", true);
                    } else if (rm[0].equals("returnitem")) {
                        res = functionImpl.returnItem(rm[1], rm[2], rm[3], true);
                    } else if (rm[0].equals("checkotherstorepurchasevalid")) {
                        res = functionImpl.checkOtherStorePurchaseValid(rm[1], rm[2], rm[3], rm[4], rm[5], true);
                    } else if (rm[0].equals("checkotherstorereturnvalid")) {
                        res = functionImpl.checkOtherStoreReturnValid(rm[1], rm[2], rm[3], true);
                    }
                    System.out.println("reply: " + res);
                    DatagramPacket reply = new DatagramPacket(res.getBytes(), res.length(), request.getAddress(), request.getPort());// reply packet ready
                    aSocket.send(reply);// reply sent
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                logger.warning(e.getMessage());
            } finally {
                if (aSocket != null){
                    aSocket.close();
                }
            }
            // wait for invocations from clients
            for (;;) {
                orb.run();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.warning(e.getMessage());
        }
    }
}
