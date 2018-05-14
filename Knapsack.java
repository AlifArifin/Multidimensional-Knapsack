import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Locale;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.DecimalFormatSymbols;

public class Knapsack {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.print("Masukkan nama file yang menyimpan data masakan = ");
        String nameFile = input.nextLine();
        System.out.print("Masukkan waktu yang dimiliki = ");
        int _t = input.nextInt();
        System.out.print("Masukkan modal yang dimiliki = ");
        int _m = input.nextInt();

        Knapsack k = new Knapsack(nameFile, _m, _t);
    }

    // Atribut
    private int[][] dpTableOld;
    private int[][] dpTableNew;    
    private int moneyBound;
    private int timeBound;
    private int moneyMultiple;
    private int timeMultiple;    
    private int n;
    private int mDP;
    private int tDP;
    private ArrayList<Integer[]> dishList = new ArrayList<>();
    private ArrayList<String> dishName = new ArrayList<>();

    public Knapsack(String nameFile, int _m, int _t) {
        n = 0;
        moneyBound = _m;
        timeBound = _t;

        try (BufferedReader br = new BufferedReader(new FileReader(nameFile))) {
            String line = br.readLine();

            while (line != null) {
                Integer[] temp = new Integer[3];
                
                line = line.replaceAll("\n", "");
                String[] splitLine = line.split("\\|");
                Arrays.parallelSetAll(splitLine, (i) -> splitLine[i].trim());

                dishName.add(splitLine[0]);
                temp[0] = Integer.parseInt(splitLine[1]);
                temp[1] = Integer.parseInt(splitLine[2]);
                temp[2] = Integer.parseInt(splitLine[3]);
                dishList.add(temp);

                line = br.readLine();
                n++;
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        }

        if (n != 0) {
            long mulai = System.nanoTime();
            
            // mencari multiple yang tepat
            boolean found = false;
            for (int x = 1; !found; x *= 10) {
                for (int i = 0; i < dishList.size(); i++) {
                    if (dishList.get(i)[0] % x != 0) {
                        timeMultiple = x / 10;
                        found = true;
                        break;
                    }
                }
            }

            found = false;            
            for (int x = 1; !found; x *= 10) {
                for (int i = 0; i < dishList.size(); i++) {
                    if (dishList.get(i)[1] % x != 0) {
                        moneyMultiple = x / 10;
                        found = true;
                        break;
                    }
                }
            }

            // System.out.println(moneyMultiple);
            // System.out.println(timeMultiple);            

            mDP = moneyBound / moneyMultiple + 1;
            tDP = timeBound / timeMultiple + 1;

            dpTableOld = new int[mDP * tDP][3 + n];
            dpTableNew = new int[mDP * tDP][3 + n];
            
            // inisiasi tabel
            int temp1 = 0;
            int temp2 = - moneyMultiple;
            for (int i = 0; i < mDP * tDP; i++) {
                temp2 += moneyMultiple;
                if (temp2 / moneyBound > 0 && temp2 % moneyBound != 0) {
                    temp2 = 0;
                    temp1 += timeMultiple;
                }
                
                dpTableOld[i][0] = temp1;
                dpTableOld[i][1] = temp2;                
                dpTableNew[i][0] = temp1;
                dpTableNew[i][1] = temp2;
                
                // System.out.printf("%d %d\n", temp1, temp2);
            }
            // mengisi tabel baru
            // System.out.println(tDP * mDP);            

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < mDP * tDP; j++) {
                    // System.out.printf("%d %d\n", i, j);
                    DP(j, i);
                }
                for (int a = 0; a < mDP * tDP; a++) {
                    for (int b = 0; b < n + 3; b++) {
                        dpTableOld[a][b] = dpTableNew[a][b];
                    }
                }
            }

            long selesai = System.nanoTime();
            
            
            System.out.println();
            DecimalFormat nt = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.GERMANY); 
            DecimalFormatSymbols symbols = nt.getDecimalFormatSymbols();
            symbols.setCurrencySymbol(""); // Don't use null.
            nt.setDecimalFormatSymbols(symbols);
            String test = nt.format(moneyBound);
            test.replace('?', ' ');
            System.out.printf("Dengan modal = Rp%s\n", test);            
            System.out.printf("Dengan batas waktu = %d menit\n", timeBound);            
            System.out.println("Masakan yang disarankan");
            int sumTime = 0;
            int sumMoney = 0;
            for (int i = 0; i < n; i++) {
                if (dpTableNew[tDP * mDP - 1][3 + i] == 1) {
                    System.out.printf("- %s\n", dishName.get(i));
                    sumTime += dishList.get(i)[0];
                    sumMoney += dishList.get(i)[1];
                }
            }
            test = nt.format(sumMoney);
            System.out.printf("Modal yang dibutuhkan = Rp%s\nWaktu yang dibutuhkan = %d menit\n", test, sumTime);
            test = nt.format(dpTableNew[tDP * mDP - 1][2]);            
            System.out.printf("Dengan total profit = Rp%s\n", test);
            System.out.printf("Memakan waktu: %d,%d ms\n", (selesai - mulai) / 1000000, (selesai - mulai) % 1000000);                
        }
    }

    public static int Max2(int a, int b) {
        return a > b ? 1 : 0;
    }
    
    public void DP(int i, int j) {
        int temp1 = dpTableNew[i][0] - dishList.get(j)[0];
        int temp2 = dpTableNew[i][1] - dishList.get(j)[1];
        int temp3;
        // System.out.println(temp1);
        // System.out.println(temp2);        
        if (temp1 < 0 || temp2 < 0) {
            for (int k = 0; k < j; k++) {
                dpTableNew[i][2 + k] = dpTableOld[i][2 + k];
            }
        } else {
            // System.out.println("lala");
            temp3 = 0;
            int ktemp = 0;
            for (int k = 0; ; ) {
                if (temp1 == dpTableOld[k][0]) {
                    if (temp2 == dpTableOld[k][1]) {
                        temp3 = dpTableOld[k][2];
                        ktemp = k;
                        break;
                    } else {
                        k++;
                    }
                } else {
                    k += mDP;
                }
            }
            // System.out.println("lala");            
            // System.out.println(temp3);
            
            if (Max2(dpTableOld[i][2], temp3 + dishList.get(j)[2]) == 1) {
                for (int k = 0; k < j; k++) {
                    dpTableNew[i][2 + k] = dpTableOld[i][2 + k];
                } 
            } else {
                dpTableNew[i][2] = temp3 + dishList.get(j)[2];
                for (int k = 0; k < j; k++) {
                    dpTableNew[i][3 + k] = dpTableOld[ktemp][3 + k];
                }
                dpTableNew[i][3 + j] = 1;
            }
        }
    }
}