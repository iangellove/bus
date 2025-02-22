package org.miaixz.bus.pay.magic;

import lombok.*;
import org.miaixz.bus.core.center.map.CaseInsensitiveMap;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.util.List;
import java.util.Map;

/**
 * Http Response
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Results {

    /**
     * 数据信息
     */
    private String body;
    /**
     * 状态
     */
    private int status;
    /**
     * 请求头信息
     */
    private Map<String, List<String>> headers;

    public String getHeader(String name) {
        List<String> values = null;

        if (StringKit.isNotBlank(name)) {
            CaseInsensitiveMap<String, List<String>> headersIgnoreCase = new CaseInsensitiveMap<>(getHeaders());
            values = headersIgnoreCase.get(name.trim());
        }

        return CollKit.isEmpty(values) ? null : values.get(0);
    }

}
