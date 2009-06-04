package com.dasel.math;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: May 29, 2009
 * Time: 4:30:37 PM
* Copyright (c) 2009, DASEL Software, Inc.  All Rights Reserved
 */
public class ParallelMathHelper {
    static Random random = new Random();
    static ExecutorService service = null;
    static int threadCount = 4;
    static int FORK_ARRAY_SIZE = 1000000;

    public static void startIfNeeded() {
        getService();
    }

    public static ExecutorService getService() {
        synchronized (ParallelMathHelper.class) {
            if (service == null) {
                service = Executors.newFixedThreadPool(threadCount);
                GroovyNumerics.initDSL();
            }
        }
        return service;
    }

    public static void shutdownPoolNow() {
        ParallelMathHelper.getService().shutdownNow();
    }

    public static Callable remapData(final double[][] data,  final Number oldColumnCount,  final Number targetRow, final Number columnCount ) {
        return new Callable() {
            public Object call() throws Exception {
                int cols = columnCount.intValue();
                int srcCols = oldColumnCount.intValue();
                int origin = (cols * targetRow.intValue());
                int sourceRow = origin / srcCols;
                int sourceCol = origin % srcCols;
                double[] newVector = new double[cols];
                for (int i = 0; i < cols; i++) {
                    newVector[i] = data[sourceRow][sourceCol];
                    sourceCol++;
                    if (sourceCol >= srcCols) {
                        sourceCol = 0;
                        sourceRow++;
                    }
                }
                return newVector;
            }
        };
    }

    public static ExecutorService createNewPool() {
        try {
            shutdownPool();
        } catch (Exception e) {
        }
        return getService();
    }

    public static ExecutorService createNewPool(int count) {
        threadCount = count;
        try {
            shutdownPool();
        } catch (Exception e) {
        }
        return getService();
    }

    public static void shutdownPool(long waitTime) throws InterruptedException {
        ParallelMathHelper.getService().shutdown();
        ParallelMathHelper.getService().awaitTermination(waitTime, TimeUnit.MILLISECONDS);
    }

    public static void shutdownPool() throws InterruptedException {
        ParallelMathHelper.getService().shutdown();
        ParallelMathHelper.getService().awaitTermination(1, TimeUnit.MINUTES);
    }

    public static Callable copyInto(final List list, final double[] data) {
        Callable c = new Callable() {
            public Object call() throws Exception {
                for (int i = 0; i < data.length; i++) {
                    list.add(data[i]);
                }
                return list;
            }
        };
        return c;
    }

    // WARNING! Does not lock on data!  Threads MUST access different collumns to be safe!

    public static Callable shuffleCol(final double[][] data, final int col) {
        return new Callable() {
            int len = data.length;
            public Object call() throws Exception {
                for (int i = 0; i < data.length; i++) {
                    double temp = data[i][col];
                    int index = random.nextInt(len);
                    data[i][col] = data[index][col];
                    data[index][col] = temp;
                }
                return data;
            }
        };
    }

    public static Callable shuffleRow(final double[] data) {
        return new Callable() {
            int len = data.length;
            public Object call() throws Exception {
                for (int i = 0; i < data.length; i++) {
                    double temp = data[i];
                    int index = random.nextInt(len);
                    data[i] = data[index];
                    data[index] = temp;
                }
                return data;
            }
        };
    }


    public static Callable shuffle(final double[][] data) {
        final int rows = data.length;
        int size = 0;
        for (int i = 0; i < rows; i++) {
            size += data[i].length;
        }
        if (size < ParallelMathHelper.FORK_ARRAY_SIZE) {
            return new Callable() {
                public Object call() throws Exception {
                    ParallelMathHelper.simpleShuffle(data);
                    return data;
                }
            };
        }
        return shuffleParallel(data);
    }

