/*
 * Copyright 2013 Sławomir Śledź <slawomir.sledz@sof-tech.pl>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.softech.server.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import pl.softech.socket.server.IRequest;

/**
 *
 * @author Sławomir Śledź <slawomir.sledz@sof-tech.pl>
 * @since 1.0
 */
public class HttpRequest implements IRequest {

    public enum Method {

        GET, POST;

        public static Method getInstance(String method) {
            for (Method m : values()) {
                if (m.toString().equals(method)) {
                    return m;
                }
            }
            return null;
        }
    }
    private final Map<String, String> header;
    private Method method;
    private URI uri;
    private String httpProtocol;
    private Map<String, String[]> parameters;
    private final IRequest request;

    public HttpRequest(IRequest request) throws HttpBadRequestException, IOException {
        this.request = request;
        this.header = new HashMap<String, String>();
        process();
    }

    @Override
    public SocketChannel getSocket() {
        return request.getSocket();
    }

    @Override
    public byte[] getRawData() {
        return request.getRawData();
    }

    private void processMethod(String line) throws HttpBadRequestException {
        if (line.trim().length() == 0) {
            throw new HttpBadRequestException("No HTTP Method");
        }

        String[] arr = line.split("\\s");
        method = Method.getInstance(arr[0]);
        try {
            uri = new URI(arr[1]);
        } catch (URISyntaxException e) {
            throw new HttpBadRequestException(e);
        }

        if (!arr[2].equals("HTTP/1.1") && !arr[2].equals("HTTP/1.0")) {
            throw new HttpBadRequestException("Unknown HTTP protocol: " + arr[2]);
        }

        httpProtocol = arr[2];
    }

    private void processHeader(String line) {
        String[] arr = line.split(":");
        header.put(arr[0].trim(), arr[1].trim());
    }

    public Map<String, String[]> getParameters() {
        processParametrs();
        return parameters;

    }

    public String getHttpProtocol() {
        return httpProtocol;
    }

    public URI getUri() {
        return uri;
    }

    private void processParametrs() {

        if (parameters != null) {
            return;
        }

        if (method == Method.GET) {

            parameters = new HashMap<String, String[]>();
            if (uri.getQuery() == null) {
                return;
            }

            String[] params = uri.getQuery().split("&");
            Map<String, ArrayList<String>> tmp = new HashMap<String, ArrayList<String>>();
            for (String p : params) {
                String[] key2value = p.split("=");

                ArrayList<String> value = tmp.get(key2value[0]);
                if (value == null) {
                    value = new ArrayList<String>();
                    tmp.put(key2value[0], value);
                }

                value.add(key2value[1]);
            }

            for (Map.Entry<String, ArrayList<String>> e : tmp.entrySet()) {
                parameters.put(e.getKey(), e.getValue().toArray(new String[e.getValue().size()]));
            }

        }
    }

    private void process() throws HttpBadRequestException, IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(getRawData());
        BufferedReader bin = new BufferedReader(new InputStreamReader(in));
        try {
            processMethod(bin.readLine());
            String line;
            while ((line = bin.readLine()) != null && line.length() > 0) {
                processHeader(line);
            }
        } finally {
            if (bin != null) {
                bin.close();
            }
        }
    }

    @Override
    public String toString() {
        return "HttpRequest [method=" + method + ", uri=" + uri + ", httpProtocol=" + httpProtocol + ", parameters="
                + parameters + ", header=" + header + "]";
    }
}
