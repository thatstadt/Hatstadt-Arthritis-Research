import java.io.*;
import java.util.*;

public class Algo {

    private static Random rng = new Random(67); //For reproducibility
    private static ArrayList<Patient> patients = new ArrayList<>();
    private static ArrayList<Patient> ageGroup1 = new ArrayList<>(); //Age <25
    private static ArrayList<Patient> ageGroup2 = new ArrayList<>(); //Age 25-54
    private static ArrayList<Patient> ageGroup3 = new ArrayList<>(); //Age 55+
    private static int n = 1_000_000; //Number of patients to simulate
    @SuppressWarnings("unused")
    private static double decayRate = -0.065; //Decay rate for BMI OR with age

    public static void main(String[] args) throws Exception {
        //generatePatients(27, "Dataset.csv", decayRate);
        readPatients("Dataset5.csv");
        simulate("Results5.csv");
    }   
    
    //Generate synthetic patients
    public static void generatePatients(int mean, String filename, double k) throws Exception {
        /*
        Works on 1,000,000 patients in ~2 minutes
        Works on 250,000 patients in ~30 seconds
        Works on 100,000 patients in ~12 seconds
        */

        //Generate patients
        for (int i = 0; i < n; i++) {
            Patient p = new Patient(i, mean);
            p.setORS();
            double z = linearPredictor(p, k);
            double risk = sigmoid(z);
            p.riskAssessment = risk;
            patients.add(p);
        }

        //Determine condition status based on risk assessment
        for (Patient p : patients) {
            double random = rng.nextDouble();
            if (random < p.riskAssessment) {
                p.hasRA = 1;
            } else {
                p.hasRA = 0;
            }
        }

        exportPatients(patients, filename);
    }

