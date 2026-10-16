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
