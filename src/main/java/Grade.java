public class Grade {
    private int id;
    private int studentId;
    private String courseName;
    private double score;
    
    public Grade() {}
    
    public Grade(int id, int studentId, String courseName, double score) {
        this.id = id;
        this.studentId = studentId;
        this.courseName = courseName;
        this.score = score;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}
