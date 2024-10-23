package com.example.plugins.tutorial.confluence.simplebp;
 
import com.atlassian.confluence.event.events.content.comment.CommentEvent;
import com.atlassian.confluence.event.events.content.comment.CommentCreateEvent;
import com.atlassian.confluence.pages.Comment;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.ModuleCompleteKey;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import com.atlassian.confluence.api.model.content.id.ContentId;
import com.atlassian.confluence.user.ConfluenceUser;
 
import javafx.beans.value.ObservableObjectValue;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
 
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.text.Document;
import javax.net.ssl.HttpsURLConnection;
 
import java.net.*;
import java.io.*;
import java.util.EventObject;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.LinkedList;
import org.apache.commons.lang.StringEscapeUtils;
 
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
 
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
 
@Named
public class MyCommentListener implements InitializingBean, DisposableBean {
 
    static String message = "";
    static String author = "";
    static String link = "";
    static String time = "";
    static String plainText = "";
    static String username = "";
    static String url = "";
    static String url2 = "";
    static String title = "";
    static String fix_title = "";
    static List<String> usernamelist = new ArrayList();
 
 
    public static String sendPostRequest(String requestUrl, String payload) {
    try {
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        writer.write(payload);
        writer.close();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuffer jsonString = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
                jsonString.append(line);
        }
        br.close();
        connection.disconnect();
        return jsonString.toString();
    } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
    }
 
}
 
    public static boolean checkGroup(String requestUrl) {
        try {
            URL url = new URL(requestUrl);
            //local
            //HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            //dev
            //HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            //real
            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json; charset=UTF-8");
            //local
            //conn.setRequestProperty("Authorization", "Basic $$YOUR TOKEN$$");
            //dev
            //conn.setRequestProperty("Authorization", "Bearer $$YOUR TOKEN$$");
            //real
            conn.setRequestProperty("Authorization", "Bearer $$YOUR TOKEN$$");
            conn.setDoInput(true);
            BufferedReader br;
 
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <=300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                System.out.println("성공함" + conn.getResponseCode());
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                System.out.println("실패함" + conn.getResponseCode());
            }
 
            StringBuffer jsonString = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                    jsonString.append(line);
            }
            br.close();
            conn.disconnect();
 
            String jsonString2 = jsonString.toString();
            String expectedgroup = "$GROUP THAT USES OTHER SLACK WORKSPACE$";
 
            if(jsonString2.contains(expectedgroup)) {
                return true;
            } else {
                return false;
            }
 
        } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
        }
    }
 
    public static void sendGetRequest(String requestUrl) {
        try{
            URL url = new URL(requestUrl);
            //local
            //HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            //dev
            //HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            //real
            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json; charset=UTF-8");
            //local
            //conn.setRequestProperty("Authorization", "$$YOUR TOKEN$$");
            //dev
            //conn.setRequestProperty("Authorization", "$$YOUR TOKEN$$");
            //real
            conn.setRequestProperty("Authorization", "$$YOUR TOKEN$$");
 
            conn.setDoInput(true);
            BufferedReader br;
 
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <=300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                System.out.println("성공함" + conn.getResponseCode());
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                System.out.println("실패함" + conn.getResponseCode());
            }
 
            StringBuffer jsonString = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                    jsonString.append(line);
            }
            br.close();
            conn.disconnect();
 
            String parsestring = jsonString.toString();
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(parsestring);
 
            JSONObject jsonObj = (JSONObject) obj;
            JSONObject jsonObj2 = (JSONObject) jsonObj.get("body");
            JSONObject jsonObj3 = (JSONObject) jsonObj2.get("view");
            JSONObject jsonObj4 = (JSONObject) jsonObj.get("history");
            JSONObject jsonObj5 = (JSONObject) jsonObj4.get("createdBy");
            JSONObject jsonObj6 = (JSONObject) jsonObj.get("_links");
 
            title = (String) jsonObj.get("title");
            fix_title = title.replaceFirst("^Re: ", "");
            message = (String) jsonObj3.get("value");
            Pattern pattern = Pattern.compile("(\\bdata-username\\b)(.*?)(\\bhref\\b)");
            Matcher matcher = pattern.matcher(message);
                     
            while (matcher.find()) {  
                usernamelist.add(matcher.group(2).replace("\"", "").replace("=","").replace(" ",""));
                System.out.println(usernamelist.toString());
                                     
                if(matcher.group(2) ==  null)
                    break;
            }
             
            // Remove all HTML tags and backslashses except <p>, <ul>, <li>, <ol>, and <br> tags
            message = message.replaceAll("(?i)<(?!p|ul|li|ol|br)[^>]*>|\\\\", "")
            // Replace <br> tags with line breaks (\n)
            .replaceAll("(?i)<br[^>]*>", "\n")
            // Replace <p>, <ul>, <li>, and <ol> tags with double line breaks (\n\n)
            .replaceAll("(?i)</?(p|ul|li|ol)[^>]*>", "\n\n")
            // Remove any remaining HTML tags
            .replaceAll("<[^>]*>", "")
            // Trim any leading or trailing white spaces
            .trim();
 
            // Replace line breaks represented by other HTML tags with "\n"
            message = message.replaceAll("(?i)(?<!\\n)\\s*<[/]?(div|span|tr|td|table|tbody)[^>]*>\\s*(?!\\n)", "\n");
 
            // Replace consecutive line breaks with a single line break
            message = message.replaceAll("\n{2,}", "\n");
 
            // Replace any remaining "\r" with "\n"
            message = message.replace("\r", "\n");
 
            // Replace "\n" with "\\n"
            message = message.replace("\n", "\\n");
 
 
            author = (String) jsonObj5.get("username");
            time = (String) jsonObj4.get("createdDate");
            // local
            //link = "http://localhost:1990/confluence" + (String) jsonObj6.get("webui");
            // dev
            // link = "https://$YOUR CONFLUENCE URL$" + (String) jsonObj6.get("webui");
            // real
            link = "https://$YOUR CONFLUENCE URL$" + (String) jsonObj6.get("webui");
 
 
            System.out.println("message: " + message);
            System.out.println("author: " + author);
            System.out.println("link: " + link);
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
}
 
    @ConfluenceImport
    private EventPublisher eventPublisher;
 
    @Inject
    public MyCommentListener(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
 
    @EventListener
    public void onCommentCreate(CommentCreateEvent event) {
        if (event.getContent() != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // handle exception
                    }
                    Long commentid = event.getComment().getContentId().asLong();
                    // local
                    //url = "http://localhost:1990/confluence/rest/api/content/" + commentid.toString() + "?expand=body.view,history.username";
                    // dev
                    //url = "https://$YOUR CONFLUENCE URL$/rest/api/content/" + commentid.toString() + "?expand=body.view,history.username";
                    // real
                    url = "https://$YOUR CONFLUENCE URL$/rest/api/content/" + commentid.toString() + "?expand=body.view,history.username";
                    afterCommentCreate();
                }
             }).start();
        }
    }
 
    public void afterCommentCreate(){
        sendGetRequest(url);
        while (!usernamelist.isEmpty()) {
            username = usernamelist.get(0);
 
            //dev
            // url2 = "https://$YOUR CONFLUENCE URL$/rest/api/user/memberof?username=" + username;
            // real
            url2 = "https://$YOUR CONFLUENCE URL$/rest/api/user/memberof?username=" + username;
 
 
            if (checkGroup(url2) == true) {
                String payload="{\"channel\":\"@" + username + "\",\"message\":" + "\"" + "@" + author + " mentioned you on - " + fix_title + "\\n" + link + "\\n" + message + "\"}";
                String requestUrl="$SLACK API FOR SENDING MESSAGE$";
                usernamelist.remove(0);
                sendPostRequest(requestUrl, payload);
            } else {
                String payload="{\"channel\":\"@" + username + "\",\"message\":" + "\"" + "@" + author + " mentioned you on - " + fix_title + "\\n" + link + "\\n" + message + "\"}";
                String requestUrl="$SLACK API FOR SENDING MESSAGE$";
                usernamelist.remove(0);
                sendPostRequest(requestUrl, payload);
            }
        }
    }
 
    @Override
    public void afterPropertiesSet() throws Exception {
        eventPublisher.register(this);
    }
 
    @Override
    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }
 
}
