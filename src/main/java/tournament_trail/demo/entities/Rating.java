package tournament_trail.demo.entities;

public enum Rating {
    POOR(1),
    FAIR(2),
    NEUTRAL(3),
    GOOD(4),
    EXCELLENT(5);

    private final int grade;

    Rating(int number){
        this.grade =number;
    }

    public int getGrade() {
        return grade;
    }
}
