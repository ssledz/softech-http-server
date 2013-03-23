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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.softech.socket.server.Server;

/**
 *
 * @author Sławomir Śledź <slawomir.sledz@sof-tech.pl>
 */
public class HttpServerTest {

    private static Server httpServer;
    private static final int PORT = 12345;
    private static File rootHtml;

    @BeforeClass
    public static void setUpClass() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {

                    rootHtml = new File(HttpServerTest.class.getResource(".").getFile());
                    httpServer = new Server(PORT,
                            new HttpRequestProcessor().addHandler(new HttpStaticContent(rootHtml)));
                    httpServer.run();
                } catch (Exception e) {
                    Logger.getLogger(HttpServerTest.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private static String fileContentAsString(File file) throws Exception {
        BufferedReader in = null;
        StringBuilder buffer = new StringBuilder();
        try {
            in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file)));

            String line;
            while ((line = in.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            return buffer.toString();

        } finally {
            if (in != null) {
                in.close();
            }
        }

    }

    @Test
    public void httpStaticContentTest() throws Exception {
        Content content = Request.Get(String.format("http://localhost:%d/index.html", PORT)).execute().returnContent();
        Assert.assertEquals(fileContentAsString(new File(rootHtml, "index.html")), content.asString());
        httpServer.shutdown();
    }
}