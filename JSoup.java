import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.Connection.Response;
import java.util.Arrays;

// https://stackoverflow.com/questions/12465586/how-can-i-download-an-image-using-jsoup

public class JSoup {
    public static void main(String[] args) throws java.io.IOException {
        Response response = Jsoup.connect("http://example.com/").execute();
        System.out.print(new String(response.bodyAsBytes()));

        Document doc = response.parse();
        System.out.println (doc.title());
    }
}
