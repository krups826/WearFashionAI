package com.virtualtryon.Service;

import com.virtualtryon.Dto.PythonGenerateRequest;
import com.virtualtryon.Dto.PythonGenerateResponse;

public interface PythonService {

    PythonGenerateResponse generate(PythonGenerateRequest request);
}
