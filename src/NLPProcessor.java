public class NLPProcessor {

    public String process(String input) {

        // Convert to lowercase
        input = input.toLowerCase();

        // Remove extra spaces
        input = input.trim();
        input = input.replaceAll("\\s+", " ");

        // Remove punctuation
        input = input.replaceAll("[^a-zA-Z0-9 ]", "");

        // Remove common words
        input = input.replaceAll(
                "\\b(please|can|could|would|you|tell|me|about|explain|what|is|are|the)\\b",
                ""
        );

        // Remove extra spaces again
        input = input.replaceAll("\\s+", " ").trim();

        return input;
    }
}