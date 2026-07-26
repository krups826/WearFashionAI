package com.virtualtryon.Service;
import com.virtualtryon.Dto.PersonResponse;
import com.virtualtryon.Dto.UploadPersonRequest;

import java.util.List;

public interface PersonService {

    PersonResponse uploadPerson(UploadPersonRequest request);

    List<PersonResponse> getAllPerson();

    PersonResponse getPersonById(Long id);

    void deletePerson(Long id);
}
