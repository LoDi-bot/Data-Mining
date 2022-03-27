import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

public class Crawler {
    public static void main(String[] args) {
        String landingUrl = "https://ru.wikipedia.org/wiki/Заглавная_страница";
        crawl(landingUrl, 1, new ArrayList<>());

//        Graph graph = new SingleGraph();
//        graph.c
    }

    public static void crawl(String url, int level, ArrayList<String> visitedPages) {
        if (level <= 3) {
            Document document = request(url, visitedPages);

            if (document != null) {
                for (Element link : document.select("a[href]")) {
                    String nextLink = link.absUrl("href");
                    if (!visitedPages.contains(nextLink)) {
                        crawl(nextLink, level++, visitedPages);
                    }
                }
            }
        }
    }

    public static Document request(String url, ArrayList<String> visitedPages) {
        try {
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();

            if (connection.response().statusCode() == 200) {
                System.out.println("Link: " + url);
                System.out.println(document.title());
                visitedPages.add(url);
                return document;
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
