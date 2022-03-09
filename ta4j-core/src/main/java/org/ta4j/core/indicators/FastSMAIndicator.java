/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2021 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators;

import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

/**
 * A faster implementation of {@link SMAIndicator} used specifically for HughStrategy. If {@link #getValue(int)} is
 * called with sequential indices, then a rolling sum is used internally, otherwise the normal SMA calculation is done.
 */
public class FastSMAIndicator extends CachedIndicator<Num> {

    private static final int ROLLING_SUM_RESET_COUNT = 2000;

    private final Indicator<Num> indicator;
    private final int barCount;

    private int lastCalculatedIndex = -1;
    private Num rollingSum = numOf(0);

    // This is used to periodically reset the rolling sum to account for accumulating rounding errors
    private int rollingSumCalculatedCount = 0;

    /**
     * Instantiates a new {@link FastSMAIndicator}.
     *
     * @param indicator the {@link Indicator}
     * @param barCount  the bar count
     */
    public FastSMAIndicator(Indicator<Num> indicator, int barCount) {
        super(indicator);
        this.indicator = indicator;
        this.barCount = barCount;
    }

    @Override
    protected Num calculate(int index) {
        if (index == lastCalculatedIndex + 1 && rollingSumCalculatedCount != ROLLING_SUM_RESET_COUNT) {
            rollingSum = rollingSum.plus(indicator.getValue(index));

            int indexToSubtract = index - barCount;
            if (indexToSubtract >= 0) {
                rollingSum = rollingSum.minus(indicator.getValue(indexToSubtract));
            }

            lastCalculatedIndex = index;
            rollingSumCalculatedCount++;

            double realBarCount = Math.min(barCount, index + 1);
            return rollingSum.dividedBy(numOf(realBarCount));
        } else {
            Num sum = numOf(0);
            for (int i = Math.max(0, index - barCount + 1); i <= index; i++) {
                sum = sum.plus(indicator.getValue(i));
            }

            lastCalculatedIndex = index;
            rollingSum = sum;
            rollingSumCalculatedCount = 0;

            int realBarCount = Math.min(barCount, index + 1);
            return sum.dividedBy(numOf(realBarCount));
        }
    }
}
