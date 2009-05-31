package com.dasel.finance.pxengines

import com.dasel.math.NumericGrid
import com.dasel.math.LinearInterpolation
import com.dasel.finance.equity.LogNormalPathHelper
import com.dasel.finance.options.PayoutType
import com.dasel.finance.options.SimpleOption
import com.dasel.math.ParallelMathHelper

/**
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: May 31, 2009
 * Time: 4:38:18 PM
 * To change this template use File | Settings | File Templates.
 */

public class MonteCarloEngine {
  def size = 100
  def timeHorizen
  def pathGenerator
  def options = []

  def yieldCurve = new LinearInterpolation([ 0.003:0.05, 1000:0.05])
  //def yieldCurve = new LinearInterpolation([ 0.003:0.0125, 0.25:0.025, 0.5:0.04, 1.0: 0.05, 2.0: 0.0591, 3.0:0554, 5.0:0.0634, 7.0:0.0625, 10.0:0.03125, 30.0:0.0425 ])
  def priceAccuracy = 0.005
  def convergenceTest = { size, standardDeviation ->
    standardDeviation / size
  }
  def stochasticProcess = { size ->
    def length = Math.round(Math.sqrt(size)) + 1
    NumericGrid.createQuasiGaussian(length,length)
  }


  def priceOptions(def paths) {
    def discountFactor = Math.exp(-yieldCurve.getAt(timeHorizen) * timeHorizen)
    def table = [:]
    for (option in options) {
      def payout = option.payout(paths).avg()
      table[option] = discountFactor * payout
    }
    table
  }

  def runSimulation() {
    def stochaticVariables = stochasticProcess(size)
    def paths = pathGenerator(stochaticVariables)
    def stdDev = paths.stdDev()
    def stdErr = convergenceTest(size,stdDev)
    while (stdErr > priceAccuracy) {
      size *= stdErr / priceAccuracy
      stdErr = convergenceTest(size,stdDev)
    }
    def flag = true
    while (flag) {
      stochaticVariables = stochasticProcess(size)
      paths = pathGenerator(stochaticVariables)
      stdDev = paths.stdDev()
      stdErr = convergenceTest(size,stdDev)
      flag = stdErr > priceAccuracy
      if (flag) {
        size *= Math.max(stdErr / priceAccuracy,2)
      } else {
        size = paths.size()
      }
    }
    priceOptions(paths)
  }

  public static void main(String[] args) {
    def strikes = [80, 90, 100, 110, 120]
    MonteCarloEngine engine = new MonteCarloEngine()
    engine.timeHorizen = 1.0
    strikes.each { strike ->
      engine.options << new SimpleOption( strike : strike, payoutType : PayoutType.Put, name : "${strike} Strike 1 Yr Put" )
      engine.options << new SimpleOption( strike : strike, payoutType : PayoutType.Call, name : "${strike} Strike 1 Yr Call" )
    }
    LogNormalPathHelper brownianMotion = new LogNormalPathHelper(spot : 100, vol : 0.15, rate : 0.05, time : 1.0)
    def closure = { random ->
      brownianMotion.generatePaths(random)
    }
    engine.pathGenerator = closure
    def options = engine.runSimulation() 
    options.each { option, price ->
      println "Contract: ${option.name} was priced @ ${price} over ${engine.size} iterations"
    }
    ParallelMathHelper.shutdownPool()
    
  }
}