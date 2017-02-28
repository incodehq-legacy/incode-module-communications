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
package org.incode.module.communications.demo.application.integtests;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.FluentIterable;

import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.xactn.TransactionService;

import org.incode.module.communications.demo.application.services.fakeemail.EmailMessage;
import org.incode.module.communications.demo.application.services.fakeemail.FakeEmailService;
import org.incode.module.communications.demo.module.dom.impl.customers.DemoCustomer;
import org.incode.module.communications.demo.module.dom.impl.customers.DemoCustomerMenu;
import org.incode.module.communications.demo.module.dom.impl.invoices.DemoInvoice;
import org.incode.module.communications.demo.module.dom.impl.invoices.DemoInvoiceRepository;
import org.incode.module.communications.demo.module.fixture.demodata.DemoCustomersFixture;
import org.incode.module.communications.demo.module.fixture.scenario.DemoAppFixture;
import org.incode.module.communications.dom.impl.commchannel.CommunicationChannel;
import org.incode.module.communications.dom.impl.commchannel.CommunicationChannelOwnerLinkRepository;
import org.incode.module.communications.dom.impl.commchannel.CommunicationChannelType;
import org.incode.module.communications.dom.impl.commchannel.EmailAddress;
import org.incode.module.communications.dom.impl.commchannel.PostalAddress;
import org.incode.module.communications.dom.impl.comms.CommChannelRole;
import org.incode.module.communications.dom.impl.comms.Communication;
import org.incode.module.communications.dom.impl.comms.CommunicationState;
import org.incode.module.communications.dom.impl.comms.Communication_printAsBlob;
import org.incode.module.communications.dom.mixins.Document_email;
import org.incode.module.communications.dom.mixins.Document_print;
import org.incode.module.document.dom.impl.docs.DocumentAbstract;
import org.incode.module.document.dom.impl.paperclips.Paperclip;
import org.incode.module.document.dom.impl.paperclips.PaperclipRepository;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class Smoke_IntegTest extends DemoAppIntegTestAbstract {

    @Inject
    FixtureScripts fixtureScripts;
    @Inject
    TransactionService transactionService;

    @Inject
    DemoCustomerMenu customerMenu;
    @Inject
    CommunicationChannelOwnerLinkRepository linkRepository;

    @Inject
    DemoInvoiceRepository invoiceRepository;

    @Inject
    PaperclipRepository paperclipRepository;


    @Test
    public void can_send_email() throws Exception {

        // given
        fixtureScripts.runFixtureScript(new DemoAppFixture(), null);
        transactionService.nextTransaction();

        // and so given customer with an email
        final DemoCustomer fred = customerMenu.findByName(DemoCustomersFixture.FRED_HAS_EMAIL_AND_PHONE).get(0);

        final EmailAddress fredEmail = (EmailAddress) linkRepository
                .findByOwnerAndCommunicationChannelType(fred, CommunicationChannelType.EMAIL_ADDRESS)
                .get(0)
                .getCommunicationChannel();

        // and with an invoice
        final DemoInvoice fredInvoice = invoiceRepository.findByCustomer(fred).get(0);

        // that has an attached document
        final Paperclip paperclip = paperclipRepository.findByAttachedTo(fredInvoice).get(0);
        final DocumentAbstract document = paperclip.getDocument();

        // when
        final Document_email documentEmail = mixin(Document_email.class, document);
        final Set<EmailAddress> emailAddresses = documentEmail.choices0$$();

        // then
        assertThat(emailAddresses).contains(fredEmail);

        // and when
        final Communication comm = wrap(documentEmail).$$(fredEmail, null, null);

        // then
        assertThat(comm).isNotNull();

        assertThat(comm.getState()).isEqualTo(CommunicationState.PENDING);
        assertThat(comm.getQueuedAt()).isNotNull();
        assertThat(comm.hideQueuedAt()).isFalse();
        assertThat(comm.getType()).isEqualTo(CommunicationChannelType.EMAIL_ADDRESS);
        assertThat(comm.getSubject()).isNotNull();
        assertThat(comm.getSentAt()).isNull();

        // hmm, using Java8 equivalent yields no results.
        // My guess is something to do with lazy loading, but I don't really understand why...
        final List<CommunicationChannel> correspondentChannels =
                FluentIterable.from(comm.getCorrespondents())
                        .transform(CommChannelRole::getChannel)
                        .filter(Objects::nonNull)
                        .toList();
        assertThat(correspondentChannels).contains(fredEmail);

        List<EmailMessage> emailMessages = fakeEmailService.viewSentEmails();
        assertThat(emailMessages).isEmpty();

        // when
        fakeScheduler.runBackgroundCommands(5000);

        // then
        assertThat(comm.getState()).isEqualTo(CommunicationState.SENT);
        assertThat(comm.getSentAt()).isNotNull();

        emailMessages = fakeEmailService.viewSentEmails();
        assertThat(emailMessages).isNotEmpty();
    }

    @Test
    public void can_print() throws Exception {

        // given
        fixtureScripts.runFixtureScript(new DemoAppFixture(), null);
        transactionService.nextTransaction();

        // and so given customer with an email
        final DemoCustomer mary = customerMenu.findByName(DemoCustomersFixture.MARY_HAS_PHONE_AND_POST).get(0);

        final PostalAddress maryPost = (PostalAddress) linkRepository
                .findByOwnerAndCommunicationChannelType(mary, CommunicationChannelType.POSTAL_ADDRESS)
                .get(0)
                .getCommunicationChannel();

        // and with an invoice
        final DemoInvoice fredInvoice = invoiceRepository.findByCustomer(mary).get(0);

        // that has an attached document
        final Paperclip paperclip = paperclipRepository.findByAttachedTo(fredInvoice).get(0);
        final DocumentAbstract document = paperclip.getDocument();

        // when
        final Document_print documentPrint = mixin(Document_print.class, document);
        final Set<PostalAddress> postalAddresses = documentPrint.choices0$$();

        // then
        assertThat(postalAddresses).contains(maryPost);

        // and when
        final Communication comm = wrap(documentPrint).$$(maryPost);

        // then
        assertThat(comm).isNotNull();

        assertThat(comm.getState()).isEqualTo(CommunicationState.PENDING);
        assertThat(comm.getQueuedAt()).isNull();    // unlike emails, never queued
        assertThat(comm.hideQueuedAt()).isTrue();   // which is why they are hidden
        assertThat(comm.getType()).isEqualTo(CommunicationChannelType.POSTAL_ADDRESS);
        assertThat(comm.getSubject()).isNotNull();
        assertThat(comm.getSentAt()).isNull();

        // hmm, using Java8 equivalent yields no results.
        // My guess is something to do with lazy loading, but I don't really understand why...
        final List<CommunicationChannel> correspondentChannels =
                FluentIterable.from(comm.getCorrespondents())
                        .transform(CommChannelRole::getChannel)
                        .filter(Objects::nonNull)
                        .toList();
        assertThat(correspondentChannels).contains(maryPost);

        // when
        wrap(mixin(Communication_printAsBlob.class, comm)).$$();

        // then
        assertThat(comm.getState()).isEqualTo(CommunicationState.SENT);
        assertThat(comm.getSentAt()).isNotNull();
    }



    @Inject
    FakeEmailService fakeEmailService;
}

