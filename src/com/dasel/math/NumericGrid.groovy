package com.dasel.math

import java.util.concurrent.Future
import org.apache.commons.math.distribution.NormalDistributionImpl;

/**
 *
 *
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: May 29, 2009
 * Time: 4:33:01 PM
* Copyright (c) 2009, DASEL Software, Inc.  All Rights Reserved
 */

public class NumericGrid {
  
  def data = new double[0][0];
  def rows = 0;
  def cols = 0;

  static {
    ParallelMathHelper.startIfNeeded()
  }

  def NumericGrid() {
  }

  def NumericGrid(def doubles, rowCount, colCount) {
    data = doubles;
    rows = rowCount;
    cols = colCount;
  }

  def synchronized getValue(def row, col) {
    data[row][col]
  }

  def synchronized setValue(def row, col, value) {
    data[row][col] = value
  }

  def synchronized appendVertically(NumericGrid peer) {
    assert peer.cols == cols
    def ng = null
    synchronized(this) {
      def size = rows + peer.rows
      def newGrid = new double[size][]
      for (int i = 0; i < rows; i++) {
        newGrid[i] = data[i]
      }
      for (int i = 0; i < peer.rows; i++) {
        newGrid[i+rows] = peer.data[i]
      }
      ng = new NumericGrid(newGrid,peer.rows + rows,cols)
    }
    ng
  }

  def synchronized reshape(def newRows, newCols) {
    assert (newRows * newCols) == (rows * cols)
    def taskResults = []
    for (int i = 0; i < newRows; i++) {
      taskResults <<  ParallelMathHelper.getService().submit(ParallelMathHelper.remapData(data,cols,i,newCols))
    }
    double[][] matrixData = new double[newRows][];
    for (int i = 0; i < newRows; i++) {
      matrixData[i] = taskResults[i].get()
    }
    new NumericGrid(matrixData,newRows,newCols)
  }



  def static synchronized shutdownPool() {
    ParallelMathHelper.shutdownPool()
  }

  def synchronized shuffle() {
    ParallelMathHelper.shuffle(data).call()
  }
 /*
  def shuffle() {
    def biglist = []
    for (int i = 0; i < rows; i++) {
      double[] vector = data[i]
      def list = Arrays.asList(vector)
      biglist.addAll(list)
    }
    Collections.shuffle(biglist)
    int index = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        data[i][j] = biglist[index]
        index++
      }
    }
  }
  */

