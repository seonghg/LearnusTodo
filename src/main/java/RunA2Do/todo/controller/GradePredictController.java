package RunA2Do.todo.controller;

import org.springframework.web.bind.annotation.*;

/*
Request Sample
{
       "midtermWeight": 30,
       "finalWeight": 35,
       "homeworkWeight": 15,
       "presentationWeight": 10,
       "attendanceWeight": 10,

       "midtermScore": 82,
       "finalScore": 90,
       "homeworkScore": 95,
       "presentationScore": 88,
       "attendanceScore": 100
}

Response Sample
        {
         80.34
         }
*/

@RestController
@RequestMapping("/api/grade")
public class GradePredictController {

    @PostMapping("/predict")
    public double predict(@RequestBody GradeRequest request) {

        validateWeights(request);

        double predictedFinalScore = 0.0;

        predictedFinalScore += calculateWeightedScore(request.midtermScore, request.midtermWeight);
        predictedFinalScore += calculateWeightedScore(request.finalScore, request.finalWeight);
        predictedFinalScore += calculateWeightedScore(request.homeworkScore, request.homeworkWeight);
        predictedFinalScore += calculateWeightedScore(request.presentationScore, request.presentationWeight);
        predictedFinalScore += calculateWeightedScore(request.attendanceScore, request.attendanceWeight);

        return predictedFinalScore;
    }

    @PostMapping("/required-final")
    public double requiredFinal(@RequestBody GradeTargetRequest request) {

        validateWeights(request);

        double currentScore = 0.0;

        currentScore += calculateWeightedScore(request.midtermScore, request.midtermWeight);
        currentScore += calculateWeightedScore(request.homeworkScore, request.homeworkWeight);
        currentScore += calculateWeightedScore(request.presentationScore, request.presentationWeight);
        currentScore += calculateWeightedScore(request.attendanceScore, request.attendanceWeight);

        return (request.targetScore - currentScore) / (request.finalWeight / 100.0);
    }

    private double calculateWeightedScore(Double score, double weight) {
        if (score == null) {
            return 0.0;
        }

        return score * weight / 100.0;
    }

    private void validateWeights(GradeRequest request) {
        double totalWeight =
                request.midtermWeight
                        + request.finalWeight
                        + request.homeworkWeight
                        + request.presentationWeight
                        + request.attendanceWeight;

        if (Math.abs(totalWeight - 100.0) > 0.0001) {
            throw new IllegalArgumentException("평가 비율의 합은 반드시 100이어야 합니다.");
        }
    }

    public static class GradeRequest {
        public double midtermWeight;
        public double finalWeight;
        public double homeworkWeight;
        public double presentationWeight;
        public double attendanceWeight;

        public Double midtermScore;
        public Double finalScore;
        public Double homeworkScore;
        public Double presentationScore;
        public Double attendanceScore;
    }

    public static class GradeTargetRequest extends GradeRequest {
        public double targetScore;
    }
}