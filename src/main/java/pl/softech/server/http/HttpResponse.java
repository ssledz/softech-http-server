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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import pl.softech.socket.server.IResponse;
import pl.softech.socket.server.Server;


/**
 *
 * @author Sławomir Śledź <slawomir.sledz@sof-tech.pl>
 * @since 1.0
 */
public class HttpResponse implements IResponse {
    
    private final static String EOL = "\r\n";
    private final static String HEADER_D = ": ";
    private final static String D = " ";
    private final static String HTTP_PROTOCOL = "HTTP/1.1";
    
    private final Map<String, String> header;
    
    private byte[] content;
    
    private HttpStatus status;
    
    private byte[] rawData;
    
    public HttpResponse() {
        header = new HashMap<String, String>();
        status = HttpStatus.HTTP_200;
        initHeader();
    }

    private void initHeader() {
        header.put("Server", Server.class.getName());
        header.put("Content-Type", "text/html; charset=utf-8");
        header.put("Connection","close");
    }
    
    public void setContent(String content) {
        this.content = content.getBytes();
    }

    public byte[] getRawData() {
        return rawData;
    }
    
    private void processStatus(StringBuilder builder) {
        builder.append(HTTP_PROTOCOL).append(D).append(status.toString()).append(EOL);
    }
    
    private void processHeader(StringBuilder builder) {
        for(Map.Entry<String, String> e : header.entrySet()) {
            builder.append(e.getKey()).append(HEADER_D).append(e.getValue()).append(EOL);
        }
    }
    
    public HttpResponse build() throws IOException {
        StringBuilder builder = new StringBuilder();
        processStatus(builder);
        processHeader(builder);
        builder.append(EOL);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(builder.toString().getBytes());
        out.write(content);
        rawData = out.toByteArray();
        return this;
    }

    @Override
    public boolean closeConnection() {
        
        String connection = header.get("Connection");
        
        return connection == null || connection.equals("close");
    }
    
}
