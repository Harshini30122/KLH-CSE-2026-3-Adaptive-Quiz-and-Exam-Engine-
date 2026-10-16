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
