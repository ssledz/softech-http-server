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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author Sławomir Śledź <slawomir.sledz@sof-tech.pl>
 * @since 1.0
 */
public class HttpStaticContent implements IHttpRequestHandler {

    private final File rootHtml;

    public HttpStaticContent(File rootHtml) {
        this.rootHtml = rootHtml;
    }
    
    private String getTxtFileContent(File file) throws IOException {
        StringBuilder ret = new StringBuilder();
        BufferedReader bin = null;
        try {
            bin = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bin.readLine()) != null) {
                ret.append(line + "\n");
            }
        } finally {
            if (bin != null) {
                bin.close();
            }
        }
        return ret.toString();
    }

    @Override
    public HttpResponse handle(HttpRequest request, HttpResponse response) throws Exception {
        try {
            File file = new File(rootHtml, request.getUri().getPath());
            String fileContent = getTxtFileContent(file);
            response.setContent(fileContent);

        } catch (Exception e) {
            StringWriter out = new StringWriter();
            PrintWriter pout = new PrintWriter(out);
            e.printStackTrace(pout);
            response.setContent("<html>" + out.getBuffer().toString() + "</html>");
        }
        return response;
    }

    @Override
    public boolean match(HttpRequest request) {
        return true;
    }

}
