package weka1;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;

import java.util.Random;
import java.util.Arrays;

public class Newcross {
    public static void main(String[] args) throws Exception {
        // Load Dataset
        DataSource source = new DataSource("C:\\Users\\hella\\Desktop\\results\\avro_result.arff"); // Replace with your dataset path
        Instances data = source.getDataSet();
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }
        int trainSize = (int) Math.round(data.numInstances() * 0.8);
        int testSize = data.numInstances() - trainSize;
        Instances trainingData = new Instances(data, 0, trainSize);
        //Instances testingData = new Instances(data, trainSize, testSize); // Create a testing set

        J48 classifier = new J48(); // Replace with your classifier setup
        classifier.buildClassifier(trainingData);

        int nEpochs = 200;
        int nIterations = 10; // Number of iterations
        int kFolds = 10;      // Number of folds

        // Initialize arrays to store cross-validation results
        double[][] cvScores = new double[nIterations][kFolds];
        double[] precisionScores = new double[nIterations];
        
        double[] f1Scores = new double[nIterations];
        double[] mccScores = new double[nIterations];
        double[] aucScores = new double[nIterations];
        double[] gMeanScores = new double[nIterations];
        double[] stdScores = new double[nIterations];
        
        double[] epochAverageMeanScores = new double[nEpochs]; // Array to store average mean scores for each epoch
        double[] epochAverageStdScores = new double[nEpochs]; // Array to store average standard deviation scores for each epoch

        for (int epoch = 1; epoch <= nEpochs; epoch++) {
            System.out.println("Epoch " + epoch + ":");

            for (int i = 0; i < nIterations; i++) {
                // Create an Evaluation object
                Evaluation eval = new Evaluation(trainingData);

                // Perform cross-validation
                eval.crossValidateModel(classifier, trainingData, kFolds, new Random());
                precisionScores[i] = eval.weightedPrecision();
                f1Scores[i] = eval.weightedFMeasure();
                mccScores[i] = eval.weightedMatthewsCorrelation();
                aucScores[i] = eval.weightedAreaUnderROC();
                gMeanScores[i] = Math.sqrt(eval.weightedTruePositiveRate() * eval.weightedTrueNegativeRate());

                // Store the scores
                for (int j = 0; j < kFolds; j++) {
                    cvScores[i][j] = eval.pctCorrect();
                }
            }
        

            // Calculate and print the mean of the scores for this epoch
            double sumOfMeanScores = 0;
            double sumOfStdScores = 0;
            for (int i = 0; i < nIterations; i++) {
            	System.out.println("Iteration " + (i + 1) + ":");
                double sum = 0;
                for (int j = 0; j < kFolds; j++) {
                	System.out.println("Fold " + (j + 1) + ": " + (cvScores[i][j]/100));
                    sum += cvScores[i][j];
                }
                System.out.println();
            
                double meanScore = (sum / kFolds) / 100;
                sumOfMeanScores += meanScore;
                double squaredDiffSum = 0;
                for (int j = 0; j < kFolds; j++) {
                    squaredDiffSum += Math.pow(cvScores[i][j] - meanScore, 2);
            }
                stdScores[i] = (Math.sqrt(squaredDiffSum / (kFolds - 1))) / 100;
                sumOfStdScores += stdScores[i];
            }
            
            double epochAverageMeanScore = sumOfMeanScores / nIterations;
            epochAverageMeanScores[epoch - 1] = epochAverageMeanScore;
            double epochAverageStdScore = sumOfStdScores / nIterations;
            epochAverageStdScores[epoch - 1] = epochAverageStdScore;
            System.out.println("Average Mean Score for Epoch " + epoch + ": " + epochAverageMeanScore);
            System.out.println("Average Std Score for Epoch " + epoch + ": " + epochAverageStdScore);
        }

        // Calculate and print the overall average of average mean scores across all epochs
        double sumOfAllEpochAverageMeanScores = 0;
        double sumOfAllEpochAverageStdScores = 0;
        for (int i = 0; i < nEpochs; i++) {
            sumOfAllEpochAverageMeanScores += epochAverageMeanScores[i];
            sumOfAllEpochAverageStdScores += epochAverageStdScores[i];
        }
        double overallAverageMeanScore = sumOfAllEpochAverageMeanScores / nEpochs;
        double overallAverageStdScore = sumOfAllEpochAverageStdScores / nEpochs;
        System.out.println("Overall Average Mean Score: " + overallAverageMeanScore);
        System.out.println("Overall Average Std Score: " + overallAverageStdScore);
    }
}
