import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AdaptiveQuizEngine {

    private static final java.util.Scanner sc = new java.util.Scanner(System.in);
    private static final ArrayList<Question> questions = new ArrayList<>();
    private static final ArrayList<String> results = new ArrayList<>();
    private static final String PASSWORD = "admin123";
    private static int nextId = 1;

    /* CO4: interface-based validation and a custom checked exception. */
    interface Validatable {
        void validate() throws InvalidQuestionException;
    }

    static class InvalidQuestionException extends Exception {
        InvalidQuestionException(String message) {
            super(message);
        }
    }

    /* CO4: encapsulated class with private fields, constructor, and methods. */
    static class Question implements Comparable<Question>, Validatable {
        private int id;
        private int answer;
        private int difficulty;
        private String text;
        private String[] options;

        Question(int id, String text, String[] options,
                 int answer, int difficulty) throws InvalidQuestionException {
            this.id = id;
            this.text = text;
            this.options = Arrays.copyOf(options, options.length);
            this.answer = answer;
            this.difficulty = difficulty;
            validate();
        }

        @Override
        public void validate() throws InvalidQuestionException {
            if (text == null || text.trim().isEmpty())
                throw new InvalidQuestionException("Question text cannot be empty.");
            if (options == null || options.length != 4)
                throw new InvalidQuestionException("Exactly four options are required.");
            for (String option : options) {
                if (option == null || option.trim().isEmpty())
                    throw new InvalidQuestionException("Options cannot be empty.");
            }
            if (answer < 1 || answer > 4)
                throw new InvalidQuestionException("Answer must be between 1 and 4.");
            if (difficulty < 1 || difficulty > 3)
                throw new InvalidQuestionException("Difficulty must be between 1 and 3.");
        }

        public int getId() { return id; }
        public int getAnswer() { return answer; }
        public int getDifficulty() { return difficulty; }
        public String getText() { return text; }
        public String[] getOptions() { return Arrays.copyOf(options, options.length); }

        public void setText(String text) { this.text = text; }
        public void setOption(int index, String option) { options[index] = option; }
        public void setAnswer(int answer) { this.answer = answer; }
        public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

        @Override
        public int compareTo(Question other) {
            return Integer.compare(this.id, other.id);
        }
    }

    public static void main(String[] args) {
        loadQuestions();
        loadResults();

        if (questions.isEmpty()) {
            addSamples();
            saveQuestions();
        }

        while (true) {
            System.out.println("\n=== ADAPTIVE QUIZ AND EXAM ENGINE ===");
            System.out.println("1. Student");
            System.out.println("2. Teacher");
            System.out.println("3. Reports");
            System.out.println("4. Exit");

            int choice = readInt("Enter choice: ", 1, 4);

            switch (choice) {
                case 1:
                    studentMenu();
                    break;
                case 2:
                    teacherLogin();
                    break;
                case 3:
                    reportsMenu();
                    break;
                case 4:
                    System.out.println("Thank you!");
                    sc.close();
                    return;
                default:
                    break;
            }
        }
    }

    static void studentMenu() {
        System.out.print("Enter student name: ");
        String name = sc.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }

        while (true) {
            System.out.println("\n1. Take Quiz");
            System.out.println("2. View My Results");
            System.out.println("3. Back");

            int choice = readInt("Enter choice: ", 1, 3);

            if (choice == 1) takeQuiz(name);
            else if (choice == 2) viewMyResults(name);
            else return;
        }
    }

    static void takeQuiz(String name) {
        if (questions.isEmpty()) {
            System.out.println("No questions available.");
            return;
        }

        int count = readInt(
            "Number of questions (1-" + questions.size() + "): ",
            1, questions.size());

        Set<Integer> used = new HashSet<>();
        int score = 0;
        int difficulty = 2;
        int asked = 0;

        while (asked < count) {
            Question q = selectQuestion(difficulty, used);
            if (q == null) break;

            used.add(q.getId());
            asked++;

            System.out.println("\nQuestion " + asked + "/" + count);
            System.out.println("Difficulty: " + level(difficulty));
            System.out.println(q.getText());

            String[] options = q.getOptions();
            for (int i = 0; i < options.length; i++)
                System.out.println((i + 1) + ". " + options[i]);

            int answer = readInt("Your answer (1-4): ", 1, 4);

            if (answer == q.getAnswer()) {
                score++;
                difficulty = Math.min(3, difficulty + 1);
                System.out.println("Correct answer!");
            } else {
                difficulty = Math.max(1, difficulty - 1);
                System.out.println("Incorrect. Correct answer: "
                    + q.getAnswer() + ". " + options[q.getAnswer() - 1]);
            }
        }

        if (asked == 0) return;

        double percentage = score * 100.0 / asked;
        String date = LocalDateTime.now().toString();
        String result = name + "|" + date + "|" + score
            + "|" + asked + "|" + percentage;

        results.add(result);
        saveResults();

        System.out.println("\n===== QUIZ RESULT =====");
        System.out.println("Student: " + name);
        System.out.println("Score: " + score + "/" + asked);
        System.out.printf(Locale.US, "Percentage: %.2f%%%n", percentage);
        System.out.println("Performance: " + performance(percentage));
    }

    static Question selectQuestion(int difficulty, Set<Integer> used) {
        ArrayList<Question> available = new ArrayList<>();

        for (Question q : questions) {
            if (!used.contains(q.getId()) && q.getDifficulty() == difficulty)
                available.add(q);
        }

        if (available.isEmpty()) {
            for (Question q : questions)
                if (!used.contains(q.getId())) available.add(q);
        }

        if (available.isEmpty()) return null;

        Collections.shuffle(available);
        return available.get(0);
    }

    static void viewMyResults(String name) {
        boolean found = false;

        for (String r : results) {
            String[] p = r.split("\\|", -1);
            if (p.length == 5 && p[0].equalsIgnoreCase(name)) {
                found = true;
                System.out.println("\nDate: " + p[1]);
                System.out.println("Score: " + p[2] + "/" + p[3]);
                System.out.printf(Locale.US,
                    "Percentage: %.2f%%%n", Double.parseDouble(p[4]));
            }
        }

        if (!found) System.out.println("No results found.");
    }

    static void teacherLogin() {
        System.out.print("Enter teacher password: ");

        if (sc.nextLine().equals(PASSWORD)) teacherMenu();
        else System.out.println("Incorrect password.");
    }

    static void teacherMenu() {
        while (true) {
            System.out.println("\n===== TEACHER MENU =====");
            System.out.println("1. Add Question");
            System.out.println("2. View Questions");
            System.out.println("3. Update Question");
            System.out.println("4. Delete Question");
            System.out.println("5. View All Results");
            System.out.println("6. Search Question by ID");
            System.out.println("7. Back");

            int choice = readInt("Enter choice: ", 1, 7);

            switch (choice) {
                case 1: addQuestion(); break;
                case 2: viewQuestions(); break;
                case 3: updateQuestion(); break;
                case 4: deleteQuestion(); break;
                case 5: viewAllResults(); break;
                case 6: searchQuestionById(); break;
                case 7: return;
                default: break;
            }
        }
    }

    static void addQuestion() {
        String text = readText("Enter question: ");
        String[] options = new String[4];

        for (int i = 0; i < options.length; i++)
            options[i] = readText("Option " + (i + 1) + ": ");

        int answer = readInt("Correct option (1-4): ", 1, 4);
        System.out.println("1. Easy  2. Medium  3. Hard");
        int difficulty = readInt("Difficulty: ", 1, 3);

        try {
            questions.add(new Question(nextId++, text, options, answer, difficulty));
            saveQuestions();
            System.out.println("Question added successfully.");
        } catch (InvalidQuestionException e) {
            System.out.println("Question could not be added: " + e.getMessage());
        }
    }

    static void viewQuestions() {
        if (questions.isEmpty()) {
            System.out.println("No questions available.");
            return;
        }

        ArrayList<Question> sorted = new ArrayList<>(questions);
        Collections.sort(sorted); // CO5: library sorting algorithm

        for (Question q : sorted) printQuestion(q);
    }

    static void printQuestion(Question q) {
        System.out.println("\nID: " + q.getId());
        System.out.println(q.getText());
        System.out.println("Difficulty: " + level(q.getDifficulty()));

        String[] options = q.getOptions();
        for (int i = 0; i < options.length; i++)
            System.out.println((i + 1) + ". " + options[i]);

        System.out.println("Correct answer: " + q.getAnswer());
    }

    static void updateQuestion() {
        viewQuestions();
        if (questions.isEmpty()) return;

        int id = readInt("Enter question ID: ", 1, Integer.MAX_VALUE);
        Optional<Question> found = findQuestionOptional(id);

        if (!found.isPresent()) {
            System.out.println("Question not found.");
            return;
        }

        Question q = found.get();

        System.out.print("New question (Enter to keep): ");
        String text = sc.nextLine().trim();
        if (!text.isEmpty()) q.setText(text);

        for (int i = 0; i < 4; i++) {
            System.out.print("New option " + (i + 1) + " (Enter to keep): ");
            String option = sc.nextLine().trim();
            if (!option.isEmpty()) q.setOption(i, option);
        }

        q.setAnswer(readInt("New correct option (1-4): ", 1, 4));
        System.out.println("1. Easy  2. Medium  3. Hard");
        q.setDifficulty(readInt("New difficulty: ", 1, 3));

        try {
            q.validate();
            saveQuestions();
            System.out.println("Question updated.");
        } catch (InvalidQuestionException e) {
            System.out.println("Question is invalid: " + e.getMessage());
        }
    }

    static void deleteQuestion() {
        viewQuestions();
        if (questions.isEmpty()) return;

        int id = readInt("Enter question ID to delete: ", 1, Integer.MAX_VALUE);
        Optional<Question> found = findQuestionOptional(id);

        if (!found.isPresent()) {
            System.out.println("Question not found.");
            return;
        }

        System.out.print("Confirm deletion (yes/no): ");
        if (sc.nextLine().trim().equalsIgnoreCase("yes")) {
            questions.remove(found.get());
            saveQuestions();
            System.out.println("Question deleted.");
        }
    }

    static Question findQuestion(int id) {
        return findQuestionOptional(id).orElse(null);
    }

    /* CO6: Optional provides a null-safe search result. */
    static Optional<Question> findQuestionOptional(int id) {
        return questions.stream()
            .filter(q -> q.getId() == id)
            .findFirst();
    }

    static void searchQuestionById() {
        if (questions.isEmpty()) {
            System.out.println("No questions available.");
            return;
        }

        ArrayList<Question> sorted = new ArrayList<>(questions);
        Collections.sort(sorted);
        int id = readInt("Enter question ID to search: ", 1, Integer.MAX_VALUE);

        int index = Collections.binarySearch(sorted,
            createSearchQuestion(id), Comparator.naturalOrder());

        if (index >= 0) printQuestion(sorted.get(index));
        else System.out.println("Question not found.");
    }

    static Question createSearchQuestion(int id) {
        try {
            return new Question(id, "search", new String[]{"a", "b", "c", "d"}, 1, 1);
        } catch (InvalidQuestionException e) {
            throw new IllegalStateException(e);
        }
    }

    static void viewAllResults() {
        if (results.isEmpty()) {
            System.out.println("No results available.");
            return;
        }

        results.stream()
            .sorted()
            .forEach(System.out::println); // CO6: method reference
    }

    static void reportsMenu() {
        while (true) {
            System.out.println("\n===== REPORTS =====");
            System.out.println("1. Difficulty distribution");
            System.out.println("2. Question count by difficulty (recursive)");
            System.out.println("3. Performance summary using streams");
            System.out.println("4. Build text report");
            System.out.println("5. Back");

            int choice = readInt("Enter choice: ", 1, 5);

            switch (choice) {
                case 1: showDifficultyDistribution(); break;
                case 2: showRecursiveDifficultyCounts(); break;
                case 3: showPerformanceSummary(); break;
                case 4: System.out.println(buildTextReport()); break;
                case 5: return;
                default: break;
            }
        }
    }

    /* CO3: two-dimensional array for [difficulty][question statistic]. */
    static int[][] buildDifficultyMatrix() {
        int[][] matrix = new int[3][2];
        for (Question q : questions) {
            int row = q.getDifficulty() - 1;
            matrix[row][0]++;
            matrix[row][1] += q.getOptions().length;
        }
        return matrix;
    }

    static void showDifficultyDistribution() {
        int[][] matrix = buildDifficultyMatrix();
        System.out.println("\nDifficulty | Questions | Options");
        for (int i = 0; i < matrix.length; i++)
            System.out.printf("%-10s | %-9d | %d%n",
                level(i + 1), matrix[i][0], matrix[i][1]);
    }

    /* CO3: recursion counts questions at a selected difficulty. */
    static int countDifficultyRecursive(int index, int difficulty) {
        if (index >= questions.size()) return 0;
        int current = questions.get(index).getDifficulty() == difficulty ? 1 : 0;
        return current + countDifficultyRecursive(index + 1, difficulty);
    }

    static void showRecursiveDifficultyCounts() {
        for (int difficulty = 1; difficulty <= 3; difficulty++)
            System.out.println(level(difficulty) + ": "
                + countDifficultyRecursive(0, difficulty));
    }

    /* CO6: Predicate, lambda, groupingBy, averaging, and collectors. */
    static void showPerformanceSummary() {
        if (results.isEmpty()) {
            System.out.println("No results available.");
            return;
        }

        Map<String, Long> performanceCounts = results.stream()
            .map(AdaptiveQuizEngine::percentageFromResult)
            .map(AdaptiveQuizEngine::performance)
            .collect(Collectors.groupingBy(label -> label, Collectors.counting()));

        double average = results.stream()
            .mapToDouble(AdaptiveQuizEngine::percentageFromResult)
            .average()
            .orElse(0.0);

        Predicate<String> validResult = r -> r.split("\\|", -1).length == 5;
        long validCount = results.stream().filter(validResult).count();

        System.out.println("Valid results: " + validCount);
        System.out.printf(Locale.US, "Average percentage: %.2f%%%n", average);
        System.out.println("Performance groups: " + performanceCounts);
    }

    static double percentageFromResult(String result) {
        String[] parts = result.split("\\|", -1);
        return parts.length == 5 ? Double.parseDouble(parts[4]) : 0.0;
    }

    /* CO5: StringBuilder creates a readable report efficiently. */
    static String buildTextReport() {
        StringBuilder report = new StringBuilder();
        report.append("===== ADAPTIVE QUIZ REPORT =====\n");
        report.append("Total questions: ").append(questions.size()).append('\n');
        report.append("Total quiz attempts: ").append(results.size()).append('\n');

        for (int difficulty = 1; difficulty <= 3; difficulty++) {
            report.append(level(difficulty)).append(" questions: ")
                .append(countDifficultyRecursive(0, difficulty)).append('\n');
        }

        report.append("Generated: ").append(LocalDateTime.now()).append('\n');
        return report.toString();
    }

    static void addSamples() {
        addSample("Which keyword creates a class in Java?",
            new String[]{"class", "define", "struct", "object"}, 1, 1);
        addSample("Which method starts a Java application?",
            new String[]{"start()", "run()", "main()", "init()"}, 3, 1);
        addSample("Which type stores true or false?",
            new String[]{"int", "boolean", "char", "double"}, 2, 1);
        addSample("Which loop executes at least once?",
            new String[]{"for", "while", "do-while", "foreach"}, 3, 2);
        addSample("Which keyword supports inheritance?",
            new String[]{"implements", "extends", "inherits", "super"}, 2, 2);
        addSample("Which collection prevents duplicates?",
            new String[]{"ArrayList", "LinkedList", "HashSet", "Vector"}, 3, 2);
        addSample("Which keyword prevents overriding?",
            new String[]{"static", "final", "private", "abstract"}, 2, 3);
        addSample("Which exception is associated with file I/O?",
            new String[]{"ArithmeticException", "NullPointerException",
                         "IOException", "ClassCastException"}, 3, 3);
    }

    static void addSample(String text, String[] options,
                          int answer, int difficulty) {
        try {
            questions.add(new Question(nextId++, text, options, answer, difficulty));
        } catch (InvalidQuestionException e) {
            System.out.println("Sample question skipped: " + e.getMessage());
        }
    }

    static void loadQuestions() {
        Path path = Paths.get("questions.txt");
        if (!Files.exists(path)) return;

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] p = line.split("\\t", -1);
                    if (p.length != 8) continue;

                    int id = Integer.parseInt(p[0]);
                    int difficulty = Integer.parseInt(p[1]);
                    String text = decode(p[2]);
                    String[] options = {
                        decode(p[3]), decode(p[4]), decode(p[5]), decode(p[6])
                    };
                    int answer = Integer.parseInt(p[7]);

                    if (id < 1 || findQuestion(id) != null) continue;

                    questions.add(new Question(id, text, options, answer, difficulty));
                    nextId = Math.max(nextId, id + 1);
                } catch (RuntimeException | InvalidQuestionException e) {
                    System.out.println("Skipping invalid question record.");
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load questions: " + e.getMessage());
        }
    }

    static void saveQuestions() {
        ArrayList<String> lines = new ArrayList<>();

        for (Question q : questions) {
            String[] options = q.getOptions();
            lines.add(q.getId() + "\t" + q.getDifficulty() + "\t"
                + encode(q.getText()) + "\t"
                + encode(options[0]) + "\t"
                + encode(options[1]) + "\t"
                + encode(options[2]) + "\t"
                + encode(options[3]) + "\t" + q.getAnswer());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get("questions.txt"), StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Could not save questions: " + e.getMessage());
        }
    }

    static void loadResults() {
        Path path = Paths.get("results.txt");
        if (!Files.exists(path)) return;

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null)
                if (!line.trim().isEmpty()) results.add(line);
        } catch (IOException e) {
            System.out.println("Could not load results: " + e.getMessage());
        }
    }

    static void saveResults() {
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get("results.txt"), StandardCharsets.UTF_8)) {
            for (String result : results) {
                writer.write(result);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Could not save results: " + e.getMessage());
        }
    }

    static String encode(String s) {
        return Base64.getEncoder().encodeToString(
            s.getBytes(StandardCharsets.UTF_8));
    }

    static String decode(String s) {
        return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
    }

    static int readInt(String message, int min, int max) {
        while (true) {
            System.out.print(message);
            try {
                int n = Integer.parseInt(sc.nextLine().trim());
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException e) {
                // Handle invalid input without terminating the program.
            }
            System.out.println("Enter a valid number.");
        }
    }

    static String readText(String message) {
        while (true) {
            System.out.print(message);
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("Input cannot be empty.");
        }
    }

    static String level(int d) {
        if (d == 1) return "Easy";
        if (d == 2) return "Medium";
        return "Hard";
    }

    static String performance(double p) {
        if (p >= 90) return "Excellent";
        if (p >= 75) return "Very Good";
        if (p >= 60) return "Good";
        if (p >= 40) return "Average";
        return "Needs Improvement";
    }
}
