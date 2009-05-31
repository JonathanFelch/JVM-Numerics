package com.dasel.finance.equity
/**
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: May 30, 2009
 * Time: 6:38:42 PM
 * To change this template use File | Settings | File Templates.
 */

public class LogNormalPathHelper {
  def spot, vol, rate, time

  def generatePaths(def random) {
    def drift = (rate - 0.5 * vol * vol) * time
    def diffusion = Math.sqrt(time) * vol * random
    spot * Math.exp(drift + diffusion)
  }
}