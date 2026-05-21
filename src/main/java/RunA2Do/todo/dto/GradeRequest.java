package RunA2Do.todo.dto;

public record GradeRequest(
        double midtermWeight,
        double finalWeight,
        double homeworkWeight,
        double presentationWeight,
        double attendanceWeight,
        Double midtermScore,
        Double finalScore,
        Double homeworkScore,
        Double presentationScore,
        Double attendanceScore
) implements GradeRequestView {
}