     public static Callable shuffleParallel(final double[][] data) {
         final int rows = data.length;
         return new Callable() {
            public Object call() throws Exception {

                Integer cols = null;
                try {
                    List<Future> tasks = new ArrayList<Future>();
                    for (int i = 0; i < rows; i++) {
                        if (i == 0) {
                            cols = data[i].length;
                        }
                        Future f = ParallelMathHelper.getService().submit(ParallelMathHelper.shuffleRow(data[i]));
                        tasks.add(f);
                    }
                    for (Future f : tasks) {
                        f.get();
                    }
                    for (int i = 0; i < cols; i++) {
                        Future f = ParallelMathHelper.getService().submit(ParallelMathHelper.shuffleCol(data,i));
                        tasks.add(f);
                    }
                    for (Future f : tasks) {
                        f.get();
                    }
                } catch (Exception e) {

                }
                return data;
            }
        };
    }


    public static void simpleShuffle(double[][] million) {
        Random random = new Random();
        int rows = million.length;
        for (int i = 0; i < rows; i++) {
            double[] data = million[i];
            int cols = data.length;
            for (int j = 0; j < cols; j++) {
                int srcRow = random.nextInt(rows);
                int srcCol = random.nextInt(cols);
                double temp = data[j];
                data[j] = million[srcRow][srcCol];
                million[srcRow][srcCol] = temp;
            }
        }
    }

