package com.dasel.finance.equity
/**
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: May 31, 2009
 * Time: 4:46:15 PM
 * To change this template use File | Settings | File Templates.
 */

public class MovingAvgPathHelper extends LogNormalPathHelper {
  
  def generatePaths(def periodDuration, numberOfPeriods) {
    def start = time - periodDuration * numberOfPeriods

    def observations = []
    priceSpace = spot
    def timeStep = start
    numberOfPeriods.times {
      def drift = (rate - 0.05 * vol * vol) * timeStep
      def diffusion = Math.sqrt(time) * vol * timeStep
      priceSpace *= Math.exp(drift * diffusion)
      observations << priceSpace
      timeStep = periodDuration
    }
    observations.sum() / observations.size()
  }

}