  def synchronized sqrt() {
    def futures = []
    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.sqrt(data[i]))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }

  def synchronized max(NumericGrid right) {
    def futures = []
    if (rows != right.rows) throw new Exception("Matrix A and Matrix B must be same size");
    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.max(data[i],right.data[i]))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }

  def synchronized min(NumericGrid right) {
    def futures = []
    if (rows != right.rows) throw new Exception("Matrix A and Matrix B must be same size");
    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.min(data[i],right.data[i]))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }

  def synchronized stdDev() {
    minus(avg()).square().avg().sqrt()
  }

  def synchronized valueOrAbove(Number value) {
    def futures = []

    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.maxScalar(data[i],value.doubleValue()))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }

  def synchronized square() {
    multiply(this)
  }

  def synchronized valueOrBelow(Number value) {
    def futures = []

    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.minScalar(data[i],value.doubleValue()))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }



  def synchronized maxValue() {
    def futures = []
    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.maxOf(data[i]))
    }
    def max = futures.collect {
      it.get()
    }.max()
  }

  def synchronized minValue() {
    def futures = []
    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.minOf(data[i]))
    }
    def max = futures.collect {  future ->
      future.get()
    }.min()
  }


  def synchronized sum() {
    def futures = []
    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.sum(data[i]))
    }
    def max = futures.collect {  future ->
      future.get()
    }.sum()
  }

  def avg() {
    def size = rows * cols
    sum() / size
  }


  def plus(NumericGrid right) {
    def futures = []
    if (rows != right.rows) throw new Exception("Matrix A and Matrix B must be same size");
    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.plus(data[i],right.data[i]))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }

  def synchronized plus(Number right) {
    def futures = []

    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.plusScalar(data[i],right.doubleValue()))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }

  def synchronized power(Number exponent) {
    def futures = []

    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.raisedPowerTo(data[i],exponent.doubleValue()))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }


  def synchronized leftPower(Number base) {
    def futures = []

    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.powerTo(base.doubleValue(),data[i]))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }


  def synchronized multiply(NumericGrid right) {
    def futures = []
    if (rows != right.rows) {
      throw new Exception("Matrix A and Matrix B must be same size");
    }
    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.times(data[i],right.data[i]))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }

  def synchronized multiply(Number right) {
    def futures = []

    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.timesScalar(data[i],right.doubleValue()))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }

  def synchronized div(NumericGrid right) {
    def futures = []
    if (rows != right.rows) throw new Exception("Matrix A and Matrix B must be same size");
    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.div(data[i],right.data[i]))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }

  def synchronized div(Number value) {
    def futures = []
    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.divScalar(data[i],value.doubleValue()))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }

  def synchronized leftDivScalar(Number leftDivScalar) {
    def futures = []
    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.leftDivScalar(data[i],value.doubleValue()))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }

  def synchronized minus(NumericGrid right) {
    def futures = []
    if (rows != right.rows) throw new Exception("Matrix A and Matrix B must be same size");
    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.minus(data[i],right.data[i]))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }

  def synchronized minus(Number value) {
    def futures = []

    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.minusScalar(data[i],value.doubleValue()))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }

  def synchronized leftMinusScalar(Number value) {
    def futures = []

    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.leftMinusScalar(data[i],value.doubleValue()))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)

  }

  def synchronized collectAbove(Number value) {
    def futures = []

    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.collectAbove(data[i],value.doubleValue()))
    }
    def results = []
    for (int i = 0; i < rows; i++) {
      def subset = futures[i].get()
      results.addAll(subset)
    }
    results
  }


  def synchronized collectBelow(Number value) {
    def futures = []

    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.collectBelow(data[i],value.doubleValue()))
    }
    def results = []
    for (int i = 0; i < rows; i++) {
      def subset = futures[i].get()
      results.addAll(subset)
    }
    results
  }


  def synchronized exp() {
    def futures = []
    for (int i = 0; i < rows; i++) {
      futures << ParallelMathHelper.getService().submit( ParallelMathHelper.exp(data[i]))
    }
    double[][] results = new double[rows][];
    for (int i = 0; i < rows; i++) {
      results[i] = futures[i].get()
    }
    return new NumericGrid(results,rows,cols)
  }


  public String toString() {
    StringBuffer buffer = new StringBuffer("[ ");
      int rowLimit = Math.min(10,this.rows);
      for (int row = 0; row < rowLimit; row++) {
        buffer.append("\n\t[");
        int colLimit = Math.min(10,cols);
        for (int col = 0; col < colLimit; col++) {
          if (col > 0) buffer.append(",");
          buffer.append(" " + data[row][col]);
        }
        if (colLimit < cols) buffer.append(" ...");
        buffer.append(" ]");

      }
    if (rowLimit < rows) buffer.append("\n\t...");
    buffer.append("\n]");
    return buffer.toString();
  }

  def synchronized size() {
    rows * cols
  }

 

  def static enhanceNumber() {
    Math.metaClass.'static'.max = { NumericGrid matrix, Number value ->
      matrix.valueOrAbove(value.doubleValue())
    }

    Math.metaClass.'static'.max = { Number value, NumericGrid matrix  ->
      matrix.valueOrAbove(value.doubleValue())
    }

    Math.metaClass.'static'.max = { NumericGrid leftMatrix, NumericGrid rightMatrix   ->
      leftMatrix.max(rightMatrix)
    }

    Math.metaClass.'static'.min = { NumericGrid matrix, Number value ->
      matrix.valueOrBelow(value.doubleValue())
    }

    Math.metaClass.'static'.min = { Number value, NumericGrid matrix  ->
      matrix.valueOrBelow(value.doubleValue())
    }

    Math.metaClass.'static'.min = { NumericGrid leftMatrix, NumericGrid rightMatrix   ->
      leftMatrix.min(rightMatrix)
    }

    Math.metaClass.'static'.exp = { NumericGrid matrix ->
      matrix.exp()
    }

    Number.metaClass.plus = { NumericGrid matrix ->
      return matrix.plus(delegate)
    }

    Number.metaClass.multiply = { NumericGrid matrix ->
      return matrix.multiply(delegate)
    }

    Number.metaClass.minus = { NumericGrid matrix ->
      return matrix.leftMinusScalar(delegate)
    }

    Number.metaClass.div = { NumericGrid matrix ->
      return matrix.leftDivScalar(delegate)
    }

    Number.metaClass.power = { NumericGrid matrix ->
      return matrix.leftPower(delegate)
    }

    Number.metaClass.exp = {
      return Math.exp(delegate)
    }

    Number.metaClass.avg = {
      return delegate
    }
  }

  public static void test(String[] args) {
    def guassian = ProbabilitySpace.createQuasiGaussian(1000,1000)
    NumericGrid.enhanceNumber()
    println guassian
    guassian.shuffle()
    println guassian
    100.times {
      def start = System.currentTimeMillis()
      guassian.shuffle()
      println guassian
      println "${(System.currentTimeMillis() - start) / 1000.0}"
    }
  }

  public static void main(String[] args) {
    def guassian = ProbabilitySpace.createQuasiGaussian(1000,1000)
    500.times {
      def start = System.currentTimeMillis()
      guassian.shuffle()
      println guassian
      println "${(System.currentTimeMillis() - start)/1000.0}"
    }
  }

  public static void test2(String[] args) {
    def guassian = ProbabilitySpace.createQuasiGaussian(1000,1000)

    def size = guassian.size();
    double drift = 0.05 - 0.5 * 0.15 * 0.15
    def strikes = [80, 90, 100, 110, 120]
    def discountFactor = Math.exp(1.0 * -0.05)
    def putResultTable = [:]
    def callResultTable = [:]
    strikes.each {
      putResultTable[it] = []
      callResultTable[it] = []
    }
    def benchmarks = []
    500.times {
      long start = System.currentTimeMillis()
      def diffusion = guassian * 0.15
      def paths = 100.0 * Math.exp(diffusion + drift)
      def speed = (System.currentTimeMillis() - start) / 1000.0
      benchmarks << speed
     // println "**************** time = ${speed} "
    }
    println "Best Time is ${benchmarks.min()} worst time is ${benchmarks.max()} avg time is ${benchmarks.sum()/benchmarks.size()}"
    benchmarks = []
    500.times {
      long start = System.currentTimeMillis()
      def paths = guassian
      paths = paths * 0.15 + drift
      paths = paths.exp() * 100.0
   //   strikes.each { strike ->
   //     def callOptionPayout = (paths - strike).valueOrAbove(0).avg()
   //     def callValue = discountFactor * callOptionPayout
   //     def putOptionPayout = (strike - paths).valueOrAbove(0).avg()
   //     def putValue = discountFactor * putOptionPayout
    //    putResultTable[strike] << putValue
    //    callResultTable[strike] << callValue
    //  }
      def speed = (System.currentTimeMillis() - start) / 1000.0
      benchmarks << speed
     // println "**************** time = ${speed} "
    }

    println "Best Time is ${benchmarks.min()} worst time is ${benchmarks.max()} avg time is ${benchmarks.sum()/benchmarks.size()}"
    NumericGrid.shutdownPool()
  }
}