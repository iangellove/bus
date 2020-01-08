/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.hardware.windows;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import com.sun.jna.platform.win32.Kernel32;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.common.windows.PerfCounterQuery;
import org.aoju.bus.health.common.windows.PerfWildcardQuery;
import org.aoju.bus.health.common.windows.PerfWildcardQuery.PdhCounterWildcardProperty;
import org.aoju.bus.health.common.windows.WmiQueryHandler;
import org.aoju.bus.health.common.windows.WmiUtils;
import org.aoju.bus.health.hardware.Disks;
import org.aoju.bus.health.hardware.HWDiskStore;
import org.aoju.bus.health.hardware.HWPartition;
import org.aoju.bus.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Windows hard disk implementation.
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8+
 */
public class WindowsDisks implements Disks {

    private static final String PHYSICALDRIVE_PREFIX = "\\\\.\\PHYSICALDRIVE";
    private static final String PHYSICAL_DISK = "PhysicalDisk";

    private static final Pattern DEVICE_ID = Pattern.compile(".*\\.DeviceID=\"(.*)\"");

    private static final int BUFSIZE = 255;
    private static final PerfWildcardQuery<PhysicalDiskProperty> physicalDiskPerfCounters = new PerfWildcardQuery<>(
            PhysicalDiskProperty.class, PHYSICAL_DISK,
            "Win32_PerfRawData_PerfDisk_PhysicalDisk WHERE NOT Name=\"_Total\"");
    private final WmiQuery<DiskDriveProperty> diskDriveQuery = new WmiQuery<>("Win32_DiskDrive",
            DiskDriveProperty.class);
    private final WmiQuery<DriveToPartitionProperty> driveToPartitionQuery = new WmiQuery<>(
            "Win32_DiskDriveToDiskPartition", DriveToPartitionProperty.class);
    private final WmiQuery<DriveToPartitionProperty> diskToParitionQuery = new WmiQuery<>(
            "Win32_LogicalDiskToPartition", DriveToPartitionProperty.class);
    private final WmiQuery<DiskPartitionProperty> partitionQuery = new WmiQuery<>("Win32_DiskPartition",
            DiskPartitionProperty.class);
    private final WmiQueryHandler wmiQueryHandler = WmiQueryHandler.createInstance();

    /**
     * <p>
     * updateDiskStats.
     * </p>
     *
     * @param diskStore a {@link HWDiskStore} object.
     * @return a boolean.
     */
    public static boolean updateDiskStats(HWDiskStore diskStore) {
        String index = null;
        HWPartition[] partitions = diskStore.getPartitions();
        if (partitions.length > 0) {
            // If a partition exists on this drive, the major property
            // corresponds to the disk index, so use it.
            index = Integer.toString(partitions[0].getMajor());
        } else if (diskStore.getName().startsWith(PHYSICALDRIVE_PREFIX)) {
            // If no partition exists, Windows reliably uses a name to match the
            // disk index. That said, the skeptical person might wonder why a
            // disk has read/write statistics without a partition, and wonder
            // why this branch is even relevant as an option. The author of this
            // comment does not have an answer for this valid question.
            index = diskStore.getName().substring(PHYSICALDRIVE_PREFIX.length(), diskStore.getName().length());
        } else {
            // The author of this comment cannot fathom a circumstance in which
            // the code reaches this point, but just in case it does, here's the
            // correct response. If you get this log warning, the circumstances
            // would be of great interest to the project's maintainers.
            Logger.warn("Couldn't match index for {}", diskStore.getName());
            return false;
        }
        DiskStats stats = queryReadWriteStats(index);
        if (stats.readMap.containsKey(index)) {
            diskStore.setReads(stats.readMap.getOrDefault(index, 0L));
            diskStore.setReadBytes(stats.readByteMap.getOrDefault(index, 0L));
            diskStore.setWrites(stats.writeMap.getOrDefault(index, 0L));
            diskStore.setWriteBytes(stats.writeByteMap.getOrDefault(index, 0L));
            diskStore.setCurrentQueueLength(stats.queueLengthMap.getOrDefault(index, 0L));
            diskStore.setTransferTime(stats.xferTimeMap.getOrDefault(index, 0L));
            diskStore.setTimeStamp(stats.timeStamp);
            return true;
        } else {
            return false;
        }

    }

