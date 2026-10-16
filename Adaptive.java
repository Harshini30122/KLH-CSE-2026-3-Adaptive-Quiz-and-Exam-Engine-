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

