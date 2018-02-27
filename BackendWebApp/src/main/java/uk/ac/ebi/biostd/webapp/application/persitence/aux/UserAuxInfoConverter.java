package uk.ac.ebi.biostd.webapp.application.persitence.aux;


import java.io.StringReader;
import java.io.StringWriter;
import javax.persistence.AttributeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import lombok.SneakyThrows;

public class UserAuxInfoConverter implements AttributeConverter<AuxInfo, String> {

    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;

    public UserAuxInfoConverter() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(AuxInfo.class);
        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

        unmarshaller = jaxbContext.createUnmarshaller();
    }

    @Override
    @SneakyThrows
    public String convertToDatabaseColumn(AuxInfo auxInfo) {
        StringWriter sw = new StringWriter();
        marshaller.marshal(auxInfo, sw);
        return sw.toString();
    }

    @Override
    @SneakyThrows
    public AuxInfo convertToEntityAttribute(String dbData) {
        return dbData == null ? new AuxInfo() : (AuxInfo) unmarshaller.unmarshal(new StringReader(dbData));
    }
}
