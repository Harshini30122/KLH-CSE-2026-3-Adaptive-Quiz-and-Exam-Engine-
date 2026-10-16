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
