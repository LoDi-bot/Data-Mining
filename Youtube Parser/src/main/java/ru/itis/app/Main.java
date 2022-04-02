package ru.itis.app;

import com.jayway.jsonpath.JsonPath;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ru.itis.models.Link;
import ru.itis.models.Video;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static final String channelId = "UC7f5bVxWsm3jlZIPDzOMcAg";

    public static final String api_key = "";

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

    public static ArrayList<Link> getAllLinksFromDescription(Video video) {
        String description = video.getDescription();

        ArrayList<String> urls = extractUrls(description);

        ArrayList<Link> links = new ArrayList<>();
        for (String url : urls) {
            Link link = Link.builder()
                    .video(video)
                    .value(url)
                    .representer(url)
                    .build();
            links.add(link);
        }

        return links;
    }

    public static ArrayList<String> extractUrls(String text) {
        ArrayList<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }

    public static void filterRepresenters(ArrayList<Link> links) {
        for (Link link : links) {
            HttpClient client = HttpClient.newHttpClient();
            switch (link.getValue()) {
                case "https://t.me/howdyho_official" -> {
                    link.setRepresenter("Telegram-канал HowdyHo");
                }
                case "https://www.vk.com/howdyho_net" -> {
                    link.setRepresenter("VK-группа HowdyHo");
                }
                case "https://vk.com/topic-84392011_33285530" -> {
                    link.setRepresenter("Сотрудничество через VK");
                }
                case "http://vk.cc/5lPADD" -> {
                    link.setRepresenter("Второй игровой канал");
                }
                case "https://www.instagram.com/abrahamtugalov/" -> {
                    link.setRepresenter("Instagram HowdyHo");
                }
                case "https://t.me/howdyho" -> {
                    link.setRepresenter("Старый Telegram-канал");
                }
                default -> {
                    if (link.getValue().matches("((https?|ftp):((//)|(\\\\))+(games.)?howdyho.net[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)")) {
                        link.setRepresenter("Личный сайт-блог HowdyHo");
                    } else {
                        HttpRequest request = HttpRequest.newBuilder()
                                .GET()
                                .uri(URI.create(link.getValue()))
                                .build();

                        try {
                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                            while (response.statusCode() == 300 || response.statusCode() == 301 || response.statusCode() == 302 || response.statusCode() == 303 ) {
                                request = HttpRequest.newBuilder()
                                        .GET()
                                        .uri(URI.create(response.headers().firstValue("location").get()))
                                        .build();
                                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                            }
                            if (response.statusCode() == 200) {
                                link.setRepresenter(response.uri().getHost());
                            }
                        } catch (IOException | InterruptedException | IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Configuration hibernateConfiguration = new Configuration().configure("hibernate/hibernate.cfg.xml");
        SessionFactory sessionFactory = hibernateConfiguration.buildSessionFactory();
        Session session = sessionFactory.openSession();

        for (Video video : getAllVideoFromChannel(channelId)) {
            session.save(video);

            ArrayList<Link> links = getAllLinksFromDescription(video);
            filterRepresenters(links);

            for (Link link : links) {
                session.save(link);
            }
        }


        session.close();
        sessionFactory.close();
    }
}
