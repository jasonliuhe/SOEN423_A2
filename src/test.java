import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class test {
    public static void main(String[] args) throws ParseException {
        DateFormat sourceFormat = new SimpleDateFormat("ddMMyyyy");
        String dateAsString = "25012010";
        String da = "24012010";
        Date date = sourceFormat.parse(dateAsString);
        Date date1 = sourceFormat.parse(da);
        long d = date1.getTime()-date.getTime();
        long diffDays = d / (24 * 60 * 60 * 1000);
        System.out.println(diffDays);
    }
}
