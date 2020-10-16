import CORBA_A2.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class orb {
    static Functions FunctionImpl;
    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");

            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            String name = "BC_Server";
            FunctionImpl = (Functions) FunctionsHelper.narrow(ncRef.resolve_str(name));
            System.out.println("Obtained a handle on server object: " + FunctionImpl);
            System.out.println(FunctionImpl.addItem("1", "2", "3", "4", "5"));
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
