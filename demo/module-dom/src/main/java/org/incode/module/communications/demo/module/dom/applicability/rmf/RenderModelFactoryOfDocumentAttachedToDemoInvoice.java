package org.incode.module.communications.demo.module.dom.applicability.rmf;

import java.util.List;

import javax.inject.Inject;

import org.incode.module.communications.demo.module.dom.impl.customers.DemoCustomer;
import org.incode.module.communications.demo.module.dom.impl.invoices.DemoInvoice;
import org.incode.module.document.dom.impl.applicability.RendererModelFactoryAbstract;
import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.docs.DocumentTemplate;
import org.incode.module.document.dom.impl.paperclips.Paperclip;
import org.incode.module.document.dom.impl.paperclips.PaperclipRepository;

import lombok.Value;

public class RenderModelFactoryOfDocumentAttachedToDemoInvoice extends RendererModelFactoryAbstract<Document> {

    public RenderModelFactoryOfDocumentAttachedToDemoInvoice() {
        super(Document.class);
    }

    @Override
    protected Object doNewRendererModel(
            final DocumentTemplate documentTemplate, final Document document) {

        final List<Paperclip> paperclips = paperclipRepository.findByDocument(document);
        final DemoInvoice demoInvoice =
                paperclips.stream().map(Paperclip::getAttachedTo)
                .filter(DemoInvoice.class::isInstance)
                .map(DemoInvoice.class::cast)
                .findFirst()
                .get(); // is safe to do this, because attachment advisor will have already run

        return new DataModel(demoInvoice.getCustomer(), demoInvoice);
    }


    @Inject
    PaperclipRepository paperclipRepository;

    @Value
    public static class DataModel {
        DemoCustomer demoCustomer;
        DemoInvoice demoInvoice;
    }


}

