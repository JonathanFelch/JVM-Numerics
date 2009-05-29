/**
 * User: Jonathan
 * Date: May 29, 2009
 * Time: 5:58:54 PM
 * Copyright (c) 2009, DASEL Software, Inc.  All Rights Reserved 
 */

import com.dasel.math.NumericGrid


def eulerMethod = { spotPrice, time, riskFreeRate, volatility, normalRandom ->
  spotPrice * Math.exp( (riskFreeRate - 0.5 * volatility * volatility) * time + Math.sqrt(time) * volatility * normalRandom)
}

def spotPrices = [80,90,100,110,120]
def gaussian  = NumericGrid.createQuasiGuassian(1000,1000);

def paths
def vol = 0.15, riskFreeRate = 0.03, time = 1.0
def benchmarks = []
for (px in spotPrices) {
  def model = eulerMethod.curry(px,time,riskFreeRate,vol)
  100.times {
    def start = System.currentTimeMillis()
    paths = model(gaussian)
    benchmarks << (System.currentTimeMillis() - start) / 1000.0
  }
  def sz = gaussian.size()

  println "Avg Time Required To Expression ${sz} times was ${(benchmarks.sum() / benchmarks.size())} "
  println "Best Time Required To Expression ${sz} times was ${(benchmarks.min() )} "
  println "Px: ${px} Time: ${time} ${vol*100.0} Interest Rate: ${riskFreeRate * 100.0} Mean: ${paths.avg()} Std Dev: ${paths.stdDev()}"
  println ""
}
