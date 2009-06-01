package com.dasel.finance.equity

import com.dasel.finance.options.SimpleOption
import org.apache.commons.math.distribution.NormalDistributionImpl
import com.dasel.finance.options.PayoutType

/**
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: Jun 1, 2009
 * Time: 7:43:02 AM
 * To change this template use File | Settings | File Templates.
 */

public class BlackScholesPDE {
  static NormalDistributionImpl normalDist = new NormalDistributionImpl()

  public static double callPrice(def spot, strike, sigma, time, interestRate) {
     def d1 = (Math.log(spot / strike) + (interestRate + sigma * sigma / 2) * time) / (sigma * Math.sqrt(time));
     def d2 = d1 - sigma * Math.sqrt(time);

     return spot * normalDist.cumulativeProbability(d1) - strike * Math.exp(-interestRate * time) * normalDist.cumulativeProbability(d2);
   }

   public static double putPrice(def spot, strike, interestRate, sigma, time) {
     def d1 = (Math.log(spot / strike) + (interestRate + sigma * sigma / 2) * time) / (sigma * Math.sqrt(time));
     def d2 = d1 - sigma * Math.sqrt(time);

     return strike * Math.exp(-interestRate * time) * normalDist.cumulativeProbability(-d2) - spot * normalDist.cumulativeProbability(-d1);
   }



  def static price(PayoutType callPut, def spot, strike, vol, time, rate) {
    def d1 = (Math.log(spot / strike) + (rate + vol * vol / 2) * time) / (vol * Math.sqrt(time));
    def d2 = d1 - vol * Math.sqrt(time);

    switch (callPut) {
      case PayoutType.Call:
        def val = spot * normalDist.cumulativeProbability(d1)
        val -= strike * Math.exp(-rate * time) * normalDist.cumulativeProbability(d2)
        return val
      case PayoutType.Put:
        def val = strike * Math.exp(-rate * time) * normalDist.cumulativeProbability(-d2)
        val -= spot * normalDist.cumulativeProbability(-d1)
        return val

    }
  }

  def priceOptions(SimpleOption option, spot, volatility, interestRate, timeToExpiry) {
    switch (option.payoutType) {
      case PayoutType.Call:

        break;
      case PayoutType.Put:
        break;
    }

  }

  public static void main(String[] args) {
    BlackScholesPDE pde = new BlackScholesPDE()
    println pde.callPrice(100, 100, 0.15, 1.0, 0.05)
    println pde.price(PayoutType.Call, 100, 100, 0.15, 1.0, 0.05)
    println pde.price(PayoutType.Put, 100, 100, 0.15, 1.0, 0.05)
  }


}