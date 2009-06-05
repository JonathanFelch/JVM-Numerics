package com.dasel.math

import org.apache.commons.math.distribution.NormalDistributionImpl

/**
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: Jun 3, 2009
 * Time: 6:15:03 PM
 * To change this template use File | Settings | File Templates.
 */

public class ProbabilitySpace  {
  static NormalDistributionImpl normalDist = new NormalDistributionImpl()
  static Random random = new Random()

  public static NumericGrid createUniformDistribution(int rows, int cols) {
     int col = 0;
     int row = 0;
     double step = 1.0 / (rows * cols + 1)
     double value = 0.0
     double[][] data = new double[rows][cols]
     while (value <= 0.50) {
       data[row][col] = value;
       data[rows-row-1][cols-col-1] = 1.0-value
       value += step
       if (col >= cols) {
         col = 0
         row++
       }
     }
     new NumericGrid(data,rows,cols)
   }

   def static createPoissonProcess(def rows, cols, PoissonProcess jump) {
    int col = 0;
    int row = 0;

    double[][] data = new double[rows][cols]
    int populationSize = rows * cols
    int eventCount = jump.frequency * populationSize
    for (int i = 0; i < eventCount; i++) {

    }
    def grid = new NumericGrid(data,rows,cols)
    grid.shuffle()
    grid
  }

    public static NumericGrid createScaledQuasiGaussian(def rows, cols, scale) {
      def power = (Math.log(scale as double) / Math.log(2.0d) as int) + 1.0 as double
      rows = rows as int;
      cols = cols as int;
      int col = 0;
      int row = 0;
      double step = 1.0 / (rows * cols * + 1)
      double draw = step *  ((scale % power + 1) / scale)
      def data = new double[rows][cols]
      while (draw <= 0.50) {
        def value = normalDist.inverseCumulativeProbability(draw)
        //double value = StatUtil.getInvCDF(draw,true)
        data[row][col] = value;
        data[rows-row-1][cols-col-1] = -value
        col++
        if (col >= cols) {
          col = 0
          row++
        }
        draw += step;
      }
      return new NumericGrid(data,rows,cols)
    }


  public static NumericGrid createQuasiGaussian(def rows, cols) {
      return createScaledQuasiGaussian(rows,cols,1)
    }

  def synchronized createCorrelateNormal(NumericGrid grid, Number coor) {
    def z2 = NumericGrid.createQuasiGaussian(rows,cols)
    z2.shuffle()
    return grid.multiply(coor) * (1 - coor * coor) * z2
  }

  public static void main(String ... args) {
    10.times { it ->
      int index = it + 1
      def probSpace = createScaledQuasiGaussian(1000,1000,index)
      println probSpace
      println "\nINDEX: ${index} MEAN: ${probSpace.avg()} STD DEV: ${probSpace.stdDev()}\n"
    }
    NumericGrid.shutdownPool()
  }

}