package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import play.libs.Json;
import util.StaticFunctions;

/**
 * Created by mahabaleshwar on 7/7/2016.
 */
public class GoogleTrend {
    /*
        String user = "manoj.mahabaleshwar@gmail.com";
        String password = "@Gmail5864";

        public ArrayNode getTrends(ArrayNode resources) {
            try {
                //HttpHost proxy = new HttpHost("proxy.mydomain.com", 8080, "http");
                //Credentials credentials = new NTCredentials("myLogin", "myPasswd", "", "DOMAIN");
                Credentials credentials = new UsernamePasswordCredentials(user, password);
                DefaultHttpClient httpClient = new DefaultHttpClient();
                httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
                //httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

                GoogleAuthenticator authenticator = new GoogleAuthenticator(user, password, httpClient);

                for (int i = 0; i < resources.size(); i++) {
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ObjectNode resource = (ObjectNode) resources.get(i);
                    System.out.println(".............................");
                    System.out.println(resource.get(StaticFunctions.TITLE).toString().trim());

                    GoogleTrendsRequest request = new GoogleTrendsRequest(resource.get(StaticFunctions.TITLE).toString().trim());

                    GoogleTrendsClient client = new GoogleTrendsClient(authenticator, httpClient);

                    String content = client.execute(request);
                    GoogleTrendsCsvParser csvParser = new GoogleTrendsCsvParser(content);

                    try {
                        List<String[]> section = csvParser.getSectionAsStringArrayList("Interest over time", false, ",");
                        resource.put(StaticFunctions.SCORE, computeAverageTrend(section));
                    } catch (NullPointerException e) {
                        resource.put(StaticFunctions.SCORE, 0);
                    }
                }
            } catch (GoogleTrendsRequestException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (GoogleTrendsClientException e) {
                e.printStackTrace();
            } catch (ConfigurationException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resources;
        }

        private double computeAverageTrend(List<String[]> sections) {
            int count = 0;
            double score = 0;
            for (int i = 1; i < sections.size(); i++) {
                String interval = sections.get(i)[0];
                try {
                    if (interval.split("-").length > 0 && Integer.parseInt(interval.split("-")[0]) > 2014) {
                        score += Integer.parseInt(sections.get(i)[1]);
                        count += 1;
                    }
                } catch (Exception e) {
                    System.out.println("Unable to parse string");
                }
            }
            if (count > 0) {
                return score / count;
            } else {
                return score;
            }
        }
    }

    */
    public ArrayNode getTrends(ArrayNode resources) {
        HttpClient client = new HttpClient();
        for (int i = 0; i < resources.size(); i++) {
            // try {
            //     Thread.sleep(1000);
            // } catch (InterruptedException e) {
            //     e.printStackTrace();
            // }
            ObjectNode resource = (ObjectNode) resources.get(i);
            System.out.println(".............................");
            System.out.println(resource.get(StaticFunctions.TITLE).toString().trim());
            String query = resource.get(StaticFunctions.TITLE).toString().trim().replaceAll("\"", "").replaceAll(" ", "_");
            String url = String.format("https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=%s&srlimit=1&format=json", query);
            try {
                HttpMethod method = new GetMethod(url);
                method.addRequestHeader("User-Agent", "Mozilla/5.0");
                // Execute the method.
                int statusCode = client.executeMethod(method);
                if (statusCode == HttpStatus.SC_OK) {
                    String response = IOUtils.toString(method.getResponseBodyAsStream(), "UTF-8");
                    double score = 0.0;
                    String description = "";
                    try{
                        JsonNode jsonObject = Json.parse(response);
                        score = Double.parseDouble(jsonObject.get("query").get("searchinfo").get("totalhits").asText());
                        description = jsonObject.get("query").get("search").get(0).get("snippet").asText();

                    } catch (Exception e) {}
                    System.out.println(score);
                    resource.put(StaticFunctions.SCORE, score);
                    resource.put(StaticFunctions.DESCRIPTION, description);
                }
            } catch (HttpException e) {
                System.err.println("Fatal protocol violation: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Fatal transport error: " + e.getMessage());
            }
        }
        return resources;
    }
}