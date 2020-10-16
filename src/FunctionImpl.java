import CORBA_A2.FunctionsPOA;
import org.omg.CORBA.ORB;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

class FunctionImpl extends FunctionsPOA {

    private ORB orb;
    Logger logger;
    private Map<String, String> itemID_itemName_quantity_price = new HashMap<>();
    private Map<String, String> custom_Budget = new HashMap<>();
    private Map<String, String> CustomerID_itemDate = new HashMap<>();
    private Map<String, String> waitingList = new HashMap<>();

    public void setORB(ORB orb_val){
        orb = orb_val;
    }

    public FunctionImpl (Logger logger){
        this.logger = logger;
    }

    @Override
    public String addItem(String managerID, String itemID, String itemName, String quantity, String price) {
        logger.info("Add item...");
        synchronized (this) {
            if (itemID_itemName_quantity_price.get(itemID) == null) {
                logger.info("Cannot find this item in database. Adding new item.");
                StringBuilder n = new StringBuilder(itemName);
                n.append(",");
                n.append(quantity);
                n.append(",");
                n.append(price);
                itemID_itemName_quantity_price.put(itemID, n.toString());
                System.out.println(managerID + " add: " + n.toString());
                logger.info(managerID + " add: " + n.toString());
            } else {
                String data = itemID_itemName_quantity_price.get(itemID);
                String Sdata[] = data.split(",");
                if (!Sdata[0].equals(itemName)) {
                    logger.info("Add item fail: the item name does not match the record.");
                    return "Add item fail: the item name does not match the record.";
                } else if (!Sdata[2].equals(String.valueOf(price))) {
                    logger.info("Add item fail: the item price does not match the record.");
                    return "Add item fail: the item price does not match the record.";
                } else if (Integer.parseInt(Sdata[1]) + Integer.parseInt(quantity) <= 0) {
                    logger.info("Add item fail: The quantity cannot be less than 1.");
                    return "Add item fail: The quantity cannot be less than 1.";
                } else {
                    Sdata[1] = String.valueOf(Integer.parseInt(Sdata[1]) + Integer.parseInt(quantity));
                    StringBuilder n = new StringBuilder(Sdata[0]);
                    n.append(",");
                    n.append(Sdata[1]);
                    n.append(",");
                    n.append(Sdata[2]);
                    itemID_itemName_quantity_price.put(itemID, n.toString());
                    System.out.println(managerID + " add: " + n.toString());
                    logger.info(managerID + " add: " + n.toString());
                }
            }
            logger.info("Add new item succeed");
            giveItemToCustumOnWaitingList(itemID, Double.parseDouble(price));
        }
        return "Add new item succeed";
    }

    @Override
    public String removeItem(String managerID, String itemID, String quantity) {
        synchronized (this){
            // remove item
            if (itemID_itemName_quantity_price.get(itemID) == null){
                logger.info("Item does not exist.");
                return "Item does not exist.";
            } else if (Integer.parseInt(quantity) < -1){
                logger.info("quantity cannot be less than 0");
                return "quantity cannot be less than 0";
            } else {
                if (Integer.parseInt(quantity) == -1){
                    // remove completely
                    itemID_itemName_quantity_price.remove(itemID);
                    logger.info("Item: " + itemID +" remove completely");
                } else {
                    int itemQuantity = Integer.parseInt(itemID_itemName_quantity_price.get(itemID).split(",")[1]);
                    if (itemQuantity < Integer.parseInt(quantity)){
                        logger.info("Cannot remove: quantity is higher than what we have in stock.");
                        return "Cannot remove: quantity is higher than what we have in stock.";
                    } else {
                        itemQuantity = itemQuantity - Integer.parseInt(quantity);
                        itemID_itemName_quantity_price.put(itemID, itemID_itemName_quantity_price.get(itemID).split(",")[0]+","+String.valueOf(itemQuantity)+","+itemID_itemName_quantity_price.get(itemID).split(",")[2]);
                        logger.info("Item remove succeed.");
                    }
                }
                return "remove completely";
            }
        }
    }

