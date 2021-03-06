package consistency;

import FmiConnector.FmiConnector;
import interfaces.Controller;
import model.Component;
import model.ModelData;
import consistency.mhsAlgs.RcTree;
import interfaces.Encoder;
import lombok.Builder;
import lombok.Data;
import model.Scenario;
import org.apache.commons.lang3.Pair;
import org.javafmi.wrapper.Simulation;
import util.Util;

import java.util.*;

@Data
@Builder
public class ConsistencyDriver {
    String pathToFmi;
    ModelData modelData;
    double simulationStepSize;
    Integer numberOfSteps;
    CbModel model;
    Encoder encoder;

    /**
     * Diagnosis algorithm will be executed after every time step, and diagnosis printed to standard output.
     */
    public void runDiagnosis(ConsistencyType type, Scenario scenario){
        FmiConnector fmiConnector = new FmiConnector(pathToFmi);

        ArrayList<Double> xPlot = new ArrayList<>();
        ArrayList<Double> yPlot = new ArrayList<>();
        Pair<Component, Component> plotData = modelData.getPlotData();
        Simulation simulation = fmiConnector.getSimulation();
        Integer currStep = 0;

        Controller controller = modelData.getController();

        simulation.init(0.0);
        if(type == ConsistencyType.STEP){
            model.setNumOfDistinct(model.getPredicates().getSize());
            int repairActionLen;
            while (currStep < numberOfSteps) {
                // If there is scenario and step injection is defined at current step, it will be injected at this point
                if (scenario != null)
                    scenario.injectFault(currStep, fmiConnector);

                // Save data which is going to be plotted, if plot variables are defined
                if (plotData != null) {
                    xPlot.add((Double) fmiConnector.read(plotData.left).getValue());
                    yPlot.add((Double) fmiConnector.read(plotData.right).getValue());
                }

                // Read data and encode it
                Set<String> obs = encoder.encodeObservation(fmiConnector.readMultiple(modelData.getComponentsToRead()));
                // Run diagnosis
                RcTree rcTree = new RcTree(model, model.observationToInt(obs));

                // For each step print diagnosis
                List<List<String>> diag = new ArrayList<>();
                for (List<Integer> mhs : rcTree.getDiagnosis())
                    diag.add(model.diagnosisToComponentNames(mhs));
                System.out.println("Step " + currStep + " : " + diag);

                // do step
                simulation.doStep(simulationStepSize);
                currStep++;

                // If controller is defined, perform action
                if(controller != null && !diag.get(0).isEmpty()){
                    repairActionLen = controller.performAction(fmiConnector, diag);
                    simulation.doStep(simulationStepSize);
                    while(repairActionLen >= 0){
                        if (plotData != null) {
                            xPlot.add((Double) fmiConnector.read(plotData.left).getValue());
                            yPlot.add((Double) fmiConnector.read(plotData.right).getValue());
                        }
                        // Save data which is going to be plotted, if plot variables are defined
                        repairActionLen = controller.performAction(fmiConnector, diag);
                        simulation.doStep(simulationStepSize);

                    }
                }
            }
            }else if(type == ConsistencyType.PERSISTENT || type == ConsistencyType.INTERMITTENT){
            Set<Integer> originalAbPred = new HashSet<>(model.getAbPredicates());
            int obsCounter = 0;
            boolean increaseHs = (type == ConsistencyType.INTERMITTENT);
            int offset = increaseHs ? model.getPredicates().getSize() : (model.getPredicates().getSize() - model.getAbPredicates().size());
            List<Integer> observations = new ArrayList<>();

            while(currStep < numberOfSteps){
                if(scenario != null)
                    scenario.injectFault(currStep , fmiConnector);

                if(modelData.getPlotData() != null){
                    xPlot.add((Double) fmiConnector.read(modelData.getPlotData().left).getValue());
                    yPlot.add((Double) fmiConnector.read(modelData.getPlotData().right).getValue());
                }

                Set<String> obs = encoder.encodeObservation(fmiConnector.readMultiple(modelData.getComponentsToRead()));
                obsCounter = obs.size();
                List<Integer> encodedObs = model.observationToInt(obs);
                observations.addAll(increaseObservation(encodedObs, currStep, offset));
                model.increaseByOffset(increaseHs, currStep);
                simulation.doStep(simulationStepSize);
                currStep++;
            }

            if(increaseHs)
                model.setNumOfDistinct(offset * currStep);
            else
                model.setNumOfDistinct(model.getAbPredicates().size() + ((offset ) * currStep));


            RcTree rcTree = new RcTree(model, observations);
            List<List<Integer>> mhs = rcTree.getDiagnosis();
            for (List<Integer> hs : mhs)
                System.out.println(model.getComponentNamesTimed(hs, type, obsCounter));


            // abPredicates were updated, so revert them back to original to enable reuse
            model.setAbPredicates(originalAbPred);
        }

        // if plot values are specified
        if(modelData.getPlotData() != null)
            Util.plot(xPlot, yPlot, "plot");

        // for reuse, clear and reset members of this class
        model.clearModel();
        fmiConnector.resetSimulation();
    }

    public void runDiagnosis(ConsistencyType type){
        runDiagnosis(type, null);
    }

    private List<Integer> increaseObservation(List<Integer> obs, int currStep, int offset){
        for (int i = 0; i < obs.size(); i++) {
            Integer increasedOb = obs.get(i) > 0 ? obs.get(i)  + (offset * currStep) : obs.get(i) - (offset * currStep);
            obs.set(i, increasedOb);
        }
        return obs;
    }
}
