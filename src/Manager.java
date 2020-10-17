import CORBA_A2.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.util.Calendar.YEAR;

public class Manager {
    static Functions FunctionImpl;
    private static String checkLocation(String ID){
        String location;
        if (ID.substring(0, 2).toLowerCase().equals("qc")){
            location = "QC";
        } else if (ID.substring(0, 2).toLowerCase().equals("on")) {
            location = "ON";
        } else {
            location = "BC";
        }
        return location;
    }

    public static void main(String[] args) {

		while (true) {
			//------------------------get manager ID-----------------------------
			String MID = "";
			try {
				System.out.println("Enter your Manager ID: ");
				// read user input
				InputStreamReader is = new InputStreamReader(System.in);
				BufferedReader br = new BufferedReader(is);
				String UserInput = br.readLine();
				MID = UserInput.toUpperCase();
				if (!MID.substring(0, 2).equals("QC") && !MID.substring(0, 2).equals("BC") && !MID.substring(0, 2).equals("ON")){
					System.out.println("Manager ID incorrect.");
					continue;
				}
			}	 catch (IOException e) {
				System.out.println(e.getMessage());
			}
			// open logger
			Logger logger = Logger.getLogger("MANAGER_interface");
			FileHandler fh = null;
			try {
				fh = new FileHandler("src/Manager_log/" + MID + ".log", true);
				logger.addHandler(fh);
				SimpleFormatter formatter = new SimpleFormatter();
				fh.setFormatter(formatter);
			} catch (Exception e){
				System.out.println(e.getMessage());
			}
			//------------------------read command-------------------------------
			String location = checkLocation(MID);
			while (true){
				try {
					System.out.println("Please key in your command: ");
					logger.info("Display: Please key in your command: ");
					// read user input
					InputStreamReader is = new InputStreamReader(System.in);
					BufferedReader br = new BufferedReader(is);
					String UserInput = br.readLine().toLowerCase();
					logger.info("User key in: " + UserInput);
					String[] splitedUserInput = UserInput.split(" ");
					if (UserInput.equals("quit")){															//QUIT
						if (fh != null){
							fh.close();
						}
						System.exit(0);
					} else if (splitedUserInput[0].toLowerCase().equals("logout")) {																			//Log other ID
						if (fh != null) {
							fh.close();
						}
						break;
					} else if (splitedUserInput[0].toLowerCase().equals("additem")) {						//AddItem
						if (splitedUserInput.length != 5){
							System.out.println("Number of Parameter incorrect. Please try again");
							logger.info("Display: Number of Parameter incorrect. Please try again");
						} else {
							try{
								Integer.parseInt(splitedUserInput[3]);
								Double.parseDouble(splitedUserInput[4]);
							} catch (Exception e){
								System.out.println("The parameter format is incorrect.");
								logger.warning("The parameter format is incorrect.");
								if (fh != null){
									fh.close();
								}
								continue;
							}
							try {
								ORB orb = ORB.init(args, null);
								org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");

								NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
								String name = "";
								if (location.equals("QC")){
									name = "QC_Server";
								} else if (location.equals("BC")) {
									name = "BC_Server";
								} else {
									name = "ON_Server";
								}
								FunctionImpl = FunctionsHelper.narrow(ncRef.resolve_str(name));
								System.out.println(FunctionImpl.addItem(MID, splitedUserInput[1], splitedUserInput[2], splitedUserInput[3], splitedUserInput[4]));
							} catch (Exception e){
								System.out.println(e.getMessage());
							}
						}
					} else if (splitedUserInput[0].toLowerCase().equals("removeitem")) {					//RemoveItem
						if (splitedUserInput.length != 3){
							System.out.println("Number of Parameter incorrect. Please try again");
							logger.info("Display: Number of Parameter incorrect. Please try again");
						} else {
							try{
								Integer.parseInt(splitedUserInput[2]);
							} catch (Exception e){
								System.out.println("The parameter format is incorrect.");
								logger.warning("The parameter format is incorrect.");
								if (fh != null){
									fh.close();
								}
								continue;
							}
							// TODO invoke function
							try {
								ORB orb = ORB.init(args, null);
								org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");

								NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
								String name = "";
								if (location.equals("QC")){
									name = "QC_Server";
								} else if (location.equals("BC")) {
									name = "BC_Server";
								} else {
									name = "ON_Server";
								}
								FunctionImpl = FunctionsHelper.narrow(ncRef.resolve_str(name));
								System.out.println(FunctionImpl.removeItem(MID, splitedUserInput[1], splitedUserInput[2]));
							} catch (Exception e){
								System.out.println(e.getMessage());
							}
						}
					} else if (splitedUserInput[0].toLowerCase().equals("listitemavailability")) {			//ListItemAvailability
						if (splitedUserInput.length != 1){
							System.out.println("Number of Parameter incorrect. Please try again");
							logger.info("Display: Number of Parameter incorrect. Please try again");
						} else {
							try {
								ORB orb = ORB.init(args, null);
								org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");

								NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
								String name = "";
								if (location.equals("QC")){
									name = "QC_Server";
								} else if (location.equals("BC")) {
									name = "BC_Server";
								} else {
									name = "ON_Server";
								}
								FunctionImpl = FunctionsHelper.narrow(ncRef.resolve_str(name));
								System.out.println(FunctionImpl.listItemAvailability(MID));
							} catch (Exception e){
								System.out.println(e.getMessage());
							}
						}
					} else {
						System.out.println("Please key in the right function name.");
						logger.info("Display: Please key in the right function name.");
						continue;
					}
				} catch (IOException e) {
					logger.warning(e.getMessage());
				}
			}

			//-----------------------------Final---------------------------------

		}
    }


}
