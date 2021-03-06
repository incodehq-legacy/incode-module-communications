/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
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
package org.incode.module.communications.dom.mixins;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.incode.module.document.dom.DocumentModule;
import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.paperclips.Paperclip;
import org.incode.module.document.dom.impl.paperclips.PaperclipRepository;
import org.incode.module.document.dom.spi.SupportingDocumentsEvaluator;

@Mixin(method = "coll")
public class Document_communicationAttachments {

    private final Document document;

    public Document_communicationAttachments(final Document document) {
        this.document = document;
    }

    public static class ActionDomainEvent extends DocumentModule.ActionDomainEvent<Document_communicationAttachments> { }

    @Action(
            semantics = SemanticsOf.SAFE,
            domainEvent = ActionDomainEvent.class
    )
    @ActionLayout(
            contributed = Contributed.AS_ASSOCIATION
    )
    public List<Document> coll() {
        return provider.attachmentsFor(document);
    }

    public boolean hideColl() {
        for (SupportingDocumentsEvaluator supportingDocumentsEvaluator : supportingDocumentsEvaluators) {
            final SupportingDocumentsEvaluator.Evaluation evaluation =
                    supportingDocumentsEvaluator.evaluate(document);
            if(evaluation == SupportingDocumentsEvaluator.Evaluation.SUPPORTING) {
                return true;
            }
        }
        return false;
    }

    @Inject
    List<SupportingDocumentsEvaluator> supportingDocumentsEvaluators;

    @Inject
    Provider provider;

    /**
     * Factored out so can be injected elsewhere also.
     */
    @DomainService(nature = NatureOfService.DOMAIN)
    public static class Provider {

        @Programmatic
        public List<Document> attachmentsFor(final Document document) {
            final List<Paperclip> paperclips = paperclipRepository.findByAttachedTo(document);

            return Lists.newArrayList(
                    FluentIterable.from(paperclips)
                        .transform(Paperclip::getDocument)
                        .filter(Document.class::isInstance)
                        .transform(Document.class::cast)
                        .toList());
        }

        @Inject
        PaperclipRepository paperclipRepository;

    }


}
