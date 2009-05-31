package com.dasel.finance.options

import com.dasel.math.NumericGrid

/**
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: May 30, 2009
 * Time: 7:24:45 PM
 * To change this template use File | Settings | File Templates.
 */

enum PayoutType { Put, Call }
enum ExpiryType { European, Asian, American }

public class SimpleOption {
  def name = null
  def payoutType = null
  def expiryType =  ExpiryType.European
  def strike

  def payout(def assetValue) {
    switch (payoutType) {
      case PayoutType.Call:
        return Math.max(assetValue - strike,0)
      case PayoutType.Put:
      return Math.max(strike - assetValue,0)
        break;
    }
  }

  def pv(def interestRate, timeToExpiry, assetValue, spotPx = null) {
    if (assetValue instanceof NumericGrid) {
      def avgPayout = payout(assetValue).avg()
      def pv = Math.exp(-interestRate * timeToExpiry) * avgPayout
      if (spot && expiryType == ExpiryType.American) {   // Adjust for differences with dividends in pricing American Options
        pv = Math.max(pv,payout(spot))
      }
      return pv
    }
    return Math.exp(-interestRate * timeToExpiry) * payout(assetValue)
  }

  public String getName() {
    if (!name) {
      name = "${strike} String ${payoutType.name()}"
    }
    return name
  }
}