package com.dasel.math
/**
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: May 30, 2009
 * Time: 8:02:58 PM
 * To change this template use File | Settings | File Templates.
 */

public class LinearInterpolation {
  def functionData = new TreeMap<BigDecimal,BigDecimal>()

  def LinearInterpolation() {
  }

  def LinearInterpolation(def map) {
    map.each { x, y ->
      functionData[new BigDecimal(x)] = new BigDecimal(y)
    }
  }

  def LinearInterpolation(x, y) {
    assert x.size() == y.size()
    for( def i = 0; i < x.size(); i++) {
      functionData[new BigDecimal(x[i])] = new BigDecimal(y[i])
    }
  }


  def putAt(x, y) {
    functionData[new BigDecimal(x)] = new BigDecimal(y)
  }

  def getAt( x ) {
    if (!(x instanceof BigDecimal)) {  x =  new BigDecimal(x) }
    if (functionData.containsKey(x)) {
      return functionData[x]
    }
    if ( functionData.firstKey() < x  && x < functionData.lastKey() ) {
      def lowerEntry = functionData.lowerEntry(x)
      def higherEntry = functionData.higherEntry(x)
      def yEst = (x - lowerEntry.key) * (higherEntry.value - lowerEntry.value) /  (higherEntry.key - lowerEntry.key) + lowerEntry.value
      return yEst
    } else if ( x < functionData.firstKey() ) {
      return functionData.firstEntry().value
    }
    return functionData.lastEntry().value
  }

  public static void main(String[] args) {
    LinearInterpolation interp = new LinearInterpolation()
    interp.putAt(5,10)
    interp.putAt(10,20)
    interp.putAt(30,60)
    def range = 6..59

    print "Default: "
    range.each {
      print "(${it}, ${interp.getAt(it)}) "
    }
    println " "

    print "Map: "
    interp = new LinearInterpolation([ 5:10, 10:20, 30:60 ])
    range.each {
      print "(${it}, ${interp.getAt(it)}) "
    }
    println " "

    print "Lists: "
    interp = new LinearInterpolation([ 5, 10, 30], [10, 20, 60])
    range.each {
      print "(${it}, ${interp.getAt(it)}) "
    }
    println " "

    print "Arrays: "
    Number[] x = [5, 10, 30]
    Number[] y = [10, 20, 60]
    interp = new LinearInterpolation(x,y)
    range.each {
      print "(${it}, ${interp.getAt(it)}) "
    }
    println " "

  }
}