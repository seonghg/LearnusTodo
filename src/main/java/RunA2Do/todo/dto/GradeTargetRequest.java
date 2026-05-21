package RunA2Do.todo.dto;

public record GradeTargetRequest(
        double midtermWeight,
        double finalWeight,
        double homeworkWeight,
        double presentationWeight,
        double attendanceWeight,
        Double midtermScore,
        Double finalScore,
        Double homeworkScore,
        Double presentationScore,
        Double attendanceScore,
        double targetScore
) implements GradeRequestView {
}
