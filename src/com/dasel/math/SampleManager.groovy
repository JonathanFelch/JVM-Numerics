package com.dasel.math
/**
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: Jun 4, 2009
 * Time: 1:07:27 PM
 * To change this template use File | Settings | File Templates.
 */

public class SampleManager {
  def samples = []
  def sampleSizes = [:]
  def sampleMeans = [:]
  def sampleVariance = [:]
  def sampleMins = [:]
  def sampleMaxs = [:]

  def addSample(NumericGrid sample) {
    samples << sample
    sampleSizes[sample] = (sample.size())
    sampleMeans[sample] = (sample.avg())
    sampleVariance[sample] = (sample.variance())
    sampleMins[sample] = (sample.minValue())
    sampleMaxs[sample] = (sample.maxValue())
  }

  def mean() {
    if (samples.size() == 0) return Double.NaN

    def total = 0
    def population = 0
    samples.each {
      def mean = new BigDecimal(sampleMeans[it])
      def size = new BigDecimal(sampleSizes[it])

      population += size
      total += mean * size
    }
    total / population
  }

  def variance() {
    if (samples.size() == 0) return Double.NaN

    def total = 0
    def population = 0
    def truePop = 0
    samples.each {
      def var = sampleVariance[it]
      def size = sampleSizes[it]
      def adjSize = (size - 1)
      truePop += size
      population += adjSize
      total += (var * adjSize)
    }
    def est = total / truePop
    est
  }

  public static void main(String[] args) {
      SampleManager manager = new SampleManager()
      NumericGrid aggregate = null
      10.times { count ->
        def space = ProbabilitySpace.createQuasiGaussian(100,100)
        space += 3
        space *= 5
        manager.addSample(space)
        if (!aggregate) {
          aggregate = space
        } else {
          aggregate = aggregate.appendVertically(space)
        }
        println "COUNT : ${count} VAR EST: ${manager.variance()} AVG EST: ${manager.mean()}"
        println "VAR OBS: ${aggregate.variance()} AVG OBS: ${aggregate.avg()}\n"
      }
      aggregate.shutdownPool()
  }

}