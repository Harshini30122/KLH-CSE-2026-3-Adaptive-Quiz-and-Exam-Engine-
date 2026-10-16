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
