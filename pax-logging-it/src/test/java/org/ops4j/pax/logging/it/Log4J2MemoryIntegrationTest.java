/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.logging.it;

import java.io.IOException;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;

import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class Log4J2MemoryIntegrationTest extends AbstractControlledIntegrationTestBase {

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLog4J2(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    public void memoryIssues() throws IOException {
        LOG.info("Starting");
        String[] loggerNames = new String[10_000];
        for (int i = 0; i < 10_000; i++) {
            loggerNames[i] = UUID.randomUUID().toString();
        }
        for (int i = 0; i < 1_000_000; i++) {
            if (i % 10_000 == 0) {
                LOG.info("iteration {}", i);
                System.gc();
            }
            new Log4J2MemoryIntegrationTest.MyClass(loggerNames[i % 10_000]).run();
        }
        LOG.info("Done");
    }

    private static class MyClass {
        private final String name;

        public MyClass(String name) {
            this.name = name;
        }

        public void run() {
            org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(name);
            slf4jLogger.trace("TRACE through SLF4J");

            org.apache.commons.logging.Log commonsLogger = org.apache.commons.logging.LogFactory.getLog(name);
            commonsLogger.trace("TRACE through Apache Commons Logging");

            org.apache.juli.logging.Log juliLogger = org.apache.juli.logging.LogFactory.getLog(name);
            juliLogger.trace("TRACE through JULI Logging");

            org.apache.avalon.framework.logger.Logger avalonLogger = org.ops4j.pax.logging.avalon.AvalonLogFactory.getLogger(name);
            avalonLogger.debug("DEBUG through Avalon Logger API");

            org.jboss.logging.Logger jbossLogger = org.jboss.logging.Logger.getLogger(name);
            jbossLogger.trace("TRACE through JBoss Logging Logger API");


            org.apache.logging.log4j.Logger log4j2Logger = org.apache.logging.log4j.LogManager.getLogger(name);
            log4j2Logger.trace("TRACE through Log4J v2 API");
        }
    }

}
