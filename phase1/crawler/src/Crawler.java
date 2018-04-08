import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URLEncoder;

public class Crawler {
    private static final int DELAY = 7;
    private static final int PAGES = 20;
    private static final String[] categories = { "machine learning", "databases", "data mining", "artificial intelligence" };

    public static void main(String[] args) {
        for (String category : categories) {
            File file = new File("wikicfp_crawl-" + category.replaceAll(" ", "_") + ".txt");
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                scrapeCategory(writer, category);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void scrapeCategory(FileWriter writer, String category) throws IOException {
        for(int i = 1;i <= PAGES; i++) {
            //Create the initial request to read the first page
            //and get the number of total results
            String linkToScrape = "http://www.wikicfp.com/cfp/call?conference="+
                    URLEncoder.encode(category, "UTF-8") +"&page=" + i;
            Document doc = Jsoup.connect(linkToScrape).get();
            Elements rows = doc.select("div.contsec table tr:nth-child(3) table tbody tr");

            boolean firstRow = true;
            for (Element row : rows.next()) {
                // Skip header rows
                if(row.attr("align").equals("center")) {
                    System.out.println("Skipping row!");
                    continue;
                }
                if (firstRow) {
                    String acronym = row.select("td > a").text();
                    String name = row.select("td[colspan=3]").text();
                    System.out.println("Acronym: " + acronym);
                    System.out.println("Name: " + name);
                    writer.write(acronym + "\t" + name + "\t");
                } else {
                    String location = row.select("td:nth-child(2)").text();
                    System.out.println("Location: " + location);
                    writer.write(location + "\n");
                }
                firstRow = !firstRow;
            }

            try {
                Thread.sleep(DELAY*1000); //rate-limit the queries
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
