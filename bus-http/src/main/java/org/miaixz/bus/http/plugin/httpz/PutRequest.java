/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.http.plugin.httpz;

import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.bodys.FormBody;
import org.miaixz.bus.http.bodys.MultipartBody;
import org.miaixz.bus.http.bodys.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * PUT请求处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PutRequest extends HttpRequest {

    public PutRequest(String url,
                      Object tag,
                      Map<String, String> formMap,
                      Map<String, String> headerMap,
                      List<PostRequest.FileInfo> fileInfos,
                      String body,
                      MultipartBody multipartBody,
                      String id) {
        super(url, tag, formMap, headerMap, fileInfos, body, multipartBody, id);
    }

    @Override
    protected RequestBody buildRequestBody() {
        if (null != multipartBody) {
            return multipartBody;
        } else if (null != fileInfos && fileInfos.size() > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MediaType.MULTIPART_FORM_DATA_TYPE);
            form(builder);
            fileInfos.forEach(fileInfo -> {
                RequestBody fileBody;
                if (null != fileInfo.file) {
                    fileBody = RequestBody.create(MediaType.APPLICATION_OCTET_STREAM_TYPE, fileInfo.file);
                } else if (null != fileInfo.fileInputStream) {
                    fileBody = createRequestBody(MediaType.APPLICATION_OCTET_STREAM_TYPE, fileInfo.fileInputStream);
                } else {
                    fileBody = RequestBody.create(MediaType.valueOf(FileKit.getMimeType(fileInfo.fileName)),
                            fileInfo.fileContent);
                }
                builder.addFormDataPart(fileInfo.partName, fileInfo.fileName, fileBody);
            });
            if (null != body && body.length() > 0) {
                builder.addPart(RequestBody.create(MediaType.MULTIPART_FORM_DATA_TYPE, body));
            }
            return builder.build();
        } else if (null != body && body.length() > 0) {
            MediaType mediaType;
            if (headerMap.containsKey(Header.CONTENT_TYPE)) {
                mediaType = MediaType.valueOf(headerMap.get(Header.CONTENT_TYPE));
            } else {
                mediaType = MediaType.TEXT_PLAIN_TYPE;
            }
            return RequestBody.create(mediaType, body);
        } else {
            FormBody.Builder builder = new FormBody.Builder();
            form(builder);
            return builder.build();
        }
    }

    @Override
    protected Request buildRequest(RequestBody requestBody) {
        return builder.put(requestBody).build();
    }

    private void form(FormBody.Builder builder) {
        if (null != formMap) {
            formMap.forEach((k, v) -> builder.add(k, v));
        }
        if (null != encodedForm) {
            encodedForm.forEach((k, v) -> builder.addEncoded(k, v));
        }
    }

    private void form(MultipartBody.Builder builder) {
        if (null != formMap && !formMap.isEmpty()) {
            formMap.forEach((k, v) ->
                    builder.addPart(Headers.of(
                                    Header.CONTENT_DISPOSITION,
                                    "form-data; name=" + k + Symbol.DOUBLE_QUOTES),
                            RequestBody.create(null, v)
                    )
            );
        }
    }

}
