/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2021 Ta4j Organization & respective
 * authors (see AUTHORS)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators.helpers;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class SumIndexIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> indicator;
    private final int barCount;
    private final int quarterBarCount;

    private int lastCalculatedIndex = -1;
    private Num rollingSum = numOf(0);
    private Num lastIndexIndicatorValue;

    /**
     * Instantiates a new {@link SumIndexIndicator}.
     *
     * @param indicator the {@link Indicator} use to sum values
     * @param barCount  the number of bars to sum values of
     */
    public SumIndexIndicator(Indicator<Num> indicator, int barCount) {
        super(indicator);
        this.indicator = indicator;
        this.barCount = barCount;
        quarterBarCount = barCount / 4;
    }

    @Override
    protected Num calculate(int index) {
        int indexDifference = index - lastCalculatedIndex;

        // TODO
        //if (indexDifference >= quarterBarCount) {
        //    // If the 'indexDifference' is more than one-fourth the 'barCount', then the below
        //    // optimization has diminishing returns.
        //
        //} else {
        //
        //}

        boolean indexDifferenceIsPositive = indexDifference > 0;
        for (int i = 1, iMax = Math.abs(indexDifference); i <= iMax; i++) {
            Num plusValue = indicator.getValue(lastCalculatedIndex + i);
            rollingSum = indexDifferenceIsPositive ? rollingSum.plus(plusValue) : rollingSum.minus(plusValue);

            int indexToSubtract = lastCalculatedIndex + i - barCount;
            if (indexToSubtract >= 0) {
                Num minusValue = indicator.getValue(indexToSubtract);
                rollingSum = indexDifferenceIsPositive ? rollingSum.minus(minusValue) : rollingSum.plus(minusValue);
            }
        }

        if (indexDifference == 0 && lastIndexIndicatorValue != null) {
            rollingSum = rollingSum.minus(lastIndexIndicatorValue);
            lastIndexIndicatorValue = indicator.getValue(index);
            rollingSum = rollingSum.plus(lastIndexIndicatorValue);
        }

        lastCalculatedIndex = index;
        return rollingSum;
    }
}
