package com.dasel.finance.pxengines

import com.dasel.math.NumericGrid
import com.dasel.math.LinearInterpolation
import com.dasel.finance.equity.LogNormalPathHelper
import com.dasel.finance.options.PayoutType
import com.dasel.finance.options.SimpleOption
import com.dasel.math.ParallelMathHelper
import java.text.NumberFormat
import org.apache.commons.math.distribution.NormalDistributionImpl
import com.dasel.finance.volitility.HestonModel
import com.dasel.math.PoissonProcess
import com.dasel.math.ProbabilitySpace

/**
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: May 31, 2009
 * Time: 4:38:18 PM
 * To change this template use File | Settings | File Templates.
 */

public class MonteCarloEngine {
  def sampleCount = 0
  def sampleWidth = null
  def static final MAX_COLLECTION_SIZE = 250000
  def stochasticDimensions = 1
  def mandatoryIterations = null//6500
  def size = 100
  def timeHorizen
  def pathGenerator
  def options = []
  def paths = null
  def yieldCurve = new LinearInterpolation([ 0.003:0.05, 1000:0.05])
  //def yieldCurve = new LinearInterpolation([ 0.003:0.0125, 0.25:0.025, 0.5:0.04, 1.0: 0.05, 2.0: 0.0591, 3.0:0554, 5.0:0.0634, 7.0:0.0625, 10.0:0.03125, 30.0:0.0425 ])
  def priceAccuracy = 0.005
  def convergenceTest = { size, standardDeviation ->
    def stdErr = standardDeviation / Math.sqrt(size)
    1.96 * stdErr 
  }
  def stochasticProcess = { size ->
    def length = Math.round(Math.sqrt(size)) + 1
    ProbabilitySpace.createScaledQuasiGaussian(length,length,sampleCount)
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

  def generateFixedNumberOfPaths(count) {
    if (count < MAX_COLLECTION_SIZE) {
      def stochaticVariables = stochasticProcess(count)
      paths = pathGenerator(stochaticVariables)
    } else {
      def balance = count
      while (balance > 0) {
        def sampleSize = Math.min(balance as double, MAX_COLLECTION_SIZE as double)
        def stochaticVariables = stochasticProcess(sampleSize)
        def sample = pathGenerator(stochaticVariables)
        if (!paths) {
          paths = sample
        } else {
          paths.appendVertically(sample)
        }

        balance =- sampleSize
      }
    }
    paths
  }

  def generatePathsSufficientForPriceConfergence() {
    def meanEstimates
    paths = generateFixedNumberOfPaths(100)
    def stdDev = paths.stdDev()
    def stdErr = convergenceTest(size,stdDev)
    while (stdErr > priceAccuracy) {      
      size *= stdErr / priceAccuracy
      stdErr = convergenceTest(size,stdDev)
    }
    size *= 1.05 // account for the small sample behind the std dev estimate
    size = Math.min(size,MAX_COLLECTION_SIZE)
    paths = generateFixedNumberOfPaths(size)
    sampleCount = 1
    stdDev = paths.stdDev()
    stdErr = convergenceTest(size,stdDev)
    while (stdErr > priceAccuracy && size ) {
      size *= Math.max(stdErr / priceAccuracy,1.25)
      size = Math.min(size,MAX_COLLECTION_SIZE)
      scale++
      paths = generateFixedNumberOfPaths(size)
      stdErr = convergenceTest(size,stdDev)
      stdDev = paths.stdDev()
    }
    size = paths.size()
    
    paths
  }

  def runSimulation() {
    long start = System.currentTimeMillis()
    if (mandatoryIterations) {
      paths = generateFixedNumberOfPaths(mandatoryIterations)
    } else {
      paths = generatePathsSufficientForPriceConfergence()
    }
    def options = priceOptions(paths)
    println "${(System.currentTimeMillis() - start) / 1000.0} Seconds"
    options
  }

  public static void main(String[] args) {
    100.times {
      test(args)
    }
    ParallelMathHelper.shutdownPool()
  }

  public static void test(String[] args) {
    NormalDistributionImpl normalDist = new NormalDistributionImpl()
    NumberFormat frmt = NumberFormat.getNumberInstance()
    frmt.setMaximumFractionDigits 2
    frmt.setMinimumFractionDigits 2

    def strikes = [80, 90, 100, 110, 120]
    HestonModel model = new HestonModel()
    MonteCarloEngine engine = new MonteCarloEngine()
    engine.stochasticDimensions = 2
    //engine.convergenceTest = { size, standardDeviation ->
    //  1.96 * standardDeviation / size
    //}
    engine.timeHorizen = 1.0
    strikes.each { strike ->
      engine.options << new SimpleOption( strike : strike, payoutType : PayoutType.Put, name : "${strike} Strike 1 Yr Put" )
      engine.options << new SimpleOption( strike : strike, payoutType : PayoutType.Call, name : "${strike} Strike 1 Yr Call" )
    }
    engine.stochasticProcess = { size ->
      def length = Math.round(Math.sqrt(size)) + 1
      ProbabilitySpace.createQuasiGaussian(length,length)
    }
    LogNormalPathHelper brownianMotion = new LogNormalPathHelper(spot : 100, vol : 0.15, rate : 0.05, time : 1.0)
    def randomWalk = { random ->
      brownianMotion.generatePaths(random)
    }
    def brownianMotionWithStochasticVol = { random ->
      def stochasticVol = model.generateVolatilitySpace(random)
      brownianMotion.vol = stochasticVol
      brownianMotion.generatePaths(random)
    }
    def brownianMotionWithJumps = { random ->
      def normal = random[0]
      def takeout = random[1]
      def defaultRisk = random[2]

      def drift = (brownianMotion.rate - 0.5 * brownianMotion.vol * brownianMotion.vol) * brownianMotion.time
      def diffusion = brownianMotion.vol * Math.sqrt(brownianMotion.time) * normal
      brownianMotion.spot * Math.exp(drift + diffusion + takeout + defaultRisk)
    }

    engine.pathGenerator = randomWalk
    def options = engine.runSimulation() 
    //options.each { option, price ->
    //  println "Contract: ${option.name} was priced @ ${frmt.format(price)} over ${engine.size} iterations"
    //}

    brownianMotion.time = 0.5
    engine.timeHorizen = 0.5
    options = engine.runSimulation()
    //options.each { option, price ->
    //  println "Contract: ${option.name} was priced @ ${frmt.format(price)} over ${engine.size} iterations"
    //}

    brownianMotion.time = 0.25
    engine.timeHorizen = 0.25
    options = engine.runSimulation()
    //options.each { option, price ->
    //  println "Contract: ${option.name} was priced @ ${frmt.format(price)} over ${engine.size} iterations"
    //}

    brownianMotion.time = 0.08333333
    engine.timeHorizen = 0.08333333
    options = engine.runSimulation()
   // options.each { option, price ->
   //   println "Contract: ${option.name} was priced @ ${frmt.format(price)} over ${engine.size} iterations"
   // }

    brownianMotion.time = 1.0
    engine.timeHorizen = 1.0
    options = engine.runSimulation()
    options.each { option, price ->
      println "Contract: ${option.name} was priced @ ${frmt.format(price)} over ${engine.size} iterations"
    }
  }
}