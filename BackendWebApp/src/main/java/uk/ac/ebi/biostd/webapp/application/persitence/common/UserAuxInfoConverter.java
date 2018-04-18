package uk.ac.ebi.biostd.webapp.application.persitence.common;


import java.io.StringReader;
import java.io.StringWriter;
import javax.persistence.AttributeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import lombok.SneakyThrows;

public class UserAuxInfoConverter implements AttributeConverter<AuxInfo, String> {

    private final JAXBContext jaxbContext;

    public UserAuxInfoConverter() throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(AuxInfo.class);
    }

    @Override
    @SneakyThrows
    public String convertToDatabaseColumn(AuxInfo auxInfo) {
        StringWriter sw = new StringWriter();
        createMarshaller().marshal(auxInfo, sw);
        return sw.toString();
    }

    @Override
    @SneakyThrows
    public AuxInfo convertToEntityAttribute(String dbData) {
        return dbData == null ? new AuxInfo()
                : (AuxInfo) jaxbContext.createUnmarshaller().unmarshal(new StringReader(dbData));
    }

    private Marshaller createMarshaller() throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        return marshaller;
    }
}
