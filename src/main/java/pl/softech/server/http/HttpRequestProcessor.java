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

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.softech.socket.server.IRequest;
import pl.softech.socket.server.IRequestProcessor;
import pl.softech.socket.server.IResponse;

/**
 *
 * @author Sławomir Śledź <slawomir.sledz@sof-tech.pl>
 * @since 1.0
 */
public class HttpRequestProcessor implements IRequestProcessor {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    private final List<IHttpRequestHandler> handlers;

    public HttpRequestProcessor() {
        handlers = new LinkedList<IHttpRequestHandler>();
    }

    public HttpRequestProcessor addHandler(IHttpRequestHandler handler) {
        handlers.add(handler);
        return this;
    }

    @Override
    public IResponse processRequest(IRequest request) {

        HttpResponse httpResponse = new HttpResponse();

        try {
            HttpRequest httpRequest = new HttpRequest(request);
            logger.debug(HttpRequestProcessor.class.getName(), "processRequest", "Request:\n" + new String(httpRequest.getRawData(), Charset.defaultCharset()));
            for (IHttpRequestHandler h : handlers) {
                if (h.match(httpRequest)) {
                    h.handle(httpRequest, httpResponse);
                    break;
                }
            }

            return httpResponse.build();

        } catch (Exception e) {
            logger.error("", e);
            return HttpStatus.HTTP_504.response;

        }
    }
}
