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
