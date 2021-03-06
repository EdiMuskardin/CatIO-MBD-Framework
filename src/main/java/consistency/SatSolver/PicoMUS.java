package consistency.SatSolver;

import consistency.CbModel;
import lombok.Data;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class PicoMUS {
    private String filename;
    BufferedWriter fr;
    CbModel cbModel;
    File file;

    private void addProblemLine(int numClauses, int numVars) throws IOException {
        fr.write("p cnf " + numVars + " " +  numClauses);
        fr.newLine();
    }

    private void addClause(List<Integer> clause) throws IOException {
        StringBuilder sb = new StringBuilder();
        for(Integer i : clause)
            sb.append(i).append(" ");
        sb.append(0);
        fr.write(sb.toString());
        fr.newLine();
    }

    public PicoMUS(String pathToFile) throws IOException {
        filename = pathToFile;
        file = new File(filename);
        fr = new BufferedWriter(new FileWriter(file));
    }

    /**
     * Writes to file in DIMCAS format used in SAT solvers
     * @param model Consistency based model
     * @param obs Observations mapped to integers with respect to the propositions found in model
     * @throws IOException
     */
    public void writeModelAndObsToFile(CbModel model, List<Integer> obs) throws IOException {
        this.cbModel = model;
        addProblemLine(model.getWorkingModel().size() + obs.size(), model.getNumOfDistinct());
        for(List<Integer> clause : model.getWorkingModel())
            addClause(clause);
        for(Integer ob: obs)
            addClause(Collections.singletonList(ob));
        fr.close();
    }

    /**
     * @return List of integers (which correspond to propositions) which forms minimal unsatisfiable core
     * @throws IOException
     */
    public List<Integer> getMUS() throws IOException {
        List<Integer> mhs = new ArrayList<>();
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"lib/picomus", filename};
        Process proc = rt.exec(commands);

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        String s;
        while ((s = stdInput.readLine()) != null) {
            String[] line = s.split(" ");
            if(line[0].equals("c") || line[1] == null)
                continue;
            if(line[0].equals("s") && !line[1].equals("UNSATISFIABLE")) {
                file.deleteOnExit();
                return mhs;
            }
            if (line[1].equals("UNSATISFIABLE"))
                continue;
            if(line[1].equals("maximum"))
                System.err.println("Model contains more variables than declared, check if model and encoder use same " +
                        "propositional variables!");

            int var = Integer.parseInt(line[1]);
            if(var > cbModel.getWorkingModel().size() || var == 0)
                continue;
            if(cbModel.isHealthStatePredicate(cbModel.getWorkingModel().get(var-1).get(0)) && cbModel.getWorkingModel().get(var - 1).size() == 1)
                mhs.add(var-1);
        }
        file.deleteOnExit();
        return mhs;
    }

}
