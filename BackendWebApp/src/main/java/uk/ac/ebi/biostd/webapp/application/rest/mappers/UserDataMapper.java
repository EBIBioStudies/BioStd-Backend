package uk.ac.ebi.biostd.webapp.application.rest.mappers;

import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserData;
import uk.ac.ebi.biostd.webapp.application.rest.dto.UserDataDto;

@Component
public class UserDataMapper extends BaseMappper<UserDataDto, UserData> {

    @Override
    public UserDataDto map(UserData userData) {
        UserDataDto userDataDto = new UserDataDto();
        userDataDto.setData(userData.getData());
        userDataDto.setTopic(userData.getTopic());
        userDataDto.setUserId(userData.getUserDataId().getUserId());
        userDataDto.setDataKey(userData.getUserDataId().getDataKey());
        return userDataDto;
    }
}
