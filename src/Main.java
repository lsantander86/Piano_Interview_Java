import java.io.*;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

public class Main {

    public static final String BASE_URL = "https://sandbox.piano.io/id/api/v1/publisher/users/get";
    public static final String API_TOKEN = "xeYjNEhmutkgkqCZyhBn6DErVntAKDx30FqFOS6D";
    public static final String APP_ID = "o1sRRZSLlw";

    public static String getUserId(String email, String userId) {
        // Define API endpoint and parameters
        JSONObject params = new JSONObject();
        params.put("aid", APP_ID);
        params.put("api_token", API_TOKEN);
        params.put("email", email);
        params.put("uid", "");

        // Hit the API
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(BASE_URL);

        try {
            StringEntity entity = new StringEntity(params.toString());
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");

            HttpResponse response = httpClient.execute(httpPost);

            String uid = "";
            // Check if the response is successful
            if (response.getStatusLine().getStatusCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                JSONObject data = new JSONObject(result.toString());
                uid = data.getString("uid");
            }

            if (uid.equals(userId) || uid.isEmpty()) {
                return userId;
            } else {
                return uid;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Map<String, Map<String, String>> readCsvAsMap(String filename) {
        Map<String, Map<String, String>> dataMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean firstLine = true;
            String[] headers = null;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    // Read headers
                    headers = line.split(",");
                    firstLine = false;
                } else {
                    // Read data rows
                    String[] values = line.split(",");
                    if (headers != null && values.length == headers.length) {
                        Map<String, String> rowData = new HashMap<>();
                        for (int i = 0; i < headers.length; i++) {
                            rowData.put(headers[i], values[i]);
                        }
                        dataMap.put(values[0], rowData);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataMap;
    }

    public static void main(String[] args) {
        // Read both files
        Map<String, Map<String, String>> dataA = readCsvAsMap("file_a.csv");
        Map<String, Map<String, String>> dataB = readCsvAsMap("file_b.csv");

        // Combine the two datasets
        List<Map<String, String>> combinedList = new ArrayList<>();

        for (String userId : dataA.keySet()) {
            if (dataB.containsKey(userId)) {
                String uid = getUserId(dataA.get(userId).get("email"), userId);
                Map<String, String> combinedEntry = new HashMap<>();
                combinedEntry.put("user_id", uid);
                combinedEntry.put("email", dataA.get(userId).get("email"));
                combinedEntry.put("first_name", dataB.get(userId).get("first_name"));
                combinedEntry.put("last_name", dataB.get(userId).get("last_name"));
                combinedList.add(combinedEntry);
            }
        }

        // Write the result to a CSV file
        try (PrintWriter writer = new PrintWriter("combined_JAVA.csv")) {
            writer.println("user_id,email,first_name,last_name");
            for (Map<String, String> item : combinedList) {
                writer.println(item.get("user_id") + "," + item.get("email") + "," + item.get("first_name") + ","
                        + item.get("last_name"));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
