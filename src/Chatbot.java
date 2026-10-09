import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Chatbot {

    private final String apiKey;

    private static final String MODEL = "gemini-3.8-flash";

    private static final int MAX_RETRIES = 3;

    public Chatbot() {

        apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("GEMINI_API_KEY not found.");
        }
    }

    public String getResponse(String input) {

        if (apiKey == null || apiKey.isEmpty()) {
            return "API key not found. Please check your GEMINI_API_KEY environment variable.";
        }

        int attempt = 0;

        while (attempt < MAX_RETRIES) {

            attempt++;

            try {

                String result = sendRequest(input);

                // If successful, return the answer
                if (!result.startsWith("RETRY_")) {
                    return result;
                }

                // API temporarily unavailable
                if (attempt < MAX_RETRIES) {

                    try {
                        Thread.sleep(2000 * attempt);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

            } catch (Exception e) {

                if (attempt >= MAX_RETRIES) {
                    return "Error connecting to AMZAD AI: "
                            + e.getMessage();
                }

                try {
                    Thread.sleep(2000 * attempt);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return "AMZAD AI is temporarily busy. Please try again in a few moments.";
    }

    private String sendRequest(String input) throws Exception {

        String endpoint =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                + MODEL
                + ":generateContent?key="
                + URLEncoder.encode(
                        apiKey,
                        StandardCharsets.UTF_8
                );

        URL url = new URI(endpoint).toURL();

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);

        connection.setDoOutput(true);

        /*
         * AMZAD AI instructions
         */
        String systemInstruction =
                "You are AMZAD AI, a helpful AI assistant created by Amzad. "
                + "Your visible name is AMZAD AI. "
                + "When the user asks your name, say your name is AMZAD AI. "
                + "Do not say that you are Gemini or Google Gemini. "
                + "Do not mention the underlying AI model unless the user specifically "
                + "asks what technology powers you. "
                + "Answer questions clearly, accurately, and helpfully. "
                + "Use simple language when appropriate. "
                + "Do not use Markdown heading symbols such as ###. "
                + "Do not use LaTeX formatting or dollar signs for equations. "
                + "Do not use $$ for mathematical equations. "
                + "Write mathematical and scientific equations in normal readable text. "
                + "You may use Unicode symbols such as →, ×, ÷, √, ≤ and ≥. "
                + "For example, write: Carbon Dioxide + Water + Sunlight → Glucose + Oxygen. "
                + "Do not put unnecessary Markdown formatting in your answers.";

        String safeInstruction =
                escapeJson(systemInstruction);

        String safeInput =
                escapeJson(input);

        String jsonRequest =
                "{"
                + "\"systemInstruction\":{"
                + "\"parts\":["
                + "{"
                + "\"text\":\""
                + safeInstruction
                + "\""
                + "}"
                + "]"
                + "},"
                + "\"contents\":["
                + "{"
                + "\"parts\":["
                + "{"
                + "\"text\":\""
                + safeInput
                + "\""
                + "}"
                + "]"
                + "}"
                + "]"
                + "}";

        /*
         * Send request
         */
        try (OutputStream outputStream =
                     connection.getOutputStream()) {

            outputStream.write(
                    jsonRequest.getBytes(
                            StandardCharsets.UTF_8
                    )
            );
        }

        int responseCode =
                connection.getResponseCode();

        InputStream stream;

        if (responseCode >= 200
                && responseCode < 300) {

            stream = connection.getInputStream();

        } else {

            stream = connection.getErrorStream();
        }

        String response =
                readStream(stream);

        /*
         * Temporary server overload
         */
        if (responseCode == 503) {

            return "RETRY_503";
        }

        /*
         * Other API errors
         */
        if (responseCode < 200
                || responseCode >= 300) {

            return "AI API Error ("
                    + responseCode
                    + "):\n"
                    + response;
        }

        /*
         * Extract answer
         */
        return extractAnswer(response);
    }

    /*
     * Escape JSON characters
     */
    private String escapeJson(String text) {

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /*
     * Read API response
     */
    private String readStream(InputStream stream)
            throws IOException {

        if (stream == null) {
            return "";
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                stream,
                                StandardCharsets.UTF_8
                        )
                );

        StringBuilder result =
                new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {

            result.append(line);
        }

        reader.close();

        return result.toString();
    }

    /*
     * Extract AI answer
     */
    private String extractAnswer(String json) {

        String marker = "\"text\":";

        int start =
                json.indexOf(marker);

        if (start == -1) {

            return "I received a response, but I couldn't read the answer.";
        }

        start += marker.length();

        while (start < json.length()
                && Character.isWhitespace(
                        json.charAt(start))) {

            start++;
        }

        if (start >= json.length()
                || json.charAt(start) != '"') {

            return "I couldn't read the AI response.";
        }

        start++;

        StringBuilder answer =
                new StringBuilder();

        boolean escaped = false;

        for (int i = start;
             i < json.length();
             i++) {

            char c = json.charAt(i);

            if (escaped) {

                if (c == 'n') {

                    answer.append('\n');

                } else if (c == 'r') {

                    answer.append('\r');

                } else if (c == 't') {

                    answer.append('\t');

                } else if (c == '"') {

                    answer.append('"');

                } else if (c == '\\') {

                    answer.append('\\');

                } else {

                    answer.append(c);
                }

                escaped = false;

            } else if (c == '\\') {

                escaped = true;

            } else if (c == '"') {

                break;

            } else {

                answer.append(c);
            }
        }

        return cleanResponse(
                answer.toString()
        );
    }

    /*
     * Clean Markdown and LaTeX
     */
    private String cleanResponse(String response) {

        // Remove Markdown headings
        response =
                response.replaceAll(
                        "(?m)^#{1,6}\\s*",
                        ""
                );

        // Remove LaTeX delimiters
        response =
                response.replace("$$", "");

        response =
                response.replace("$", "");

        // Remove common LaTeX commands
        response =
                response.replace("\\text{", "");

        response =
                response.replace("\\mathrm{", "");

        response =
                response.replace("\\mathbf{", "");

        // Replace arrows
        response =
                response.replace(
                        "\\rightarrow",
                        "→"
                );

        response =
                response.replace(
                        "\\to",
                        "→"
                );

        // Remove unnecessary braces
        response =
                response.replace("}", "");

        // Remove Markdown bold
        response =
                response.replace("**", "");

        response =
                response.replace("__", "");

        return response.trim();
    }
}