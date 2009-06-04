package com.dasel.math
/**
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: Jun 3, 2009
 * Time: 6:12:36 PM
 * To change this template use File | Settings | File Templates.
 */

public class GroovyNumerics {
  def static initDSL() {
    Math.metaClass.'static'.max = { NumericGrid matrix, Number value ->
      matrix.valueOrAbove(value.doubleValue())
    }

    Math.metaClass.'static'.max = { Number value, NumericGrid matrix  ->
      matrix.valueOrAbove(value.doubleValue())
    }

    Math.metaClass.'static'.max = { NumericGrid leftMatrix, NumericGrid rightMatrix   ->
      leftMatrix.max(rightMatrix)
    }

    Math.metaClass.'static'.min = { NumericGrid matrix, Number value ->
      matrix.valueOrBelow(value.doubleValue())
    }

    Math.metaClass.'static'.min = { Number value, NumericGrid matrix  ->
      matrix.valueOrBelow(value.doubleValue())
    }

    Math.metaClass.'static'.min = { NumericGrid leftMatrix, NumericGrid rightMatrix   ->
      leftMatrix.min(rightMatrix)
    }

    Math.metaClass.'static'.exp = { NumericGrid matrix ->
      matrix.exp()
    }

    Number.metaClass.plus = { NumericGrid matrix ->
      return matrix.plus(delegate)
    }

    Number.metaClass.multiply = { NumericGrid matrix ->
      return matrix.multiply(delegate)
    }

    Number.metaClass.minus = { NumericGrid matrix ->
      return matrix.leftMinusScalar(delegate)
    }

    Number.metaClass.div = { NumericGrid matrix ->
      return matrix.leftDivScalar(delegate)
    }

    Number.metaClass.power = { NumericGrid matrix ->
      return matrix.leftPower(delegate)
    }

    Number.metaClass.exp = {
      return Math.exp(delegate)
    }

    Number.metaClass.avg = {
      return delegate
    }
  }

}