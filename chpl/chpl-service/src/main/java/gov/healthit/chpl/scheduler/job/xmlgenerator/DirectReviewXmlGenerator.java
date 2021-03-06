package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.compliance.DirectReview;

public class DirectReviewXmlGenerator extends XmlGenerator {
    public static void add(List<DirectReview> drs, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (drs != null) {
            sw.writeStartElement(rootNodeName);
            for (DirectReview dr : drs) {
                add(dr, "directReview", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(DirectReview directReview, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        sw.writeStartElement(rootNodeName);
        sw.writeStartElement("circumstances");
        for (String circumstance : directReview.getCircumstances()) {
            createSimpleElement(circumstance, "circumstance", sw);
        }
        sw.writeEndElement();
        createSimpleElement(directReview.getCreated(), "created", sw);
        createSimpleElement(directReview.getDeveloperId(), "developerId", sw);
        createSimpleElement(directReview.getEndDate(), "endDate", sw);
        createSimpleElement(directReview.getLastUpdated(), "lastUpdated", sw);
        DirectReviewNonConformityXmlGenerator.add(directReview.getNonConformities(), "nonConformities", sw);
        createSimpleElement(directReview.getStartDate(), "startDate", sw);
        sw.writeEndElement();
    }
}
