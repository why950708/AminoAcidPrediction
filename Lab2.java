import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Zirui Tao on 2/12/2017.
 */
public class Lab2 {
    static int batchNum = 1;
    static int acidNum = 21;
    static int padNum = 8;

    public static void main(String[] args) throws IOException {
        String train_FileName = args[0];
        //String tune_FileName = args[1];
        List<Strian> train = ParseFile(train_FileName);
        List<Strian> removeList= new ArrayList<>();
        List<Strian>tune  = new ArrayList<Strian>();
        List<Strian>test  = new ArrayList<Strian>();

        // split index into train , tune and test set
        for (int i = 5; i < train.size(); i++) {
            if(i % 5 ==0) {
                tune.add(train.get(i));
                removeList.add(train.get(i));
                test.add(train.get(i + 1));
                removeList.add(train.get(i));
            }
        }
        // remove overlaps
        for (Strian strian : removeList) {
            train.remove(strian);
        }


        // List<Strian> tune = ParseFile(tune_FileName);
        //TODO
        // List<dfvec>  raw_inputs = new ArrayList<>();
        for (Strian strian : train) {
            List<dfvec>  raw_inputs = convertToOneHotFvec(strian);

       //     if(raw_inputs.isEmpty()) System.out.println("raw_inputs empty");
            int count = 0;
            while(count + batchNum < raw_inputs.size()){

                List<dfvec> batch  = raw_inputs.subList(count, count + batchNum);
                update(batch);

                count += batchNum;
            }
            if(batchNum > raw_inputs.size()){
                /* List<dfvec>batch  = raw_inputs.subList(count, raw_inputs.size() - 1);
                update(batch);
                count = 0;
                raw_inputs.clear();
                raw_inputs.addAll(convertToOneHotFvec(strian));*/
                System.out.println("batchNum greater than input sizes, so make the full list as a batch");
                break;
            }
            List<dfvec> rest = raw_inputs.subList(count, raw_inputs.size());
            // if(rest.isEmpty()) System.out.println("rest empty: count: " + count + "raw_input_size" + raw_inputs.size());
            update(rest);
        }

    }

    private static void update(List<dfvec> raw_inputs) {
        //TODO
       /*     System.out.println(raw_inputs);
        System.out.println("--------------");*/


    }

    private static  List<dfvec> convertToOneHotFvec(Strian sample) {
        List<dfvec> dfvecs = new ArrayList<dfvec>();
        //List<dfvec> updateset  = new ArrayList<dfvec>();

        //pad first padNum: 8

        dfvec solvent = new dfvec('0', 21);
        for(int i  = 0 ; i < padNum; i++){
            dfvecs.add(solvent);
        }

        for (pair p : sample.ps) {
            char acid_c = p.acid.charAt(0);
              /*  if (acid_c > 'U') {
                    System.out.println(acid_c);
                    System.exit(6);
                }*/
            dfvec curFeatureVector = new dfvec(acid_c, acidNum);
            dfvecs.add(curFeatureVector);
        }

        // pad last padNum: 8
        for(int i  = 0 ; i < padNum; i++){
            dfvecs.add(solvent);
        }
        return dfvecs;
    }

    private static List<Strian> ParseFile(String fileName) throws IOException {
        if(fileName == null || fileName.equals("")){
            System.out.println("file name is null or empty");
            System.exit(10);
        }

        String [] lines =  null;
        // Files.lines(Paths.get(fileName), StandardCharsets.UTF_8);
        List<Strian> samples = new ArrayList<Strian>();
        try{
            Stream <String> s = Files.lines(Paths.get(fileName));
            // convert into String arrays
            lines = s.toArray(size -> new String[size]);
            List<String> l = Arrays.asList(lines);
            samples = extractStrains(l);

            // System.out.println(samples);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return samples;
    }

    private static List<Strian> extractStrains(List<String> raw_input) {
        // split according to "<>"
        List<Strian> samples = new ArrayList<>();
        // pair p = null;
        List acids = new ArrayList<String>();
        List labels = new ArrayList<String>();
        for (String s : raw_input) {
            if(s.trim().startsWith("#") || s.equals("")){
                continue;
            }
            if(s.trim().equals("<>")){
                // crate new pair by clearing the previous elements in acids
                //System.out.println("<>" );
                if(!acids.isEmpty() && !labels.isEmpty()){
                    Strian cursample = new Strian(acids, labels);
                    samples.add(cursample);
                    acids.clear();
                    labels.clear();
                }

            }
            else if(s.trim().equals("end") || s.trim().equals("<end>")){
                // add created one
                continue;
                /*Strian cursample = new Strian(acids, labels);
                samples.add(cursample);
                acids.clear();
                labels.clear();*/
                //System.out.println("<end>" );
            }
            else{
                String[] fields = s.trim().split(" ");
                if(fields.length!=2){
                    System.out.println("splied line size is not 2 (acid + label)");
                    System.exit(2);
                }
                if(fields[0].equals("")) {
                    System.out.println("fields[0] = \"\",missing acid letter in this line ");
                    System.exit(3);
                }
                if(fields[1].equals("")) {
                    System.out.println("fields[1] = \"\",missing label in this line ");
                    System.exit(4);
                }

                acids.add(fields[0]);
                labels.add(fields[1]);
                //System.out.println(fields[0] + " " + fields[1]);
            }

        }
        return samples;
    }
}

class Strian {
    static int ID = 1;
    List<pair> ps;
    public Strian(List<String> acids, List<String> labels) {
        ps = new ArrayList<pair>();
        if(acids.size()!= labels.size()){
            System.out.println("acids list and labels are in different size, "
                    + "acids.size(): " + acids.size() + "labels.size(): " + labels.size()
            );
            System.exit(1);
        }

        for (int i = 0; i < acids.size(); i++) {
            pair p = new pair (acids.get(i), labels.get(i));
            ps.add(p);
        }
        ID ++;

    }

