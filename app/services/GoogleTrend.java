package services;

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
}