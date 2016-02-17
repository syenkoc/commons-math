/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math4.analysis.differentiation.finite;

import org.apache.commons.math4.analysis.UnivariateFunction;
import org.apache.commons.math4.exception.NullArgumentException;

/**
 * A univariate finite difference differentiator.
 * 
 * @since 4.0
 */
public class UnivariateFiniteDifferenceDerivativeFunction implements UnivariateFunction {

    /**
     * The underlying function.
     */
    private final UnivariateFunction function;

    /**
     * The stencil.
     */
    private final FiniteDifference finiteDifference;

    /**
     * The bandwidth strategy.
     */
    private final UnivariateBandwidthStrategy bandwidthStrategy;

    /**
     * Constructor.
     * 
     * @param function The function.
     * @param finiteDifference The finite difference stencil.
     * @param bandwidthStrategy The bandwidth strategy.
     * @throws NullArgumentException If any arguments are <code>null</code>.
     */
    public UnivariateFiniteDifferenceDerivativeFunction(final UnivariateFunction function,
	    final FiniteDifference finiteDifference,
	    final UnivariateBandwidthStrategy bandwidthStrategy) {
	if ((function == null) || (finiteDifference == null) || (bandwidthStrategy == null)) {
	    throw new NullArgumentException();
	}

	this.function = function;
	this.finiteDifference = finiteDifference;
	this.bandwidthStrategy = bandwidthStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double value(final double x) {
	double h = bandwidthStrategy.getBandwidth(function, finiteDifference, x);
	double value = getDerivative(x, h);

	return value;
    }

    /**
     * Compute the derivative of the function at the specified function using
     * the specified grid width.
     * 
     * @param x The point.
     * @param h The grid width.
     * @return The derivative of the function.
     */
    private double getDerivative(final double x, final double h) {
	double[] coefficients = finiteDifference.getCoefficients();
	double[] grid = getFunctionValues(coefficients, h, x);

	double derivative = 0;
	for (int index = 0; index < grid.length; index++) {
	    derivative += coefficients[index] * grid[index];
	}

	int derivativeOrder = finiteDifference.getDerivativeOrder();
	derivative /= Math.pow(h, derivativeOrder);

	return derivative;
    }

    /**
     * Get function value vector.
     * 
     * @param coefficients The coefficients.
     * @param h The step size.
     * @param x The x value.
     * @return Get grid of function values.
     */
    private double[] getFunctionValues(final double[] coefficients, final double h,
	    final double x) {
	double[] grid = new double[coefficients.length];
	int minimumIndex = finiteDifference.getLeftMultiplier();

	for (int index = 0, multiplier = minimumIndex; index < grid.length; index++, multiplier++) {
	    double gridPoint = x + (multiplier * h);

	    // check if we can short-circuit. Also useful when we are
	    // taking a central difference of a function with a (removable)
	    // singularity at the point of differentiation!
	    grid[index] = (coefficients[index] == 0) ? 0 : function.value(gridPoint);
	}

	return grid;
    }

}
