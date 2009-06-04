package com.dasel.finance.volitility

import java.text.NumberFormat
import com.dasel.math.NumericGrid

/**
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: May 30, 2009
 * Time: 6:48:00 PM
 * To change this template use File | Settings | File Templates.
 */

public class HestonModel {
  def NumberFormat nf = NumberFormat.getNumberInstance()
  def rho = 0.50
  def speed = 1
  def level = 0.15
  def varianceVol = 0.0025
  def step = 1.0

  def generateVolatilitySpace(def rn) {
    nf.setMaximumFractionDigits 3
    nf.setMinimumFractionDigits(3)

    def r = rn.rows
    def c = rn.cols
    def sigma = ProbabilitySpace.createQuasiGaussian(r,c) * level
    

    def var = sigma.square()
    def alpha = step/ speed

    def crn = rn.createCorrelateNormal(rho)
    def dv = alpha * (level * level -  var)  + varianceVol * var * crn * Math.sqrt(alpha)
    var += dv

    return var.sqrt()
  }
}
