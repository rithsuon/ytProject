
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.*;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;

import java.io.*;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class YTViewsMatcher {
    private static final String CLIENT_SECRETS= "C:\\Users\\Rith\\Documents\\GitHub\\YTTest\\src\\main\\resources\\client_secret.json";
    private static final Collection<String> SCOPES =
            Arrays.asList("https://www.googleapis.com/auth/youtube.force-ssl");//Arrays.asList("https://www.googleapis.com/auth/youtube.readonly", "https://www.googleapis.com/auth/youtube.force-ssl", "https://www.googleapis.com/auth/youtube");

    private static final String APPLICATION_NAME = "API code samples";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        InputStream in = new FileInputStream(CLIENT_SECRETS);//YTViewsMatcher.class.getResourceAsStream(CLIENT_SECRETS);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                        .build();
        Credential credential =
                new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     */
    public static void main(String[] args)
            throws GeneralSecurityException, IOException, GoogleJsonResponseException {
        YouTube youtubeService = getService();
        // get the youtube channel and get uploaded videos
        //YouTube.Channels.List channel = youtubeService.channels().list("contentDetails");
        //ChannelListResponse response = channel.setMine(true).execute();
        //String playlistId = find(response.toString(), "uploads");
        //System.out.println("ID: " + playlistId);
        //System.out.println(response.toPrettyString());


        Video vid = new Video();
        vid.setId("jZ11KrNeXQM");
        VideoSnippet snippet = new VideoSnippet();
        snippet.setDescription("1234");
        snippet.setCategoryId("22");
        // select the right video from the uploads playlist
        //YouTube.PlaylistItems.List playlist =  youtubeService.playlistItems().list("contentDetails,status");
        //PlaylistItemListResponse uploaded = playlist.setPlaylistId(playlistId).execute();
        String currViews = null;
        int i = 0;
        YouTube.Videos.List videos = youtubeService.videos().list("statistics");
        videos.setId("jZ11KrNeXQM");
        while(i <= 1000) {
            VideoListResponse videoListResponse = videos.execute();
            System.out.println(videoListResponse.toPrettyString());
            //System.out.println(videoListResponse.toPrettyString());
            String viewCount = find(videoListResponse.toString(), "viewCount");
            System.out.println("\n VIEWCOUNT: " + viewCount);
            if(currViews == null) {
                /*videos = youtubeService.videos().list("snippet");
                videoListResponse = videos.execute();
                System.out.println(videoListResponse.toPrettyString());
                String title = find(videoListResponse.toString(), "title");*/
                currViews = viewCount;
            }

            if(!currViews.equals(viewCount)) {
                snippet.setTitle("This video has " + viewCount + " views.");
                vid.setSnippet(snippet);
                YouTube.Videos.Update req = youtubeService.videos().update("snippet", vid);
                Video vidResp = req.execute();
                System.out.println(vidResp.toString());
                currViews = viewCount;
            } else {
                System.out.println("View count has not changed. (" + i + ")");
            }

            try {
                System.out.println("sleeping for 20s (" + i + ")");
                TimeUnit.SECONDS.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
            }

            i++;
        }


    }

    private static String find(String json, String key) {
        String id = "NULL";
        try {
            JsonParser parser = JSON_FACTORY.createJsonParser(json);
            parser.nextToken();
            while(parser.getCurrentToken() != JsonToken.END_OBJECT) {
                if(parser.getText().equalsIgnoreCase(key)) {
                    parser.nextToken();
                    id = parser.getText();
                } else {
                    parser.nextToken();
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        return id;
    }
}