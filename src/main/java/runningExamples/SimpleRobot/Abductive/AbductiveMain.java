package runningExamples.SimpleRobot.Abductive;

import abductive.AbductiveDriver;
import abductive.AbductiveModel;
import abductive.AbductiveModelGenerator;
import model.Component;
import model.ModelData;
import model.Type;
import util.Util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class AbductiveMain {
    public static void main(String[] args) throws IOException {
        String pathToRobotFmi = "FMIs/ERobot.SubModel.InputSimpleRobot.fmu";
        String pathToModelData = "src/main/java/runningExamples/SimpleRobot/Consistency/simpleRobot.json";
        ModelData robotData = Util.modelDataFromJson(pathToModelData);

        AbductiveModelGenerator abductiveModelGenerator = new AbductiveModelGenerator(pathToRobotFmi, robotData, new RobotDiff());
        abductiveModelGenerator.setEnc(new StrongFaultAbEncoder());
        abductiveModelGenerator.generateModel(10, 1.0, 3);
        AbductiveModel learnedModel = abductiveModelGenerator.getAbductiveModel();
        System.out.println(learnedModel.getRules());
        learnedModel.modelToFile("test.txt");
        learnedModel.tryToExplain(new HashSet<>(Arrays.asList("wantedDirection(straight)", "actualDirection(left)")));

        System.out.println(learnedModel.getDiagnosis());
        String[] fmiPath = {"FMIs/ERobot.Experiments.RampInput.fmu", "FMIs/ERobot.Experiments.RampWFault.fmu",
                "FMIs/ERobot.Experiments.constWFault.fmu", "FMIs/ERobot.Experiments.RampIntermittent.fmu",
                "FMIs/ERobot.Experiments.ConstBothBreak.fmu"};

        ModelData abModelData = new ModelData();
        abModelData.setComponentsToRead(
                Arrays.asList(
                        new Component("robot.rightWheel.i", Type.DOUBLE),
                        new Component("robot.rightWheel.o", Type.DOUBLE),
                        new Component("robot.leftWheel.i", Type.DOUBLE),
                        new Component("robot.leftWheel.o", Type.DOUBLE)
                )
        );

        AbductiveDriver abductiveDriver = AbductiveDriver.builder()
                .pathToFmi(fmiPath[4])
                .abductiveModel(new AbductiveModel("src/main/java/runningExamples/SimpleRobot/Abductive/abductiveBookModel.txt"))
                .modelData(abModelData)
                .encoder(new BookAbEncoder())
                .numberOfSteps(20)
                .simulationStepSize(1)
                .build();

        //abductiveDriver.runSimulation();
    }
}
