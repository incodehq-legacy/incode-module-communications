/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.incode.module.communications.demo.module.fixture.teardown;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.jdosupport.IsisJdoSupport;

import org.incode.module.communications.fixture.teardown.CommunicationModuleTearDown;
import org.incode.module.country.fixture.teardown.CountryModuleTearDown;
import org.incode.module.document.fixture.teardown.DocumentModuleTearDown;

public class DemoModuleTearDown extends FixtureScript {

    @Override
    protected void execute(ExecutionContext executionContext) {

        isisJdoSupport.executeUpdate("delete from \"incodeCommunicationsDemo\".\"PaperclipForDemoInvoice\"");
        isisJdoSupport.executeUpdate("delete from \"incodeCommunicationsDemo\".\"DemoInvoice\"");
        isisJdoSupport.executeUpdate("delete from \"incodeCommunicationsDemo\".\"CommunicationChannelOwnerLinkForDemoCustomer\"");
        isisJdoSupport.executeUpdate("delete from \"incodeCommunicationsDemo\".\"DemoCustomer\"");

        executionContext.executeChild(this, new CommunicationModuleTearDown());
        executionContext.executeChild(this, new DocumentModuleTearDown());
        executionContext.executeChild(this, new CountryModuleTearDown());

    }


    @javax.inject.Inject
    private IsisJdoSupport isisJdoSupport;

}