    /**
     * Populates the maps for the specified index. If the index is null, populates
     * all the maps
     *
     * @param index The index to populate/update maps for
     */
    private static DiskStats queryReadWriteStats(String index) {
        // Create object to hold and return results
        DiskStats stats = new DiskStats();

        Map<PhysicalDiskProperty, List<Long>> valueMap = physicalDiskPerfCounters.queryValuesWildcard();
        stats.timeStamp = System.currentTimeMillis();
        List<String> instances = physicalDiskPerfCounters.getInstancesFromLastQuery();
        List<Long> readList = valueMap.get(PhysicalDiskProperty.DISKREADSPERSEC);
        List<Long> readByteList = valueMap.get(PhysicalDiskProperty.DISKREADBYTESPERSEC);
        List<Long> writeList = valueMap.get(PhysicalDiskProperty.DISKWRITESPERSEC);
        List<Long> writeByteList = valueMap.get(PhysicalDiskProperty.DISKWRITEBYTESPERSEC);
        List<Long> queueLengthList = valueMap.get(PhysicalDiskProperty.CURRENTDISKQUEUELENGTH);
        List<Long> xferTimeList = valueMap.get(PhysicalDiskProperty.PERCENTDISKTIME);

        if (instances.isEmpty() || readList == null || readByteList == null || writeList == null
                || writeByteList == null || queueLengthList == null || xferTimeList == null) {
            return stats;
        }
        for (int i = 0; i < instances.size(); i++) {
            String name = getIndexFromName(instances.get(i));
            // If index arg passed, only update passed arg
            if (index != null && !index.equals(name)) {
                continue;
            }
            stats.readMap.put(name, readList.get(i));
            stats.readByteMap.put(name, readByteList.get(i));
            stats.writeMap.put(name, writeList.get(i));
            stats.writeByteMap.put(name, writeByteList.get(i));
            stats.queueLengthMap.put(name, queueLengthList.get(i));
            stats.xferTimeMap.put(name, xferTimeList.get(i) / 10_000L);
        }
        return stats;
    }

    /**
     * Parse a drive name like "0 C:" to just the index "0"
     *
     * @param s A drive name to parse
     * @return The first space-delimited value
     */
    private static String getIndexFromName(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return s.split("\\s")[0];
    }

    @Override
    public HWDiskStore[] getDisks() {
        List<HWDiskStore> result;
        result = new ArrayList<>();
        DiskStats stats = queryReadWriteStats(null);
        PartitionMaps maps = queryPartitionMaps();

        WmiResult<DiskDriveProperty> vals = wmiQueryHandler.queryWMI(diskDriveQuery);

        for (int i = 0; i < vals.getResultCount(); i++) {
            HWDiskStore ds = new HWDiskStore();
            ds.setName(WmiUtils.getString(vals, DiskDriveProperty.NAME, i));
            ds.setModel(String.format("%s %s", WmiUtils.getString(vals, DiskDriveProperty.MODEL, i),
                    WmiUtils.getString(vals, DiskDriveProperty.MANUFACTURER, i)).trim());
            // Most vendors store serial # as a hex string; convert
            ds.setSerial(Builder.hexStringToString(WmiUtils.getString(vals, DiskDriveProperty.SERIALNUMBER, i)));
            String index = Integer.toString(WmiUtils.getUint32(vals, DiskDriveProperty.INDEX, i));
            ds.setReads(stats.readMap.getOrDefault(index, 0L));
            ds.setReadBytes(stats.readByteMap.getOrDefault(index, 0L));
            ds.setWrites(stats.writeMap.getOrDefault(index, 0L));
            ds.setWriteBytes(stats.writeByteMap.getOrDefault(index, 0L));
            ds.setCurrentQueueLength(stats.queueLengthMap.getOrDefault(index, 0L));
            ds.setTransferTime(stats.xferTimeMap.getOrDefault(index, 0L));
            ds.setTimeStamp(stats.timeStamp);
            ds.setSize(WmiUtils.getUint64(vals, DiskDriveProperty.SIZE, i));
            // Get partitions
            List<HWPartition> partitions = new ArrayList<>();
            List<String> partList = maps.driveToPartitionMap.get(ds.getName());
            if (partList != null && !partList.isEmpty()) {
                for (String part : partList) {
                    if (maps.partitionMap.containsKey(part)) {
                        partitions.add(maps.partitionMap.get(part));
                    }
                }
            }
            ds.setPartitions(partitions.toArray(new HWPartition[0]));
            // Add to list
            result.add(ds);
        }
        return result.toArray(new HWDiskStore[0]);
    }

