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
package org.aoju.bus.health.mac.drivers;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.CoreFoundation.*;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.builtin.software.OSDesktopWindow;
import org.aoju.bus.health.mac.CoreGraphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to query desktop windows
 *
 * @author Kimi Liu
 * @version 6.2.2
 * @since JDK 1.8+
 */
@ThreadSafe
public final class WindowInfo {

    private WindowInfo() {

    }

    /**
     * Gets windows on the operating system's GUI desktop.
     *
     * @param visibleOnly Whether to restrict the list to only windows visible to the user.
     * @return A list of {@link OSDesktopWindow} objects
     * representing the desktop windows.
     */
    public static List<OSDesktopWindow> queryDesktopWindows(boolean visibleOnly) {
        CFArrayRef windowInfo = CoreGraphics.INSTANCE.CGWindowListCopyWindowInfo(
                visibleOnly ? CoreGraphics.kCGWindowListOptionOnScreenOnly | CoreGraphics.kCGWindowListExcludeDesktopElements
                        : CoreGraphics.kCGWindowListOptionAll,
                CoreGraphics.kCGNullWindowID);
        int numWindows = windowInfo.getCount();
        // Prepare a list to return
        List<OSDesktopWindow> windowList = new ArrayList<>();
        // Set up keys for dictionary lookup
        CFStringRef kCGWindowIsOnscreen = CFStringRef.createCFString("kCGWindowIsOnscreen");
        CFStringRef kCGWindowNumber = CFStringRef.createCFString("kCGWindowNumber");
        CFStringRef kCGWindowOwnerPID = CFStringRef.createCFString("kCGWindowOwnerPID");
        CFStringRef kCGWindowLayer = CFStringRef.createCFString("kCGWindowLayer");
        CFStringRef kCGWindowBounds = CFStringRef.createCFString("kCGWindowBounds");
        CFStringRef kCGWindowName = CFStringRef.createCFString("kCGWindowName");
        CFStringRef kCGWindowOwnerName = CFStringRef.createCFString("kCGWindowOwnerName");
        try {
            // Populate the list
            for (int i = 0; i < numWindows; i++) {
                // For each array element, get the dictionary
                Pointer result = windowInfo.getValueAtIndex(i);
                CFDictionaryRef windowRef = new CFDictionaryRef(result);
                // Now get information from the dictionary.
                result = windowRef.getValue(kCGWindowIsOnscreen); // Optional key, check for null
                boolean visible = null == result || new CFBooleanRef(result).booleanValue();
                if (!visibleOnly || visible) {
                    result = windowRef.getValue(kCGWindowNumber); // kCFNumberSInt64Type
                    long windowNumber = new CFNumberRef(result).longValue();

                    result = windowRef.getValue(kCGWindowOwnerPID); // kCFNumberSInt64Type
                    long windowOwnerPID = new CFNumberRef(result).longValue();

                    result = windowRef.getValue(kCGWindowLayer); // kCFNumberIntType
                    int windowLayer = new CFNumberRef(result).intValue();

                    result = windowRef.getValue(kCGWindowBounds);
                    CoreGraphics.CGRect rect = new CoreGraphics.CGRect();
                    CoreGraphics.INSTANCE.CGRectMakeWithDictionaryRepresentation(new CFDictionaryRef(result), rect);
                    Rectangle windowBounds = new Rectangle(Builder.roundToInt(rect.origin.x),
                            Builder.roundToInt(rect.origin.y), Builder.roundToInt(rect.size.width),
                            Builder.roundToInt(rect.size.height));

                    // Note: the Quartz name returned by this field is rarely used
                    result = windowRef.getValue(kCGWindowName); // Optional key, check for null
                    String windowName = cfPointerToString(result);
                    // This is the program running the window, use as name if name blank or add in
                    // parenthesis
                    result = windowRef.getValue(kCGWindowOwnerName); // Optional key, check for null
                    String windowOwnerName = cfPointerToString(result);
                    if (windowName.isEmpty()) {
                        windowName = windowOwnerName;
                    } else {
                        windowName = windowName + "(" + windowOwnerName + ")";
                    }

                    windowList.add(new OSDesktopWindow(windowNumber, windowName, windowOwnerName, windowBounds,
                            windowOwnerPID, windowLayer, visible));
                }
            }
        } finally {
            // CF references from "Copy" or "Create" must be released
            kCGWindowIsOnscreen.release();
            kCGWindowNumber.release();
            kCGWindowOwnerPID.release();
            kCGWindowLayer.release();
            kCGWindowBounds.release();
            kCGWindowName.release();
            kCGWindowOwnerName.release();
            windowInfo.release();
        }

        return windowList;
    }

    /**
     * Temporary workaround for zero length CFStrings. Should be fixed in JNA 5.7.
     * See https://github.com/java-native-access/jna/pull/1275
     *
     * @param result Pointer to the CFString
     * @return a CFString including a possible empty one, without exception.
     */
    private static String cfPointerToString(Pointer result) {
        if (null != result) {
            CFStringRef cfs = new CFStringRef(result);
            if (CoreFoundation.INSTANCE.CFStringGetLength(cfs).intValue() > 0) {
                return cfs.stringValue();
            }
        }
        return Normal.EMPTY;
    }

}