    @Override
    public String listItemAvailability(String managerID) {
        StringBuilder list = new StringBuilder();
        for (String key : itemID_itemName_quantity_price.keySet()){
            String[] data = itemID_itemName_quantity_price.get(key).split(",");
            list.append("itemID: ").append(key).append(" itemName: ").append(data[0]).append(" quantity: ").append(data[1]).append(" price: ").append(data[2]).append("\n\r");
        }
        logger.info("Get item list");
        logger.info(list.toString());
        return "\n\r"+ list.toString();
    }

    @Override
    public String purchaseItem(String customerID, String itemID, String dateOfPurchase) {
        //-----------------------check user whether in custom_Budget-----------------------
        itemID = itemID.toLowerCase();
        synchronized (this){
            custom_Budget.putIfAbsent(customerID, "1000");
            String loc = checkLocation(customerID);
            if (loc.equals(itemID.substring(0, 2).toUpperCase())){
                // local store item
                if (itemID_itemName_quantity_price.get(itemID) == null){
                    // no this item
                    if (waitingList.get(itemID) == null){
                        waitingList.put(itemID, customerID);
                        logger.info("The Store have no this item, put you on waiting list.");
                        return "The Store have no this item, put you on waiting list.";
                    } else {
                        String[] wl = waitingList.get(itemID).split(",");
                        for (int x = 0; x < wl.length; x++){
                            if (wl[x].trim().toUpperCase().equals(customerID)){
                                logger.info("Item out of stock, you already on waiting list.");
                                return "Item out of stock, you already on waiting list.";
                            }
                        }
                        waitingList.put(itemID, waitingList.get(itemID)+","+customerID);
                        return "Item out of stock, put you on waiting list.";
                    }
                } else if (Integer.parseInt(itemID_itemName_quantity_price.get(itemID).split(",")[1])<1){
                    // has this item, but 0 in stock.
                    if (checkBudget(customerID, itemID)){
                        // enough budget
                        if (waitingList.get(itemID) == null){
                            waitingList.put(itemID, customerID);
                            return "Item out of stock, put you on waiting list.";
                        } else {
                            String[] wl = waitingList.get(itemID).split(",");
                            for (int x = 0; x < wl.length; x++){
                                if (wl[x].trim().toUpperCase().equals(customerID)){
                                    logger.info("Item out of stock, you already on waiting list.");
                                    return "Item out of stock, you already on waiting list.";
                                }
                            }
                            waitingList.put(itemID, waitingList.get(itemID)+","+customerID);
                            return "Item out of stock, put you on waiting list.";
                        }
                    } else {
                        // not enough budget
                        return "You don't have enough budget";
                    }
                } else {
                    // has this item.
                    if (checkBudget(customerID, itemID)){
                        // enough budget
                        // remove 1 item from itemID_itemName_quantity_price
                        int itemQuantity = Integer.parseInt(itemID_itemName_quantity_price.get(itemID).split(",")[1]);
                        itemQuantity = itemQuantity - 1;
                        itemID_itemName_quantity_price.put(itemID, itemID_itemName_quantity_price.get(itemID).split(",")[0]+","+String.valueOf(itemQuantity)+","+itemID_itemName_quantity_price.get(itemID).split(",")[2]);
                        // add purchase record to purchaseDateCustomerID_item
                        if (CustomerID_itemDate.get(customerID) != null){
                            CustomerID_itemDate.put(customerID, CustomerID_itemDate.get(customerID)+","+itemID+":"+dateOfPurchase);
                        } else {
                            CustomerID_itemDate.put(customerID, itemID+":"+dateOfPurchase);
                        }
                        // update budget
                        custom_Budget.put(customerID, String.valueOf(Double.parseDouble(custom_Budget.get(customerID))-Double.parseDouble(itemID_itemName_quantity_price.get(itemID).split(",")[2])));
                        return "Purchase succeed.";
                    } else {
                        // not enough budget
                        return "You don't have enough budget";
                    }
                }
            } else {
                // other store
                int ServerPort = 300;
                if (itemID.substring(0, 2).toUpperCase().equals("QC")){
                    ServerPort = 300;
                } else if (itemID.substring(0, 2).toUpperCase().equals("ON")) {
                    ServerPort = 400;
                } else {
                    ServerPort = 500;
                }
                DatagramSocket aSocket = null;
                try {
                    System.out.println("Start UDP client.");
                    aSocket = new DatagramSocket(); //reference of the original socket

                    String msg = "purchaseitem," + customerID + "," + itemID + "," + dateOfPurchase + "," + custom_Budget.get(customerID);

                    byte [] message = msg.getBytes();
                    InetAddress aHost = InetAddress.getByName("localhost");
                    DatagramPacket request = new DatagramPacket(message, msg.length(), aHost, ServerPort);//request packet ready
                    aSocket.send(request);//request sent out
                    System.out.println("Request message sent from the Server" + " is : "+ new String(request.getData()));

                    byte [] buffer = new byte[1000];//to store the received data, it will be populated by what receive method returns
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.

                    //Client waits until the reply is received-----------------------------------------------------------------------
                    aSocket.receive(reply);//reply received and will populate reply packet now.
                    String replymsg = new String(reply.getData());
                    System.out.println("Reply received from the server is: "+ replymsg);//print reply message after converting it to a string from bytes
                    if (replymsg.substring(0, 2).equals("OK")){
                        // add purchase record to purchaseDateCustomerID_item
                        if (CustomerID_itemDate.get(customerID) != null){
                            CustomerID_itemDate.put(customerID, CustomerID_itemDate.get(customerID)+","+itemID+":"+dateOfPurchase);
                        } else {
                            CustomerID_itemDate.put(customerID, itemID+":"+dateOfPurchase);
                        }
                        // update budget
                        String[] rm = replymsg.split(",");
                        custom_Budget.put(customerID, String.valueOf(Double.parseDouble(rm[1])));
                        System.out.println("Budget:" + custom_Budget.get(customerID));
                        return "Purchase succeed from " + itemID.substring(0, 2) + " store";
                    } else if (replymsg.trim().equals("WAITLIST")) {
                        return "Item out of stock at " + itemID.substring(0, 2) + " store, put you on waiting list.";
                    } else {
                        return replymsg;
                    }
                }catch(SocketException e){
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
        return "2";
    }

    @Override
    public String findItem(String customerID, String itemName) {
        customerID = customerID.toUpperCase();
        String loc = checkLocation(customerID);
        // check local
        StringBuilder rmsg = new StringBuilder();
        for (String key : itemID_itemName_quantity_price.keySet()){
            String[] data = itemID_itemName_quantity_price.get(key).split(",");
            if (data[0].trim().toLowerCase().equals(itemName.trim().toLowerCase())){
                rmsg.append(loc).append(" Store: ").append("itemID: ").append(key).append(" itemName: ").append(data[0]).append(" quantity: ").append(data[1]).append(" price: ").append(data[2]).append("\n\r");
            }
        }
        int ServerPort1 = 300;
        int ServerPort2 = 400;
        if (loc.equals("QC")){
            ServerPort1 = 400;
            ServerPort2 = 500;
        } else if (loc.equals("ON")){
            ServerPort1 = 300;
            ServerPort2 = 500;
        }
        // check other store
        DatagramSocket aSocket = null;
		try{
			System.out.println("Start UDP client.");
			aSocket = new DatagramSocket(); //reference of the original socket
            String msg = "findItem,"+itemName;
			byte [] message = msg.getBytes();

			InetAddress aHost = InetAddress.getByName("localhost");

			DatagramPacket request = new DatagramPacket(message, msg.length(), aHost, ServerPort1);//request packet ready
			aSocket.send(request);//request sent out
			System.out.println("Request message sent from the Server" + " is : "+ new String(request.getData()));

			byte [] buffer = new byte[1000];//to store the received data, it will be populated by what receive method returns
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.

			//Client waits until the reply is received-----------------------------------------------------------------------
			aSocket.receive(reply);//reply received and will populate reply packet now.
            String replymsg = new String(reply.getData());
            rmsg.append(replymsg);
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
        // check another store
        aSocket = null;
		try{
			System.out.println("Start UDP client.");
			aSocket = new DatagramSocket(); //reference of the original socket
            String msg = "findItem,"+itemName;
			byte [] message = msg.getBytes();

			InetAddress aHost = InetAddress.getByName("localhost");

			DatagramPacket request = new DatagramPacket(message, msg.length(), aHost, ServerPort2);//request packet ready
			aSocket.send(request);//request sent out
			System.out.println("Request message sent from the Server" + " is : "+ new String(request.getData()));

			byte [] buffer = new byte[1000];//to store the received data, it will be populated by what receive method returns
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.

			//Client waits until the reply is received-----------------------------------------------------------------------
			aSocket.receive(reply);//reply received and will populate reply packet now.
            String replymsg = new String(reply.getData());
            rmsg.append(replymsg);
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
        return rmsg.toString();
    }

    @Override
    public String returnItem(String customerID, String itemID, String dateOfReturn) {
        return "6";
    }

    @Override
    public String exchangeItem(String customerID, String newItemID, String oldItemID) {
        return "7";
    }

    private void giveItemToCustumOnWaitingList(String itemID, Double price){
        if (waitingList.get(itemID) == null){
            // no one on waiting list
        } else {
            // someone on waiting list
            String[] wc = waitingList.get(itemID).split(",");
            StringBuilder nwc = new StringBuilder();
            for (int x = 0; x < wc.length; x++){
                if (Integer.parseInt(itemID_itemName_quantity_price.get(itemID).split(",")[1]) == 0){
                    // if item is 0
                    for (int i = x; i < wc.length; i++){
                        nwc.append(",").append(wc[i].trim());
                    }
                    break;
                }
                String CID = wc[x].trim();
                // check customer budget
                if (custom_Budget.get(CID) != null){
                    // local customer
                    if (price <= Double.parseDouble(custom_Budget.get(CID))) {
                        //enough budget
                        int itemQuantity = Integer.parseInt(itemID_itemName_quantity_price.get(itemID).split(",")[1]);
                        itemQuantity = itemQuantity - 1;
                        itemID_itemName_quantity_price.put(itemID, itemID_itemName_quantity_price.get(itemID).split(",")[0]+","+String.valueOf(itemQuantity)+","+itemID_itemName_quantity_price.get(itemID).split(",")[2]);
                        // add purchase record to purchaseDateCustomerID_item
                        CustomerID_itemDate.put(CID, itemID+":"+"20102020");
                        // update budget
                        custom_Budget.put(CID, String.valueOf(Double.parseDouble(custom_Budget.get(CID))-price));
                        System.out.println("Give Item to customer: "+ CID +" on waiting list.");
                    } else {
                        // not enough budget
                        nwc.append(",").append(wc[x].trim());
                        System.out.println("Customer: " + CID + " has no enough budget.");
                    }
                } else {
                    // other store customer
                    if (CustomerID_itemDate.get(CID) != null){
                        // if customer already bought item from this store.
                        nwc.append(",").append(wc[x].trim());
                        System.out.println("You have already bought item from this store.");
                    } else {
                        // get budget
                        double CB = getBudget(CID);
                        if (CB >= price){
                            // enough budget
                            int itemQuantity = Integer.parseInt(itemID_itemName_quantity_price.get(itemID).split(",")[1]);
                            itemQuantity = itemQuantity - 1;
                            itemID_itemName_quantity_price.put(itemID, itemID_itemName_quantity_price.get(itemID).split(",")[0]+","+String.valueOf(itemQuantity)+","+itemID_itemName_quantity_price.get(itemID).split(",")[2]);
                            // add purchase record to purchaseDateCustomerID_item
                            CustomerID_itemDate.put(CID, itemID+":"+"20102020");
                            // update budget
                            SUpdateBudget(CID, String.valueOf(CB-price));
                            System.out.println("Give Item to customer: "+ CID +" on waiting list.");
                        } else {
                            // not enough budget
                            nwc.append(",").append(wc[x].trim());
                            System.out.println("Customer: " + CID + " has no enough budget.");
                        }
                    }
                }
            }
            if (nwc.length()>0){
                waitingList.put(itemID, nwc.deleteCharAt(0).toString());
            } else {
                waitingList.remove(itemID);
            }

        }
    }

    private double getBudget(String cid) {
        double b = 0;
        cid = cid.toUpperCase();
        DatagramSocket aSocket = null;
		try{
			System.out.println("Start UDP client.");
			aSocket = new DatagramSocket(); //reference of the original socket
            String msg = "CheckBudget," + cid;
			byte [] message = msg.getBytes();

			InetAddress aHost = InetAddress.getByName("localhost");
			int serverPort1 = 500;
			if (cid.substring(0, 2).equals("QC")){
			    serverPort1 = 300;
            } else if (cid.substring(0, 2).equals("ON")){
			    serverPort1 = 400;
            } else {
			    serverPort1 = 500;
            }

			DatagramPacket request = new DatagramPacket(message, msg.length(), aHost, serverPort1);//request packet ready
			aSocket.send(request);//request sent out
			System.out.println("Request message sent from the Server" + " is : "+ new String(request.getData()));

			byte [] buffer = new byte[1000];//to store the received data, it will be populated by what receive method returns
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.

			//Client waits until the reply is received-----------------------------------------------------------------------
			aSocket.receive(reply);//reply received and will populate reply packet now.
            String replymsg = new String(reply.getData());
			System.out.println("Reply received from the server is: "+ replymsg);//print reply message after converting it to a string from bytes
            b = Double.parseDouble(replymsg.trim());
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
        return b;
    }

    private void SUpdateBudget(String CustomerID, String Budget){
        DatagramSocket aSocket = null;
		try{
			System.out.println("Start UDP client.");
			aSocket = new DatagramSocket(); //reference of the original socket
            String msg = "updateBudget," + CustomerID + "," + Budget;
			byte [] message = msg.getBytes();

			InetAddress aHost = InetAddress.getByName("localhost");
			int serverPort1 = 500;
			if (CustomerID.substring(0, 2).equals("QC")){
			    serverPort1 = 300;
            } else if (CustomerID.substring(0, 2).equals("ON")){
			    serverPort1 = 400;
            } else {
			    serverPort1 = 500;
            }

			DatagramPacket request = new DatagramPacket(message, msg.length(), aHost, serverPort1);//request packet ready
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

    private boolean checkBudget(String CustomerID, String ItemID){
        Double CB = Double.parseDouble(custom_Budget.get(CustomerID));
        Double PI = 0.0;
        PI = Double.parseDouble(itemID_itemName_quantity_price.get(ItemID).split(",")[2]);
        if (PI > CB){
            return false;
        } else {
            return true;
        }
    }

    private String checkLocation(String ID){
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

    public String purchaseItem(String customerID, String itemID, String dateOfPurchase, String Budget){
        itemID = itemID.toLowerCase();
        if (CustomerID_itemDate.get(customerID) != null){
                // if customer already bought item from this store.
                return "You have already bought item from this store.";
        }
        if (itemID_itemName_quantity_price.get(itemID) == null){
            // no this item
            if (waitingList.get(itemID) == null){
                waitingList.put(itemID, customerID);
                System.out.println("Item out of stock, put you on waiting list.");
                logger.info("Item out of stock, put you on waiting list.");
                return "WAITLIST";
            } else {
                String[] wl = waitingList.get(itemID).split(",");
                for (int x = 0; x < wl.length; x++){
                    if (wl[x].trim().toUpperCase().equals(customerID)){
                        System.out.println("Item out of stock, you already on waiting list.");
                        logger.info("Item out of stock, you already on waiting list.");
                        return "WAITLIST";
                    }
                }
                waitingList.put(itemID, waitingList.get(itemID)+","+customerID);
                System.out.println("Item out of stock, put you on waiting list.");
                logger.info("Item out of stock, put you on waiting list.");
                return "WAITLIST";
            }
        } else if (Integer.parseInt(itemID_itemName_quantity_price.get(itemID).split(",")[1])<1){
            // has this item, but 0 in stock.
            if (Double.parseDouble(itemID_itemName_quantity_price.get(itemID).split(",")[2])< Double.parseDouble(Budget)){
                // enough budget
                if (waitingList.get(itemID) == null){
                    waitingList.put(itemID, customerID);
                    System.out.println("Item out of stock, put you on waiting list.");
                    return "WAITLIST";
                } else {
                    String[] wl = waitingList.get(itemID).split(",");
                    for (int x = 0; x < wl.length; x++){
                        if (wl[x].trim().toUpperCase().equals(customerID)){
                            System.out.println("Item out of stock, you already on waiting list.");
                            logger.info("Item out of stock, you already on waiting list.");
                            return "WAITLIST";
                        }
                    }
                    waitingList.put(itemID, waitingList.get(itemID)+","+customerID);
                    System.out.println("Item out of stock, put you on waiting list.");
                    logger.info("Item out of stock, put you on waiting list.");
                    return "WAITLIST";
                }
            } else {
                // not enough budget
                return "You don't have enough budget";
            }
        } else {
            // has this item.
            if (Double.parseDouble(itemID_itemName_quantity_price.get(itemID).split(",")[2])< Double.parseDouble(Budget)){
                // enough budget
                // remove 1 item from itemID_itemName_quantity_price
                int itemQuantity = Integer.parseInt(itemID_itemName_quantity_price.get(itemID).split(",")[1]);
                itemQuantity = itemQuantity - 1;
                itemID_itemName_quantity_price.put(itemID, itemID_itemName_quantity_price.get(itemID).split(",")[0]+","+String.valueOf(itemQuantity)+","+itemID_itemName_quantity_price.get(itemID).split(",")[2]);
                // add purchase record to purchaseDateCustomerID_item
                CustomerID_itemDate.put(customerID, itemID+":"+dateOfPurchase);
                // update budget
                System.out.println((Double.parseDouble(Budget)-Double.parseDouble(itemID_itemName_quantity_price.get(itemID).split(",")[2])));
                return "OK,"+(Double.parseDouble(Budget)-Double.parseDouble(itemID_itemName_quantity_price.get(itemID).split(",")[2]));
            } else {
                // not enough budget
                return "You don't have enough budget";
            }
        }
    }

    public String checkBudget(String CustomerID){
        return custom_Budget.get(CustomerID);
    }

    public void updateBudget(String CustomerID, String Budget){
        custom_Budget.put(CustomerID, Budget);
        System.out.println("new budget: "+ Budget);

    }

    public String findItem(String itemName, String location, boolean loc){
        StringBuilder rmsg = new StringBuilder();
        for (String key : itemID_itemName_quantity_price.keySet()){
            String[] data = itemID_itemName_quantity_price.get(key).split(",");
            if (data[0].trim().toLowerCase().equals(itemName.trim().toLowerCase())){
                rmsg.append(location).append(" Store: ").append("itemID: ").append(key).append(" itemName: ").append(data[0]).append(" quantity: ").append(data[1]).append(" price: ").append(data[2]).append("\n\r");
            }
        }
        return rmsg.toString();
    }
}
