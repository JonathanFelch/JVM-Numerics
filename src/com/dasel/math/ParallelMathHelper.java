package com.dasel.math;

import java.util.concurrent.Callable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: May 29, 2009
 * Time: 4:30:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParallelMathHelper {
    static ExecutorService service = null;
    static int threadCount = 4;

    public static void startIfNeeded() {
        getService();
    }

    public static ExecutorService getService() {
        synchronized (ParallelMathHelper.class) {
            if (service == null) {
                service = Executors.newFixedThreadPool(threadCount);
                NumericGrid.enhanceNumber();
            }
        }
        return service;
    }

    public static void shutdownPoolNow() {
        ParallelMathHelper.getService().shutdownNow();
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
                    rowData[col] = (Double) list.get(i);
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
