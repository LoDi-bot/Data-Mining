package ru.itis.app;

import com.jayway.jsonpath.JsonPath;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ru.itis.models.Video;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final String channelId = "UC7f5bVxWsm3jlZIPDzOMcAg";

    public static final String api_key = "AIzaSyDfaOZqbkh8mhdEIMC05eukGO0PlqWmCjI";

    public static final String baseSearchUrl = "https://www.googleapis.com/youtube/v3/search?";

    public static final String baseVideoInfoUrl = "https://www.googleapis.com/youtube/v3/videos?";

    public static final String baseVideoUrl = "https://www.youtube.com/watch?v=";

    public static List<Video> getAllVideoFromChannel(String channelId) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        String firstUrl = baseSearchUrl + "key=" + api_key + "&channelId=" + channelId + "&part=snippet,id&order=date&maxResults=25";
        String url = firstUrl;

        ArrayList<String> videoIds = new ArrayList<>();
        while (true) {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .header("accept", "application/json")
                    .uri(URI.create(url))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            videoIds.addAll(JsonPath.read(response.body(), "$.items[?(@.id.kind == 'youtube#video')].id.videoId"));

            try {
                String nextPageToken = JsonPath.read(response.body(), "$.nextPageToken");
                url = firstUrl + "&pageToken=" + nextPageToken;
            } catch (Exception e) {
                break;
            }
        }
        System.out.println(videoIds.size());
        ArrayList<Video> videos = new ArrayList<>();
        for (String videoId : videoIds) {
            videos.add(getInfoAboutVideo(videoId, client));
        }
        return videos;
    }

    public static Video getInfoAboutVideo(String videoId, HttpClient client) throws IOException, InterruptedException {
        String infoUrl = baseVideoInfoUrl + "part=snippet,statistics&key=" + api_key + "&id=" + videoId;

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .header("accept", "application/json")
                .uri(URI.create(infoUrl))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return Video.builder()
                .url(baseVideoUrl + JsonPath.read(response.body(), "$.items[0].id"))
                .watchId(videoId)
                .title(JsonPath.read(response.body(), "$.items[0].snippet.title"))
                .publishedAt(JsonPath.read(response.body(), "$.items[0].snippet.publishedAt"))
                .description(JsonPath.read(response.body(), "$.items[0].snippet.description"))
                .viewsCounter(Long.valueOf(JsonPath.read(response.body(), "$.items[0].statistics.viewCount")))
                .likesCounter(Long.valueOf(JsonPath.read(response.body(), "$.items[0].statistics.likeCount")))
                .build();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Configuration hibernateConfiguration = new Configuration().configure("hibernate/hibernate.cfg.xml");
        SessionFactory sessionFactory = hibernateConfiguration.buildSessionFactory();
        Session session = sessionFactory.openSession();

//        for (Video video : getAllVideoFromChannel(channelId)) {
//            session.save(video);
//        }
//        System.out.println(getAllVideoFromChannel(channelId));
    }
}
