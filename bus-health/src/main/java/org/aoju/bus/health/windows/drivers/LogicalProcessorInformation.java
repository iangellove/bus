/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.windows.drivers;

import com.sun.jna.platform.win32.Kernel32Util;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.health.builtin.hardware.CentralProcessor;
import org.aoju.bus.health.windows.Kernel32Kit;
import org.aoju.bus.health.windows.WinNT;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Utility to query Logical Processor Information pre-Win7
 *
 * @author Kimi Liu
 * @version 6.2.2
 * @since JDK 1.8+
 */
@ThreadSafe
public final class LogicalProcessorInformation {

    private LogicalProcessorInformation() {
    }

    /**
     * Get a list of logical processors on this machine. Requires Windows 7 and
     * higher.
     *
     * @return A list of logical processors
     */
    public static List<CentralProcessor.LogicalProcessor> getLogicalProcessorInformationEx() {
        // Collect a list of logical processors on each physical core and
        // package. These will be 64-bit bitmasks.
        WinNT.SYSTEM_LOGICAL_PROCESSOR_INFORMATION_EX[] procInfo = Kernel32Util
                .getLogicalProcessorInformationEx(WinNT.LOGICAL_PROCESSOR_RELATIONSHIP.RelationAll);
        // Used to cross-reference a processor to package pr core
        List<WinNT.GROUP_AFFINITY[]> packages = new ArrayList<>();
        List<WinNT.GROUP_AFFINITY> cores = new ArrayList<>();
        // Used to iterate
        List<WinNT.NUMA_NODE_RELATIONSHIP> numaNodes = new ArrayList<>();

        for (int i = 0; i < procInfo.length; i++) {
            switch (procInfo[i].relationship) {
                case WinNT.LOGICAL_PROCESSOR_RELATIONSHIP.RelationProcessorPackage:
                    // could assign a package to more than one processor group
                    packages.add(((WinNT.PROCESSOR_RELATIONSHIP) procInfo[i]).groupMask);
                    break;
                case WinNT.LOGICAL_PROCESSOR_RELATIONSHIP.RelationProcessorCore:
                    // for Core, groupCount is always 1
                    cores.add(((WinNT.PROCESSOR_RELATIONSHIP) procInfo[i]).groupMask[0]);
                    break;
                case WinNT.LOGICAL_PROCESSOR_RELATIONSHIP.RelationNumaNode:
                    numaNodes.add((WinNT.NUMA_NODE_RELATIONSHIP) procInfo[i]);
                    break;
                default:
                    // Ignore Group and Cache info
                    break;
            }
        }
        // Windows doesn't define core and package numbers, so we sort the lists
        // so core and package numbers increment consistently with processor
        // numbers/bitmasks, ordered in groups
        cores.sort(Comparator.comparing(c -> c.group * 64L + c.mask.longValue()));
        // if package in multiple groups will still use first group for sorting
        packages.sort(Comparator.comparing(p -> p[0].group * 64L + p[0].mask.longValue()));

        // Iterate Logical Processors and use bitmasks to match packages, cores,
        // and NUMA nodes. Perfmon instances are numa node + processor number, so we
        // iterate by numa node so the returned list will properly index perfcounter
        // numa/proc-per-numa indices with all numa nodes grouped together
        numaNodes.sort(Comparator.comparing(n -> n.nodeNumber));
        List<CentralProcessor.LogicalProcessor> logProcs = new ArrayList<>();
        for (WinNT.NUMA_NODE_RELATIONSHIP node : numaNodes) {
            int nodeNum = node.nodeNumber;
            int group = node.groupMask.group;
            long mask = node.groupMask.mask.longValue();
            // Processor numbers are uniquely identified by processor group and processor
            // number on that group, which matches the bitmask.
            int lowBit = Long.numberOfTrailingZeros(mask);
            int hiBit = 63 - Long.numberOfLeadingZeros(mask);
            for (int lp = lowBit; lp <= hiBit; lp++) {
                if ((mask & (1L << lp)) != 0) {
                    CentralProcessor.LogicalProcessor logProc = new CentralProcessor.LogicalProcessor(lp, getMatchingCore(cores, group, lp),
                            getMatchingPackage(packages, group, lp), nodeNum, group);
                    logProcs.add(logProc);
                }
            }
        }
        return logProcs;
    }

    private static int getMatchingPackage(List<WinNT.GROUP_AFFINITY[]> packages, int g, int lp) {
        for (int i = 0; i < packages.size(); i++) {
            for (int j = 0; j < packages.get(i).length; j++) {
                if ((packages.get(i)[j].mask.longValue() & (1L << lp)) != 0 && packages.get(i)[j].group == g) {
                    return i;
                }
            }
        }
        return 0;
    }

    private static int getMatchingCore(List<WinNT.GROUP_AFFINITY> cores, int g, int lp) {
        for (int j = 0; j < cores.size(); j++) {
            if ((cores.get(j).mask.longValue() & (1L << lp)) != 0 && cores.get(j).group == g) {
                return j;
            }
        }
        return 0;
    }

    /*
     * Non-EX version for pre-Win7
     */

    /**
     * Get a list of logical processors on this machine
     *
     * @return A list of logical processors
     */
    public static List<CentralProcessor.LogicalProcessor> getLogicalProcessorInformation() {
        // Collect a list of logical processors on each physical core and
        // package.
        List<Long> packageMaskList = new ArrayList<>();
        List<Long> coreMaskList = new ArrayList<>();
        WinNT.SYSTEM_LOGICAL_PROCESSOR_INFORMATION[] processors = Kernel32Kit.getLogicalProcessorInformation();
        for (WinNT.SYSTEM_LOGICAL_PROCESSOR_INFORMATION proc : processors) {
            if (proc.relationship == WinNT.LOGICAL_PROCESSOR_RELATIONSHIP.RelationProcessorPackage) {
                packageMaskList.add(proc.processorMask.longValue());
            } else if (proc.relationship == WinNT.LOGICAL_PROCESSOR_RELATIONSHIP.RelationProcessorCore) {
                coreMaskList.add(proc.processorMask.longValue());
            }
        }
        // Sort the list (natural ordering) so core and package numbers
        // increment as expected.
        coreMaskList.sort(null);
        packageMaskList.sort(null);

        // Assign logical processors to cores and packages
        List<CentralProcessor.LogicalProcessor> logProcs = new ArrayList<>();
        for (int core = 0; core < coreMaskList.size(); core++) {
            long coreMask = coreMaskList.get(core);
            // Lowest and Highest set bits, indexing from 0
            int lowBit = Long.numberOfTrailingZeros(coreMask);
            int hiBit = 63 - Long.numberOfLeadingZeros(coreMask);
            // Create logical processors for this core
            for (int i = lowBit; i <= hiBit; i++) {
                if ((coreMask & (1L << i)) != 0) {
                    CentralProcessor.LogicalProcessor logProc = new CentralProcessor.LogicalProcessor(i, core,
                            LogicalProcessorInformation.getBitMatchingPackageNumber(packageMaskList, i));
                    logProcs.add(logProc);
                }
            }
        }
        return logProcs;
    }

    /**
     * Iterate over the package mask list and find a matching mask index
     *
     * @param packageMaskList The list of bitmasks to iterate
     * @param logProc         The bit to find matching mask
     * @return The index of the list which matched the bit
     */
    private static int getBitMatchingPackageNumber(List<Long> packageMaskList, int logProc) {
        for (int i = 0; i < packageMaskList.size(); i++) {
            if ((packageMaskList.get(i).longValue() & (1L << logProc)) != 0) {
                return i;
            }
        }
        return 0;
    }

}