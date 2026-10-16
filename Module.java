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
