package org.incode.module.communications.demo.module.dom.impl.commchannel;

import org.apache.isis.applib.annotation.Mixin;

import org.incode.module.communications.dom.impl.commchannel.CommunicationChannelOwner_phoneNumberTitles;

@Mixin
public class DemoCommunicationChannelOwner_phoneNumbers extends
        CommunicationChannelOwner_phoneNumberTitles {

    public DemoCommunicationChannelOwner_phoneNumbers(final CommChannelDemoObject demoObject) {
        super(demoObject, " | ");
    }

}
