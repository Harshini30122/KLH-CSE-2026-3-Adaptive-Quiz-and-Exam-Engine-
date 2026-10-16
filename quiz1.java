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