    public static void main(String[] args) {
        int rows = 10;
        int cols = 100000;

        double[][] million = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                million[i][j] = i*rows+j;
            }
        }


        //for (int loop = 0; loop < 100; loop++) {
        //    long start = System.currentTimeMillis();

        //    for (int i = 0; i < rows; i++) {
        //        double[] data = million[i];
        //        int cols = data.length;
        //        for (int j = 0; j < cols; j++) {
        //            int srcRow = random.nextInt(rows);
        //            int srcCol = random.nextInt(cols);
        //            double temp = data[j];
        //            data[j] = million[srcRow][srcCol];
        //            million[srcRow][srcCol] = temp;
        //        }
        //    }
        //}
        for (int loop = 0; loop < 100; loop++) {
            long start = System.currentTimeMillis();
            try {
                ParallelMathHelper.shuffle(million).call();
            } catch (Exception e) { }

            System.out.println("Parallel: time = "+(System.currentTimeMillis()-start)+" seconds");
        }
        for (int loop = 0; loop < 100; loop++) {
            long start = System.currentTimeMillis();
            simpleShuffle(million);
            System.out.println("Simple: time = "+(System.currentTimeMillis()-start)+" seconds");
        }
        try {
            ParallelMathHelper.shutdownPool();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    public static Callable copyFromList(final List list, final double[][] data, final int cols) {
        Callable c = new Callable() {
            public Object call() throws Exception {
                int s = list.size();
                double[] rowData = null;
                int row = -1;
                int col = 0;
                for (int i = 0; i < s; i++) {
                    if (i % cols == 0) {
                        row++;
                        rowData = new double[cols];
                        col = 0;
                        data[row] = rowData;
                    }
                    Object o = list.get(i);
                    rowData[col] = (Double)o;
                    col++;

                }
                return data;
            }
        };
        return c;
    }

    public static Callable times(final double[] a, final double[] b) throws Exception {
        if (a.length != b.length) throw new Exception("");
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = a[i] * b[i];
                }
                return result;
            }
        };
        return c;
    }


    public static Callable timesScalar(final double[] a, final double scalar) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = a[i] * scalar;
                }
                return result;
            }
        };
        return c;
    }

    public static Callable plus(final double[] a, final double[] b) throws Exception {
        if (a.length != b.length) throw new Exception("");
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = a[i] + b[i];
                }
                return result;
            }
        };
        return c;
    }

    public static Callable plusScalar(final double[] a, final double scalar) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = a[i] + scalar;
                }
                return result;
            }
        };
        return c;
    }

    public static Callable div(final double[] a, final double[] b) throws Exception {
        if (a.length != b.length) throw new Exception("");
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = a[i] - b[i];
                }
                return result;
            }
        };
        return c;
    }

    public static Callable divScalar(final double[] a, final double scalar) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = a[i] / scalar;
                }
                return result;
            }
        };
        return c;
    }


    public static Callable leftDivScalar(final double[] a, final double scalar) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = scalar / a[i];
                }
                return result;
            }
        };
        return c;
    }

    public static Callable minus(final double[] a, final double[] b) throws Exception {
        if (a.length != b.length) throw new Exception("");
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = a[i] - b[i];
                }
                return result;
            }
        };
        return c;
    }

    public static Callable minusScalar(final double[] a, final double scalar) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = a[i] - scalar;
                }
                return result;
            }
        };
        return c;
    }

    public static Callable leftMinusScalar(final double[] a, final double scalar) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = scalar - a[i];
                }
                return result;
            }
        };
        return c;
    }

    public static Callable leftScalarDivision(final double[] a, final double scalar) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = scalar / a[i];
                }
                return result;
            }
        };
        return c;
    }

    public static Callable sqrt(final double[] a) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = Math.sqrt(a[i]);
                }
                return result;
            }
        };
        return c;
    }

    public static Callable powerTo(final double base, final double[] exponents) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[exponents.length];
                for (int i = 0; i < exponents.length; i++) {
                    result[i] = Math.pow(base, exponents[i]);
                }
                return result;
            }
        };
        return c;
    }

    public static Callable raisedPowerTo(final double[] baseVector, final double exponent) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[baseVector.length];
                for (int i = 0; i < baseVector.length; i++) {
                    result[i] = Math.pow(baseVector[i], exponent);
                }
                return result;
            }
        };
        return c;
    }

    public static Callable exp(final double[] a) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = Math.exp(a[i]);
                }
                return result;
            }
        };
        return c;
    }


    public static Callable minOf(final double[] a) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                Double min = null;
                double[] result = new double[a.length];
                min = a[0];
                for (int i = 1; i < a.length; i++) {
                    min = Math.min(a[i], min);
                }
                return min;
            }
        };
        return c;
    }


    public static Callable maxOf(final double[] a) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                Double max = null;
                double[] result = new double[a.length];
                max = a[0];
                for (int i = 1; i < a.length; i++) {
                    max = Math.max(a[i], max);
                }
                return max;
            }
        };
        return c;
    }


    public static Callable sum(final double[] a) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                Double max = null;
                double[] result = new double[a.length];
                max = a[0];
                for (int i = 1; i < a.length; i++) {
                    max += a[i];
                }
                return max;
            }
        };
        return c;
    }

    public static Callable max(final double[] a, final double[] b) throws Exception {
        if (a.length != b.length) throw new Exception("");
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = Math.max(a[i], b[i]);
                }
                return result;
            }
        };
        return c;
    }


    public static Callable collectAbove(final double[] a, final double scalar) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                ArrayList<Double> values = new ArrayList<Double>();
                for (int i = 0; i < a.length; i++) {
                    double val = a[i];
                    if (val >= scalar) {
                        values.add(val);
                    }
                }
                return values;
            }
        };
        return c;
    }

    public static Callable collectBelow(final double[] a, final double scalar) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                ArrayList<Double> values = new ArrayList<Double>();
                for (int i = 0; i < a.length; i++) {
                    double val = a[i];
                    if (val <= scalar) {
                        values.add(val);
                    }
                }
                return values;
            }
        };
        return c;
    }


    public static Callable maxScalar(final double[] a, final double scalar) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = Math.max(a[i], scalar);
                }
                return result;
            }
        };
        return c;
    }

    public static Callable min(final double[] a, final double[] b) throws Exception {
        if (a.length != b.length) throw new Exception("");
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = Math.min(a[i], b[i]);
                }
                return result;
            }
        };
        return c;
    }


    public static Callable minScalar(final double[] a, final double scalar) throws Exception {
        Callable c = new Callable() {
            public Object call() throws Exception {
                double[] result = new double[a.length];
                for (int i = 0; i < a.length; i++) {
                    result[i] = Math.min(a[i], scalar);
                }
                return result;
            }
        };
        return c;
    }

}
