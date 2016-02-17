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
import org.apache.commons.math4.exception.MathIllegalArgumentException;
import org.apache.commons.math4.exception.NullArgumentException;
import org.apache.commons.math4.exception.util.LocalizedFormats;

/**
 * Wraps an underlying univariate bandwidth strategy and rounds the bandwidth to
 * the next highest power of two. Power of two bandwidths ensure that
 * <code>x +/- h</code> is representable <i>exactly</i> and can thus help to
 * reduce the roundoff error of the resulting numerical derivative.
 * <p>
 * This class uses the decorator design pattern.
 * </p>
 * 
 * @since 4.0
 */
public class PowerOfTwoUnivariateBandwidthStrategy implements UnivariateBandwidthStrategy {

    /**
     * IEEE double 64 exponent mask.
     */
    private static final long EXPONENT_MASK = 0x7FFL << 52;

    /**
     * Fraction mask.
     */
    public static final long FRACTION_MASK = 0x000FFFFFFFFFFFFFL;

    /**
     * The strategy to wrap.
     */
    private UnivariateBandwidthStrategy underlyingStrategy;

    /**
     * Constructor.
     * 
     * @param underlyingStrategy The underlying strategy.
     * @throws NullArgumentException If <code>underlyingStrategy</code> is
     *             <code>null</code>.
     */
    public PowerOfTwoUnivariateBandwidthStrategy(
	    final UnivariateBandwidthStrategy underlyingStrategy) throws NullArgumentException {

	if (underlyingStrategy == null) {
	    throw new NullArgumentException();
	}

	this.underlyingStrategy = underlyingStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getBandwidth(final UnivariateFunction function,
	    final FiniteDifference finiteDifference, final double x) {

	double underlyingBandwidth = underlyingStrategy.getBandwidth(function,
		finiteDifference, x);

	// this method is effective a no-op if the underlying bandwidth is
	// already a power of two.
	double powerOfTwo = getNextLargerPowerOfTwo(underlyingBandwidth);

	return powerOfTwo;
    }

    /**
     * Gets the next larger power-of-two.
     * <p>
     * Consider moving this method to something like <code>MathUtils</code>.
     * </p>
     * 
     * @param x The value.
     * @return The next larger power of two.
     */
    private double getNextLargerPowerOfTwo(final double x) {

	if (x <= 0) {
	    // bandwidths must be positive.
	    throw new MathIllegalArgumentException(LocalizedFormats.BANDWIDTH, x);
	}

	if (x < Double.MIN_NORMAL) {
	    // x is subnormal, so just return the smallest (normal) power of
	    // two.
	    return Double.MIN_NORMAL;
	}

	// convert to bits.
	long bits = Double.doubleToLongBits(x);

	long fraction = bits & FRACTION_MASK;
	if (fraction == 0) {
	    // x is already a power of two - there is no work to do.
	    return x;
	}

	long exponent = (bits & EXPONENT_MASK) >> 52;
	if (exponent == 2046) {
	    throw new MathIllegalArgumentException(LocalizedFormats.OVERFLOW);
	}

	// up the exponent by 1 and convert back to a double.
	long powerOfTwoBits = (exponent + 1) << 52;
	double powerOfTwo = Double.longBitsToDouble(powerOfTwoBits);

	return powerOfTwo;
    }

}
