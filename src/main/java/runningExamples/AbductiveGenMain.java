package runningExamples;

import abductive.AbductiveModel;
import abductive.AbductiveModelGenerator;
import examples.SingleBulbDiff;
import model.Component;
import model.Type;
import model.ModelData;
import model.ModelInput;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AbductiveGenMain {
    public static void main(String[] args) throws IOException {

        String pathToFmi = "FMIs/SC_Example1.fmu";
        List<Object> bateryFaultTypes = Arrays.asList("ok", "empty");
        List<Object> componentFaultTypes = Arrays.asList("ok", "short", "broken");
        ModelData modelData = new ModelData();
        modelData.setComponentsToRead(
                Collections.singletonList(
                        new Component("b1.on", Type.BOOLEAN)
                )
        );
        modelData.setHealthStates(
                Arrays.asList(
                        new ModelInput("r1State", Type.ENUM, bateryFaultTypes),
                        new ModelInput("bat1State", Type.ENUM, componentFaultTypes),
                        new ModelInput("b1State", Type.ENUM, componentFaultTypes),
                        new ModelInput("rLoadState", Type.ENUM, componentFaultTypes),
                        new ModelInput("rIntState", Type.ENUM, componentFaultTypes)
                ));

        AbductiveModelGenerator abductiveModelGenerator = new AbductiveModelGenerator(pathToFmi, modelData, new SingleBulbDiff());
        AbductiveModel ab = abductiveModelGenerator.generateModel(20.0, 1.0);
        ab.addExplain(Collections.singletonList("noLight"));
        System.out.println(ab.getDiagnosis());
        abductiveModelGenerator.writeModeltoFile("autModel.txt");

    }
}