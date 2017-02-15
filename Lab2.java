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
    static int windowSize = 2*padNum + 1;
   // static final int RAW_INPUT_LAYER_NUM = windowSize;
    static final int input_type_Nums = 21;
    static final int INPUT_LAYER_NUM = windowSize;
    static final int HIDDEN_LAYER_NUM = 7;
    static final int OUTPUT_LAYER_NUM = 3;
    static List<netPerceptron> input_layer;
    static List<netPerceptron> hidden_layer;
    static List<netPerceptron> output_layer;
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

        // build network
        // forward
        // raw_input layer
         input_layer  = new ArrayList<netPerceptron>();
        for (int i = 0 ; i < INPUT_LAYER_NUM ; i ++){
            netPerceptron p = new netPerceptron(1,null,input_type_Nums);
            input_layer.add(p);
        }

        hidden_layer  = new ArrayList<netPerceptron>();
        for(int i = 0 ; i < HIDDEN_LAYER_NUM; i ++){
            netPerceptron p = new netPerceptron(2,input_layer,null);
            hidden_layer.add(p);
        }

        for (netPerceptron perceptron : input_layer) {
            perceptron.nextLayer = hidden_layer;
        }
        for (netPerceptron perceptron : hidden_layer) {
            perceptron.prevLayer = input_layer;
        }

         output_layer  = new ArrayList<netPerceptron>();
        for (int i = 0; i < OUTPUT_LAYER_NUM; i++) {
            netPerceptron p = new netPerceptron(3,hidden_layer);
            output_layer.add(p);
        }

        for (netPerceptron perceptron : output_layer) {
            perceptron.prevLayer = hidden_layer;
        }
        for (netPerceptron perceptron: hidden_layer){
            perceptron.nextLayer = output_layer;
        }

        for (Strian strian : train) {
            List<dfvec>  raw_inputs = convertToOneHotFvec(strian);
            int max_batchNum = raw_inputs.size() - (windowSize - 1);
            int real_batchNum = Math.min(batchNum,max_batchNum);

            if(real_batchNum < batchNum ){
                System.out.println("batchNum greater than maximum possible number of updates in " +
                        "in the current strain, so make the batchNum as " +
                        " the size of total number of movings for the input window, which the maximum number of " +
                        "batchNum for each strain (protein sequence) can take ");
            }
       //     if(raw_inputs.isEmpty()) System.out.println("raw_inputs empty");
            List<List<dfvec>> batch = new ArrayList<>();
            int start = 0, end = windowSize - 1;

            while(end < raw_inputs.size()){
                if(start % real_batchNum == 0 && start != 0){
                update(batch);
                batch.clear();
                }

                List<dfvec> sample = raw_inputs.subList(start, end + 1);
                batch.add(sample);
                start++;
                end++;
            }

        }

    }

    private static void update(List<List<dfvec>> samples) {
        //TODO
       /*     System.out.println(raw_inputs);
        System.out.println("--------------");*/
        /*for (List<dfvec> sample : samples) {
            for (dfvec dfvec : sample) {
                System.out.println(dfvec);
            }
            System.out.println("------------------------------");
        }*/
        List<Character> labels  = Arrays.asList('_','e','h');
        // forward
        double[] errs  = new double[OUTPUT_LAYER_NUM];
        for (List<dfvec> sample : samples) {
           // input layer
            for(dfvec featureVec : sample){
                int i = sample.indexOf(featureVec);
                netPerceptron p = input_layer.get(i);
                p.CalculateWeightedSum(featureVec.features);
                // input layer: output = input
                p.output = p.netinput;
            }

            // hidden layer
            for (netPerceptron hp : hidden_layer) {
                for (netPerceptron child : hp.prevLayer) {
                  hp.netinput += child.output*hp.weights[hp.prevLayer.indexOf(child)];
                }
                hp.output = hp.ReLU(hp.netinput);
            }

            //output
            for (netPerceptron op : output_layer) {
                for (netPerceptron child : hidden_layer) {
                    op.netinput += child.output*op.weights[hidden_layer.indexOf(child)];
                }
                op.output = op.sigmoid(op.netinput);
            }

            // calculate and update the errs
            double[] teacher = new double[OUTPUT_LAYER_NUM];
            teacher[labels.indexOf(sample.get((windowSize + 1)/2).label)] = 1.0;
            for (int i = 0; i < errs.length; i++) {
                netPerceptron p = output_layer.get(i);
                 errs[i] += teacher[i] - p.output;
            }
        }

        for (int i = 0; i < errs.length; i++) {
            System.out.print(errs[i] + " ");
        }
        // backprop


        // clear input and output
        for (netPerceptron p : input_layer) {
            p.output = 0;
            p.netinput = 0;
        }
        for (netPerceptron p : hidden_layer) {
            p.output = 0;
            p.netinput = 0;
        }
        for (netPerceptron p : output_layer) {
            p.output = 0;
            p.netinput = 0;
        }

        System.out.println();
    }

    private static  List<dfvec>  convertToOneHotFvec(Strian sample) {
        List<dfvec> dfvecs = new ArrayList<dfvec>();
        //List<dfvec> updateset  = new ArrayList<dfvec>();

        //pad first padNum: 8

        dfvec solvent = new dfvec('0', 21, "S");
        for(int i  = 0 ; i < padNum; i++){
            dfvecs.add(solvent);
        }
             char label = ' ';
        for (pair p : sample.ps) {
            char acid_c = p.acid.charAt(0);

              /*  if (acid_c > 'U') {
                    System.out.println(acid_c);
                    System.exit(6);
                }*/
            dfvec curFeatureVector = new dfvec(acid_c, acidNum, p.label);
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
    double features[];
    char label;
    public dfvec(char input, int flen, String label) {
        //check if acid type exists
        int indexOfOne = 0 ;
        if(!dic.containsKey(input)){
            dic.put(input,index);
            index++;
        }

        indexOfOne = dic.get(input);
        features = new double[flen];
        features[indexOfOne] = 1.0;
        this.flen = flen;
        label = label.trim();
        if((label == null) || (label.length() != 1)){
            System.out.println("string label not a single char: " + label);
            System.exit(8);
        }
        this.label = label.charAt(0);
    }

    @Override
    public String toString() {
        return "dfvec{" +
                "flen=" + flen +
                ", features=" + Arrays.toString(features) +
                '}';
    }
}
class data {
    List<dfvec> input;
    char label;

    public data(List<dfvec> input, char label) {
        this.input = input;
        this.label = label;
    }
}

class netPerceptron{
    static double alpha = 0.01;
    double [] weights;
    List<netPerceptron> nextLayer;
    List<netPerceptron> prevLayer;
    int layerNum;
    double netinput;
    double output;
    double delta;
    // layerNum 1: raw_input layer's perceptron constructor: without prevLayer
    public netPerceptron(int layerNum, List<netPerceptron> nextLayer, int input_Num) {
        this.layerNum = layerNum;
        if(layerNum == 1){
            this.nextLayer = nextLayer;
        }
        else{
            System.out.println("layerNum number error, 1: input 2: hidden(ReLu) 3: output(sigmoid)");
            System.out.println("expected layerNum: 1, + actual layerNum" + layerNum);
            System.exit(7);
        }

        // initialize weights
        weights = new double[input_Num];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = 1;
        }

        // generate random weight value for bias
      /*  Random r = new Random();
        weights[input.features.length] = r.nextDouble();*/

    }

    // layerNum 4: output(sigmoid as activation function) layer's perception constructor: without nextLayer
    public netPerceptron(int layerNum, List<netPerceptron> prevLayer) {
        // generate using random Generators
        if(layerNum == 3){
            this.prevLayer = prevLayer;
        }
        else{
            System.out.println("layerNum number error, 1：input 2: hidden(ReLu) 3: output(sigmoid)");
            System.out.println("expected layerNum: 3, + actual layerNum" + layerNum);
            System.exit(7);
        }

        // initialize weights
        Random r = new Random();
        weights = new double[prevLayer.size() + 1];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = r.nextDouble();
        }
    }

    public netPerceptron(int layerNum, List<netPerceptron> prevLayer, List<netPerceptron> nextLayer) {
        if((layerNum == 2) ){
            this.prevLayer = prevLayer;
            this.nextLayer = nextLayer;
        }
        else{
            System.out.println("layerNum number error, 1：input 2: hidden(ReLu) 3: output(sigmoid)");
            System.out.println("expected layerNum: 2, + actual layerNum" + layerNum);
            System.exit(7);
        }

        this.layerNum = layerNum;
        // initialize weights : forward
        Random r = new Random();
        weights = new double[prevLayer.size() + 1];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = r.nextDouble();
        }

    }

    public void addnextLayers(List<netPerceptron> nextLayer){
        this.nextLayer = nextLayer;
    }

    public void prevLayer(List<netPerceptron> prevLayer){
        this.prevLayer = prevLayer;
    }

    public void CalculateWeightedSum(double[] input){
        if(input.length != weights.length){
            System.out.println("input and weights size does not match");
            System.out.println("input: " + input.length + "weight: " + weights.length);
            System.exit(8);
        }
        double sum = 0;
        for (int i = 0; i < input.length; i++) {
            sum += input[i] * weights[i];
        }

        // bias weight
       /* sum+= (-1)*weights[input.length ];*/
        netinput = sum;

        // return sum;
    }

    public void updateweights (double[] input, double[] delta){

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
