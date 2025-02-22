/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org 6tail and other contributors.              *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.center.date.culture.lunar;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.Direction;
import org.miaixz.bus.core.center.date.culture.cn.Duty;
import org.miaixz.bus.core.center.date.culture.cn.Phase;
import org.miaixz.bus.core.center.date.culture.cn.Week;
import org.miaixz.bus.core.center.date.culture.cn.fetus.FetusDay;
import org.miaixz.bus.core.center.date.culture.cn.sixty.EarthBranch;
import org.miaixz.bus.core.center.date.culture.cn.sixty.HeavenStem;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.cn.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.cn.star.twelve.TwelveStar;
import org.miaixz.bus.core.center.date.culture.cn.star.twentyeight.TwentyEightStar;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerm;

/**
 * 农历日
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarDay extends Loops {

    public static final String[] NAMES = {
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };

    /**
     * 农历月
     */
    protected LunarMonth month;

    /**
     * 日
     */
    protected int day;

    /**
     * 初始化
     *
     * @param year  农历年
     * @param month 农历月，闰月为负
     * @param day   农历日
     */
    public LunarDay(int year, int month, int day) {
        LunarMonth m = LunarMonth.fromYm(year, month);
        if (day < 1 || day > m.getDayCount()) {
            throw new IllegalArgumentException(String.format("illegal day %d in %s", day, m));
        }
        this.month = m;
        this.day = day;
    }

    /**
     * 从农历年月日初始化
     *
     * @param year  农历年
     * @param month 农历月，闰月为负
     * @param day   农历日
     */
    public static LunarDay fromYmd(int year, int month, int day) {
        return new LunarDay(year, month, day);
    }

    /**
     * 农历月
     *
     * @return 农历月
     */
    public LunarMonth getMonth() {
        return month;
    }

    /**
     * 日
     *
     * @return 日
     */
    public int getDay() {
        return day;
    }

    public String getName() {
        return NAMES[day - 1];
    }

    @Override
    public String toString() {
        return month + getName();
    }

    public LunarDay next(int n) {
        if (n == 0) {
            return fromYmd(month.getYear().getYear(), month.getMonthWithLeap(), day);
        }
        int d = day + n;
        LunarMonth lm = month;
        int daysInMonth = lm.getDayCount();
        boolean forward = n > 0;
        int add = forward ? 1 : -1;
        while (forward ? (d > daysInMonth) : (d <= 0)) {
            if (forward) {
                d -= daysInMonth;
            }
            lm = lm.next(add);
            daysInMonth = lm.getDayCount();
            if (!forward) {
                d += daysInMonth;
            }
        }
        return fromYmd(lm.getYear().getYear(), lm.getMonthWithLeap(), d);
    }

    /**
     * 是否在指定农历日之前
     *
     * @param target 农历日
     * @return true/false
     */
    public boolean isBefore(LunarDay target) {
        int aYear = month.getYear().getYear();
        LunarMonth targetMonth = target.getMonth();
        int bYear = targetMonth.getYear().getYear();
        if (aYear == bYear) {
            int aMonth = month.getMonth();
            int bMonth = targetMonth.getMonth();
            if (aMonth == bMonth) {
                if (month.isLeap() && !targetMonth.isLeap()) {
                    return false;
                }
                return day < target.getDay();
            }
            return aMonth < bMonth;
        }
        return aYear < bYear;
    }

    /**
     * 是否在指定农历日之后
     *
     * @param target 农历日
     * @return true/false
     */
    public boolean isAfter(LunarDay target) {
        int aYear = month.getYear().getYear();
        LunarMonth targetMonth = target.getMonth();
        int bYear = targetMonth.getYear().getYear();
        if (aYear == bYear) {
            int aMonth = month.getMonth();
            int bMonth = targetMonth.getMonth();
            if (aMonth == bMonth) {
                if (month.isLeap() && !targetMonth.isLeap()) {
                    return true;
                }
                return day > target.getDay();
            }
            return aMonth > bMonth;
        }
        return aYear > bYear;
    }

    /**
     * 星期
     *
     * @return 星期
     */
    public Week getWeek() {
        return getSolarDay().getJulianDay().getWeek();
    }

    /**
     * 当天的年干支
     *
     * @return 干支
     */
    public SixtyCycle getYearSixtyCycle() {
        SolarDay solarDay = getSolarDay();
        int solarYear = solarDay.getMonth().getYear().getYear();
        SolarDay springSolarDay = SolarTerm.fromIndex(solarYear, 3).getJulianDay().getSolarDay();
        LunarYear lunarYear = month.getYear();
        int year = lunarYear.getYear();
        SixtyCycle sixtyCycle = lunarYear.getSixtyCycle();
        if (year == solarYear) {
            if (solarDay.isBefore(springSolarDay)) {
                sixtyCycle = sixtyCycle.next(-1);
            }
        } else if (year < solarYear) {
            if (!solarDay.isBefore(springSolarDay)) {
                sixtyCycle = sixtyCycle.next(1);
            }
        }
        return sixtyCycle;
    }

    /**
     * 当天的月干支
     *
     * @return 干支
     */
    public SixtyCycle getMonthSixtyCycle() {
        SolarDay solarDay = getSolarDay();
        int year = solarDay.getMonth().getYear().getYear();
        SolarTerm term = solarDay.getTerm();
        int index = term.getIndex() - 3;
        if (index < 0 && term.getJulianDay().getSolarDay().isAfter(SolarTerm.fromIndex(year, 3).getJulianDay().getSolarDay())) {
            index += 24;
        }
        return LunarMonth.fromYm(year, 1).getSixtyCycle().next((int) Math.floor(index * 1D / 2));
    }

    /**
     * 干支
     *
     * @return 干支
     */
    public SixtyCycle getSixtyCycle() {
        int offset = (int) month.getFirstJulianDay().next(day - 12).getDay();
        return SixtyCycle.fromName(HeavenStem.fromIndex(offset).getName() + EarthBranch.fromIndex(offset).getName());
    }

    /**
     * 建除十二值神
     *
     * @return 建除十二值神
     */
    public Duty getDuty() {
        return Duty.fromIndex(getSixtyCycle().getEarthBranch().getIndex() - getMonthSixtyCycle().getEarthBranch().getIndex());
    }

    /**
     * 黄道黑道十二神
     *
     * @return 黄道黑道十二神
     */
    public TwelveStar getTwelveStar() {
        return TwelveStar.fromIndex(getSixtyCycle().getEarthBranch().getIndex() + (8 - getMonthSixtyCycle().getEarthBranch().getIndex() % 6) * 2);
    }

    /**
     * 九星
     *
     * @return 九星
     */
    public NineStar getNineStar() {
        SolarDay solar = getSolarDay();
        SolarTerm dongZhi = SolarTerm.fromIndex(solar.getMonth().getYear().getYear(), 0);
        SolarTerm xiaZhi = dongZhi.next(12);
        SolarTerm dongZhi2 = dongZhi.next(24);
        SolarDay dongZhiSolar = dongZhi.getJulianDay().getSolarDay();
        SolarDay xiaZhiSolar = xiaZhi.getJulianDay().getSolarDay();
        SolarDay dongZhiSolar2 = dongZhi2.getJulianDay().getSolarDay();
        int dongZhiIndex = dongZhiSolar.getLunarDay().getSixtyCycle().getIndex();
        int xiaZhiIndex = xiaZhiSolar.getLunarDay().getSixtyCycle().getIndex();
        int dongZhiIndex2 = dongZhiSolar2.getLunarDay().getSixtyCycle().getIndex();
        SolarDay solarShunBai = dongZhiSolar.next(dongZhiIndex > 29 ? 60 - dongZhiIndex : -dongZhiIndex);
        SolarDay solarShunBai2 = dongZhiSolar2.next(dongZhiIndex2 > 29 ? 60 - dongZhiIndex2 : -dongZhiIndex2);
        SolarDay solarNiZi = xiaZhiSolar.next(xiaZhiIndex > 29 ? 60 - xiaZhiIndex : -xiaZhiIndex);
        int offset = 0;
        if (!solar.isBefore(solarShunBai) && solar.isBefore(solarNiZi)) {
            offset = solar.subtract(solarShunBai);
        } else if (!solar.isBefore(solarNiZi) && solar.isBefore(solarShunBai2)) {
            offset = 8 - solar.subtract(solarNiZi);
        } else if (!solar.isBefore(solarShunBai2)) {
            offset = solar.subtract(solarShunBai2);
        } else if (solar.isBefore(solarShunBai)) {
            offset = 8 + solarShunBai.subtract(solar);
        }
        return NineStar.fromIndex(offset);
    }

    /**
     * 太岁方位
     *
     * @return 方位
     */
    public Direction getJupiterDirection() {
        int index = getSixtyCycle().getIndex();
        if (index % 12 < 6) {
            return Direction.fromIndex(new int[]{2, 8, 4, 6, 0}[index / 12]);
        }
        return month.getYear().getJupiterDirection();
    }

    /**
     * 逐日胎神
     *
     * @return 逐日胎神
     */
    public FetusDay getFetusDay() {
        return FetusDay.fromLunarDay(this);
    }

    /**
     * 月相
     *
     * @return 月相
     */
    public Phase getPhase() {
        return Phase.fromIndex(day - 1);
    }

    /**
     * 公历日
     *
     * @return 公历日
     */
    public SolarDay getSolarDay() {
        return month.getFirstJulianDay().next(day - 1).getSolarDay();
    }

    /**
     * 二十八宿
     *
     * @return 二十八宿
     */
    public TwentyEightStar getTwentyEightStar() {
        return TwentyEightStar.fromIndex(new int[]{10, 18, 26, 6, 14, 22, 2}[getSolarDay().getWeek().getIndex()]).next(-7 * getSixtyCycle().getEarthBranch().getIndex());
    }

    /**
     * 农历传统节日，如果当天不是农历传统节日，返回null
     *
     * @return 农历传统节日
     */
    public LunarFestival getFestival() {
        LunarMonth m = getMonth();
        return LunarFestival.fromYmd(m.getYear().getYear(), m.getMonthWithLeap(), day);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LunarDay)) {
            return false;
        }
        LunarDay target = (LunarDay) o;
        return month.equals(target.getMonth()) && day == target.getDay();
    }

}