    private PartitionMaps queryPartitionMaps() {
        // Create object to hold and return results
        PartitionMaps maps = new PartitionMaps();

        // For Regexp matching DeviceIDs
        Matcher mAnt;
        Matcher mDep;

        // Map drives to partitions
        WmiResult<DriveToPartitionProperty> drivePartitionMap = wmiQueryHandler.queryWMI(driveToPartitionQuery);
        for (int i = 0; i < drivePartitionMap.getResultCount(); i++) {
            mAnt = DEVICE_ID.matcher(WmiUtils.getRefString(drivePartitionMap, DriveToPartitionProperty.ANTECEDENT, i));
            mDep = DEVICE_ID.matcher(WmiUtils.getRefString(drivePartitionMap, DriveToPartitionProperty.DEPENDENT, i));
            if (mAnt.matches() && mDep.matches()) {
                maps.driveToPartitionMap
                        .computeIfAbsent(mAnt.group(1).replaceAll("\\\\\\\\", "\\\\"), x -> new ArrayList<>())
                        .add(mDep.group(1));
            }
        }

        // Map partitions to logical disks
        WmiResult<DriveToPartitionProperty> diskPartitionMap = wmiQueryHandler.queryWMI(diskToParitionQuery);
        for (int i = 0; i < diskPartitionMap.getResultCount(); i++) {
            mAnt = DEVICE_ID.matcher(WmiUtils.getRefString(diskPartitionMap, DriveToPartitionProperty.ANTECEDENT, i));
            mDep = DEVICE_ID.matcher(WmiUtils.getRefString(diskPartitionMap, DriveToPartitionProperty.DEPENDENT, i));
            if (mAnt.matches() && mDep.matches()) {
                maps.partitionToLogicalDriveMap.put(mAnt.group(1), mDep.group(1) + Symbol.BACKSLASH);
            }
        }

        // Next, get all partitions and create objects
        WmiResult<DiskPartitionProperty> hwPartitionQueryMap = wmiQueryHandler.queryWMI(partitionQuery);
        for (int i = 0; i < hwPartitionQueryMap.getResultCount(); i++) {
            String deviceID = WmiUtils.getString(hwPartitionQueryMap, DiskPartitionProperty.DEVICEID, i);
            String logicalDrive = maps.partitionToLogicalDriveMap.getOrDefault(deviceID, "");
            String uuid = "";
            if (!logicalDrive.isEmpty()) {
                // Get matching volume for UUID
                char[] volumeChr = new char[BUFSIZE];
                Kernel32.INSTANCE.GetVolumeNameForVolumeMountPoint(logicalDrive, volumeChr, BUFSIZE);
                uuid = Builder.parseUuidOrDefault(new String(volumeChr).trim(), "");
            }
            maps.partitionMap.put(deviceID,
                    new HWPartition(WmiUtils.getString(hwPartitionQueryMap, DiskPartitionProperty.NAME, i),
                            WmiUtils.getString(hwPartitionQueryMap, DiskPartitionProperty.TYPE, i),
                            WmiUtils.getString(hwPartitionQueryMap, DiskPartitionProperty.DESCRIPTION, i), uuid,
                            WmiUtils.getUint64(hwPartitionQueryMap, DiskPartitionProperty.SIZE, i),
                            WmiUtils.getUint32(hwPartitionQueryMap, DiskPartitionProperty.DISKINDEX, i),
                            WmiUtils.getUint32(hwPartitionQueryMap, DiskPartitionProperty.INDEX, i), logicalDrive));
        }
        return maps;
    }


    enum DiskDriveProperty {
        INDEX, MANUFACTURER, MODEL, NAME, SERIALNUMBER, SIZE
    }

    enum DriveToPartitionProperty {
        ANTECEDENT, DEPENDENT
    }

    enum DiskPartitionProperty {
        DESCRIPTION, DEVICEID, DISKINDEX, INDEX, NAME, SIZE, TYPE
    }

    /*
     * For disk query
     */
    enum PhysicalDiskProperty implements PdhCounterWildcardProperty {
        // First element defines WMI instance name field and PDH instance filter
        NAME(PerfCounterQuery.NOT_TOTAL_INSTANCE),
        // Remaining elements define counters
        DISKREADSPERSEC("Disk Reads/sec"), //
        DISKREADBYTESPERSEC("Disk Read Bytes/sec"), //
        DISKWRITESPERSEC("Disk Writes/sec"), //
        DISKWRITEBYTESPERSEC("Disk Write Bytes/sec"), //
        CURRENTDISKQUEUELENGTH("Current Disk Queue Length"), //
        PERCENTDISKTIME("% Disk Time");

        private final String counter;

        PhysicalDiskProperty(String counter) {
            this.counter = counter;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getCounter() {
            return counter;
        }
    }

    /**
     * Maps to store read/write bytes per drive index
     */
    private static final class DiskStats {
        private final Map<String, Long> readMap = new HashMap<>();
        private final Map<String, Long> readByteMap = new HashMap<>();
        private final Map<String, Long> writeMap = new HashMap<>();
        private final Map<String, Long> writeByteMap = new HashMap<>();
        private final Map<String, Long> queueLengthMap = new HashMap<>();
        private final Map<String, Long> xferTimeMap = new HashMap<>();
        private long timeStamp;
    }

    /**
     * Maps for the partition structure
     */
    private static final class PartitionMaps {
        private final Map<String, List<String>> driveToPartitionMap = new HashMap<>();
        private final Map<String, String> partitionToLogicalDriveMap = new HashMap<>();
        private final Map<String, HWPartition> partitionMap = new HashMap<>();
    }
}