    public static void readPatients(String filename) throws Exception {
        FileReader fr = new FileReader(new File(filename));
        Scanner sc = new Scanner(fr);
        sc.nextLine(); //Skip header

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] parts = line.split(",");
            Patient p = new Patient(0, 27); //Dummy patient
            p.id = parts[0];
            p.age = Integer.parseInt(parts[1]);
            p.bmiRaw = Double.parseDouble(parts[2]);
            p.riskAssessment = Double.parseDouble(parts[3]);
            p.hasRA = Integer.parseInt(parts[4]);
            patients.add(p);
        }
        sc.close();
        n = patients.size();

        //Categorize patients into age groups
        for (Patient p : patients) {
            if (p.age < 25) {
                ageGroup1.add(p);
            } else if (p.age >= 25 && p.age < 55) {
                ageGroup2.add(p);
            } else {
                ageGroup3.add(p);
            }
        }
    }

    public static void simulate(String filename) throws Exception {

        int trials = 2_500; //Number of trials for prevalence calculation
        double[] riskResults = new double[trials];
        double[] prevalenceResults = new double[trials];
        double[] age1PrevalenceResults = new double[trials];
        double[] age2PrevalenceResults = new double[trials];
        double[] age3PrevalenceResults = new double[trials];
        double[] bmiResults = new double[trials];
        double[] bmi1Results = new double[trials];
        double[] bmi2Results = new double[trials];
        double[] bmi3Results = new double[trials];
        double[] obesityResults = new double[trials];
        double[] obesity1Results = new double[trials];
        double[] obesity2Results = new double[trials];
        double[] obesity3Results = new double[trials];

        for (int t = 0; t < trials; t++) {
            //Calculate prevalence
            int count = 0; //Counter for patients with the condition
            double avgBmi = 0.0;
            int obesity = 0;
            int countAge1 = 0;
            int age1 = 0;
            double avgBmi1 = 0.0;
            int obesity1 = 0;
            int countAge2 = 0;
            int age2 = 0;
            double avgBmi2 = 0.0;
            int obesity2 = 0;
            int countAge3 = 0;
            int age3 = 0;
            double avgBmi3 = 0.0;
            int obesity3 = 0;
            double averageRisk = 0.0;

            ArrayList<Patient> trialPatients = new ArrayList<>();

            //Bootstrap resampling
            for (int i = 0; i < n; i++) {
                trialPatients.add(patients.get(rng.nextInt(n)));
            }

            //Calculate risk and prevalence in the trial sample
            for (Patient p : trialPatients) {
                averageRisk += p.riskAssessment;
                if (p.hasRA == 1) {
                    count++;
                    avgBmi += p.bmiRaw;
                    if (p.bmiRaw >= 30.0) obesity++;
                    if (p.age < 25) {
                        countAge1++;
                        avgBmi1 += p.bmiRaw;
                        if (p.bmiRaw >= 30.0) obesity1++;
                    } else if (p.age < 55) {
                        countAge2++;
                        avgBmi2 += p.bmiRaw;
                        if (p.bmiRaw >= 30.0) obesity2++;
                    } else {
                        countAge3++;
                        avgBmi3 += p.bmiRaw;
                        if (p.bmiRaw >= 30.0) obesity3++;
                    }
                }

                //Count total in age groups
                if (p.age < 25) {
                    age1++;
                } else if (p.age < 55) {
                    age2++;
                } else {
                    age3++;
                }
            }
            riskResults[t] = averageRisk / (double) n;
            prevalenceResults[t] = (double) count / (double) n;
            age1PrevalenceResults[t] = (double) countAge1 / (double) age1;
            age2PrevalenceResults[t] = (double) countAge2 / (double) age2;
            age3PrevalenceResults[t] = (double) countAge3 / (double) age3;
            bmiResults[t] = (count > 0) ? (avgBmi / (double) count) : 0.0;
            bmi1Results[t] = (countAge1 > 0) ? (avgBmi1 / (double) countAge1) : 0.0;
            bmi2Results[t] = (countAge2 > 0) ? (avgBmi2 / (double) countAge2) : 0.0;
            bmi3Results[t] = (countAge3 > 0) ? (avgBmi3 / (double) countAge3) : 0.0;
            obesityResults[t] = (count > 0) ? ((double) obesity / (double) count) : 0.0;
            obesity1Results[t] = (countAge1 > 0) ? ((double) obesity1 / (double) countAge1) : 0.0;
            obesity2Results[t] = (countAge2 > 0) ? ((double) obesity2 / (double) countAge2) : 0.0;
            obesity3Results[t] = (countAge3 > 0) ? ((double) obesity3 / (double) countAge3) : 0.0;
        }

        //Calculate medians
        Arrays.sort(riskResults);
        Arrays.sort(prevalenceResults);
        Arrays.sort(age1PrevalenceResults);
        Arrays.sort(age2PrevalenceResults);
        Arrays.sort(age3PrevalenceResults);
        Arrays.sort(bmiResults);
        Arrays.sort(bmi1Results);
        Arrays.sort(bmi2Results);
        Arrays.sort(bmi3Results);
        Arrays.sort(obesityResults);
        Arrays.sort(obesity1Results);
        Arrays.sort(obesity2Results);
        Arrays.sort(obesity3Results);

        //Output results
        double risk = (riskResults[trials / 2] + riskResults[trials / 2 + 1]) / 2.0;
        double prevalence = (prevalenceResults[trials / 2] + prevalenceResults[trials / 2 + 1]) / 2.0;
        double prevalenceAge1 = (age1PrevalenceResults[trials / 2] + age1PrevalenceResults[trials / 2 + 1]) / 2.0;
        double prevalenceAge2 = (age2PrevalenceResults[trials / 2] + age2PrevalenceResults[trials / 2 + 1]) / 2.0;
        double prevalenceAge3 = (age3PrevalenceResults[trials / 2] + age3PrevalenceResults[trials / 2 + 1]) / 2.0;
        double avgBmi = (bmiResults[trials / 2] + bmiResults[trials / 2 + 1]) / 2.0;
        double avgBmi1 = (bmi1Results[trials / 2] + bmi1Results[trials / 2 + 1]) / 2.0;
        double avgBmi2 = (bmi2Results[trials / 2] + bmi2Results[trials / 2 + 1]) / 2.0;
        double avgBmi3 = (bmi3Results[trials / 2] + bmi3Results[trials / 2 + 1]) / 2.0;
        double avgObesity = (obesityResults[trials / 2] + obesityResults[trials / 2 + 1]) / 2.0;
        double avgObesity1 = (obesity1Results[trials / 2] + obesity1Results[trials / 2 + 1]) / 2.0;
        double avgObesity2 = (obesity2Results[trials / 2] + obesity2Results[trials / 2 + 1]) / 2.0;
        double avgObesity3 = (obesity3Results[trials / 2] + obesity3Results[trials / 2 + 1]) / 2.0;

        //Bootstrap confidence intervals
        double ciLowerRisk = riskResults[(int)(trials * 0.025)];
        double ciUpperRisk = riskResults[(int)(trials * 0.975)];
        double ciLower = prevalenceResults[(int)(trials * 0.025)];
        double ciUpper = prevalenceResults[(int)(trials * 0.975)];
        double ciLowerAge1 = age1PrevalenceResults[(int)(trials * 0.025)];
        double ciUpperAge1 = age1PrevalenceResults[(int)(trials * 0.975)];
        double ciLowerAge2 = age2PrevalenceResults[(int)(trials * 0.025)];
        double ciUpperAge2 = age2PrevalenceResults[(int)(trials * 0.975)];
        double ciLowerAge3 = age3PrevalenceResults[(int)(trials * 0.025)];
        double ciUpperAge3 = age3PrevalenceResults[(int)(trials * 0.975)];
        double ciLowerBmi = bmiResults[(int)(trials * 0.025)];
        double ciUpperBmi = bmiResults[(int)(trials * 0.975)];
        double ciLowerBmi1 = bmi1Results[(int)(trials * 0.025)];
        double ciUpperBmi1 = bmi1Results[(int)(trials * 0.975)];
        double ciLowerBmi2 = bmi2Results[(int)(trials * 0.025)];
        double ciUpperBmi2 = bmi2Results[(int)(trials * 0.975)];
        double ciLowerBmi3 = bmi3Results[(int)(trials * 0.025)];
        double ciUpperBmi3 = bmi3Results[(int)(trials * 0.975)];
        double ciLowerObesity = obesityResults[(int)(trials * 0.025)];
        double ciUpperObesity = obesityResults[(int)(trials * 0.975)];
        double ciLowerObesity1 = obesity1Results[(int)(trials * 0.025)];
        double ciUpperObesity1 = obesity1Results[(int)(trials * 0.975)];
        double ciLowerObesity2 = obesity2Results[(int)(trials * 0.025)];
        double ciUpperObesity2 = obesity2Results[(int)(trials * 0.975)];
        double ciLowerObesity3 = obesity3Results[(int)(trials * 0.025)];
        double ciUpperObesity3 = obesity3Results[(int)(trials * 0.975)];

        //Export Setup
        double[][] exportData = new double[13][3];
        exportData[0][0] = risk;
        exportData[0][1] = ciLowerRisk;
        exportData[0][2] = ciUpperRisk;
        exportData[1][0] = prevalence;
        exportData[1][1] = ciLower;
        exportData[1][2] = ciUpper;
        exportData[2][0] = prevalenceAge1;
        exportData[2][1] = ciLowerAge1;
        exportData[2][2] = ciUpperAge1;
        exportData[3][0] = prevalenceAge2;
        exportData[3][1] = ciLowerAge2;
        exportData[3][2] = ciUpperAge2;
        exportData[4][0] = prevalenceAge3;
        exportData[4][1] = ciLowerAge3;
        exportData[4][2] = ciUpperAge3;
        exportData[5][0] = avgBmi;
        exportData[5][1] = ciLowerBmi;
        exportData[5][2] = ciUpperBmi;
        exportData[6][0] = avgBmi1;
        exportData[6][1] = ciLowerBmi1;
        exportData[6][2] = ciUpperBmi1;
        exportData[7][0] = avgBmi2;
        exportData[7][1] = ciLowerBmi2;
        exportData[7][2] = ciUpperBmi2;
        exportData[8][0] = avgBmi3;
        exportData[8][1] = ciLowerBmi3;
        exportData[8][2] = ciUpperBmi3;
        exportData[9][0] = avgObesity;
        exportData[9][1] = ciLowerObesity;
        exportData[9][2] = ciUpperObesity;
        exportData[10][0] = avgObesity1;
        exportData[10][1] = ciLowerObesity1;
        exportData[10][2] = ciUpperObesity1;
        exportData[11][0] = avgObesity2;
        exportData[11][1] = ciLowerObesity2;
        exportData[11][2] = ciUpperObesity2;
        exportData[12][0] = avgObesity3;
        exportData[12][1] = ciLowerObesity3;
        exportData[12][2] = ciUpperObesity3;
        exportData(exportData, filename);

        //Print results
        System.out.println("Simulation Results on " + n + " patients:");
        System.out.println("---------------------------------------------------");
        System.out.printf("Average Risk: %.4f (95%% CI: %.4f - %.4f)%n", risk * 100, ciLowerRisk * 100, ciUpperRisk * 100);
        System.out.printf("Prevalence: %.4f (95%% CI: %.4f - %.4f)%n", prevalence * 100, ciLower * 100, ciUpper * 100);
        System.out.printf("Prevalence Age <25: %.4f (95%% CI: %.4f - %.4f)%n", prevalenceAge1 * 100, ciLowerAge1 * 100, ciUpperAge1 * 100);
        System.out.printf("Prevalence Age 20-54: %.4f (95%% CI: %.4f - %.4f)%n", prevalenceAge2 * 100, ciLowerAge2 * 100, ciUpperAge2 * 100);
        System.out.printf("Prevalence Age 55+: %.4f (95%% CI: %.4f - %.4f)%n", prevalenceAge3 * 100, ciLowerAge3 * 100, ciUpperAge3 * 100);
        System.out.println("---------------------------------------------------");
        System.out.println("GBD U.S. results for comparison:");
        System.out.printf("Prevalence: %.4f (95%% CI: %.4f - %.4f)%n", 0.0046 * 100, 0.0043 * 100, 0.0050 * 100);
        System.out.printf("Prevalence Age <25: %.4f (95%% CI: %.4f - %.4f)%n", 0.0007 * 100, 0.0006 * 100, 0.0008 * 100);
        System.out.printf("Prevalence Age 20-54: %.4f (95%% CI: %.4f - %.4f)%n", 0.0028 * 100, 0.0024 * 100, 0.0032 * 100);
        System.out.printf("Prevalence Age 55+: %.4f (95%% CI: %.4f - %.4f)%n", 0.0102 * 100, 0.0092 * 100, 0.0112 * 100);
        System.out.println("---------------------------------------------------");
        System.out.printf("Average BMI among cases: %.2f (95%% CI: %.2f - %.2f)%n", avgBmi, ciLowerBmi, ciUpperBmi);
        System.out.printf("Average BMI among cases Age <25: %.2f (95%% CI: %.2f - %.2f)%n", avgBmi1, ciLowerBmi1, ciUpperBmi1);
        System.out.printf("Average BMI among cases Age 20-54: %.2f (95%% CI: %.2f - %.2f)%n", avgBmi2, ciLowerBmi2, ciUpperBmi2);
        System.out.printf("Average BMI among cases Age 55+: %.2f (95%% CI: %.2f - %.2f)%n", avgBmi3, ciLowerBmi3, ciUpperBmi3);
        System.out.println("---------------------------------------------------");
        System.out.printf("Obesity Prevalence among cases: %.4f (95%% CI: %.4f - %.4f)%n", avgObesity, ciLowerObesity, ciUpperObesity);
        System.out.printf("Obesity Prevalence among cases Age <25: %.4f (95%% CI: %.4f - %.4f)%n", avgObesity1, ciLowerObesity1, ciUpperObesity1);
        System.out.printf("Obesity Prevalence among cases Age 20-54: %.4f (95%% CI: %.4f - %.4f)%n", avgObesity2, ciLowerObesity2, ciUpperObesity2);
        System.out.printf("Obesity Prevalence among cases Age 55+: %.4f (95%% CI: %.4f - %.4f)%n", avgObesity3, ciLowerObesity3, ciUpperObesity3);
        System.out.println("---------------------------------------------------");
    }

    public static double linearPredictor(Patient p, double k) {
        double risk = 0.0;

        ArrayList<Boolean> testing = new ArrayList<>();

        //Baseline risk
        double r = baseline(p);
        //Index 0
        p.features.add(r);
        testing.add(true);

        //Genetics contribution
        double g = genetics(p);
        //Index 1
        p.features.add(g);
        testing.add(true);

        //Smoking-genetics interaction
        double s = smoking(p);
        double sg = sgInteraction(p, s, g);
        //Index 2
        p.features.add(sg);
        testing.add(true); 

        //Age contribution
        double a = age(p);
        //Index 3
        p.features.add(a);
        testing.add(true);

        //BMI contribution
        double b = bmi(p, k);
        //Index 4
        p.features.add(b);
        testing.add(true);

        //Sum all features
        int i = 0;
        for (double feature : p.features) {
            if (testing.get(i)) {
                risk += feature;
            }
            i++;
        }

        return risk;
    }

    public static double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    //Baseline risk contribution
    public static double baseline(Patient p) {
        double risk = p.baseline; //Intercept
        
        double r = Math.log(risk/(1 - risk));
        //Insert age contributions
        return r;
    }

    //Genetics contribution
    public static double genetics(Patient p) {
        double g = Math.log((p.alleleCount == 2) ? p.homoOR : (p.alleleCount == 1) ? p.heteroOR : 1.0);
        return g;
    }

    //Smoking-Genetics interaction
    public static double sgInteraction(Patient p, double s, double g) {
        double sg = Math.log((p.alleleCount == 2) ? p.interaction_homoOR : (p.alleleCount == 1) ? p.interaction_heteroOR : 1.0); //Interaction feature

        if (s > 0 && g > 0) {
            return sg;
        } else {
            return s;
        }
    }

    //Smoking contribution
    public static double smoking(Patient p) {
        double smokingPrevalence = smokingPrevalence(p);
        double s = Math.log(p.smokingOR);

        double chance = rng.nextDouble();
        if (chance < smokingPrevalence) {
            //Smoker
            return s;
        } else {
            //Non-smoker
            return 0;
        }
    }

    //Determine smoking prevalence based on age
    private static double smokingPrevalence(Patient p) {
        if (p.age < 25) {
            return 0.05; //5% prevalence for age <25
        } else if (p.age < 55) {
            return 0.13; //13% prevalence for age 25-54
        } else {
            return 0.1; //10% prevalence for age 55+
        }
    }

    //Age contribution
    public static double age(Patient p) {
        double minAge = 15.0;
        double maxAge = 80.0;
        double ageRange = maxAge - minAge;

        //Normalize age from 0 to 1
        double fraction = (p.age - minAge) / ageRange;
        fraction = Math.max(0.0, Math.min(fraction, 1.0));

        //Logistic curve parameters for young-age protection
        double steepness = 6.0; //Controls how quickly risk increases with age
        double midpoint = 0.5; //Midpoint of the curve (age ~45)

        double minOR = -3.0; //Strong negative log-odds at young age
        double maxOR = 0.1; //Mild positive log-odds at old age

        //Sigmoid for smooth transition
        double logistic = sigmoid(steepness * (fraction - midpoint));

        //Scale to log-odds
        double a = minOR + logistic * (maxOR - minOR);
        return a;
    }

    //BMI contribution
    public static double bmi(Patient p, double k) {
        double bmiZ = p.normalizeBMI(); //Z-score
        double ORmax = 1.26; //Peak OR at age 15
        double a = Math.exp(k * Math.max(p.age - 15, 0)) ; //Decay factor with age

        double effectiveOR = Math.max(1.0 + (ORmax - 1.0) * a, 1.01); //Ensure OR does not go below CI
        double b = Math.log(effectiveOR) * bmiZ;
        return b;
    }

    //Nested Patient class
    private static class Patient {
        //Patient attributes
        private String id;
        private ArrayList<Double> features;
        private double riskAssessment;
        private int age;
        private double bmiRaw;
        private int alleleCount;
        private int hasRA;
        private double baseline;
        private double smokingOR;
        private double heteroOR;
        private double homoOR;
        private double interaction_heteroOR;
        private double interaction_homoOR;

        //Constructor
        public Patient(int i, int mean) {
            this.id = "P" + i;
            double chance = rng.nextDouble();
            //Simulate age
            if (chance < 0.18) {
                this.age = rng.nextInt(15); //Age 0-14
            } else if (chance < 0.94) {
                this.age = rng.nextInt(66) + 15; //Age 15-80
            } else {
                this.age = rng.nextInt(19) + 81; //Age 81-99
            }
            if (age < 25) {
                this.bmiRaw = Math.min(Math.max(rng.nextGaussian() * 5.0 + mean, 15), 50); //Mean 27, SD 5
            } else {
                this.bmiRaw = Math.min(Math.max(rng.nextGaussian() * 5.0 + 27.0, 15), 50); //Mean 27, SD 5
            }
            this.features = new ArrayList<>();
        }

        public double normalizeBMI() {
            return (bmiRaw - 27.0) / 5.0;
        }

        public void setORS() {
            double dist = 0.0;

            //Baseline
            dist = rng.nextGaussian();
            this.baseline = Math.exp(dist * (((Math.log(0.0050) - Math.log(0.0043)) / 2) / 1.96) + (Math.log(0.0050) + Math.log(0.0043)) / 2);

            //Smoking OR
            dist = rng.nextGaussian();
            this.smokingOR = Math.max(Math.exp(dist * (((Math.log(1.55) - Math.log(1.32)) / 2) / 1.96) + (Math.log(1.55) + Math.log(1.32)) / 2), 1.0);

            //Genetics ORs
            dist = rng.nextGaussian();
            this.heteroOR = Math.max(Math.exp(dist * (((Math.log(2.5) - Math.log(1.5)) / 2) / 1.96) + (Math.log(2.5) + Math.log(1.5)) / 2), 1.0);
            dist = rng.nextGaussian();
            this.homoOR = Math.max(Math.exp(dist * (((Math.log(6.0) - Math.log(5.0)) / 2) / 1.96) + (Math.log(6.0) + Math.log(5.0)) / 2), 1.0);

            //Interaction ORs
            dist = rng.nextGaussian();
            this.interaction_heteroOR = Math.max(Math.exp(dist * (((Math.log(13.1) - Math.log(4.2)) / 2) / 1.96) + (Math.log(13.1) + Math.log(4.2)) / 2), 1.0);
            dist = rng.nextGaussian();
            this.interaction_homoOR = Math.max(Math.exp(dist * (((Math.log(34.2) - Math.log(7.2)) / 2) / 1.96) + (Math.log(34.2) + Math.log(7.2)) / 2), 1.0);

            //Default values
            dist = rng.nextDouble();
            this.alleleCount = dist < 0.08 ? 2 : (dist < 0.45 ? 1 : 0);
            this.hasRA = 0;
        }
    }

    public static void exportPatients(ArrayList<Patient> patients, String filename) throws Exception {
        try (PrintWriter pw = new PrintWriter(filename)) {
            pw.println("id,age,bmi,risk,hasRA");
            for (Patient p : patients) {
                pw.println(p.id + "," + p.age + "," + p.bmiRaw + "," + p.riskAssessment + ',' + p.hasRA);
            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportData(double[][] data, String filename) throws Exception {
        int n = data.length;

        try (PrintWriter pw = new PrintWriter(filename)) {
            pw.println("mean,lower,upper"); //Header
            for (int i = 0; i < n; i++) {
                pw.println(data[i][0] + "," + data[i][1] + ", " + data[i][2]);
            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}