    public Strian(List<pair> ps) {
        this.ps = ps;
    }

    @Override
    public String toString() {
        String out = "ID: " + ID;
        for (pair p : ps) {
            out += p.toString();
        }
        return out;
    }
}

class pair{
    // static int count = 10;
    String acid;
    String label;

    public pair(String acid, String label) {
        this.acid = acid;
        this.label = label;
    }

    @Override
    public String toString() {
        return  acid + " " + label + " " + "\n";
    }
}

class dfvec{
    static HashMap<Character, Integer> dic = new HashMap<>();
    static int index = 0;
    int flen;
    int features[];
    public dfvec(char input, int flen) {
        //check if acid type exists
        int indexOfOne = 0 ;
        if(!dic.containsKey(input)){
            dic.put(input,index);
            index++;
        }

        indexOfOne = dic.get(input);
        features = new int[flen];
        features[indexOfOne] = 1;
        this.flen = flen;
    }

    @Override
    public String toString() {
        return "dfvec{" +
                "flen=" + flen +
                ", features=" + Arrays.toString(features) +
                '}';
    }
}

class netPerceptron{
    static double alpha = 0.01;
    double [] weights;
    List<netPerceptron> nextLayer;
    List<netPerceptron> prevLayer;
    int mode;

    // mode 1: raw_input layer's perceptron constructor: without prevLayer
    public netPerceptron(int mode, List<netPerceptron> nextLayer, dfvec input) {
        this.mode = mode;
        if(mode == 1){
            this.nextLayer = nextLayer;
        }
        else{
            System.out.println("mode number error, 1: raw_input 2: input 3: hidden(ReLu) 4: output(sigmoid)");
            System.out.println("expected mode: 1, + actual mode" + mode);
            System.exit(7);
        }

        // initialize weights

        weights = new double[input.features.length + 1];
        for (int i = 0; i < input.features.length; i++) {
            weights[i] = input.features[i];
        }

        // generate random weight value for bias
        Random r = new Random();
        weights[input.features.length] = r.nextDouble();

    }

    // mode 4: output(sigmoid as activation function) layer's perception constructor: without nextLayer
    public netPerceptron(int mode, List<netPerceptron> prevLayer) {
        // generate using random Generators

        if(mode == 4){
            this.prevLayer = prevLayer;
        }
        else{
            System.out.println("mode number error, 1: raw_input 2: input 3: hidden(ReLu) 4: output(sigmoid)");
            System.out.println("expected mode: 4, + actual mode" + mode);
            System.exit(7);
        }


        // initialize weights
        int prevPerceptronNum = prevLayer.size();
        weights = new double[prevPerceptronNum + 1];
        Random rand = new Random();
        for (int i = 0; i < weights.length; i++) {
            weights[i] = rand.nextDouble();
        }
    }

    public netPerceptron( int mode, List<netPerceptron> prevLayer, List<netPerceptron> nextLayer ) {
        if((mode == 2) || (mode == 3)){
            this.prevLayer = prevLayer;
            this.nextLayer = nextLayer;
        }
        else{
            System.out.println("mode number error, 1: raw_input 2: input 3: hidden(ReLu) 4: output(sigmoid)");
            System.out.println("expected mode: 2 or 3, + actual mode" + mode);
            System.exit(7);
        }

        this.mode = mode;
    }



    public void addnextLayers(List<netPerceptron> nextLayer){
        this.nextLayer = nextLayer;
    }

    public void prevLayer(List<netPerceptron> prevLayer){
        this.prevLayer = prevLayer;
    }

    public double CalculateWeightedSum(double[] input){
        if(input.length != weights.length - 1){
            System.out.println("input and weights size does not match");
            System.out.println("input: " + input.length + "weight: " + weights.length);
            System.exit(8);
        }
        double sum = 0;
        for (int i = 0; i < input.length; i++) {
            sum += input[i] * weights[i];
        }

        // bias weight
        sum+= (-1)*weights[input.length];
        return sum;
    }

    public void updateweights (double[] input, double delta){

        //TODO
    }
    public double sigmoid (double x){
        return 1/(1 + (Math.exp(-x)));
    }
    public double sigmoidP (double x){
        return x*(1-x);
    }
    public double ReLU (double x){

        return Math.max(0,x);
    }
    public double ReLUP(double x){
        // TODO
        return x > 0 ? 1: 0;
    }

}

