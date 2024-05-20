/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
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
package org.miaixz.bus.health.mac.hardware;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;
import org.miaixz.bus.core.annotation.Immutable;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractBaseboard;

import java.util.function.Supplier;

/**
 * Baseboard data obtained from ioreg
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class MacBaseboard extends AbstractBaseboard {

    private final Supplier<Tuple> manufModelVersSerial = Memoizer.memoize(
            MacBaseboard::queryPlatform);

    private static Tuple queryPlatform() {
        String manufacturer = null;
        String model = null;
        String version = null;
        String serialNumber = null;

        IORegistryEntry platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        if (platformExpert != null) {
            byte[] data = platformExpert.getByteArrayProperty("manufacturer");
            if (data != null) {
                manufacturer = Native.toString(data, Charset.UTF_8);
            }
            data = platformExpert.getByteArrayProperty("board-id");
            if (data != null) {
                model = Native.toString(data, Charset.UTF_8);
            }
            if (StringKit.isBlank(model)) {
                data = platformExpert.getByteArrayProperty("model-number");
                if (data != null) {
                    model = Native.toString(data, Charset.UTF_8);
                }
            }
            data = platformExpert.getByteArrayProperty("version");
            if (data != null) {
                version = Native.toString(data, Charset.UTF_8);
            }
            data = platformExpert.getByteArrayProperty("mlb-serial-number");
            if (data != null) {
                serialNumber = Native.toString(data, Charset.UTF_8);
            }
            if (StringKit.isBlank(serialNumber)) {
                serialNumber = platformExpert.getStringProperty("IOPlatformSerialNumber");
            }
            platformExpert.release();
        }
        return new Tuple(StringKit.isBlank(manufacturer) ? "Apple Inc." : manufacturer,
                StringKit.isBlank(model) ? Normal.UNKNOWN : model, StringKit.isBlank(version) ? Normal.UNKNOWN : version,
                StringKit.isBlank(serialNumber) ? Normal.UNKNOWN : serialNumber);
    }

    @Override
    public String getManufacturer() {
        return manufModelVersSerial.get().get(0);
    }

    @Override
    public String getModel() {
        return manufModelVersSerial.get().get(1);
    }

    @Override
    public String getVersion() {
        return manufModelVersSerial.get().get(2);
    }

    @Override
    public String getSerialNumber() {
        return manufModelVersSerial.get().get(3);
    }

}
