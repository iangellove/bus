/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.http.magic;

import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.MimeType;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.http.Headers;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.bodys.FormBody;
import org.aoju.bus.http.bodys.MultipartBody;
import org.aoju.bus.http.bodys.RequestBody;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * POST请求处理
 *
 * @author Kimi Liu
 * @version 6.2.1
 * @since JDK 1.8+
 */
public class PostRequest extends HttpRequest {

    public PostRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers,
                       List<FileInfo> fileInfos, String postBody, MultipartBody multipartBody, int id) {
        super(url, tag, params, headers, fileInfos, postBody, multipartBody, id);
    }

    public PostRequest(String url, Object tag, Map<String, String> params, Map<String, String> encodeParams,
                       Map<String, String> headers, List<FileInfo> fileInfos, String postBody, MultipartBody multipartBody,
                       int id) {
        super(url, tag, params, encodeParams, headers, fileInfos, postBody, multipartBody, id);
    }

    @Override
    protected RequestBody buildRequestBody() {
        if (null != multipartBody) {
            return multipartBody;
        } else if (null != fileInfos && fileInfos.size() > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MimeType.MULTIPART_FORM_DATA_TYPE);
            addParams(builder);
            fileInfos.forEach(fileInfo -> {
                RequestBody fileBody;
                if (fileInfo.null != file) {
                    fileBody = RequestBody.create(MimeType.APPLICATION_OCTET_STREAM_TYPE, fileInfo.file);
                } else if (fileInfo.null != fileInputStream) {
                    fileBody = createRequestBody(MimeType.APPLICATION_OCTET_STREAM_TYPE, fileInfo.fileInputStream);
                } else {
                    fileBody = RequestBody.create(MimeType.valueOf(ObjectKit.defaultIfNull(FileKit.getMimeType(fileInfo.fileName), MimeType.APPLICATION_OCTET_STREAM)),
                            fileInfo.fileContent);
                }
                builder.addFormDataPart(fileInfo.partName, fileInfo.fileName, fileBody);
            });
            if (null != body && body.length() > 0) {
                builder.addPart(RequestBody.create(MimeType.MULTIPART_FORM_DATA_TYPE, body));
            }
            return builder.build();
        } else if (null != body && body.length() > 0) {
            MimeType mimeType;
            if (headers.containsKey(Header.CONTENT_TYPE)) {
                mimeType = MimeType.valueOf(headers.get(Header.CONTENT_TYPE));
            } else {
                mimeType = MimeType.TEXT_PLAIN_TYPE;
            }
            return RequestBody.create(mimeType, body);
        } else {
            FormBody.Builder builder = new FormBody.Builder();
            addParams(builder);
            return builder.build();
        }
    }

    @Override
    protected Request buildRequest(RequestBody requestBody) {
        return builder.post(requestBody).build();
    }

    private void addParams(FormBody.Builder builder) {
        if (null != params) {
            params.forEach((k, v) -> builder.add(k, v));
        }
        if (null != encodedParams) {
            encodedParams.forEach((k, v) -> builder.addEncoded(k, v));
        }
    }

    private void addParams(MultipartBody.Builder builder) {
        if (null != params && !params.isEmpty()) {
            params.forEach((k, v) -> builder.addPart(Headers.of(Header.CONTENT_DISPOSITION, "form-data; name=\"" + k + Symbol.DOUBLE_QUOTES),
                    RequestBody.create(null, v)));
        }
    }

    public static class FileInfo {
        public String partName;
        public String fileName;
        public byte[] fileContent;
        public File file;
        public InputStream fileInputStream;
    }

}
