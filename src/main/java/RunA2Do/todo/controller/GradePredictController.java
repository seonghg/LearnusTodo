package RunA2Do.todo.controller;

import RunA2Do.todo.service.GradePredictService;
import RunA2Do.todo.service.GradePredictService.GradeRequest;
import RunA2Do.todo.service.GradePredictService.GradeTargetRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grade")
public class GradePredictController {

    private final GradePredictService gradePredictService;

    public GradePredictController(GradePredictService gradePredictService) {
        this.gradePredictService = gradePredictService;
    }

    @PostMapping("/predict")
    public double predict(@RequestBody GradeRequest request) {
        return gradePredictService.predict(request);
    }

    @PostMapping("/required-final")
    public double requiredFinal(@RequestBody GradeTargetRequest request) {
        return gradePredictService.requiredFinal(request);
    }
}
