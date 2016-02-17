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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math4.fraction.BigFraction;
import org.apache.commons.math4.linear.FieldVector;

/**
 * Provides a threadsafe cache of finite difference coefficients.
 * <p>
 * This class uses the singleton design pattern.
 * </p>
 */
public final class FiniteDifferenceCoefficients {

    /**
     * The lone instance.
     */
    private static final FiniteDifferenceCoefficients instance = new FiniteDifferenceCoefficients();
    
    /**
     * Gets the instance of this class.
     * 
     * @return The (lone) instance.
     */
    public static FiniteDifferenceCoefficients getInstance() {
	return instance;
    }
    
    /**
     * The actual cache.
     */
    private final Map<FiniteDifference, double[]> cache = new ConcurrentHashMap<>();
    
    /**
     * Constructor, here only for access protection.
     */
    private FiniteDifferenceCoefficients() {	
    }

    /**
     * Gets specified finite difference coefficients.
     * 
     * @param finiteDifference The finite difference descriptor.
     * @return The finite difference coefficients.
     */
    public double[] getFiniteDifferenceCoefficients(
	    final FiniteDifference finiteDifference) {
	double[] coefficients = cache.get(finiteDifference);

	if (coefficients == null) {
	    // make a suitable generator and grab the coefficients in arbitrary
	    // precision form.
	    FiniteDifferenceCoefficientGenerator generator = new FiniteDifferenceCoefficientGenerator(
		    finiteDifference);
	    FieldVector<BigFraction> bigCoefficients = generator.getCoefficients();

	    // and now we just need to convert to doubles.
	    coefficients = new double[bigCoefficients.getDimension()];
	    for (int index = 0; index < coefficients.length; index++) {
		coefficients[index] = bigCoefficients.getEntry(index).doubleValue();
	    }

	    cache.put(finiteDifference, coefficients);
	}

	return coefficients;
    }

}
