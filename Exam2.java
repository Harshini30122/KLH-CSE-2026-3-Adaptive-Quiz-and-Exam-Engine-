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
