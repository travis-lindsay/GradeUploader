import com.google.gson.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/*
    GradeUploader:
        - A cli based prototype for uploading a csv of students grades to Canvas via their REST API
        - Requests courses, assignments, and users associated with the chosen course
    Current Limitations:
        - I wasn't able to test any of this with an API_TOKEN other than my own as a result
        I was only able to do things with student permissions, and cannot use the 'change grade' API endpoint successfully
        - The basic code is also in place for parsing a CSV (using CSVParser), however I didn't add any CSV format specific code
        as the format of the CSV that would be used wasn't specified
 */

public class Main {
    // Instructions for generating an API token are here: https://canvas.instructure.com/courses/785215/pages/getting-started-with-the-api
    private static String API_TOKEN = "ENTER_YOUR_API_KEY_HERE"; // API key generated by Canvas to access their API endpoints

    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        System.out.println("GET/Print list of courseID's? (y/n)");
        String ans = reader.next();
        if (ans.toLowerCase().equals("y")) {
            getCourseListViaAPICall();
        }
        System.out.println("Enter the course ID for which you wish to upload grades: ");
        int courseID = reader.nextInt();
        System.out.println("GET/Print list of assignmentID's? (y/n)");
        ans = reader.next();
        if (ans.toLowerCase().equals("y")) {
            getAssignmentListViaAPICall(courseID);
        }
        System.out.println("Enter the assignment ID: ");
        int assignmentID = reader.nextInt();
        System.out.println("Enter file path to CSV: ");
        String filePath = reader.next();
        try {
            Map<String, String> usersInCourse = getMapOfUsersInCourse(courseID);
            File csvFile = new File(filePath);
            if (csvFile.isFile()) {
                CSVParser parser = CSVParser.parse(csvFile, Charset.defaultCharset(), CSVFormat.EXCEL);
                for (CSVRecord record : parser) {
                    // TODO, get the zyBooks actual CSV record so we know what the names of the columns are we need to parse
                    // TODO, map the userIDs we pulled in usersInCourse to the names of the each student in the CSV
                    // TODO, put that data into the body of the API call below
                    /*
                        See Docs: https://canvas.instructure.com/doc/api/all_resources.html#method.submissions_api.update
                        API Call: https://uvu.instructure.com/api/v1/courses/:course_id/assignments/:assignment_id/submissions/:user_id
                        Note: use submission[posted_grade] to specify the score of the assignment
                     */
                }
            } else {
                System.out.println("Invalid file");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        reader.close();
    }

    private static Map<String, String> getMapOfUsersInCourse(int courseID) {
        // /api/v1/courses/:course_id/users
        Map<String, String> userNamesAndIDs = new HashMap<>();
        try {
            URL url = new URL("https://uvu.instructure.com/api/v1/courses/" + courseID + "/users/?per_page=100");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("authorization", "Bearer " + API_TOKEN);

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String rawJSON;
            while ((rawJSON = br.readLine()) != null) {
                JsonElement jelement = new JsonParser().parse(rawJSON);
                JsonArray jArray = jelement.getAsJsonArray();
                for (JsonElement elem: jArray) {
                    JsonObject user = elem.getAsJsonObject();
                    System.out.println("Username: " + user.get("name").toString() + " ID: " + user.get("id").toString());
                    userNamesAndIDs.put(user.get("name").toString(), user.get("id").toString());
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userNamesAndIDs;
    }

    private static void getCourseListViaAPICall() {
        try {
            URL url = new URL("https://uvu.instructure.com/api/v1/courses/?enrollment_state=active&per_page=100");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("authorization", "Bearer " + API_TOKEN);

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String rawJSON;
            while ((rawJSON = br.readLine()) != null) {
                JsonElement jelement = new JsonParser().parse(rawJSON);
                JsonArray jArray = jelement.getAsJsonArray();
                for (JsonElement elem: jArray) {
                    JsonObject course = elem.getAsJsonObject();
                    System.out.println("Course Name: " + course.get("name") + ", Course ID: " + course.get("id"));
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getAssignmentListViaAPICall(int courseID) {
        // https://uvu.instructure.com/api/v1/courses/450673/assignments
        try {
            URL url = new URL("https://uvu.instructure.com/api/v1/courses/" + courseID + "/assignments/?per_page=100");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("authorization", "Bearer " + API_TOKEN);

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String rawJSON;
            while ((rawJSON = br.readLine()) != null) {
                JsonElement jelement = new JsonParser().parse(rawJSON);
                JsonArray jArray = jelement.getAsJsonArray();
                for (JsonElement elem: jArray) {
                    JsonObject course = elem.getAsJsonObject();
                    System.out.println("Assignment Name: " + course.get("name") + ", Assignment ID: " + course.get("id"));
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

