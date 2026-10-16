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
