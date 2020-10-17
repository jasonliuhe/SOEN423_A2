import CORBA_A2.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class User {
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
        while (true){
            //------------------------get User ID-----------------------------
            String UID = "";
			try {
				System.out.println("Enter your User ID: ");
				// read user input
				InputStreamReader is = new InputStreamReader(System.in);
				BufferedReader br = new BufferedReader(is);
				String UserInput = br.readLine();
				UID = UserInput.toUpperCase();
				if (!UID.substring(0, 2).equals("QC") && !UID.substring(0, 2).equals("BC") && !UID.substring(0, 2).equals("ON")){
					System.out.println("User ID incorrect.");
					continue;
				}
			}	 catch (IOException e) {
				System.out.println(e.getMessage());
			}
			// open logger
			Logger logger = Logger.getLogger("USER_interface");
			FileHandler fh = null;
			try {
				fh = new FileHandler("src/User_log/" + UID + ".log", true);
				logger.addHandler(fh);
				SimpleFormatter formatter = new SimpleFormatter();
				fh.setFormatter(formatter);
			} catch (Exception e){
				System.out.println(e.getMessage());
			}
			//------------------------read command-------------------------------
			String location = checkLocation(UID);
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
					} else if (splitedUserInput[0].toLowerCase().equals("logout")) {
						if (fh != null){
							fh.close();
						}
						break;
					} else if (splitedUserInput[0].toLowerCase().equals("purchaseitem")) {					//purchaseitem
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
								System.out.println(FunctionImpl.purchaseItem(UID, splitedUserInput[1], splitedUserInput[2]));
							} catch (Exception e){
								System.out.println(e.getMessage());
							}
						}
					} else if (splitedUserInput[0].toLowerCase().equals("finditem")) {					   //finditem
						if (splitedUserInput.length != 2){
							System.out.println("Number of Parameter incorrect. Please try again");
							logger.info("Display: Number of Parameter incorrect. Please try again");
						} else {
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
								System.out.println(FunctionImpl.findItem(UID, splitedUserInput[1]));
							} catch (Exception e){
								System.out.println(e.getMessage());
							}
						}
					} else if (splitedUserInput[0].toLowerCase().equals("returnitem")) {			        //returnitem
						if (splitedUserInput.length != 3){
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
								System.out.println(FunctionImpl.returnItem(UID, splitedUserInput[1], splitedUserInput[2]));
							} catch (Exception e){
								System.out.println(e.getMessage());
							}
						}
					} else if (splitedUserInput[0].toLowerCase().equals("exchangeitem")) {      //exchangeitem
                        if (splitedUserInput.length != 4){
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
								System.out.println(FunctionImpl.exchangeItem(UID, splitedUserInput[1], splitedUserInput[2], splitedUserInput[3]));
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
        }
    }
}
