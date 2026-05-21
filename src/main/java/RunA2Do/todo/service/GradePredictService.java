package RunA2Do.todo.service;

import RunA2Do.todo.dto.GradeRequest;
import RunA2Do.todo.dto.GradeRequestView;
import RunA2Do.todo.dto.GradeTargetRequest;
import org.springframework.stereotype.Service;

@Service
public class GradePredictService {

    public double predict(GradeRequest request) {
        validateWeights(request);

        double predictedFinalScore = 0.0;
        predictedFinalScore += calculateWeightedScore(request.midtermScore(), request.midtermWeight());
        predictedFinalScore += calculateWeightedScore(request.finalScore(), request.finalWeight());
        predictedFinalScore += calculateWeightedScore(request.homeworkScore(), request.homeworkWeight());
        predictedFinalScore += calculateWeightedScore(request.presentationScore(), request.presentationWeight());
        predictedFinalScore += calculateWeightedScore(request.attendanceScore(), request.attendanceWeight());
        return predictedFinalScore;
    }

    public double requiredFinal(GradeTargetRequest request) {
        validateWeights(request);

        double currentScore = 0.0;
        currentScore += calculateWeightedScore(request.midtermScore(), request.midtermWeight());
        currentScore += calculateWeightedScore(request.homeworkScore(), request.homeworkWeight());
        currentScore += calculateWeightedScore(request.presentationScore(), request.presentationWeight());
        currentScore += calculateWeightedScore(request.attendanceScore(), request.attendanceWeight());
        return (request.targetScore() - currentScore) / (request.finalWeight() / 100.0);
    }

    private double calculateWeightedScore(Double score, double weight) {
        if (score == null) {
            return 0.0;
        }
        return score * weight / 100.0;
    }

    private void validateWeights(GradeRequestView request) {
        double totalWeight = request.midtermWeight()
                + request.finalWeight()
                + request.homeworkWeight()
                + request.presentationWeight()
                + request.attendanceWeight();

        if (Math.abs(totalWeight - 100.0) > 0.0001) {
            throw new IllegalArgumentException("평가 비율의 합은 반드시 100이어야 합니다.");
        }
    }
}
