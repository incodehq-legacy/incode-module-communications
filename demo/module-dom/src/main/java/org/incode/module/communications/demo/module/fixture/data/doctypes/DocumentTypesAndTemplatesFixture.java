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
package org.incode.module.communications.demo.module.fixture.data.doctypes;

import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.joda.time.LocalDate;

import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.value.Clob;

import org.incode.module.communications.demo.module.dom.applicability.rmf.RenderModelFactoryOfDocumentAttachedToDemoInvoice;
import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.docs.DocumentTemplate;
import org.incode.module.document.dom.impl.rendering.RenderingStrategy;
import org.incode.module.document.dom.impl.rendering.RenderingStrategyRepository;
import org.incode.module.document.dom.impl.types.DocumentType;
import org.incode.module.document.fixture.DocumentTemplateFSAbstract;

import lombok.Getter;

public class DocumentTypesAndTemplatesFixture extends DocumentTemplateFSAbstract {

    public static final String DOC_TYPE_REF_INVOICE = "INVOICE";
    public static final String DOC_TYPE_REF_RECEIPT = "RECEIPT";


    // applicable to DemoInvoice.class
    public static final String DOC_TYPE_REF_FREEMARKER_HTML = "FREEMARKER_HTML";

    @Getter
    DocumentTemplate fmkTemplate;

    @Getter
    DocumentTemplate siTemplate;

    @Getter
    DocumentTemplate xdpTemplate;

    @Getter
    DocumentTemplate xddTemplate;

    @Override
    protected void execute(final ExecutionContext executionContext) {

        // these document types have no associated templates
        final DocumentType invoiceType =
                upsertType(DOC_TYPE_REF_INVOICE, "Demo invoice document type", executionContext);
        final DocumentType docType =
                upsertType(DOC_TYPE_REF_RECEIPT, "Receipt document type", executionContext);


        // doc type for rendering cover notes.
        final DocumentType docTypeForFreemarkerHtml =
                upsertType(DOC_TYPE_REF_FREEMARKER_HTML, "Demo Freemarker HTML (eg email Cover Note)", executionContext);

        final RenderingStrategy fmkRenderingStrategy = renderingStrategyRepository.findByReference(RenderingStrategiesFixture.REF_FMK);

        final String atPath = "/";


        //
        // freemarker template, with html
        //
        final LocalDate now = clockService.now();

        final Clob clob = new Clob(docTypeForFreemarkerHtml.getName(), "text/html",
                loadResource("FreemarkerHtmlCoverNote.html"));
        fmkTemplate = upsertDocumentClobTemplate(
                docTypeForFreemarkerHtml, now, atPath,
                ".html",
                false,
                clob, fmkRenderingStrategy,
                "Freemarker-html-cover-note-for-${demoInvoice.num}", fmkRenderingStrategy,
                executionContext);

        mixin(DocumentTemplate._applicable.class, fmkTemplate).applicable(
                Document.class,
                RenderModelFactoryOfDocumentAttachedToDemoInvoice.class,
                null);

        executionContext.addResult(this, fmkTemplate);

    }

    private static String loadResource(final String resourceName) {
        final URL templateUrl = Resources
                .getResource(DocumentTypesAndTemplatesFixture.class, resourceName);
        try {
            return Resources.toString(templateUrl, Charsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to read resource URL '%s'", templateUrl));
        }
    }


    @Inject
    RenderingStrategyRepository renderingStrategyRepository;
    @Inject
    ClockService clockService;


}
